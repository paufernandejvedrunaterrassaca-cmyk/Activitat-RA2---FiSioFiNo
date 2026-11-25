package com.fisiofino.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**** AQUESTA CLASSE DEFINEIX LA CONNEXIÓ I EL DATASOURCE */

public class Connexio {

    private static HikariDataSource dataSource;

    /**************
     * S'HA D'IMPLEMENTAR TOT INCLOSES LES FUNCIONS
     * DEFINIDES A SOTA
     ********************/

    /**
     * Inicia el pool de connexions amb el fitxer de configuració EXTERN (fora del jar).
     *
     * @param connPropertiesPath ruta del fitxer conn.properties (absoluta o relativa al directori de
     *                           treball). Conté DB_URL, DB_USER, DB_PASSWORD i DB_MAX_POOL; com que
     *                           inclou la contrasenya, no s'ha de pujar al repositori.
     */
    public static void iniciaConnexio(String connPropertiesPath) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(connPropertiesPath)) {
            properties.load(input);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("DB_URL"));
        config.setUsername(properties.getProperty("DB_USER"));
        config.setPassword(properties.getProperty("DB_PASSWORD"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("DB_MAX_POOL")));
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void tancaConnexio() {
    if (dataSource != null && !dataSource.isClosed()) {
        dataSource.close();
    }
}

}
