package br.edu.loja.dao;

import br.edu.loja.conexao.ConnectionFactory;
import br.edu.loja.exception.DAOException;
import br.edu.loja.exception.EstoqueInsuficienteException;
import br.edu.loja.exception.ProdutoNaoEncontradoException;
import br.edu.loja.exception.QuantidadeInvalidaException;
import br.edu.loja.model.Venda;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public Venda registrarVenda(int produtoId, int quantidade)
            throws ProdutoNaoEncontradoException, EstoqueInsuficienteException {

        if (quantidade <= 0) {
            throw new QuantidadeInvalidaException("A quantidade vendida deve ser maior que zero.");
        }

        String sqlBusca = "SELECT nome, preco, quantidade FROM produto WHERE id = ? FOR UPDATE";
        String sqlVenda = "INSERT INTO venda (produto_id, quantidade, valor_total) VALUES (?, ?, ?)";
        String sqlEstoque = "UPDATE produto SET quantidade = quantidade - ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            try {
                conn.setAutoCommit(false);

                String nome = null;
                BigDecimal total = null;

                try (PreparedStatement ps = conn.prepareStatement(sqlBusca)) {
                    ps.setInt(1, produtoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ProdutoNaoEncontradoException(produtoId);
                        }
                        nome = rs.getString("nome");
                        BigDecimal preco = rs.getBigDecimal("preco");
                        int estoque = rs.getInt("quantidade");

                        if (estoque < quantidade) {
                            throw new EstoqueInsuficienteException(nome, estoque, quantidade);
                        }
                        total = preco.multiply(BigDecimal.valueOf(quantidade));
                    }
                }

                int vendaId = 0;
                try (PreparedStatement ps = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, produtoId);
                    ps.setInt(2, quantidade);
                    ps.setBigDecimal(3, total);
                    ps.executeUpdate();

                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) {
                            vendaId = chaves.getInt(1);
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlEstoque)) {
                    ps.setInt(1, quantidade);
                    ps.setInt(2, produtoId);
                    ps.executeUpdate();
                }

                conn.commit();
                return new Venda(vendaId, produtoId, nome, quantidade, total, LocalDateTime.now());

            } catch (SQLException | ProdutoNaoEncontradoException | EstoqueInsuficienteException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Erro ao registrar a venda: " + e.getMessage(), e);
        }
    }

    public List<Venda> listarVendasDoDia() {
        String sql = "SELECT v.id, v.produto_id, p.nome, v.quantidade, v.valor_total, v.data_venda "
                + "FROM venda v JOIN produto p ON p.id = v.produto_id "
                + "WHERE DATE(v.data_venda) = CURDATE() ORDER BY v.data_venda";
        List<Venda> vendas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vendas.add(new Venda(
                        rs.getInt("id"),
                        rs.getInt("produto_id"),
                        rs.getString("nome"),
                        rs.getInt("quantidade"),
                        rs.getBigDecimal("valor_total"),
                        rs.getTimestamp("data_venda").toLocalDateTime()));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar as vendas do dia: " + e.getMessage(), e);
        }
        return vendas;
    }

    public BigDecimal totalVendidoNoDia() {
        String sql = "SELECT COALESCE(SUM(valor_total), 0) FROM venda WHERE DATE(data_venda) = CURDATE()";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getBigDecimal(1);

        } catch (SQLException e) {
            throw new DAOException("Erro ao calcular o total vendido no dia: " + e.getMessage(), e);
        }
    }
}