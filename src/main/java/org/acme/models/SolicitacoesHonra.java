package org.acme.models;

import java.util.List;

public class SolicitacoesHonra {
    private String idOperacaoAgenteCreditoLivre;
    private String sistemaRepasse;
    private String contratoRepasse;
    private Double valorSaldoDevedorPrincipal = Double.valueOf(0.00);
    private Double valorSaldoDeJurosNaNormalidade = Double.valueOf(0.00);
    private Double valorEncargosMoratorios = Double.valueOf(0.00);
    List<AmortizacoesAteADataDaSolicitacaoDeHonra> amortizacoesAteADataDaSolicitacaoDeHonra;

    public String getIdOperacaoAgenteCreditoLivre() {
        return idOperacaoAgenteCreditoLivre;
    }

    public void setIdOperacaoAgenteCreditoLivre(String idOperacaoAgenteCreditoLivre) {
        this.idOperacaoAgenteCreditoLivre = idOperacaoAgenteCreditoLivre;
    }

    public String getSistemaRepasse() {
        return sistemaRepasse;
    }

    public void setSistemaRepasse(String sistemaRepasse) {
        this.sistemaRepasse = sistemaRepasse;
    }

    public String getContratoRepasse() {
        return contratoRepasse;
    }

    public void setContratoRepasse(String contratoRepasse) {
        this.contratoRepasse = contratoRepasse;
    }

    public Double getValorSaldoDevedorPrincipal() {
        return valorSaldoDevedorPrincipal;
    }

    public void setValorSaldoDevedorPrincipal(Double valorSaldoDevedorPrincipal) {
        this.valorSaldoDevedorPrincipal = valorSaldoDevedorPrincipal;
    }

    public Double getValorSaldoDeJurosNaNormalidade() {
        return valorSaldoDeJurosNaNormalidade;
    }

    public void setValorSaldoDeJurosNaNormalidade(Double valorSaldoDeJurosNaNormalidade) {
        this.valorSaldoDeJurosNaNormalidade = valorSaldoDeJurosNaNormalidade;
    }

    public Double getValorEncargosMoratorios() {
        return valorEncargosMoratorios;
    }

    public void setValorEncargosMoratorios(Double valorEncargosMoratorios) {
        this.valorEncargosMoratorios = valorEncargosMoratorios;
    }

    public List<AmortizacoesAteADataDaSolicitacaoDeHonra> getAmortizacoesAteADataDaSolicitacaoDeHonra() {
        return amortizacoesAteADataDaSolicitacaoDeHonra;
    }

    public void setAmortizacoesAteADataDaSolicitacaoDeHonra(
            List<AmortizacoesAteADataDaSolicitacaoDeHonra> amortizacoesAteADataDaSolicitacaoDeHonra) {
        this.amortizacoesAteADataDaSolicitacaoDeHonra = amortizacoesAteADataDaSolicitacaoDeHonra;
    }

    @Override
    public String toString() {
        return "solicitacoesHonra [idOperacaoAgenteCreditoLivre=" + idOperacaoAgenteCreditoLivre + ", sistemaRepasse="
                + sistemaRepasse + ", contratoRepasse=" + contratoRepasse + ", valorSaldoDevedorPrincipal="
                + valorSaldoDevedorPrincipal + ", valorSaldoDeJurosNaNormalidade=" + valorSaldoDeJurosNaNormalidade
                + ", valorEncargosMoratorios=" + valorEncargosMoratorios + ", amortizacoesAteADataDaSolicitacaoDeHonra="
                + amortizacoesAteADataDaSolicitacaoDeHonra + "]";
    }

}
