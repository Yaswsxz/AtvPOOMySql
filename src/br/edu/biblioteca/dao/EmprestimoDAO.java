package br.edu.biblioteca.dao;

import br.edu.biblioteca.conexao.ConnectionFactory;
import br.edu.biblioteca.exception.DAOException;
import br.edu.biblioteca.exception.LivroIndisponivelException;
import br.edu.biblioteca.exception.LivroNaoEncontradoException;
import br.edu.biblioteca.model.Emprestimo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO {

    public void registrarEmprestimo(int livroId, String nomeLeitor)
            throws LivroNaoEncontradoException, LivroIndisponivelException {

        if (nomeLeitor == null || nomeLeitor.isBlank()) {
            throw new IllegalArgumentException("O nome do leitor é obrigatório.");
        }

        String sqlBusca = "SELECT titulo, disponivel FROM livro WHERE id = ? FOR UPDATE";
        String sqlInsere = "INSERT INTO emprestimo (livro_id, nome_leitor, data_emprestimo) VALUES (?, ?, ?)";
        String sqlAtualiza = "UPDATE livro SET disponivel = FALSE WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(sqlBusca)) {
                    ps.setInt(1, livroId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new LivroNaoEncontradoException(livroId);
                        }
                        if (!rs.getBoolean("disponivel")) {
                            throw new LivroIndisponivelException(rs.getString("titulo"));
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlInsere)) {
                    ps.setInt(1, livroId);
                    ps.setString(2, nomeLeitor.trim());
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlAtualiza)) {
                    ps.setInt(1, livroId);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException | LivroNaoEncontradoException | LivroIndisponivelException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Erro ao registrar o empréstimo: " + e.getMessage(), e);
        }
    }

    public void registrarDevolucao(int livroId) throws LivroNaoEncontradoException {
        String sqlBusca = "SELECT disponivel FROM livro WHERE id = ? FOR UPDATE";
        String sqlDevolve = "UPDATE emprestimo SET data_devolucao = ? WHERE livro_id = ? AND data_devolucao IS NULL";
        String sqlLibera = "UPDATE livro SET disponivel = TRUE WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(sqlBusca)) {
                    ps.setInt(1, livroId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new LivroNaoEncontradoException(livroId);
                        }
                        if (rs.getBoolean("disponivel")) {
                            throw new DAOException("Este livro não está emprestado.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlDevolve)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, livroId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlLibera)) {
                    ps.setInt(1, livroId);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException | LivroNaoEncontradoException | DAOException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Erro ao registrar a devolução: " + e.getMessage(), e);
        }
    }

    public List<Emprestimo> listarEmAberto() {
        String sql = "SELECT e.id, e.livro_id, l.titulo, e.nome_leitor, e.data_emprestimo "
                + "FROM emprestimo e JOIN livro l ON l.id = e.livro_id "
                + "WHERE e.data_devolucao IS NULL ORDER BY e.data_emprestimo";
        List<Emprestimo> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Emprestimo(
                        rs.getInt("id"),
                        rs.getInt("livro_id"),
                        rs.getString("titulo"),
                        rs.getString("nome_leitor"),
                        rs.getDate("data_emprestimo").toLocalDate(),
                        null));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar empréstimos em aberto: " + e.getMessage(), e);
        }
        return lista;
    }
}