package com.api.rapi.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CrearBaseDatos {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/rapientrega";
        String usuario = "root";
        String password = "";

        String sql = "CREATE DATABASE IF NOT EXISTS rapientrega "
                   + "CHARACTER SET utf8mb4 "
                   + "COLLATE utf8mb4_unicode_ci";

        try (
            Connection conexion =
                    DriverManager.getConnection(url, usuario, password);

            Statement statement = conexion.createStatement()
        ) {

            statement.executeUpdate(sql);

            System.out.println(
                    "Base de datos rapientrega creada correctamente.");

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la base de datos: " + e.getMessage());
        }
    }
}
