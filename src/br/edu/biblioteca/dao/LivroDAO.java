package br.edu.biblioteca.dao;

import br.edu.biblioteca.conexao.ConnectionFactory;
import br.edu.biblioteca.exception.DAOException;
import br.edu.biblioteca.exception.LivroNaoEncontradoException;
import br.edu.biblioteca.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {

    private static final int ERRO_DUPLICADO = 1062;
    private static final int ERRO_CHAVE_ESTRANGEIRA = 1451;

    public void inserir(Livro livro) {
        String sql = "INSERT INTO livro (titulo, autor, isbn, ano, disponivel) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            ps.setInt(4, livro.getAno());
            ps.setBoolean(5, livro.isDisponivel());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    livro.setId(chaves.getInt(1));
                }
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERRO_DUPLICADO) {
                throw new DAOException("Já existe um livro cadastrado com o ISBN " + livro.getIsbn() + ".", e);
            }
            throw new DAOException("Erro de integridade ao inserir o livro: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir o livro: " + e.getMessage(), e);
        }
    }

    public List<Livro> listar() {
        String sql = "SELECT id, titulo, autor, isbn, ano, disponivel FROM livro ORDER BY titulo";
        List<Livro> livros = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                livros.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar os livros: " + e.getMessage(), e);
        }
        return livros;
    }

    public Livro buscarPorId(int id) throws LivroNaoEncontradoException {
        String sql = "SELECT id, titulo, autor, isbn, ano, disponivel FROM livro WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar o livro: " + e.getMessage(), e);
        }
        throw new LivroNaoEncontradoException(id);
    }

    public List<Livro> buscarPorTitulo(String parteDoTitulo) {
        String sql = "SELECT id, titulo, autor, isbn, ano, disponivel FROM livro WHERE titulo LIKE ? ORDER BY titulo";
        List<Livro> livros = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + parteDoTitulo + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    livros.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar livros por título: " + e.getMessage(), e);
        }
        return livros;
    }

    public void atualizar(Livro livro) throws LivroNaoEncontradoException {
        String sql = "UPDATE livro SET titulo = ?, autor = ?, isbn = ?, ano = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            ps.setInt(4, livro.getAno());
            ps.setInt(5, livro.getId());

            if (ps.executeUpdate() == 0) {
                throw new LivroNaoEncontradoException(livro.getId());
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERRO_DUPLICADO) {
                throw new DAOException("Já existe outro livro cadastrado com o ISBN " + livro.getIsbn() + ".", e);
            }
            throw new DAOException("Erro de integridade ao atualizar o livro: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar o livro: " + e.getMessage(), e);
        }
    }

    public void excluir(int id) throws LivroNaoEncontradoException {
        String sql = "DELETE FROM livro WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new LivroNaoEncontradoException(id);
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERRO_CHAVE_ESTRANGEIRA) {
                throw new DAOException("Não é possível excluir: este livro possui empréstimos registrados.", e);
            }
            throw new DAOException("Erro de integridade ao excluir o livro: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DAOException("Erro ao excluir o livro: " + e.getMessage(), e);
        }
    }

    private Livro mapear(ResultSet rs) throws SQLException {
        return new Livro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("isbn"),
                rs.getInt("ano"),
                rs.getBoolean("disponivel"));
    }
}