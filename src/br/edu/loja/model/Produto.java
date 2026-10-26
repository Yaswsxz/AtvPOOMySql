package br.edu.loja.model;

import java.math.BigDecimal;

public class Produto {

    private int id;
    private String nome;
    private String codigo;
    private BigDecimal preco;
    private int quantidade;

    public Produto(String nome, String codigo, BigDecimal preco, int quantidade) {
        setNome(nome);
        setCodigo(codigo);
        setPreco(preco);
        setQuantidade(quantidade);
    }

    public Produto(int id, String nome, String codigo, BigDecimal preco, int quantidade) {
        this(nome, codigo, preco, quantidade);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código do produto é obrigatório.");
        }
        this.codigo = codigo.trim();
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | Código: %s | Preço: R$ %.2f | Estoque: %d",
                id, nome, codigo, preco, quantidade);
    }
}