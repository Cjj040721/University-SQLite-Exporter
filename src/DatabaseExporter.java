import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cao Junjia
 *  May-28-2025
 * DatabaseExporter is a utility for exporting SQLite database schema and data
 * to separate .sql files.
 * Output:
 * - <prefix>_schema.sql: Contains CREATE TABLE,FOREIGN KEY, PRIMARY KEY and index statements
 * - <prefix>_data.sql: Contains INSERT statements
 */
public class DatabaseExporter {

    // Main export logic: connect to SQLite DB, write schema and data to output files
    public static void exportDatabase(String dbName, String outputPrefix) {
        String url = "jdbc:sqlite:" + dbName;

        try {
            Files.createDirectories(Paths.get(outputPrefix).getParent());
        } catch (IOException e) {
            System.err.println(" Unable to create output directory: " + e.getMessage());
            return;
        }

        try (Connection conn = DriverManager.getConnection(url);
             FileWriter schemaWriter = new FileWriter(outputPrefix + "_schema.sql");
             FileWriter dataWriter = new FileWriter(outputPrefix + "_data.sql")) {

            System.out.println(" Connected to " + dbName);
            DatabaseMetaData metaData = conn.getMetaData();

            // Write metadata and export timestamp
            schemaWriter.write("-- =========================================\n");
            schemaWriter.write("-- Exported by DatabaseExporter\n");
            schemaWriter.write("-- Source Database: " + dbName + "\n");
            schemaWriter.write("-- JDBC Driver: " + metaData.getDriverName() + " " + metaData.getDriverVersion() + "\n");
            schemaWriter.write("-- DB Product: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion() + "\n");
            schemaWriter.write("-- Exported on: " + new java.util.Date() + "\n");
            schemaWriter.write("-- =========================================\n\n");
            schemaWriter.write("PRAGMA foreign_keys = OFF;\n\n");

            // Build dependency graph for topological sorting
            Map<String, Set<String>> dependencyGraph = new HashMap<>();
            List<String> allTables = new ArrayList<>();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (tableName.equals("sqlite_sequence")) continue;
                allTables.add(tableName);
                dependencyGraph.put(tableName, new HashSet<>());
                ResultSet fks = metaData.getImportedKeys(null, null, tableName);
                while (fks.next()) {
                    String pkTable = fks.getString("PKTABLE_NAME");
                    dependencyGraph.get(tableName).add(pkTable);
                }
            }

            // Sort tables by foreign key dependencies or fallback to default order
            List<String> sortedTables;
            try {
                sortedTables = topologicalSort(allTables, dependencyGraph);
            } catch (RuntimeException e) {
                System.err.println(" Warning: Cycle detected in foreign key dependencies. Exporting tables in default order.");
                sortedTables = allTables;
            }

            // Generate CREATE TABLE statements
            for (String tableName : sortedTables) {
                schemaWriter.write("-- =====================================\n");
                schemaWriter.write("-- CREATE TABLE for: " + tableName + "\n");
                schemaWriter.write("-- =====================================\n");
                schemaWriter.write("DROP TABLE IF EXISTS \"" + tableName + "\";\n");

                PreparedStatement ps = conn.prepareStatement("SELECT * FROM \"" + tableName + "\" LIMIT 1");
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData rsMeta = rs.getMetaData();
                List<String> columnDefs = new ArrayList<>();

                // Retrieve column nullability constraints
                ResultSet columnsMeta = metaData.getColumns(null, null, tableName, null);
                Map<String, Boolean> nullability = new HashMap<>();
                while (columnsMeta.next()) {
                    String colName = columnsMeta.getString("COLUMN_NAME");
                    boolean isNullable = "NO".equals(columnsMeta.getString("IS_NULLABLE"));
                    nullability.put(colName, isNullable);
                }

                // Define column types and NOT NULL constraints
                for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
                    String colName = rsMeta.getColumnName(i);
                    String colType = rsMeta.getColumnTypeName(i);

                    String definition = "    \"" + colName + "\" " + colType;
                    if (nullability.getOrDefault(colName, false)) {
                        definition += " NOT NULL";
                    }
                    columnDefs.add(definition);
                }

                // Add primary key constraint
                ResultSet pk = metaData.getPrimaryKeys(null, null, tableName);
                List<String> pkCols = new ArrayList<>();
                while (pk.next()) pkCols.add(pk.getString("COLUMN_NAME"));
                if (!pkCols.isEmpty())
                    columnDefs.add("    PRIMARY KEY (" + pkCols.stream().map(c -> "\"" + c + "\"").collect(Collectors.joining(", ")) + ")");

                // Add foreign key constraints
                ResultSet fk = metaData.getImportedKeys(null, null, tableName);
                while (fk.next()) {
                    String fkCol = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    String pkCol = fk.getString("PKCOLUMN_NAME");
                    columnDefs.add("    FOREIGN KEY (\"" + fkCol + "\") REFERENCES \"" + pkTable + "\"(\"" + pkCol + "\")");
                }

                // Write final CREATE TABLE statement
                schemaWriter.write("CREATE TABLE \"" + tableName + "\" (\n");
                for (int i = 0; i < columnDefs.size(); i++) {
                    schemaWriter.write(columnDefs.get(i));
                    schemaWriter.write(i < columnDefs.size() - 1 ? ",\n" : "\n");
                }
                schemaWriter.write(");\n\n");
            }

            // Generate CREATE INDEX statements
            for (String tableName : sortedTables) {
                ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false);
                Set<String> written = new HashSet<>();
                while (indexes.next()) {
                    String idxName = indexes.getString("INDEX_NAME");
                    if (idxName == null || written.contains(idxName)) continue;
                    String colName = indexes.getString("COLUMN_NAME");
                    String ascOrDesc = indexes.getString("ASC_OR_DESC");
                    String order = ("D".equals(ascOrDesc)) ? "DESC" : "ASC";
                    schemaWriter.write("CREATE INDEX \"" + idxName + "\" ON \"" + tableName + "\"(\"" + colName + "\" " + order + ");\n");
                    written.add(idxName);
                }
            }

            // Export data using single-row INSERT statements
            for (String tableName : sortedTables) {
                dataWriter.write("-- =====================================\n");
                dataWriter.write("-- Data for Table: " + tableName + "\n");
                dataWriter.write("-- =====================================\n");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + tableName + "\"");
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();

                while (rs.next()) {
                    StringBuilder insert = new StringBuilder("INSERT INTO \"" + tableName + "\" VALUES (\n        ");
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        if (value == null) {
                            insert.append("NULL");
                        } else if (value instanceof String) {
                            insert.append("'").append(((String) value).replace("'", "''")).append("'");
                        } else {
                            insert.append(value);
                        }
                        if (i < columnCount) insert.append(", ");
                    }
                    insert.append("\n);\n");
                    dataWriter.write(insert.toString());
                }
            }

            System.out.println(" Export complete: " + outputPrefix);

        } catch (SQLException | IOException e) {
            System.err.println(" Error with " + dbName + ": " + e.getMessage());
        }
    }

    // Topological sort for handling table creation order with foreign key dependencies
    private static List<String> topologicalSort(List<String> nodes, Map<String, Set<String>> edges) {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> temp = new HashSet<>();

        for (String node : nodes) {
            if (!visited.contains(node)) {
                dfs(node, edges, visited, temp, sorted);
            }
        }
        return sorted;
    }

    // Depth-first search helper for topological sort
    private static void dfs(String node, Map<String, Set<String>> edges, Set<String> visited, Set<String> temp, List<String> sorted) {
        if (temp.contains(node)) throw new RuntimeException("Cycle detected in table dependencies");
        if (visited.contains(node)) return;
        temp.add(node);
        for (String dep : edges.getOrDefault(node, Collections.emptySet())) {
            dfs(dep, edges, visited, temp, sorted);
        }
        temp.remove(node);
        visited.add(node);
        sorted.add(node);
    }

    // Main method to export multiple fixed-path databases
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println(" Failed to load SQLite driver: " + e.getMessage());
            return;
        }

        if (args.length > 0) {
            for (String dbName : args) {
                String filename = Paths.get(dbName).getFileName().toString();
                String outputName = filename.replaceFirst("\\.[^.]*$", "");
                exportDatabase(dbName, "output/" + outputName);
            }
            return;
        }

        exportDatabase("data/university.db", "output/university");
        exportDatabase("data/LSH.db", "output/LSH");
        exportDatabase("data/Chinook.db", "output/Chinook");
        exportDatabase("data/Northwind.db", "output/Northwind");
        exportDatabase("data/test1.db", "output/test1");
        exportDatabase("data/test2.db", "output/test2");
    }
}
