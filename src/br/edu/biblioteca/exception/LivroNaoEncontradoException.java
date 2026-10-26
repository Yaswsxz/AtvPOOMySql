package br.edu.biblioteca.exception;

public class LivroNaoEncontradoException extends Exception {

    public LivroNaoEncontradoException(int id) {
        super("Livro não encontrado com o ID: " + id);
    }
}