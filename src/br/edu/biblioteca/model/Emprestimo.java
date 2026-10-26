package br.edu.biblioteca.model;

import java.time.LocalDate;

public class Emprestimo {

    private int id;
    private int livroId;
    private String tituloLivro;
    private String nomeLeitor;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucao;

    public Emprestimo(int id, int livroId, String tituloLivro, String nomeLeitor,
                      LocalDate dataEmprestimo, LocalDate dataDevolucao) {
        this.id = id;
        this.livroId = livroId;
        this.tituloLivro = tituloLivro;
        this.nomeLeitor = nomeLeitor;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
    }

    public int getId() {
        return id;
    }

    public int getLivroId() {
        return livroId;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public String getNomeLeitor() {
        return nomeLeitor;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    @Override
    public String toString() {
        return String.format("[%d] \"%s\" (livro %d) | Leitor: %s | Emprestado em: %s",
                id, tituloLivro, livroId, nomeLeitor, dataEmprestimo);
    }
}