package br.edu.biblioteca.model;

import java.time.Year;

public class Livro {

    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private int ano;
    private boolean disponivel;

    // Para livros novos (ainda sem ID, sempre disponíveis)
    public Livro(String titulo, String autor, String isbn, int ano) {
        setTitulo(titulo);
        setAutor(autor);
        setIsbn(isbn);
        setAno(ano);
        this.disponivel = true;
    }

    // Para livros lidos do banco
    public Livro(int id, String titulo, String autor, String isbn, int ano, boolean disponivel) {
        this(titulo, autor, isbn, ano);
        this.id = id;
        this.disponivel = disponivel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }
        this.titulo = titulo.trim();
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("O autor é obrigatório.");
        }
        this.autor = autor.trim();
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("O ISBN não pode ser vazio.");
        }
        this.isbn = isbn.trim();
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        int anoAtual = Year.now().getValue();
        if (ano < 1450 || ano > anoAtual) {
            throw new IllegalArgumentException("O ano deve estar entre 1450 e " + anoAtual + ".");
        }
        this.ano = ano;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s | ISBN: %s | Ano: %d | %s",
                id, titulo, autor, isbn, ano, disponivel ? "Disponível" : "Emprestado");
    }
}