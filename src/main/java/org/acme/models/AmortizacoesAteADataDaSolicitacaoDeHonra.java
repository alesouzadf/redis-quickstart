package org.acme.models;

public class AmortizacoesAteADataDaSolicitacaoDeHonra {
    private String dataVencimento;
    private Double valorDevido = Double.valueOf(0.00);
    private Double valorRecebido = Double.valueOf(0.00);

    public String getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(String dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Double getValorDevido() {
        return valorDevido;
    }

    public void setValorDevido(Double valorDevido) {
        this.valorDevido = valorDevido;
    }

    public Double getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(Double valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

}
