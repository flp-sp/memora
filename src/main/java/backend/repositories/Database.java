package backend.repositories;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

public final class Database {
    private static HikariDataSource dataSource;
    
    private Database(){};


    private static synchronized HikariDataSource pool() {
        if (dataSource == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(getEnvVar("DB_URL"));
            cfg.setUsername(getEnvVar("DB_USER"));
            cfg.setPassword(getEnvVar("DB_PASS"));
            cfg.setMaximumPoolSize(5);
            cfg.setMinimumIdle(0);
            cfg.setConnectionTimeout(30_000);
            cfg.setIdleTimeout(60_000);
            cfg.setMaxLifetime(300_000);
            dataSource = new HikariDataSource(cfg);
        }
        return dataSource;
    }


    public static Connection getConnection() throws SQLException{
        return pool().getConnection();
    }


    private static String getEnvVar(String nome){
        Dotenv dotenv = Dotenv.configure().directory(".").filename(".env").load();
        String envVar = dotenv.get(nome);

        if (envVar == null || envVar.isBlank()){
            throw new IllegalStateException("Variável de ambiente " + envVar + " não pôde ser encontrada.");
        }

        return envVar;
    }
}
