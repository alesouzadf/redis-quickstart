package org.acme.redis.honra;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.acme.arquivo.ArquivoSolicitacoesService;
import org.acme.models.SolicitacoesHonra;

@ApplicationScoped
public class HonraReactiveService {

    @Inject
    ArquivoSolicitacoesService serviceArquivo;

    private final ReactiveHashCommands<String, String, SolicitacoesHonra> reactiveHash;
    private final ReactiveKeyCommands<String> reactiveKey;
    private static final String CHAVE = "honras-reactive";

    public HonraReactiveService(ReactiveRedisDataSource reactiveRedis) {
        this.reactiveHash = reactiveRedis.hash(String.class, String.class, SolicitacoesHonra.class);
        this.reactiveKey = reactiveRedis.key(String.class);
    }

    public Uni<Void> salvar(SolicitacoesHonra honra) {
        long segundos = calcularSegundosAteMeiaNoite();
        return reactiveHash.hset(CHAVE, honra.getIdOperacaoAgenteCreditoLivre(), honra)
                .chain(() -> reactiveKey.expire(CHAVE, Duration.ofSeconds(segundos)))
                .replaceWithVoid();
    }

    public Uni<Void> salvarTodos(Multi<SolicitacoesHonra> honras) {
        long segundos = calcularSegundosAteMeiaNoite();

        return honras
                .onItem()
                .transformToUniAndConcatenate(honra -> {
                    String chave = "00000000000000000000".equals(honra.getIdOperacaoAgenteCreditoLivre())
                            ? honra.getContratoRepasse()
                            : honra.getIdOperacaoAgenteCreditoLivre();

                    // Retorna o Uni resultante do hset
                    return reactiveHash.hset(CHAVE, chave, honra);
                })
                .collect()
                .asList() // Aguarda o processamento de todos os itens
                .chain(__ -> reactiveKey.expire(CHAVE, Duration.ofSeconds(segundos))) // Define expiração até meia-noite
                .replaceWithVoid(); // Converte para Uni<Void>
    }

    public Uni<SolicitacoesHonra> buscar(String operacao) {
        return reactiveHash.hget(CHAVE, operacao)
                .onItem()
                .ifNull()
                .failWith(() -> new RuntimeException("Solicitação não encontrada para a operacao: " + operacao));
    }

    public Multi<SolicitacoesHonra> listar() {
        // Função para ler o Hash do Redis e desserializar os valores
        // Esta lógica é comum para os casos de Cache Hit e Cache Miss (após o
        // salvamento)
        Uni<List<SolicitacoesHonra>> readAndDeserialize = reactiveHash.hgetall(CHAVE)
                .onItem().transform(map -> map.values().stream()
                        // NECESSÁRIO: Conversão do valor (String/byte[]) do Redis para o objeto
                        // SolicitacoesHonra
                        .map(this::deserialize)
                        .collect(Collectors.toList()));

        return reactiveHash.hlen(CHAVE)
                .onItem().transformToMulti(len -> {
                    if (len == 0L) {
                        List<SolicitacoesHonra> honras = serviceArquivo.lerArquivoTxt();
                        // 1. Chama salvarTodos e obtém o Uni<Void>
                        Uni<Void> saveOperation = salvarTodos(Multi.createFrom().items(honras.stream()));

                        // 2. Encadear a leitura: .onItem().transformToMulti() garante que
                        // a leitura só comece APÓS a conclusão (Void) do Uni de salvamento.
                        return saveOperation
                                .onItem().transformToMulti(
                                        v -> readAndDeserialize.onItem()
                                                .transformToMulti(list -> Multi.createFrom().items(list.stream())));
                    }
                    return reactiveHash.hgetall(CHAVE)
                            .onItem().transformToMulti(map -> Multi.createFrom().items(map.values().stream()));
                });
    }

    public Uni<Void> deletar(String operacao) {
        return reactiveHash.hdel(CHAVE, operacao).replaceWithVoid();
    }

    public Uni<Void> deletarTudo() {
        return reactiveKey.del(CHAVE).replaceWithVoid();
    }

    public Uni<List<String>> keys() {
        return reactiveKey.keys("*");
    }

    public Uni<Long> buscaDuracaokey() {
        return reactiveKey.ttl(CHAVE);
    }

    // No-op deserializer: values coming from Redis are already mapped as
    // SolicitacoesHonra
    private SolicitacoesHonra deserialize(SolicitacoesHonra raw) {
        return raw;
    }

    private long calcularSegundosAteMeiaNoite() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime meiaNoite = agora.toLocalDate().atTime(LocalTime.MAX).withSecond(59);
        return Duration.between(agora, meiaNoite.plusSeconds(1)).getSeconds();
    }
}
