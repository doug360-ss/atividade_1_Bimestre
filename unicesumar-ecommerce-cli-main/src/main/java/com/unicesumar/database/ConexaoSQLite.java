package com.unicesumar.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoSQLite {
    private static final String URL = "jdbc:sqlite:database.sqlite";

    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Conectado com sucesso ao SQLite!");
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao SQLite: " + e.getMessage(), e);
        }
    }
}