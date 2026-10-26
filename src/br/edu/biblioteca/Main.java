package br.edu.biblioteca;

import br.edu.biblioteca.dao.LivroDAO;
import br.edu.biblioteca.exception.DAOException;
import br.edu.biblioteca.exception.LivroNaoEncontradoException;
import br.edu.biblioteca.model.Livro;

public class Main {

    public static void main(String[] args) {
        LivroDAO dao = new LivroDAO();

        try {
            Livro livro = new Livro("Dom Casmurro", "Machado de Assis", "978-85-359-0277-5", 1899);
            dao.inserir(livro);
            System.out.println("Inserido com ID " + livro.getId());
        } catch (DAOException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("--- Lista ---");
        dao.listar().forEach(System.out::println);

        System.out.println("--- Busca por 'casm' ---");
        dao.buscarPorTitulo("casm").forEach(System.out::println);

        try {
            dao.buscarPorId(9999);
        } catch (LivroNaoEncontradoException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}