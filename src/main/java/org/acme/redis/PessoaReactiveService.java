package org.acme.redis;

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

@ApplicationScoped
public class PessoaReactiveService {

    private final ReactiveHashCommands<String, String, Pessoa> reactiveHash;
    private final ReactiveKeyCommands<String> reactiveKey;
    private static final String CHAVE = "pessoas-reactive";

    public PessoaReactiveService(ReactiveRedisDataSource reactiveRedis) {
        this.reactiveHash = reactiveRedis.hash(String.class, String.class, Pessoa.class);
        this.reactiveKey = reactiveRedis.key(String.class);
    }

    public Uni<Void> salvar(Pessoa pessoa) {
        long segundos = calcularSegundosAteMeiaNoite();
        return reactiveHash.hset(CHAVE, pessoa.getNome(), pessoa)
                .chain(() -> reactiveKey.expire(CHAVE, Duration.ofSeconds(segundos)))
                .replaceWithVoid();
    }

    public Uni<Void> salvarTodos(Multi<Pessoa> pessoas) {
        long segundos = calcularSegundosAteMeiaNoite();
        return pessoas
                .onItem().transformToUniAndConcatenate(pessoa -> reactiveHash.hset(CHAVE, pessoa.getNome(), pessoa))
                .collect().asList()
                .chain(__ -> reactiveKey.expire(CHAVE, Duration.ofSeconds(segundos)))
                .replaceWithVoid();
    }

    public Uni<Pessoa> buscar(String nome) {
        return reactiveHash.hget(CHAVE, nome);
    }

    public Multi<Pessoa> listar() {
        return reactiveHash.hgetall(CHAVE)
                .onItem().transformToMulti(map -> Multi.createFrom().items(map.values().stream()));
    }

    // Atualiza um campo específico de uma pessoa (nome, idade, etc.)
    public Uni<Void> atualizar(String id, String nome, String idade) {
        return reactiveHash.hget(CHAVE, id)
                .onItem().ifNull().failWith(() -> new RuntimeException("Pessoa não encontrada"))
                .onItem().transformToUni(pessoa -> {
                    if (nome != null)
                        pessoa.setNome(nome);
                    if (idade != null)
                        pessoa.setIdade(idade);
                    return reactiveHash.hset(CHAVE, id, pessoa).replaceWithVoid();
                });
    }

    public Uni<Void> deletar(String nome) {
        return reactiveHash.hdel(CHAVE, nome).replaceWithVoid();
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
