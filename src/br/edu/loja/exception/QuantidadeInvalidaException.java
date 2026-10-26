package br.edu.loja.exception;

public class QuantidadeInvalidaException extends RuntimeException {

    public QuantidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}