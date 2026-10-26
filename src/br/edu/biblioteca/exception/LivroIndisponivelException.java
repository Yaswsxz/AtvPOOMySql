package br.edu.biblioteca.exception;

public class LivroIndisponivelException extends Exception {

    public LivroIndisponivelException(String titulo) {
        super("O livro \"" + titulo + "\" já está emprestado.");
    }
}