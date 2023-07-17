// PostgresImplementation
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import static java.util.stream.Collectors.toMap;

public class PostgresVersionedRepository<K, V extends Entity<K>> extends PostgresRepository<K, V> {

    public static final String START_DATE_FIELD = "start_date";
    public static final String CLOSE_DATE_FIELD = "close_date";

    private final Logger logger;
    private final RepositoryConfig<K, V> config;
    private final String tableName;
    private final String historyTableName;

    private static final Set<String> DEFAULT_FIELDS = new HashSet<>(Arrays.asList("doc_id", "revision", "update_field", "updater"));

    public PostgresVersionedRepository(LoggerFactory loggerFactory,
                                       TimeStamper timeStamper,
                                       RepositoryConfig<K, V> config,
                                       String tableName,
                                       String historyTableName) {
        super(loggerFactory, timeStamper, config);
        this.logger = LoggerFactory.getLogger(PostgresVersionedRepository.class);
        this.config = config;
        this.tableName = tableName;
        this.historyTableName = historyTableName;

        createTableIfNotExists(tableName);
        createTableIfNotExists(historyTableName);
    }

    private void createTableIfNotExists(String tableName) {
        // Implement table creation if not exists for Postgres
    }

    @Override
    protected void doPut(K key, V value) {
        try (Connection connection = getConnection()) {
            LocalDateTime timestamp = getTimestamp(value);

            // Check if the record already exists
            String selectQuery = String.format("SELECT * FROM %s WHERE doc_id = ?", tableName);
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setObject(1, key);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        // Record doesn't exist, insert a new one
                        String insertQuery = String.format("INSERT INTO %s (doc_id, revision, start_date, data) VALUES (?, ?, ?, ?)", tableName);
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setObject(1, key);
                            insertStatement.setInt(2, 0);
                            insertStatement.setObject(3, timestamp);
                            insertStatement.setObject(4, convertToMap(value));
                            insertStatement.executeUpdate();
                        }
                    } else {
                        // Record exists, update if necessary
                        Map<String, Object> currentValueMap = (Map<String, Object>) resultSet.getObject("data");
                        if (!areRecordsEqual(currentValueMap, value.getFields())) {
                            // Update the current record
                            int currentRevision = resultSet.getInt("revision");
                            int newRevision = currentRevision + 1;
                            String updateQuery = String.format("UPDATE %s SET revision = ?, start_date = ?, data = ? WHERE doc_id = ?", tableName);
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                updateStatement.setInt(1, newRevision);
                                updateStatement.setObject(2, timestamp);
                                updateStatement.setObject(3, convertToMap(value));
                                updateStatement.setObject(4, key);
                                updateStatement.executeUpdate();
                            }

                            // Create and insert the historical record
                            String insertHistoryQuery = String.format("INSERT INTO %s (doc_id, revision, start_date, close_date, data) VALUES (?, ?, ?, ?, ?)", historyTableName);
                            try (PreparedStatement insertHistoryStatement = connection.prepareStatement(insertHistoryQuery)) {
                                insertHistoryStatement.setObject(1, key);
                                insertHistoryStatement.setInt(2, currentRevision);
                                insertHistoryStatement.setObject(3, resultSet.getObject("start_date"));
                                insertHistoryStatement.setObject(4, timestamp);
                                insertHistoryStatement.setObject(5, currentValueMap);
                                insertHistoryStatement.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing PUT operation for key: {}", key, e);
        }
    }

    @Override
    protected void doPutAll(Map<K, V> values) {
        try (Connection connection = getConnection()) {
            LocalDateTime timestamp = getCurrentTimestamp();

            for (Map.Entry<K, V> entry : values.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();

                String selectQuery = String.format("SELECT * FROM %s WHERE doc_id = ?", tableName);
                try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                    selectStatement.setObject(1, key);
                    try (ResultSet resultSet = selectStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            // Record doesn't exist, insert a new one
                            String insertQuery = String.format("INSERT INTO %s (doc_id, revision, start_date, data) VALUES (?, ?, ?, ?)", tableName);
                            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                                insertStatement.setObject(1, key);
                                insertStatement.setInt(2, 0);
                                insertStatement.setObject(3, timestamp);
                                insertStatement.setObject(4, convertToMap(value));
                                insertStatement.executeUpdate();
                            }
                        } else {
                            // Record exists, update if necessary
                            Map<String, Object> currentValueMap = (Map<String, Object>) resultSet.getObject("data");
                            if (!areRecordsEqual(currentValueMap, value.getFields())) {
                                // Update the current record
                                int currentRevision = resultSet.getInt("revision");
                                int newRevision = currentRevision + 1;
                                String updateQuery = String.format("UPDATE %s SET revision = ?, start_date = ?, data = ? WHERE doc_id = ?", tableName);
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setInt(1, newRevision);
                                    updateStatement.setObject(2, timestamp);
                                    updateStatement.setObject(3, convertToMap(value));
                                    updateStatement.setObject(4, key);
                                    updateStatement.executeUpdate();
                                }

                                // Create and insert the historical record
                                String insertHistoryQuery = String.format("INSERT INTO %s (doc_id, revision, start_date, close_date, data) VALUES (?, ?, ?, ?, ?)", historyTableName);
                                try (PreparedStatement insertHistoryStatement = connection.prepareStatement(insertHistoryQuery)) {
                                    insertHistoryStatement.setObject(1, key);
                                    insertHistoryStatement.setInt(2, currentRevision);
                                    insertHistoryStatement.setObject(3, resultSet.getObject("start_date"));
                                    insertHistoryStatement.setObject(4, timestamp);
                                    insertHistoryStatement.setObject(5, currentValueMap);
                                    insertHistoryStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing PUTALL operation", e);
        }
    }

    // Other methods...

    private Map<String, Object> convertToMap(V value) {
        // Convert the entity object to a map of column-value pairs
        // This depends on the structure of your entity and table schema
        Map<String, Object> map = new HashMap<>();
        // Populate the map with values from the entity
        return map;
    }

    private boolean areRecordsEqual(Map<String, Object> oldValue, Map<String, Object> newValue) {
        // Implement
