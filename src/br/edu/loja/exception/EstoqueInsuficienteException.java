package br.edu.loja.exception;

public class EstoqueInsuficienteException extends Exception {

    public EstoqueInsuficienteException(String nomeProduto, int disponivel, int solicitado) {
        super("Estoque insuficiente para \"" + nomeProduto + "\". Disponível: "
                + disponivel + ", solicitado: " + solicitado);
    }
}