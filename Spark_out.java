noLet's enhance the classes to support various types of joins in the Spark DataFrame caching mechanism. 
We'll start with the `SparkJobRequest` class, and then proceed to update other classes accordingly.

1. Update `SparkJobRequest` class to include join information of all types of joins:
```java
public class SparkJobRequest {
    // Existing members

    // New members for join support
    private List<JoinConfig> joinConfigs;

    // Constructor, getters, setters, and other methods

    public List<JoinConfig> getJoinConfigs() {
        return joinConfigs;
    }

    public void setJoinConfigs(List<JoinConfig> joinConfigs) {
        this.joinConfigs = joinConfigs;
    }
}
```
The `JoinConfig` class will be used to store information related to each join operation, including the type of join and join columns. 
We will define an enum `JoinType` to represent different types of joins.

2. Define the `JoinConfig` class and the `JoinType` enum:
```java
public class JoinConfig {
    private String leftDataset;
    private String rightDataset;
    private List<String> joinColumns;
    private JoinType joinType;

    // Constructor, getters, setters, and other methods
}

public enum JoinType {
    INNER, LEFT, RIGHT, FULL
}
```

3. Update `SparkMongoDataSource` class to support joins:

```java
public class SparkMongoDataSource {
    // Existing methods

    public Dataset<Row> loadDataset(SparkJobRequest request, String collectionName) {
        // Existing implementation to load a dataset from MongoDB

        // New logic to handle joins
        if (request.getJoinConfigs() != null && !request.getJoinConfigs().isEmpty()) {
            Dataset<Row> mainDataset = dataset; // Your main dataset loaded from MongoDB

            for (JoinConfig joinConfig : request.getJoinConfigs()) {
                Dataset<Row> rightDataset = // Load the right dataset from MongoDB based on joinConfig
                mainDataset = joinDatasets(mainDataset, rightDataset, joinConfig);
            }

            return mainDataset;
        }

        return dataset;
    }

    // Additional methods to support join-related operations
    private Dataset<Row> joinDatasets(Dataset<Row> leftDataset, Dataset<Row> rightDataset, JoinConfig joinConfig) {
        List<String> joinColumns = joinConfig.getJoinColumns();
        JoinType joinType = joinConfig.getJoinType();

        switch (joinType) {
            case INNER:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "inner");
            case LEFT:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "left_outer");
            case RIGHT:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "right_outer");
            case FULL:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "outer");
            default:
                throw new IllegalArgumentException("Unsupported join type: " + joinType);
        }
    }
}
```

The `SparkMongoDataSource` class now handles different types of join operations based on the information provided in the `SparkJobRequest`.

4. Update `SparkDataFrameCacheManager` class to support joins:

```java
public class SparkDataFrameCacheManager {
    // Existing methods

    public Dataset<Row> getCachedDFtoProcessRequest(final SparkJobRequest request) {
        // Existing implementation to get the cached dataset

        // New logic to handle joins
        if (request.getJoinConfigs() != null && !request.getJoinConfigs().isEmpty()) {
            Dataset<Row> mainDataset = cachedDataset; // Your cached main dataset

            for (JoinConfig joinConfig : request.getJoinConfigs()) {
                Dataset<Row> rightDataset = // Load the right dataset from cache based on joinConfig
                mainDataset = joinDatasets(mainDataset, rightDataset, joinConfig);
            }

            return mainDataset;
        }

        return cachedDataset;
    }

    // Additional methods to support join-related operations
    private Dataset<Row> joinDatasets(Dataset<Row> leftDataset, Dataset<Row> rightDataset, JoinConfig joinConfig) {
        List<String> joinColumns = joinConfig.getJoinColumns();
        JoinType joinType = joinConfig.getJoinType();

        switch (joinType) {
            case INNER:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "inner");
            case LEFT:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "left_outer");
            case RIGHT:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "right_outer");
            case FULL:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "outer");
            default:
                throw new IllegalArgumentException("Unsupported join type: " + joinType);
        }
    }
}
```

The `SparkDataFrameCacheManager` class now handles different types of join operations on cached datasets based on the information provided in the `SparkJobRequest`.

5. Update `MultiDimensionalAggFunction` class to support joins:

```java
public class MultiDimensionalAggFunction {
    // Existing methods

    public Dataset<Row> performMultiDimensionalAggregation(List<Dataset<Row>> datasets, List<JoinConfig> joinConfigs, SparkJobRequest request) {
        // Perform join operations on the datasets
        for (JoinConfig joinConfig : joinConfigs) {
            Dataset<Row> rightDataset = // Load the right dataset from the list of datasets based on joinConfig
            Dataset<Row> leftDataset = datasets.get(0);
            datasets.set(0, joinDatasets(leftDataset, rightDataset, joinConfig));
        }

        // Perform multi-dimensional aggregation on the joined dataset
        Dataset<Row> aggregatedDataset = aggregate(request, datasets.get(0), sparkRespBuilder);

        return aggregatedDataset;
    }

    // Additional methods to support join-related operations
    private Dataset<Row> joinDatasets(Dataset<Row> leftDataset, Dataset<Row> rightDataset, JoinConfig joinConfig) {
        List<String> joinColumns = joinConfig.getJoinColumns();
        JoinType joinType = joinConfig.getJoinType();

        switch (joinType) {
            case INNER:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "inner");
            case LEFT:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "left_outer");
            case RIGHT:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "right_outer");
            case FULL:
                return leftDataset.join(rightDataset, JavaConverters.asScalaIteratorConverter(joinColumns.iterator()).asScala().toSeq(), "outer");
            default:
                throw new IllegalArgumentException("Unsupported join type: " + joinType);
        }
    }
}
```

The `MultiDimensionalAgg

Function` class now performs different types of join operations on the datasets before multi-dimensional aggregation 
  based on the information provided in the `SparkJobRequest`.

With these updates, your existing classes should now fully support various types of joins in the Spark DataFrame caching mechanism. 
  Please note that this is a basic implementation, and you might need to adapt it further based on your specific use case and data structures.



///////////////////////////////////////////

Sure! To load the right dataset from MongoDB based on the `JoinConfig`, we'll add a method to the `SparkMongoDataSource` class. 
  We'll assume that the `SparkMongoDataSource` class already has a method to load datasets from MongoDB, and we'll extend it to
  support loading datasets based on the `JoinConfig`. Here's the updated implementation:

```java
public class SparkMongoDataSource {
    // Existing methods

    // New method to load the right dataset based on JoinConfig
    public Dataset<Row> loadRightDatasetForJoin(JoinConfig joinConfig) {
        String collectionName = joinConfig.getRightDataset(); // Assuming the collection name is provided in the JoinConfig
        Map<String, String> joinColumnsMapping = getJoinColumnsMapping(joinConfig); // Get the mapping of join columns
        Map<String, String> renameColumnsMapping = getRenameColumnsMapping(joinConfig); // Get the mapping of column renaming
        
        // Load the right dataset from MongoDB
        Dataset<Row> rightDataset = // Implement your logic here to load the dataset

        // Rename columns if needed
        if (renameColumnsMapping != null && !renameColumnsMapping.isEmpty()) {
            for (Map.Entry<String, String> entry : renameColumnsMapping.entrySet()) {
                rightDataset = rightDataset.withColumnRenamed(entry.getKey(), entry.getValue());
            }
        }

        // Select the join columns
        if (joinColumnsMapping != null && !joinColumnsMapping.isEmpty()) {
            List<Column> joinColumns = new ArrayList<>();
            for (Map.Entry<String, String> entry : joinColumnsMapping.entrySet()) {
                joinColumns.add(rightDataset.col(entry.getKey()).alias(entry.getValue()));
            }
            rightDataset = rightDataset.select(joinColumns.toArray(new Column[0]));
        }

        return rightDataset;
    }

    // Helper methods to get join columns mapping and column renaming mapping
    private Map<String, String> getJoinColumnsMapping(JoinConfig joinConfig) {
        // Implement your logic here to extract join columns mapping from JoinConfig
        // This method should return a Map where the key is the original column name from the right dataset
        // and the value is the corresponding column name from the left dataset to join on.
    }

    private Map<String, String> getRenameColumnsMapping(JoinConfig joinConfig) {
        // Implement your logic here to extract column renaming mapping from JoinConfig
        // This method should return a Map where the key is the original column name from the right dataset
        // and the value is the corresponding column name in the joined dataset after renaming.
    }
}
```

With this implementation, you can now load the right dataset from MongoDB based on the `JoinConfig` provided. 
  The `loadRightDatasetForJoin` method takes care of handling join columns mapping and column renaming if needed. Remember to 
  implement the `getJoinColumnsMapping` and `getRenameColumnsMapping` methods to extract the necessary information from the 
  `JoinConfig` as per your specific data structure.  

///////////////////////////////////////////

Sure, I can merge the implementation into the existing methods in the `SparkMongoDataSource` class. Here's the updated implementation:

```java
public class SparkMongoDataSource {
    // Existing methods

    // Load method with join support
    public Dataset<Row> load(final SparkJobRequest request, final List<Document> filter, JoinConfig joinConfig) {
        Dataset<Row> leftDataset = LoadWithSchema(); // Assuming LoadWithSchema() loads the left dataset
        
        if (joinConfig != null) {
            Dataset<Row> rightDataset = loadRightDatasetForJoin(joinConfig);
            String[] joinColumns = getJoinColumns(joinConfig);
            String joinType = getJoinType(joinConfig);

            // Perform the join operation
            leftDataset = leftDataset.join(rightDataset, joinColumns, joinType);
        }

        // Apply any filter conditions if provided
        if (filter != null && !filter.isEmpty()) {
            leftDataset = leftDataset.filter(functions.expr(parseMongoFilter(filter)));
        }

        return leftDataset;
    }

    // New method to load the right dataset based on JoinConfig
    private Dataset<Row> loadRightDatasetForJoin(JoinConfig joinConfig) {
        // Implementation from the previous response
        // ...
    }

    // Helper methods to extract join columns and join type from JoinConfig
    private String[] getJoinColumns(JoinConfig joinConfig) {
        // Implement your logic here to extract the join columns from JoinConfig
    }

    private String getJoinType(JoinConfig joinConfig) {
        // Implement your logic here to extract the join type from JoinConfig
    }
}
```

With this updated implementation, the `load` method in `SparkMongoDataSource` now supports loading the left dataset, 
  performing a join with the right dataset based on the `JoinConfig`, and applying filter conditions if provided. 
  The join type and join columns are extracted from the `JoinConfig` using the `getJoinType` and `getJoinColumns` methods, respectively.

Please note that you need to implement the `getJoinColumns` and `getJoinType` methods to extract the join-related information 
  from the `JoinConfig` as per your specific data structure. Additionally, the `parseMongoFilter` method in the `filter` part of the 
  `load` method should be implemented to convert the MongoDB filter list into a string expression that Spark can apply to the dataset. 
  The details of this implementation will depend on your specific filter 
  format and how you want to interpret and translate the MongoDB filter into a Spark SQL expression. 
///////////////////////////////////////////////////////////


 Sure, let's go through an end-to-end example of how you can perform a join between an Inquiry dataset and a Counterparty dataset using the caching implementation with all the classes we have discussed so far.

Assuming you have the following classes and methods:

1. `SparkJobRequest`: With support for join information.
2. `SparkMongoDataSource`: With the `load` method that supports joining datasets based on `JoinConfig`.
3. `SparkDataFrameCacheManager`: With methods to cache and retrieve datasets.
4. `MultiDimensionalAggFunction`: With support for aggregating the joined dataset.
5. `JoinConfig`: A class to hold join-related configuration.

Here's a step-by-step guide on how to achieve the join:

1. Define the JoinConfig class to hold the join-related information:

```java
public class JoinConfig {
    private String leftDatasetName;
    private String rightDatasetName;
    private String joinType;
    private String[] joinColumns;

    // Constructors, getters, and setters
    // ...
}
```

2. Define the end-to-end test method that performs the join:

```java
public class EndToEndTest {
    public static void main(String[] args) {
        // Initialize the SparkSession
        SparkSession sparkSession = SparkSession.builder()
            .appName("End-to-End Test")
            .master("local[*]")
            .getOrCreate();

        // Create instances of SparkJobRequest, SparkMongoDataSource, and SparkDataFrameCacheManager
        SparkJobRequest sparkJobRequest = new SparkJobRequest();
        SparkMongoDataSource sparkMongoDataSource = new SparkMongoDataSource(sparkSession);
        SparkDataFrameCacheManager cacheManager = new SparkDataFrameCacheManager();

        // Define the join configuration
        JoinConfig joinConfig = new JoinConfig();
        joinConfig.setLeftDatasetName("Inquiry");
        joinConfig.setRightDatasetName("Counterparty");
        joinConfig.setJoinType("inner");
        joinConfig.setJoinColumns(new String[]{"counterpartyId"});

        // Load the left dataset (Inquiry)
        List<Document> inquiryFilter = new ArrayList<>();
        Dataset<Row> inquiryDataset = sparkMongoDataSource.load(sparkJobRequest, inquiryFilter);

        // Load the right dataset (Counterparty)
        List<Document> counterpartyFilter = new ArrayList<>();
        Dataset<Row> counterpartyDataset = sparkMongoDataSource.load(sparkJobRequest, counterpartyFilter);

        // Perform the join
        Dataset<Row> joinedDataset = cacheManager.joinDatasets(sparkJobRequest, inquiryDataset, counterpartyDataset, joinConfig);

        // Perform multi-dimensional aggregation on the joined dataset
        MultiDimensionalAggFunction multiDimensionalAggFunction = new MultiDimensionalAggFunction();
        Dataset<Row> aggregatedDataset = multiDimensionalAggFunction.aggregate(sparkJobRequest, joinedDataset);

        // Show the final aggregated dataset
        aggregatedDataset.show();

        // Stop the SparkSession
        sparkSession.stop();
    }
}
```

3. Ensure that the `SparkMongoDataSource` class has the `load` method with join support:

```java
public class SparkMongoDataSource {
    // Existing methods

    // Load method with join support
    public Dataset<Row> load(final SparkJobRequest request, final List<Document> filter, JoinConfig joinConfig) {
        // Implementation as shown in the previous responses
        // ...
    }
}
```

4. Ensure that the `SparkDataFrameCacheManager` class has the `joinDatasets` method:

```java
public class SparkDataFrameCacheManager {
    // Existing methods

    public Dataset<Row> joinDatasets(SparkJobRequest request, Dataset<Row> leftDataset, Dataset<Row> rightDataset, JoinConfig joinConfig) {
        // Perform the join operation based on the joinConfig
        return leftDataset.join(rightDataset, joinConfig.getJoinColumns(), joinConfig.getJoinType());
    }
}
```

With this end-to-end example, you can load the Inquiry and Counterparty datasets from MongoDB, perform the join based on the provided 
  JoinConfig, and then perform multi-dimensional aggregation on the joined dataset using your existing classes. 
  The `EndToEndTest` class showcases how you can integrate all the components to achieve the desired functionality. 
///////////////////////////////////////////////////////////

Apologies for the confusion. Here's the implementation of the `SparkORCDataSource` class similar to `SparkMongoDataSource`, with support for loading data from ORC files and performing joins:

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.SaveMode;

public class SparkORCDataSource implements SparkDataSource<String> {
    private final SparkSession sparkSession;

    public SparkORCDataSource(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    @Override
    public Dataset<Row> load(SparkJobRequest request, String path) {
        return sparkSession.read().format("orc").load(path);
    }

    @Override
    public Dataset<Row> load(SparkJobRequest request, String path, List<Document> filter) {
        Dataset<Row> dataset = load(request, path);
        // Apply filters here based on the provided filter list
        // ...

        return dataset;
    }

    @Override
    public Dataset<Row> loadWithSchema(SparkJobRequest request, String path, StructType schema) {
        return sparkSession.read().format("orc").schema(schema).load(path);
    }

    @Override
    public Dataset<Row> loadWithSchema(SparkJobRequest request, String path, List<Document> filter, StructType schema) {
        Dataset<Row> dataset = loadWithSchema(request, path, schema);
        // Apply filters here based on the provided filter list
        // ...

        return dataset;
    }

    public Dataset<Row> join(SparkJobRequest request, String leftPath, String rightPath, JoinConfig joinConfig) {
        Dataset<Row> leftDataset = load(request, leftPath);
        Dataset<Row> rightDataset = load(request, rightPath);

        // Perform the join operation based on the joinConfig
        return leftDataset.join(rightDataset, joinConfig.getJoinColumns(), joinConfig.getJoinType());
    }

    @Override
    public void save(SparkJobRequest request, String path, Dataset<Row> data) {
        data.write().format("orc").mode(SaveMode.Overwrite).save(path);
    }
}
```

The `SparkORCDataSource` class implements the `SparkDataSource<String>` interface and provides methods to load data from ORC files, apply filters, and perform joins. 
  The `join` method is similar to the one in `SparkMongoDataSource`, but the loading of data and filters are adapted to handle ORC file formats.

You can use the `SparkORCDataSource` class to load and join datasets stored in ORC files with the provided `JoinConfig`. 
  Additionally, you can save the resulting dataset to a specified ORC file path using the `save` method.  

///////////////////////////////////////////////////////////

Sure, let's create the `SparkTableLoader` class and provide an end-to-end test example using two ORC tables: `Inquiry` and `Counterparty`. 
  The join condition will be `Inquiry.counterpartyId = Counterparty.counterpartyId`. We will also demonstrate filtering and multidimensional 
  aggregation on the cached DataFrame.

Here's the implementation of the `SparkTableLoader` class:

```java
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import static org.apache.spark.sql.functions.*;

public class SparkTableLoader {

    private SparkSession sparkSession;
    private Dataset<Row> cachedDataFrame;

    public SparkTableLoader() {
        SparkConf conf = new SparkConf().setAppName("Spark Table Loader").setMaster("local[*]");
        sparkSession = SparkSession.builder().config(conf).getOrCreate();
    }

    public Dataset<Row> loadORCTable(String tablePath) {
        return sparkSession.read().format("orc").load(tablePath);
    }

    public Dataset<Row> performJoin(Dataset<Row> left, Dataset<Row> right, String joinColumn) {
        return left.join(right, joinColumn);
    }

    public void cacheDataFrame(Dataset<Row> dataFrame) {
        dataFrame.createOrReplaceTempView("__cached__");
        cachedDataFrame = dataFrame;
    }

    public void showDataFrame(Dataset<Row> dataFrame) {
        dataFrame.show();
    }

    public Dataset<Row> runSQLQuery(String sqlQuery) {
        return sparkSession.sql(sqlQuery);
    }

    public void saveDataFrameToORC(Dataset<Row> dataFrame, String outputPath) {
        dataFrame.write().format("orc").save(outputPath);
    }

    public Dataset<Row> filterDataFrame(Dataset<Row> dataFrame, String filterColumn, String filterValue) {
        return dataFrame.filter(col(filterColumn).equalTo(filterValue));
    }

    public Dataset<Row> aggregateDataFrame(Dataset<Row> dataFrame, String groupByColumn, String aggColumn, String aggFunction) {
        String alias = aggFunction + "_" + aggColumn;
        return dataFrame.groupBy(col(groupByColumn))
                .agg(function(aggFunction, col(aggColumn)).as(alias));
    }

    public Dataset<Row> applyFiltersAndAggregations(Dataset<Row> dataFrame, String filterColumn, String filterValue,
                                                    String groupByColumn, String aggColumn, String aggFunction) {
        Dataset<Row> filteredDataFrame = filterDataFrame(dataFrame, filterColumn, filterValue);
        return aggregateDataFrame(filteredDataFrame, groupByColumn, aggColumn, aggFunction);
    }
    
    // Add other necessary methods and operations as needed.
}
```

Now let's create a test example that loads the `Inquiry` and `Counterparty` ORC tables, performs a join, applies filtering, and performs 
  multidimensional aggregation:

```java
public class SparkTableLoaderTest {

    public static void main(String[] args) {
        SparkTableLoader tableLoader = new SparkTableLoader();

        // Load the Inquiry and Counterparty ORC tables
        Dataset<Row> inquiryTable = tableLoader.loadORCTable("path_to_inquiry_table_orc");
        Dataset<Row> counterpartyTable = tableLoader.loadORCTable("path_to_counterparty_table_orc");

        // Perform the join
        String joinColumn = "counterpartyId";
        Dataset<Row> joinedTable = tableLoader.performJoin(inquiryTable, counterpartyTable, joinColumn);

        // Cache the joined DataFrame
        tableLoader.cacheDataFrame(joinedTable);

        // Show the cached DataFrame
        System.out.println("Cached DataFrame:");
        tableLoader.showDataFrame(tableLoader.cachedDataFrame);

        // Example: Apply filters and aggregate the cached DataFrame
        String filterColumn = "status";
        String filterValue = "Open";
        String groupByColumn = "product";
        String aggColumn = "amount";
        String aggFunction = "sum";
        Dataset<Row> result = tableLoader.applyFiltersAndAggregations(
            tableLoader.cachedDataFrame, filterColumn, filterValue, groupByColumn, aggColumn, aggFunction);

        // Show the aggregated DataFrame
        System.out.println("Aggregated DataFrame:");
        tableLoader.showDataFrame(result);

        // Save the aggregated DataFrame to ORC format
        tableLoader.saveDataFrameToORC(result, "output_path_orc");
    }
}
```

In this example, make sure to replace `"path_to_inquiry_table_orc"` and `"path_to_counterparty_table_orc"` with the actual file paths of the Inquiry and 
  Counterparty ORC tables, respectively. Also, ensure that the join column name `"counterpartyId"` and other columns used for filtering and aggregation 
  are present in the respective tables. The `output_path_orc` should be replaced with the desired output directory where the aggregated DataFrame will be 
  saved in ORC format.  



public class JoinConfig {
    public enum JoinType {
        INNER, LEFT, RIGHT, FULL
    }

    private String leftDataset;
    private String rightDataset;
    private JoinType joinType;
    private List<JoinGroup> joinGroups;
    private String filter;
    private String filterRight;
    private List<String> selectColumns;
    private List<String> groupByColumns;
    private String havingClause;
    private Map<String, String> aggregateFunctions;

    // Constructors, getters, setters, etc.
}

public class JoinGroup {
    public enum LogicalOperator {
        AND, OR
    }

    private LogicalOperator logicalOperator;
    private List<JoinOn> joinOns;

    public JoinGroup(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
        this.joinOns = new ArrayList<>();
    }

    public void addJoinOn(JoinOn joinOn) {
        joinOns.add(joinOn);
    }

    public List<JoinOn> getJoinOns() {
        return joinOns;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }
}



private Dataset<Row> performJoin(Dataset<Row> baseDataset, List<JoinConfig> joinConfigs) {
    Dataset<Row> finalDataset = baseDataset;

    for (JoinConfig joinConfig : joinConfigs) {
        String leftAlias = getAliasForDataset(joinConfig.getLeftDataset());
        String rightAlias = getAliasForDataset(joinConfig.getRightDataset());

        List<String> leftColumns = new ArrayList<>();
        List<String> rightColumns = new ArrayList<>();
        List<String> joinExpressions = new ArrayList<>();

        for (JoinGroup joinGroup : joinConfig.getJoinGroups()) {
            List<String> groupConditions = new ArrayList<>();

            for (JoinOn joinOn : joinGroup.getJoinOns()) {
                String leftCol = getColumnWithAliases(joinOn.getLeftColumn(), leftAlias);
                String rightCol = getColumnWithAliases(joinOn.getRightColumn(), rightAlias);
                String operator = joinOn.getOperator();

                groupConditions.add(String.format("(%s %s %s)", leftCol, operator, rightCol));
            }

            String groupCondition = String.join(" " + joinGroup.getLogicalOperator() + " ", groupConditions);
            joinExpressions.add("(" + groupCondition + ")");
        }

        String joinExpression = String.join(" AND ", joinExpressions);

        Dataset<Row> rightDataset = loadRightDatasetForJoin(joinConfig.getRightDataset());
        Dataset<Row> joinedDataset = finalDataset.join(rightDataset, functions.expr(joinExpression), joinConfig.getJoinType().toString());

        if (joinConfig.getSelectColumns() != null && !joinConfig.getSelectColumns().isEmpty()) {
            List<String> selectCols = new ArrayList<>();
            for (String col : joinConfig.getSelectColumns()) {
                String aliasCol = getColumnWithAliases(col, rightAlias);
                selectCols.add(aliasCol);
            }
            joinedDataset = joinedDataset.selectExpr(selectCols.toArray(new String[0]));
        }

        if (joinConfig.getFilter() != null) {
            String filterExpression = applyAliasToFilter(joinConfig.getFilter(), rightAlias);
            joinedDataset = joinedDataset.filter(filterExpression);
        }

        if (joinConfig.getGroupByColumns() != null && !joinConfig.getGroupByColumns().isEmpty()) {
            List<String> groupByCols = new ArrayList<>();
            for (String col : joinConfig.getGroupByColumns()) {
                String aliasCol = getColumnWithAliases(col, rightAlias);
                groupByCols.add(aliasCol);
            }
            joinedDataset = joinedDataset.groupBy(groupByCols.stream().toArray(String[]::new)).agg(aggregateFunctions(joinConfig.getAggregateFunctions()));
        }

        if (joinConfig.getHavingClause() != null) {
            String havingExpression = applyAliasToFilter(joinConfig.getHavingClause(), rightAlias);
            joinedDataset = joinedDataset.having(havingExpression);
        }

        if (finalDataset != baseDataset) {
            finalDataset.unpersist();
        }

        finalDataset = joinedDataset.cache();
    }

    return finalDataset;
}
  


public class SparkMongoDataSourceTest {
    // ... Other test methods ...

    @Test
    public void testPerformJoinWithDifferentJoinConfigs() {
        // Create a list of JoinGroups for a JoinConfig
        List<JoinGroup> joinGroups = new ArrayList<>();
        
        // Create JoinOns for the first JoinGroup
        List<JoinOn> joinOns1 = new ArrayList<>();
        joinOns1.add(JoinOn.builder()
                .leftColumn("t1.a1")
                .rightColumn("t2.b1")
                .operator("=")
                .build());
        joinOns1.add(JoinOn.builder()
                .leftColumn("t1.a2")
                .rightColumn("t2.b2")
                .operator(">")
                .build());
        JoinGroup joinGroup1 = JoinGroup.builder()
                .joinOns(joinOns1)
                .logicalOperator("AND")
                .build();
        
        // Create JoinOns for the second JoinGroup
        List<JoinOn> joinOns2 = new ArrayList<>();
        joinOns2.add(JoinOn.builder()
                .leftColumn("t1.c1")
                .rightColumn("t2.d1")
                .operator("=")
                .build());
        JoinGroup joinGroup2 = JoinGroup.builder()
                .joinOns(joinOns2)
                .logicalOperator("AND")
                .build();
        
        // Create the JoinConfig
        JoinConfig joinConfig = JoinConfig.builder()
                .leftDataset("table t1")
                .rightDataset("table t2")
                .joinType(JoinType.INNER)
                .joinGroups(joinGroups)
                .filter("t1.someColumn = 123")
                .groupByColumns(Arrays.asList("t1.groupColumn"))
                .selectColumns(Arrays.asList("t1.a1", "t1.c1", "t2.d1"))
                .aggregateFunctions(new HashMap<>())
                .build();
        
        // Print the string representation of the query
        String queryString = JoinConfigTranslator.buildQuery(joinConfig);
        System.out.println(queryString);
        
        // Perform necessary assertions
        // ...
    }
    
    // ... Other test methods ...
}



private String getAliasForDataset(String datasetName) {
    return datasetName.toLowerCase(); // You can adjust the alias generation logic as needed
}

private String getColumnWithAliases(String datasetAlias, String column) {
    return datasetAlias + "." + column; // Assumes columns in the dataset have unique names
}

private String applyAliasToFilter(String filter, String datasetAlias) {
    return filter.replaceAll("([\\w.]+)(\\s*(=|>|<|>=|<=|!=)\\s*([\\w']+))", datasetAlias + ".$1$2$3$4");
}



private String getColumnWithAliases(String columnName, Dataset<Row> dataset) {
    if (columnName.contains(".")) {
        String datasetAlias = getAliasForDataset(columnName);
        String colWithoutAlias = columnName.substring(columnName.indexOf(".") + 1);
        return datasetAlias + "." + colWithoutAlias;
    } else {
        return dataset.col(columnName).expr().toString();
    }
}


private String getAliasForDataset(String datasetName) {
    if (datasetName.contains(".")) {
        return datasetName.substring(0, datasetName.indexOf(".")).toLowerCase();
    }
    return datasetName.toLowerCase();
}


private String applyAliasToFilter(String filter, String leftDatasetName, String rightDatasetName) {
    if (filter == null || filter.isEmpty()) {
        return filter;
    }

    String[] filterParts = filter.split(" ");
    StringBuilder modifiedFilter = new StringBuilder();

    for (String part : filterParts) {
        if (part.contains(".")) {
            String[] columnParts = part.split("\\.");
            if (columnParts.length == 2) {
                String datasetAlias = columnParts[0].toLowerCase();
                String columnName = columnParts[1];
                if (datasetAlias.equals(leftDatasetName)) {
                    modifiedFilter.append(getColumnWithAliases(columnName, leftDatasetName)).append(" ");
                } else if (datasetAlias.equals(rightDatasetName)) {
                    modifiedFilter.append(getColumnWithAliases(columnName, rightDatasetName)).append(" ");
                } else {
                    modifiedFilter.append(part).append(" ");
                }
            } else {
                modifiedFilter.append(part).append(" ");
            }
        } else {
            modifiedFilter.append(part).append(" ");
        }
    }

    return modifiedFilter.toString().trim();
}


// Load the right dataset and alias all columns
private Dataset<Row> loadRightDatasetForJoin(SparkSession spark, SparkJobRequest request, JoinConfig joinConfig) {
    // Load the right dataset
    Dataset<Row> rightDataset = loadDatasetFromMongo(spark, joinConfig.getRightDataset());

    // Alias all columns in the right dataset
    rightDataset = aliasColumns(rightDataset, rightDataset.columns(), rightDataset.alias(rightDatasetAlias));

    return rightDataset;
}

// Apply joins, filters, group by, etc., and alias columns in result
private Dataset<Row> performJoin(List<JoinConfig> joinConfigs, Dataset<Row> baseDataset) {
    Dataset<Row> result = baseDataset;

    for (JoinConfig joinConfig : joinConfigs) {
        // Load the right dataset and apply alias
        Dataset<Row> rightDataset = loadRightDatasetForJoin(spark, request, joinConfig);

        // Apply joins, filters, group by, etc.
        result = result.join(rightDataset, buildJoinCondition(joinConfig), joinConfig.getJoinType().toString())
                       .where(applyFilter(joinConfig.getFilter()))
                       .groupBy(applyGroupBy(joinConfig.getGroupByColumns()))
                       .agg(applyAggregations(joinConfig.getAggregateFunctions()));

        // Alias columns in the result
        result = aliasColumns(result, result.columns(), result.alias(resultAlias));
    }

    return result;
}

// Alias columns in a dataset
private Dataset<Row> aliasColumns(Dataset<Row> dataset, String[] columns, String alias) {
    for (String column : columns) {
        dataset = dataset.withColumnRenamed(column, getColumnWithAliases(column, alias));
    }
    return dataset;
}
