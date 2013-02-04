/**
 * Framework for searching and
 * storing persistent data {@link org.molgenis.util.Entity} objects.
 * <p>
 * {@link org.molgenis.framework.db.Database} is the "layer supertype" that
 * provides easy methods to search and store data. Searches can be filtered
 * using a {@link org.molgenis.framework.db.QueryRule}. For advanced filtering
 * complex combinations of {@link org.molgenis.framework.db.QueryRule} can be
 * more easily created using {@link org.molgenis.framework.db.QueryImp}.
 * Specific measures have been taken to work with larger datasets, necessery for
 * data-intensive domains like Bioinformatics. Therefore, all data manipulation
 * methods also work on "lists" of data in order for data to be updated "in
 * batch".
 * <p>
 * Data itself is modeled as {@link org.molgenis.util.Entity} to ensure
 * data is clearly structured. This eases the programming of tools working on
 * this data. Entities can be though of as a "data row" in a data table, what
 * programmers often refer to as BEAN or POJO. Cross-references (xref) can be
 * made by explicitly referencing another Entity, e.g. using a foreign key.
 * Aggregates of Entities are explicitly unsupported to keep it clear which data
 * is loaded.
 * <p>
 * In many cases this structure is not provided by external sources. Therefore,
 * a "unstructured" data type is also provided in
 * {@link org.molgenis.util.Tuple}. Such
 * {@link org.molgenis.util.Tuple} can be easily translated in
 * {@link org.molgenis.util.Entity}, given that the columnNames of the
 * {@link org.molgenis.util.Tuple} are changed to match those in
 * {@link org.molgenis.util.Entity}.
 */
package org.molgenis.framework.db;