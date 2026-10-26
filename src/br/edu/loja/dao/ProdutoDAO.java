package br.edu.loja.dao;

import br.edu.loja.conexao.ConnectionFactory;
import br.edu.loja.exception.DAOException;
import br.edu.loja.exception.ProdutoNaoEncontradoException;
import br.edu.loja.exception.QuantidadeInvalidaException;
import br.edu.loja.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    private static final int ERRO_DUPLICADO = 1062;
    private static final int ERRO_CHAVE_ESTRANGEIRA = 1451;
    private static final int LIMITE_ESTOQUE_BAIXO = 5;

    public void inserir(Produto produto) {
        String sql = "INSERT INTO produto (nome, codigo, preco, quantidade) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getCodigo());
            ps.setBigDecimal(3, produto.getPreco());
            ps.setInt(4, produto.getQuantidade());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    produto.setId(chaves.getInt(1));
                }
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERRO_DUPLICADO) {
                throw new DAOException("Já existe um produto cadastrado com o código " + produto.getCodigo() + ".", e);
            }
            throw new DAOException("Erro de integridade ao inserir o produto: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir o produto: " + e.getMessage(), e);
        }
    }

    public List<Produto> listar() {
        String sql = "SELECT id, nome, codigo, preco, quantidade FROM produto ORDER BY nome";
        List<Produto> produtos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                produtos.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar os produtos: " + e.getMessage(), e);
        }
        return produtos;
    }

    public Produto buscarPorId(int id) throws ProdutoNaoEncontradoException {
        String sql = "SELECT id, nome, codigo, preco, quantidade FROM produto WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar o produto: " + e.getMessage(), e);
        }
        throw new ProdutoNaoEncontradoException(id);
    }

    public void atualizar(Produto produto) throws ProdutoNaoEncontradoException {
        String sql = "UPDATE produto SET nome = ?, codigo = ?, preco = ?, quantidade = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getCodigo());
            ps.setBigDecimal(3, produto.getPreco());
            ps.setInt(4, produto.getQuantidade());
            ps.setInt(5, produto.getId());

            if (ps.executeUpdate() == 0) {
                throw new ProdutoNaoEncontradoException(produto.getId());
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERRO_DUPLICADO) {
                throw new DAOException("Já existe outro produto cadastrado com o código " + produto.getCodigo() + ".", e);
            }
            throw new DAOException("Erro de integridade ao atualizar o produto: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar o produto: " + e.getMessage(), e);
        }
    }

    public void excluir(int id) throws ProdutoNaoEncontradoException {
        String sql = "DELETE FROM produto WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new ProdutoNaoEncontradoException(id);
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERRO_CHAVE_ESTRANGEIRA) {
                throw new DAOException("Não é possível excluir: este produto possui vendas registradas.", e);
            }
            throw new DAOException("Erro de integridade ao excluir o produto: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DAOException("Erro ao excluir o produto: " + e.getMessage(), e);
        }
    }

    public void reporEstoque(int id, int quantidade) throws ProdutoNaoEncontradoException {
        if (quantidade <= 0) {
            throw new QuantidadeInvalidaException("A quantidade de reposição deve ser maior que zero.");
        }

        String sql = "UPDATE produto SET quantidade = quantidade + ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantidade);
            ps.setInt(2, id);

            if (ps.executeUpdate() == 0) {
                throw new ProdutoNaoEncontradoException(id);
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao repor o estoque: " + e.getMessage(), e);
        }
    }

    public List<Produto> listarEstoqueBaixo() {
        String sql = "SELECT id, nome, codigo, preco, quantidade FROM produto "
                + "WHERE quantidade < ? ORDER BY quantidade, nome";
        List<Produto> produtos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, LIMITE_ESTOQUE_BAIXO);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    produtos.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar produtos com estoque baixo: " + e.getMessage(), e);
        }
        return produtos;
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("codigo"),
                rs.getBigDecimal("preco"),
                rs.getInt("quantidade"));
    }
}