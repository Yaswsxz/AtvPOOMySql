package br.edu.biblioteca;

import br.edu.biblioteca.dao.EmprestimoDAO;
import br.edu.biblioteca.dao.LivroDAO;
import br.edu.biblioteca.exception.DAOException;
import br.edu.biblioteca.exception.LivroIndisponivelException;
import br.edu.biblioteca.exception.LivroNaoEncontradoException;
import br.edu.biblioteca.model.Emprestimo;
import br.edu.biblioteca.model.Livro;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final LivroDAO livroDAO = new LivroDAO();
    private static final EmprestimoDAO emprestimoDAO = new EmprestimoDAO();

    public static void main(String[] args) {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInt("Escolha uma opção: ");

            try {
                executar(opcao);
            } catch (LivroNaoEncontradoException | LivroIndisponivelException e) {
                System.out.println("Aviso: " + e.getMessage());
            } catch (DAOException | IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (opcao != 0);

        System.out.println("Encerrando o sistema. Até logo!");
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("===== BIBLIOTECA =====");
        System.out.println("1 - Cadastrar livro");
        System.out.println("2 - Listar livros");
        System.out.println("3 - Buscar livro por ID");
        System.out.println("4 - Buscar livros por título");
        System.out.println("5 - Atualizar livro");
        System.out.println("6 - Excluir livro");
        System.out.println("7 - Registrar empréstimo");
        System.out.println("8 - Registrar devolução");
        System.out.println("9 - Listar empréstimos em aberto");
        System.out.println("0 - Sair");
    }

    private static void executar(int opcao) throws LivroNaoEncontradoException, LivroIndisponivelException {
        switch (opcao) {
            case 1 -> cadastrar();
            case 2 -> listar();
            case 3 -> buscarPorId();
            case 4 -> buscarPorTitulo();
            case 5 -> atualizar();
            case 6 -> excluir();
            case 7 -> emprestar();
            case 8 -> devolver();
            case 9 -> listarEmAberto();
            case 0 -> { }
            default -> System.out.println("Opção inválida. Escolha um número do menu.");
        }
    }

    private static void cadastrar() {
        String titulo = lerTexto("Título: ");
        String autor = lerTexto("Autor: ");
        String isbn = lerTexto("ISBN: ");
        int ano = lerInt("Ano: ");

        Livro livro = new Livro(titulo, autor, isbn, ano);
        livroDAO.inserir(livro);
        System.out.println("Livro cadastrado com ID " + livro.getId() + ".");
    }

    private static void listar() {
        List<Livro> livros = livroDAO.listar();
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro cadastrado.");
            return;
        }
        livros.forEach(System.out::println);
    }

    private static void buscarPorId() throws LivroNaoEncontradoException {
        int id = lerInt("ID do livro: ");
        System.out.println(livroDAO.buscarPorId(id));
    }

    private static void buscarPorTitulo() {
        String parte = lerTexto("Parte do título: ");
        List<Livro> livros = livroDAO.buscarPorTitulo(parte);
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro encontrado com esse título.");
            return;
        }
        livros.forEach(System.out::println);
    }

    private static void atualizar() throws LivroNaoEncontradoException {
        int id = lerInt("ID do livro: ");
        Livro livro = livroDAO.buscarPorId(id);
        System.out.println("Atual: " + livro);
        System.out.println("(Pressione Enter para manter o valor atual)");

        String titulo = lerTexto("Novo título: ");
        if (!titulo.isBlank()) {
            livro.setTitulo(titulo);
        }

        String autor = lerTexto("Novo autor: ");
        if (!autor.isBlank()) {
            livro.setAutor(autor);
        }

        String isbn = lerTexto("Novo ISBN: ");
        if (!isbn.isBlank()) {
            livro.setIsbn(isbn);
        }

        String ano = lerTexto("Novo ano: ");
        if (!ano.isBlank()) {
            try {
                livro.setAno(Integer.parseInt(ano.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Ano inválido: digite apenas números.");
            }
        }

        livroDAO.atualizar(livro);
        System.out.println("Livro atualizado com sucesso.");
    }

    private static void excluir() throws LivroNaoEncontradoException {
        int id = lerInt("ID do livro: ");
        livroDAO.excluir(id);
        System.out.println("Livro excluído com sucesso.");
    }

    private static void emprestar() throws LivroNaoEncontradoException, LivroIndisponivelException {
        int id = lerInt("ID do livro: ");
        String leitor = lerTexto("Nome do leitor: ");
        emprestimoDAO.registrarEmprestimo(id, leitor);
        System.out.println("Empréstimo registrado com sucesso.");
    }

    private static void devolver() throws LivroNaoEncontradoException {
        int id = lerInt("ID do livro: ");
        emprestimoDAO.registrarDevolucao(id);
        System.out.println("Devolução registrada com sucesso.");
    }

    private static void listarEmAberto() {
        List<Emprestimo> lista = emprestimoDAO.listarEmAberto();
        if (lista.isEmpty()) {
            System.out.println("Nenhum empréstimo em aberto.");
            return;
        }
        lista.forEach(System.out::println);
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return sc.nextLine();
    }

    private static int lerInt(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = sc.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida: digite apenas números.");
            }
        }
    }
}