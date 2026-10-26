package br.edu.loja.exception;

public class ProdutoNaoEncontradoException extends Exception {

    public ProdutoNaoEncontradoException(int id) {
        super("Produto não encontrado com o ID: " + id);
    }
}