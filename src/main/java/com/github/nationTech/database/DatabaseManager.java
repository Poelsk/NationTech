package com.github.nationTech.database;

import com.github.nationTech.NationTech;
import com.github.nationTech.model.Technology;
import com.github.nationTech.model.TechnologyType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final NationTech plugin;
    private HikariDataSource dataSource;
    private final File dbFile;

    public DatabaseManager(NationTech plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "nationtech.db");
    }

    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            HikariConfig config = new HikariConfig();
            config.setPoolName("NationTech-HikariPool");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            this.dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Pool de conexiones a la base de datos establecido.");

            try (Connection connection = dataSource.getConnection()) {
                setupTables(connection);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo establecer la conexión inicial para crear las tablas.", e);
            }
        });
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Pool de conexiones cerrado.");
        }
    }

    private void setupTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS tecnologias (id TEXT NOT NULL, tree_id TEXT NOT NULL, nombre TEXT NOT NULL, padre_id TEXT, tipo TEXT NOT NULL, row INTEGER NOT NULL, column INTEGER NOT NULL, requisitos TEXT NOT NULL, icono TEXT NOT NULL, recompensa TEXT NOT NULL, PRIMARY KEY (id, tree_id));");
            statement.execute("CREATE TABLE IF NOT EXISTS progreso_naciones (nation_uuid TEXT NOT NULL, tecnologia_id TEXT NOT NULL, PRIMARY KEY (nation_uuid, tecnologia_id));");
        }
    }

    public CompletableFuture<List<Technology>> loadAllTechnologies() {
        return CompletableFuture.supplyAsync(() -> {
            List<Technology> technologies = new ArrayList<>();
            String sql = "SELECT * FROM tecnologias;";
            try (Connection connection = dataSource.getConnection();
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    technologies.add(new Technology(rs.getString("tree_id"), rs.getString("id"), rs.getInt("row"), rs.getInt("column"), rs.getString("nombre"), rs.getString("padre_id"), TechnologyType.valueOf(rs.getString("tipo")), rs.getString("requisitos"), rs.getString("icono"), rs.getString("recompensa")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar las tecnologías desde la base de datos.", e);
            }
            return technologies;
        });
    }

    public CompletableFuture<Void> saveTechnology(Technology tech) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO tecnologias (id, tree_id, nombre, padre_id, tipo, row, column, requisitos, icono, recompensa) VALUES(?,?,?,?,?,?,?,?,?,?);";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, tech.getId());
                pstmt.setString(2, tech.getTreeId());
                pstmt.setString(3, tech.getNombre());
                pstmt.setString(4, tech.getPadreId());
                pstmt.setString(5, tech.getTipo().name());
                pstmt.setInt(6, tech.getRow());
                pstmt.setInt(7, tech.getColumn());
                pstmt.setString(8, tech.getRequisitos());
                pstmt.setString(9, tech.getIcono());
                pstmt.setString(10, tech.getRecompensa());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al guardar la tecnología " + tech.getId() + " en el árbol " + tech.getTreeId(), e);
            }
        });
    }

    public CompletableFuture<Void> deleteTechnology(String treeId, String technologyId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM tecnologias WHERE tree_id = ? AND id = ?;";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, treeId);
                pstmt.setString(2, technologyId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al eliminar la tecnología " + technologyId + " del árbol " + treeId, e);
            }
        });
    }

    public CompletableFuture<Set<String>> getNationUnlockedTechs(UUID nationUUID) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> unlockedTechs = new HashSet<>();
            String sql = "SELECT tecnologia_id FROM progreso_naciones WHERE nation_uuid = ?;";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nationUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    unlockedTechs.add(rs.getString("tecnologia_id"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar el progreso de la nación " + nationUUID, e);
            }
            return unlockedTechs;
        });
    }

    public CompletableFuture<Void> addUnlockedTechnology(UUID nationUUID, String techId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO progreso_naciones (nation_uuid, tecnologia_id) VALUES(?,?);";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nationUUID.toString());
                pstmt.setString(2, techId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al guardar el progreso de la nación " + nationUUID, e);
            }
        });
    }

    public CompletableFuture<Void> removeUnlockedTechnology(UUID nationUUID, String techId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM progreso_naciones WHERE nation_uuid = ? AND tecnologia_id = ?;";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nationUUID.toString());
                pstmt.setString(2, techId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al eliminar el progreso de la tecnología " + techId + " para la nación " + nationUUID, e);
            }
        });
    }
}