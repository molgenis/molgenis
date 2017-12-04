MOLGENIS Supported RSQL operators
---------------------------------

| Operator | Example | Description |
|----------|---------|-------------|
| `==`     | `/api/v2/myTable?q=columnA==queryValue` | Performs an **equals** query. Returns all rows from *myTable* where values in *columnA* exactly equal *queryValue* |  
| `=q=`    | `/api/v2/myTable?q=columnA=q=queryValue` | Performs a **search** query. Returns all rows from *myTable* where values in *columnA* contain *queryValue* |
| `=like=` | `/api/v2/myTable?q=columnA=like=queryValue` | Performs a **like** query. Returns all rows from *myTable* where values in *columnA* are like *queryValue* | 
| `=in=`   | `/api/v2/myTable?q=columnA=in=(valueA, valueB)` | Performs an **in** query. Returns all rows from *myTable* where *columnA* contains *valueA* OR *valueB* |
| `!=`     | `/api/v2/myTable?q=columnA!=queryValue` | Performs a **not equals** query. Returns all rows from *myTable* where values in *columnA* do not equal *queryValue* |
| `=notlike=` | `/api/v2/myTable?q=columnA=notlike=queryValue` | Performs a **not like** query. Returns all rows from  *myTable* where values in *columnA* are not like *queryValue* |
| `<` & `=lt=` | `/api/v2/myTable?q=columnA<queryValue`, `/api/v2/myTable?q=columnA=lt=queryValue` | Performs a **lesser than** query. Returns all rows from *myTable* where values in *columnA* are lesser than *queryValue* |
| `=le=` & `<=` | `/api/v2/myTable?q=columnA<=queryValue`, `/api/v2/myTable?q=columnA=le=queryValue` | Performs a **lesser than or equal to** query. Returns all rows from *myTable* where values in *columnA* are lesser than or equal to *queryValue* | 
| `<` & `=gt=` | `/api/v2/myTable?q=columnA>queryValue`, `/api/v2/myTable?q=columnA=gt=queryValue` | Performs a **greater than** query. Returns all rows from *myTable* where values in *columnA* are greater than *queryValue* |
| `>=` & `=ge=` | `/api/v2/myTable?q=columnA>=queryValue`, `/api/v2/myTable?q=columnA=ge=queryValue` | Performs a **equal to or greater than** query. Returns all rows from *myTable* where values in *columnA* are equal to or greater than *queryValue* |
| `=rng=`  | `/api/v2/myTable?q=columnA=rng=(fromValue,toValue)` | Performs a **from to** query. Returns all rows from *myTable* where values in *columnA* are equal or greater than the *fromValue*, and lesser than or equal to the *toValue* |
| `=should=` | N/A | Not supported
| `=dismax=` | N/A | Not supported
| `=fuzzy=` | N/A | Not supported