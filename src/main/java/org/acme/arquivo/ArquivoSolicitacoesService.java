package org.acme.arquivo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.acme.models.AmortizacoesAteADataDaSolicitacaoDeHonra;
import org.acme.models.SolicitacoesHonra;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArquivoSolicitacoesService {

    public List<SolicitacoesHonra> lerArquivoTxt() {

        String caminhoArquivoTxt = "src/public/operacoes_credito.txt"; // caminho do arquivo TXT

        List<SolicitacoesHonra> listaOperacoes = new ArrayList<>();
        SolicitacoesHonra operacaoAtual = null;

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoTxt))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                char tipo = linha.charAt(0);

                if (tipo == '1') {
                    operacaoAtual = new SolicitacoesHonra();
                    operacaoAtual.setIdOperacaoAgenteCreditoLivre(linha.substring(1, 21).trim());
                    operacaoAtual.setSistemaRepasse(linha.substring(21, 24).trim());
                    operacaoAtual.setContratoRepasse(linha.substring(24, 35).trim());
                    operacaoAtual.setValorSaldoDevedorPrincipal(Double.parseDouble(linha.substring(35, 54).trim()));
                    operacaoAtual.setValorSaldoDeJurosNaNormalidade(Double.parseDouble(linha.substring(54, 73).trim()));
                    operacaoAtual.setValorEncargosMoratorios(Double.parseDouble(linha.substring(73, 92).trim()));
                    listaOperacoes.add(operacaoAtual);
                } else if (tipo == '2' && operacaoAtual != null) {
                    // Amortização associada à última operação lida
                    AmortizacoesAteADataDaSolicitacaoDeHonra amortizacoes = new AmortizacoesAteADataDaSolicitacaoDeHonra();
                    amortizacoes.setDataVencimento(linha.substring(1, 11).trim());
                    amortizacoes.setValorDevido(Double.parseDouble(linha.substring(11, 30).trim()));
                    amortizacoes.setValorRecebido(Double.parseDouble(linha.substring(30, 49).trim()));

                    if (operacaoAtual.getAmortizacoesAteADataDaSolicitacaoDeHonra() == null) {
                        List<AmortizacoesAteADataDaSolicitacaoDeHonra> lista = new ArrayList<>();
                        lista.add(amortizacoes);
                        operacaoAtual.setAmortizacoesAteADataDaSolicitacaoDeHonra(lista);
                    } else {
                        operacaoAtual.getAmortizacoesAteADataDaSolicitacaoDeHonra().add(amortizacoes);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listaOperacoes;
    }
}
