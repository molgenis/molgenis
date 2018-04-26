# Localization
Using localization you can specify how Molgenis should look for users in different countries.

## Languages
Molgenis comes with a fixed set of languages. You'll find them in the
`Language` entity. Each language is identified by its ISO 639-1 Code.

The following languages are available:

|code | language      |
|-----|---------------|
| en  | English       |
| pt  | Portugese     |
| es  | Spanish       |
| de  | German        |
| it  | Italian       |
| fr  | French        |
| nl  | Dutch         |
| xx  | "My Language" |

> The `xx` language is made available to give you some flexibility in adding your own specific language.

By default, only the `en` language is active.
You can activate the other languages as needed by editing their row in the `Language` entity in the Data Explorer.

In the Application Settings you can specify the application-wide default language, used for anonymous users and for
users who have not yet chosen a language.

If more than one language is available, users can select their language
in the menu at the top of the screen.

## UI Messages
In the `Localization` entity in the Data Explorer you can provide translations for UI messages.
The translations are split into namespaces.
For instance the values in the `form` namespace allow you to localize how the forms look.
Changes you make will be visible in the UI once you reload the page.

## EMX
In your EMX files, you can provide translations for your data and metadata in
columns postfixed with `-` and the language code.

### Metadata
The `attributes` and `entities` sheets in your EMX files have a `label`
and a `description` column. When you upload entities in EMX, you can specify
translations for these labels and descriptions in attributes 

E.g. to provide Dutch labels and descriptions, you should specify them in the columns 
`label-nl` and `description-nl`.

### Data
If an entity you create has label attributes that you'd like to localize for the user,
you should provide your translations in attributes postfixed with the language code.

### Example

Suppose you have a `City` entity that has a `name` label attribute that you'd like to translate
into Dutch.
In the attributes sheet of your EMX, specify two rows for both the `name` and the `name-nl` attributes.

| name | entity | nillable | labelAttribute | label | label-nl | description | description-nl |
|------|--------|----------|----------------|-------|----------|-------------|----------------|
| name    | City   | FALSE	  | TRUE           | Name       | Naam             | City name       | Naam van de stad in het Engels |
| name-nl | City   |          | TRUE		   | Dutch name | Nederlandse naam | Dutch city name | Naam van de stad               |


In the data sheet for your City entity, add columns for both `name` and `name-nl`.

| id        | name      | name-nl  |
|-----------|-----------|----------|
| new_york  | New York  | New York |
| brussels  | Brussels  | Brussel  |
| paris     | Paris     | Parijs   |
| london    | London    | Londen   |
| the_hague | The Hague	| Den Haag |

Check out the [Sample EMX file](../data/l10n.xlsx).
