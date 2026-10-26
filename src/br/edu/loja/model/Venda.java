package br.edu.loja.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Venda {

    private int id;
    private int produtoId;
    private String nomeProduto;
    private int quantidade;
    private BigDecimal valorTotal;
    private LocalDateTime dataVenda;

    public Venda(int id, int produtoId, String nomeProduto, int quantidade,
                 BigDecimal valorTotal, LocalDateTime dataVenda) {
        this.id = id;
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
        this.dataVenda = dataVenda;
    }

    public int getId() {
        return id;
    }

    public int getProdutoId() {
        return produtoId;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    @Override
    public String toString() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%d] %s | Qtd: %d | Total: R$ %.2f | %s",
                id, nomeProduto, quantidade, valorTotal, dataVenda.format(formato));
    }
}