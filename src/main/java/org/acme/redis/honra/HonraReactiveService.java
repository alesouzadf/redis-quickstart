package org.acme.redis.honra;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.acme.models.SolicitacoesHonra;

@ApplicationScoped
public class HonraReactiveService {

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
                .transformToUniAndConcatenate(
                        honra -> reactiveHash.hset(CHAVE, honra.getIdOperacaoAgenteCreditoLivre(), honra))
                .collect().asList()
                .chain(__ -> reactiveKey.expire(CHAVE, Duration.ofSeconds(segundos)))
                .replaceWithVoid();
    }

    public Uni<SolicitacoesHonra> buscar(String idOperacaoCreditoLivre) {
        return reactiveHash.hget(CHAVE, idOperacaoCreditoLivre);
    }

    public Multi<SolicitacoesHonra> listar() {
        return reactiveHash.hgetall(CHAVE)
                .onItem().transformToMulti(map -> Multi.createFrom().items(map.values().stream()));
    }

    public Uni<Void> deletar(String idOperacaoCreditoLivre) {
        return reactiveHash.hdel(CHAVE, idOperacaoCreditoLivre).replaceWithVoid();
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

    private long calcularSegundosAteMeiaNoite() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime meiaNoite = agora.toLocalDate().atTime(LocalTime.MAX).withSecond(59);
        return Duration.between(agora, meiaNoite.plusSeconds(1)).getSeconds();
    }
}
