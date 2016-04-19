package org.molgenis.migrate.version.v1_19;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.migrate.version.v1_16.Step26migrateJpaBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Migrates SORTA to version 1.19 for M4094.
 * 
 * <ul>
 * <li>Changes OntologyTerm ONTOLOGY_TERM_NAME attribute datatype from STRING to TEXT.</li>
 * <li>Changes OntologyTermNodePath ONTOLOGY_TERM_NODE_PATH attribute datatype from STRING to TEXT.</li>
 * <li>Changes OntologyTermSynonym ONTOLOGY_TERM_SYNONYM attribute datatype from STRING to TEXT.</li>
 * <li>Drops the MatchingTaskContent repository, it becomes abstract.</li>
 * </ul>
 * 
 * @author fkelpin
 */
public class Step28MigrateSorta extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step26migrateJpaBackend.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public Step28MigrateSorta(DataSource dataSource)
	{
		super(27, 28);
		requireNonNull(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Upgrade...");
		updateDataType("Ontology_OntologyTerm", "ontologyTermName", "text");
		updateDataType("Ontology_OntologyTermNodePath", "nodePath", "text");
		updateDataType("Ontology_OntologyTermSynonym", "ontologyTermSynonym", "text");
		removeRepository("MatchingTaskContent");
		removeRepository("MatchingTask");
		LOG.info("Done.");
	}

	private void removeRepository(String entityFullName)
	{
		LOG.info("Removing {}...", entityFullName);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + entityFullName);
		jdbcTemplate.execute("DELETE FROM entities WHERE fullName = '" + entityFullName + "'");
		jdbcTemplate.execute(
				"DELETE FROM attributes WHERE identifier IN (SELECT attributes FROM entities_attributes WHERE fullName = '"
						+ entityFullName + "')");
		String upperCaseName = entityFullName.toUpperCase();
		jdbcTemplate.execute("DELETE FROM UserAuthority WHERE role ='ROLE_ENTITY_READ_" + upperCaseName
				+ "' OR role = 'ROLE_ENTITY_COUNT_" + upperCaseName + "' OR role = 'ROLE_ENTITY_WRITE_" + upperCaseName
				+ "'");
		jdbcTemplate.execute("DELETE FROM GroupAuthority WHERE role ='ROLE_ENTITY_READ_" + upperCaseName
				+ "' OR role = 'ROLE_ENTITY_COUNT_" + upperCaseName + "' OR role = 'ROLE_ENTITY_WRITE_" + upperCaseName
				+ "'");
	}

	private void updateDataType(String entityFullName, String attributeName, String newDataType)
	{
		LOG.info("Update data type of {}.{} to {}...", entityFullName, attributeName, newDataType);
		String attributeId = jdbcTemplate.queryForObject("SELECT a.identifier " + "FROM entities_attributes ea "
				+ "JOIN attributes a " + "ON ea.attributes = a.identifier " + "WHERE ea.fullName = '" + entityFullName
				+ "' " + "AND a.name='" + attributeName + "'", String.class);
		jdbcTemplate.update(
				"UPDATE attributes SET dataType = '" + newDataType + "' WHERE identifier = '" + attributeId + "'");
	}
}
