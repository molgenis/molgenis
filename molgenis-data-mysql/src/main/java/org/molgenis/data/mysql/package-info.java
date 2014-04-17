/**
 * Created by mswertz on 07/04/14.
 */
package org.molgenis.data.mysql;

/**
 * Work in progress. Open issues:
 * <ul>
 * <li>SQL injection in queries (updates are prepared statements)
 * <li>DONE DataType.COMPOUND
 * <li>PARTLY Convertor OMX to OMX4 (or what we want to call this???)
 * <li>PARTLY unique constraints</li>
 * <li>DONE Inheritance / Abstract
 * <li>Observablefeature.Unit -> create dedicated lookup table?
 * <li>Parsing of Characteristic subclasses/figuring out what subclass meant (by looking at the data?)
 * <li>Protocol.active
 * <li>Data format manual + parser + validator
 * <li>Transactions -> accross repositories???
 * <li>Categorical data -> solve via XREFs?
 * <li>informational error messages</li>
 * <li>What if same entity is compound twice (e.g. Address.street is then in 2x) -> suggest: add path as identifier for
 * attributes?
 * <li>
 * </ul>
 */
