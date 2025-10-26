package org.acme.arquivo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.acme.models.Pessoa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArquivoPessoaService {

    public List<Pessoa> lerPessoasDoArquivo() {
        // Caminho ajustado para a pasta src/public
        String caminhoArquivo = "src/public/pessoas_seq.txt";

        List<Pessoa> pessoas = new ArrayList<>();
        final int TAM_NOME = 30;
        final int TAM_IDADE = 3;

        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty())
                    continue;

                String nome = linha.substring(0, TAM_NOME).trim().replaceAll("\\s+", "");
                String idade = linha.substring(TAM_NOME, TAM_NOME + TAM_IDADE).trim();

                pessoas.add(new Pessoa(nome, idade));
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }

        // Conversão da lista em JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonSaida = gson.toJson(pessoas);

        // Exibe o JSON no console
        System.out.println(jsonSaida);

        return pessoas;
    }
}
