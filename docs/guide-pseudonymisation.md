# Pseudonymisation

It's possible to define your own format for automatically generated identifiers that increment sequentially.
These can be used as pseudonyms for your rows. On this page we will use the following sequence, but you can
configure the prefix and number of digits to your liking.

```
GEN-0000001
GEN-0000002
GEN-0000003
etc.
```

Besides incrementing sequentially, the digit part of th ids can also be scrambled:

```
GEN-5720385
GEN-1398822
GEN-9401776
etc.
```
These are not random: the generated ids are guaranteed to be unique and are based on the incrementing sequence.

> Note: Be aware that the scrambled identifiers _will_ repeat when all possibilities are exhausted. So
> make sure that the length of the digit-part is sufficient for your use case!

## How to configure
To configure an attribute as an incrementing identifier, first make sure the attribute has the following properties:
```
idAttribute: AUTO
dataType: string
```

To begin, you should create two tags. You can add them to the `tags` sheet of your EMX file or just create
them in the Data Explorer. In a tag with relation IRI [obo:IAO_0000596](http://www.ontobee.org/ontology/IAO?iri=http://purl.obolibrary.org/obo/IAO_0000596) 
you specify the number of digits. In the other tag with relation IRI [obo:IAO_0000599](http://www.ontobee.org/ontology/IAO?iri=http://purl.obolibrary.org/obo/IAO_0000599) 
you specify the ID prefix. The actual values you want to use should go in the `values` attribute. 
Here are the tags for the sequence we're looking at:

| identifier       | relationLabel   | relationIRI                                | value |
|------------------|-----------------|--------------------------------------------|-------|
| hasIDDigitCount7 | hasIDDigitCount | http://purl.obolibrary.org/obo/IAO_0000596 | 7     |
| hasIDPrefixGen   | hasIDPrefix     | http://purl.obolibrary.org/obo/IAO_0000599 | GEN-  |

Now it's time to add the tags to your id-attribute. You can do this in the Data Explorer (by opening 
the Attribute table and editing your attribute), or you can make it part of the model in
your EMX file:

| name      | dataType | nillable | idAttribute | tags                            |
|-----------|----------|----------|-------------|---------------------------------|
| pseudonym | string   | false    | AUTO        | hasIDDigitCount7,hasIDPrefixGen |

If you want the identifiers to be scrambled, you should also add the `scrambled` tag to the attribute:

| name      | dataType | nillable | idAttribute | tags                                      |
|-----------|----------|----------|-------------|-------------------------------------------|
| pseudonym | string   | false    | AUTO        | hasIDDigitCount7,hasIDPrefixGen,scrambled |

## Endpoints
To keep track of a sequence's current value, it is stored in the database. To interact
with a sequence there are two endpoints available:

#### `GET <host>/plugin/metadata-manager/sequences`
Returns the sequences present in the database.

> Note: Keep in mind that the sequence identifiers contain number signs (#). These need to be URL 
> escaped with `%23` in the upcoming endpoints.

#### `GET <host>/plugin/metadata-manager/sequences/<sequence_id>`
Returns the current value of the sequence.

#### `DElETE <host>/plugin/metadata-manager/sequences/<sequence_id>`
Deletes a sequence in the database. Since sequences are not automatically deleted when an entity type is deleted,
you can use this endpoint to clean up. This endpoint can also be used to "reset" a sequence: a new sequence
starting at 1 will be created as soon as a new row is added.

#### `POST <host>/plugin/metadata-manager/sequences/<sequence_id>?value=123`
Sets a sequence to a specific value. 

## Migration
A sequence's current value is not carried over when copying, exporting, migrating or re-importing a
dataset. When you import/migrate a dataset that had sequences in it, do the following:

1. Make sure everything is configured correctly (see [How to configure](#how-to-configure))
2. Use the `GET /sequences` endpoint to retrieve the sequences in the database and find the one you need
3. Look at your dataset's last rows and figure out what the value for the sequence should be
4. Use the `POST /sequences` endpoint to set the sequence's value

