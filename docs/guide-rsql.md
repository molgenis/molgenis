# RSQL operators

## Background
The MOLGENIS REST query API makes use of RSQL. RSQL is a query language for parametrized filtering of entries in RESTful APIs. It’s based on FIQL (Feed Item Query Language), that was originally specified by Mark Nottingham as a language for querying Atom feeds. However the simplicity of RSQL and its capability to express complex queries in a compact and HTTP URI-friendly way makes it a good candidate for becoming a generic query language for searching REST endpoints.

## Supported RSQL operators

MOLGENIS supports the following operators:

---------------------------------

| Operator | Example | Description |
|----------|---------|-------------|
| `==`     | `columnA==queryValue` | Performs an **equals** query. Returns all rows from *myTable* where values in *columnA* exactly equal *queryValue* |  
| `=q=`    | `columnA=q='search string'` | Performs a **search** query. Returns all rows from *myTable* where values in *columnA* match `search string` |
| `=like=` | `columnA=like=queryValue` | Performs a **like** query. Returns all rows from *myTable* where values in *columnA* contain *queryValue* |
| `=in=`   | `columnA=in=(valueA, valueB)` | Performs an **in** query. Returns all rows from *myTable* where *columnA* contains *valueA* OR *valueB* |
| `!=`     | `columnA!=queryValue` | Performs a **not equals** query. Returns all rows from *myTable* where values in *columnA* do not equal *queryValue* |
| `=notlike=` | `columnA=notlike=queryValue` | Performs a **not like** query. Returns all rows from  *myTable* where values in *columnA* are not like *queryValue* |
| `<` & `=lt=` | `columnA<queryValue`, `columnA=lt=queryValue` | Performs a **lesser than** query. Returns all rows from *myTable* where values in *columnA* are lesser than *queryValue* |
| `=le=` & `<=` | `columnA<=queryValue`, `columnA=le=queryValue` | Performs a **lesser than or equal to** query. Returns all rows from *myTable* where values in *columnA* are lesser than or equal to *queryValue* |
| `<` & `=gt=` | `columnA>queryValue`, `columnA=gt=queryValue` | Performs a **greater than** query. Returns all rows from *myTable* where values in *columnA* are greater than *queryValue* |
| `>=` & `=ge=` | `columnA>=queryValue`, `columnA=ge=queryValue` | Performs a **equal to or greater than** query. Returns all rows from *myTable* where values in *columnA* are equal to or greater than *queryValue* |
| `=rng=`  | `columnA=rng=(fromValue,toValue)` | Performs a **from to** query. Returns all rows from *myTable* where values in *columnA* are equal or greater than the *fromValue*, and lesser than or equal to the *toValue* |
| `=should=` | N/A | Not supported
| `=dismax=` | N/A | Not supported
| `=fuzzy=` | N/A | Not supported

## Api calls
The query can be entered as value for the `q` query param in an API call, e.g.
`/api/data/myTable?q=columnA==queryValue`
Use escaping where needed, i.e. both on the rsql level
and using url encoding.

# Indexing depth
You can also query fields of referenced entities, using a dot to separate
the attribute names: `columnA.columnB==queryValue`.
The maximum allowed depth of this search can be set in the "indexing depth"
attribute of the entity type, in the Data Explorer.

# Search (`=q=`)
Search is used to search text values for a query string.
Search is case-insensitive. Search uses a tokenizer algorithm to
split the text block and the search string into tokens and then
looks for matching token sequences.

## Search across all fields (`*=q=`)
You can perform a search across all fields of a document by
specifying `*` as column name: `*=q='search string'`.

## Token tag
Attributes of type ENUM, STRING, HYPERLINK, EMAIL are often not
really text blocks that should be split into pieces 
but rather *single* keyword tokens.
If you tag your attribute with the `token` tag, a search on that
attribute becomes a case-insensitive equals.
If you also tag your attribute with the `case-sensitive` tag,
the search becomes an equals.
The search across all fields is not affected by the token tag on the
attribute.

# Like (`=like=`)
Like is used to find substrings in a text value.
Like is case-insensitive.
If you tag your attribute with the `case-sensitive` tag, like 
becomes case-sensitive.

# Tagging attributes
You can tag an attribute in EMX by adding the tags to the `tags` column
of your attributes sheet, e.g.

| name  | dataType | enumOptions | tags  | ... |
|-------|----------|-------------|-------|-----|
| id    | string   |             | token |
| label | string   |             |       |
| type  | enum     | AA,Aa,BB,Bb   | token, case-sensitive |

The `token` and `case-sensitive` tags are created for you.