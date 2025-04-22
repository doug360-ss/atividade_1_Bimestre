package com.unicesumar.repository;

import com.unicesumar.entities.Sale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class SaleRepository {
    private final Connection connection;

    public SaleRepository(Connection connection) {
        this.connection = connection;
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                         "uuid VARCHAR(36) PRIMARY KEY, " +
                         "user_uuid VARCHAR(36) NOT NULL, " +
                         "total NUMERIC NOT NULL, " +
                         "payment_type VARCHAR(20) NOT NULL, " +
                         "timestamp BIGINT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_products (" +
                         "sale_uuid VARCHAR(36) NOT NULL, " +
                         "product_uuid VARCHAR(36) NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabelas de venda: " + e.getMessage(), e);
        }
    }

    public void save(Sale sale) {
        String insertSale = "INSERT INTO sales (uuid, user_uuid, total, payment_type, timestamp) VALUES (?, ?, ?, ?, ?)";
        String insertSP   = "INSERT INTO sale_products (sale_uuid, product_uuid) VALUES (?, ?)";

        try (PreparedStatement stmtSale = connection.prepareStatement(insertSale);
             PreparedStatement stmtSP   = connection.prepareStatement(insertSP)) {

            // Inserir venda
            stmtSale.setString(1, sale.getUuid().toString());
            stmtSale.setString(2, sale.getUserId().toString());
            stmtSale.setDouble(3, sale.getTotal());
            stmtSale.setString(4, sale.getPaymentType().name());
            stmtSale.setLong(5, sale.getTimestamp());
            stmtSale.executeUpdate();

            // Inserir relação venda-produto
            for (UUID pid : sale.getProductIds()) {
                stmtSP.setString(1, sale.getUuid().toString());
                stmtSP.setString(2, pid.toString());
                stmtSP.addBatch();
            }
            stmtSP.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar venda: " + e.getMessage(), e);
        }
    }
}