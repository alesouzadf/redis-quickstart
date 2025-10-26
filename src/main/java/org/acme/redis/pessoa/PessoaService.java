package org.acme.redis.pessoa;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.acme.models.Pessoa;

@ApplicationScoped
public class PessoaService {

    private final HashCommands<String, String, Pessoa> hash;
    private final KeyCommands<String> keyCommands;

    private static final String CHAVE = "pessoas";

    public PessoaService(RedisDataSource redis) {
        this.hash = redis.hash(String.class, String.class, Pessoa.class);

        this.keyCommands = redis.key(String.class);
    }

    public void salvar(Pessoa pessoa) {
        hash.hset(CHAVE, pessoa.getNome(), pessoa);

        // Define expiração até meia-noite
        long segundosAteMeiaNoite = calcularSegundosAteMeiaNoite();
        keyCommands.expire(CHAVE, Duration.ofSeconds(segundosAteMeiaNoite));
    }

    public Pessoa buscar(String nome) {
        return hash.hget(CHAVE, nome);
    }

    public List<Pessoa> listar() {
        Map<String, Pessoa> todas = hash.hgetall(CHAVE);
        return todas.values().stream().collect(Collectors.toList());
    }

    public void deletar(String nome) {
        hash.hdel(CHAVE, nome);
    }

    public void deletarTudo() {
        keyCommands.del(CHAVE);
    }

    // Cálculo do tempo até meia-noite
    private long calcularSegundosAteMeiaNoite() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime meiaNoite = agora.toLocalDate().atTime(LocalTime.MAX).withSecond(59);
        return Duration.between(agora, meiaNoite.plusSeconds(1)).getSeconds();
    }
}
