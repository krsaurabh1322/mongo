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
}

////////////


import com.scb.cat.common.cache.store.CacheStore;
import com.scb.cat.common.jdbc.ResultSetHelper;
import com.scb.cat.common.logging.LoggerFactory;
import com.scb.cat.common.model.Entity;
import com.scb.cat.common.repository.RepositoryConfig;
import com.scb.cat.common.repository.model.Field;
import com.scb.cat.common.repository.model.FieldType;
import com.scb.cat.common.repository.model.ModelSpec;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class VersionedJdbcCacheStore<K, V extends Entity<K>> implements CacheStore<K, V> {

    private final Logger logger;
    private final ModelSpec modelSpec;
    private final RepositoryConfig<K, V> repositoryConfig;
    private final DataSource dataSource;
    private final StatementGenerator select;
    private final StatementGenerator delete;
    private final StatementGenerator upsert;
    private final StatementGenerator historyInsert;

    public VersionedJdbcCacheStore(final LoggerFactory loggerFactory,
                                   final RepositoryConfig<K, V> repositoryConfig,
                                   final DataSource dataSource) {
        this.logger = loggerFactory.create(VersionedJdbcCacheStore.class);
        this.repositoryConfig = repositoryConfig;
        this.modelSpec = repositoryConfig.modelSpec();
        this.dataSource = dataSource;
        select = new StatementGenerator(select());
        delete = new StatementGenerator(delete());
        upsert = new StatementGenerator(upsert());
        historyInsert = new StatementGenerator(historyInsert());
    }

    // ... Other methods ...

    private String historyInsert() {
        final StringBuilder sb = new StringBuilder()
                .append(format(INSERT_FORMAT, modelSpec.getHistoryTableName()))
                .append(modelSpec.getFields().stream()
                        .map(Field::getDbName)
                        .collect(joining(",", "(", ")")))
                .append(format(VALUES_FORMAT, modelSpec.getFields().stream()
                        .map(f -> format(REPLACEMENT_FORMAT, f.getName()))
                        .collect(joining(", ", "(", ")"))));
        return sb.toString();
    }

    private void storeHistoryVersion(final K key, final V value, final LocalDateTime timestamp) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement historyInsertStatement = historyInsert.create(connection)) {
            historyInsertStatement.setObject(1, key);
            historyInsertStatement.setObject(2, value.getVersion());
            for (int i = 0; i < modelSpec.getFields().size(); i++) {
                final Field field = modelSpec.getFields().get(i);
                final Object fieldValue = value.getFields().get(field.getName());
                final int statementIndex = i + 3;
                if (fieldValue == null) {
                    historyInsertStatement.setNull(statementIndex, field.getType().getSqlType());
                } else {
                    historyInsertStatement.setObject(statementIndex, fieldValue);
                }
            }
            historyInsertStatement.setObject(modelSpec.getFields().size() + 3, timestamp);
            historyInsertStatement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Failed to insert historical version for key: " + key, e);
        }
    }

    @Override
    public void store(final K key, final V value) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement upsertStatement = upsert.create(connection, value)) {
            // execute the upsert statement and commit if successful
            upsertStatement.executeUpdate();
            logger.debug("Stored: [key={}]", key);

            // Store the historical version
            LocalDateTime timestamp = LocalDateTime.now(); // Replace with your timestamp generation logic
            storeHistoryVersion(key, value, timestamp);
        } catch (SQLException e) {
            logger.warn("Failed to store entity [key={}]", key, e);
        }
    }

    // ... Other methods ...

    private final class StatementGenerator {
        // ... Existing code ...

        PreparedStatement create(final Connection connection, final V entity, final int version) throws SQLException {
            return create(connection, entity.getFields(), version);
        }

        // ... Existing code ...
    }

    // ... Existing code ...
}
///////////////