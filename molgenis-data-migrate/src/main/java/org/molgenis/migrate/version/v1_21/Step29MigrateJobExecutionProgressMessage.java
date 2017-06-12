package org.molgenis.migrate.version.v1_21;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static java.util.Objects.requireNonNull;

/**
 * <ul>
 * <li>Changes JobExecution PROGRESS_MESSAGE attribute datatype from STRING to TEXT.</li>
 * </ul>
 */
public class Step29MigrateJobExecutionProgressMessage extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step29MigrateJobExecutionProgressMessage.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public Step29MigrateJobExecutionProgressMessage(DataSource dataSource)
	{
		super(28, 29);
		requireNonNull(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Upgrade...");
		updateDataType("JobExecution", "progressMessage", "text");
		LOG.info("Done.");
	}

	private void updateDataType(String entityFullName, String attributeName, String newDataType)
	{
		LOG.info("Update data type of {}.{} to {}...", entityFullName, attributeName, newDataType);
		String attributeId = jdbcTemplate.queryForObject(
				"SELECT a.identifier " + "FROM entities_attributes ea " + "JOIN attributes a "
						+ "ON ea.attributes = a.identifier " + "WHERE ea.fullName = '" + entityFullName + "' "
						+ "AND a.name='" + attributeName + "'", String.class);
		jdbcTemplate.update(
				"UPDATE attributes SET dataType = '" + newDataType + "' WHERE identifier = '" + attributeId + "'");
	}
}
