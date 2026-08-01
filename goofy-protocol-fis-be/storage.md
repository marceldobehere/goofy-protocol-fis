# Storage Details
Here are the Details of the Storage System, including Buckets and Tables.

## User Buckets
(TODO)





## User Tables
(TODO)

### Table Structure
(TODO)

#### Table Datatypes
These are the following supported datatypes for table columns:
* `FIXED_STRING_N` - Fixed length string, where N is the length of the string
* `VAR_STRING_N` - Variable length string, where N is the maximum length of the string
* `BOOLEAN` - Boolean value (true/false)
* `TINYINT` - 8-bit signed integer
* `SMALLINT` - 16-bit signed integer
* `INT` - 32-bit signed integer
* `BIGINT` - 64-bit signed integer
* `FLOAT` - 32-bit floating point number (approx. ±3.40282347E+38F)
* `DOUBLE` - 64-bit floating point number (approx. ±1.79769313486231570E+308)
* `DATE` - Date value (YYYY-MM-DD)
* `TIME` - Time value (HH:MM:SS)


### Table Access
(TODO)

### Table Permissions
(TODO)

### Table Creation
(TODO)
Important: Supported Datatypes, limits for columns, column names, primary key, foreign keys? (on update/delete?) (interop with different tables?), custom field indexing?

### Table Deletion
(TODO)

### Table Size


### Table Query
To query a table, users send a structured JSON payload matching the supported DTOs. The query is limited to a subset of SQL (selection + filtering + pagination + sorting, plus aggregate-like expressions supported by the condition tree

* Data Selection using the `TableSelectDto`. Contains Selected Columns and Basic Query
* Insert using a raw JSON Object with the Column Names as Keys and the Values as Values
* Bulk insert using the `TableMultiRowInsertDto`. Contains Columns and a List of Values
* Update using the `TableUpdateDto`. Contains Columns, Update Values and Basic Query
* Delete using the `BasicQueryDto`.

You can find the DTOs [here](src/main/java/com/masl/goofy_protocol_fis_be/dto/request/query).

The DTOs are designed to be as simple as possible, while still being able to express the needed queries.
In general the queries are limited to a subset of SQL, which should be enough for most use cases.

#### Basic Query DTO
This DTO supports:
* filtering via` where` (including nested boolean logic)
* sorting via `sortByCols` (with per-column direction in `sortOrders`)
* pagination via `limit` and `offset`

```json
{
  "where": {...},
  "sortByCols": [],
  "sortOrders": [],
  "limit": 0,
  "offset": 0
}
```
All the fields are optional

#### Where Condition Part
This represents either:
* A boolean expression node (L_AND, L_OR, L_NOT)
* A comparison node (C_EQ, C_NEQ, C_GT, C_GE, C_LT, C_LE)
* Simple value/column references (VAL, COL)
* Additional expression nodes (M_ADD, M_SUB, M_MUL, M_DIV, M_MOD, M_FLOOR, M_CEIL, M_ABS, COALESCE, LIKE)

This is the mechanism for nested conditions (AND/OR/NOT, and also expression composition like math/comparisons).
```json
{
  "type": "...",
  "conditionParts": [...],
}
OR
{
  "type": "...",
  "colName": "...",
}
OR
{
  "type": "...",
  "value": "...",
  "valueType": "..."
}
```

#### Result DTO
The result of a query is returned as a JSON object with the following structure:
```json
{
  "colNames": ["col1", "col2", ...],
  "colTypes": ["type1", "type2", ...],
  "rows": [
    [val1, val2, ...],
    [val1, val2, ...],
    ...
  ],
  "resultTruncated": false
}
```

Note: The `resultTruncated` field indicates whether the result was truncated due to a limit (either user-defined or by the quota).


#### Examples
Insert a row into a table
`INSERT INTO users (id, name, age) VALUES (10, 'Ada', 18);`
```json
{
  "id": 10,
  "name": "Ada",
  "age": 18
}
```

Insert multiple rows into a table. Is not limited to 1000 entries!
`INSERT INTO users (id, name, age) VALUES (10, 'Ada', 18), (11, 'Bob', 21);`
```json
{
  "colNames": ["id", "name", "age"],
  "rows": [
    [10, "Ada", 18],
    [11, "Bob", 21]
  ]
}
```

Select rows from a table
`SELECT id, name WHERE age >= 18 ORDER BY name ASC LIMIT 50`
```json
{
  "colNames": ["id", "name"],
  "basicQuery": {
    "where": {
      "type": "C_GE",
      "conditionParts": [
        { "type": "COL", "colName": "age" },
        { "type": "VAL", "value": 18, "valueType": "INT" }
      ]
    },
    "sortByCols": ["name"],
    "sortOrders": ["ASC"],
    "limit": 50
  }
}
```

Update rows in a table
`UPDATE users SET status = 'active' WHERE name = 'Bob';`
```json
{
  "colNames": ["status"],
  "colValues": ["active"],
  "basicQuery": {
    "where": {
      "type": "C_EQ",
      "conditionParts": [
        { "type": "COL", "colName": "name" },
        { "type": "VAL", "value": "Bob", "valueType": "VAR_STRING_N" }
      ]
    }
  }
}
```
Note, the `VAR_STRING_N` can also be `FIXED_STRING_N`, as it doesn't matter inside of queries.

Delete rows from a table
`DELETE FROM users WHERE age < 18;`
```json
{
  "basicQuery": {
    "where": {
      "type": "C_LT",
      "conditionParts": [
        { "type": "COL", "colName": "age" },
        { "type": "VAL", "value": 18, "valueType": "INT" }
      ]
    }
  }
}
```

Select rows from a table (complicated)
```SQL
SELECT id, name
FROM users
WHERE (age >= 18 AND status LIKE 'active%')
  AND COALESCE(last_login, DATE '1970-01-01') > DATE '2020-01-01';
```

```json
{
  "colNames": ["id", "name"],
  "basicQuery": {
    "where": {
      "type": "L_AND",
      "conditionParts": [
        {
          "type": "C_GE",
          "conditionParts": [
            { "type": "COL", "colName": "age" },
            { "type": "VAL", "value": 18, "valueType": "INT" }
          ]
        },
        {
          "type": "LIKE",
          "conditionParts": [
            { "type": "COL", "colName": "status" },
            { "type": "VAL", "value": "active%", "valueType": "FIXED_STRING_N" }
          ]
        },
        {
          "type": "C_GT",
          "conditionParts": [
            {
              "type": "COALESCE",
              "conditionParts": [
                { "type": "COL", "colName": "last_login" },
                { "type": "VAL", "value": "1970-01-01", "valueType": "DATE" }
              ]
            },
            { "type": "VAL", "value": "2020-01-01", "valueType": "DATE" }
          ]
        }
      ]
    }
  }
}
```

