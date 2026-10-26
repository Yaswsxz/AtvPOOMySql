package br.edu.loja;

import br.edu.loja.dao.ProdutoDAO;
import br.edu.loja.dao.VendaDAO;
import br.edu.loja.exception.EstoqueInsuficienteException;
import br.edu.loja.exception.ProdutoNaoEncontradoException;
import br.edu.loja.model.Produto;
import br.edu.loja.model.Venda;
import br.edu.loja.util.LogErros;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final ProdutoDAO produtoDAO = new ProdutoDAO();
    private static final VendaDAO vendaDAO = new VendaDAO();

    public static void main(String[] args) {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInt("Escolha uma opção: ");

            try {
                executar(opcao);
            } catch (ProdutoNaoEncontradoException | EstoqueInsuficienteException | RuntimeException e) {
                // RuntimeException cobre DAOException, QuantidadeInvalidaException e
                // IllegalArgumentException: é a última barreira para o programa nunca
                // encerrar por exceção não tratada, como pede o enunciado.
                System.out.println("Erro: " + e.getMessage());
                LogErros.registrar(e);
            }
        } while (opcao != 0);

        System.out.println("Encerrando o sistema. Até logo!");
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("===== LOJA =====");
        System.out.println("1 - Cadastrar produto");
        System.out.println("2 - Listar produtos");
        System.out.println("3 - Buscar produto por ID");
        System.out.println("4 - Atualizar produto");
        System.out.println("5 - Excluir produto");
        System.out.println("6 - Registrar venda");
        System.out.println("7 - Repor estoque");
        System.out.println("8 - Relatório: estoque baixo");
        System.out.println("9 - Relatório: vendas do dia");
        System.out.println("0 - Sair");
    }

    private static void executar(int opcao)
            throws ProdutoNaoEncontradoException, EstoqueInsuficienteException {
        switch (opcao) {
            case 1 -> cadastrar();
            case 2 -> listar();
            case 3 -> buscarPorId();
            case 4 -> atualizar();
            case 5 -> excluir();
            case 6 -> vender();
            case 7 -> repor();
            case 8 -> estoqueBaixo();
            case 9 -> vendasDoDia();
            case 0 -> { }
            default -> System.out.println("Opção inválida. Escolha um número do menu.");
        }
    }

    private static void cadastrar() {
        String nome = lerTexto("Nome: ");
        String codigo = lerTexto("Código: ");
        BigDecimal preco = lerDecimal("Preço: ");
        int quantidade = lerInt("Quantidade em estoque: ");

        Produto produto = new Produto(nome, codigo, preco, quantidade);
        produtoDAO.inserir(produto);
        System.out.println("Produto cadastrado com ID " + produto.getId() + ".");
    }

    private static void listar() {
        List<Produto> produtos = produtoDAO.listar();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        produtos.forEach(System.out::println);
    }

    private static void buscarPorId() throws ProdutoNaoEncontradoException {
        int id = lerInt("ID do produto: ");
        System.out.println(produtoDAO.buscarPorId(id));
    }

    private static void atualizar() throws ProdutoNaoEncontradoException {
        int id = lerInt("ID do produto: ");
        Produto produto = produtoDAO.buscarPorId(id);
        System.out.println("Atual: " + produto);
        System.out.println("(Pressione Enter para manter o valor atual)");

        String nome = lerTexto("Novo nome: ");
        if (!nome.isBlank()) {
            produto.setNome(nome);
        }

        String codigo = lerTexto("Novo código: ");
        if (!codigo.isBlank()) {
            produto.setCodigo(codigo);
        }

        String preco = lerTexto("Novo preço: ");
        if (!preco.isBlank()) {
            try {
                produto.setPreco(new BigDecimal(preco.trim().replace(",", ".")));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Preço inválido: digite apenas números.");
            }
        }

        String quantidade = lerTexto("Nova quantidade: ");
        if (!quantidade.isBlank()) {
            try {
                produto.setQuantidade(Integer.parseInt(quantidade.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Quantidade inválida: digite apenas números.");
            }
        }

        produtoDAO.atualizar(produto);
        System.out.println("Produto atualizado com sucesso.");
    }

    private static void excluir() throws ProdutoNaoEncontradoException {
        int id = lerInt("ID do produto: ");
        produtoDAO.excluir(id);
        System.out.println("Produto excluído com sucesso.");
    }

    private static void vender() throws ProdutoNaoEncontradoException, EstoqueInsuficienteException {
        int id = lerInt("ID do produto: ");
        int quantidade = lerInt("Quantidade: ");

        Venda venda = vendaDAO.registrarVenda(id, quantidade);
        System.out.printf("Venda registrada. Total: R$ %.2f%n", venda.getValorTotal());
    }

    private static void repor() throws ProdutoNaoEncontradoException {
        int id = lerInt("ID do produto: ");
        int quantidade = lerInt("Quantidade a repor: ");
        produtoDAO.reporEstoque(id, quantidade);
        System.out.println("Estoque reposto com sucesso.");
    }

    private static void estoqueBaixo() {
        List<Produto> produtos = produtoDAO.listarEstoqueBaixo();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto com estoque baixo.");
            return;
        }
        produtos.forEach(System.out::println);
    }

    private static void vendasDoDia() {
        List<Venda> vendas = vendaDAO.listarVendasDoDia();
        if (vendas.isEmpty()) {
            System.out.println("Nenhuma venda registrada hoje.");
        } else {
            vendas.forEach(System.out::println);
        }
        System.out.printf("Total vendido hoje: R$ %.2f%n", vendaDAO.totalVendidoNoDia());
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

    private static BigDecimal lerDecimal(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = sc.nextLine().trim().replace(",", ".");
            try {
                return new BigDecimal(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida: digite um valor numérico (ex.: 39,90).");
            }
        }
    }
}