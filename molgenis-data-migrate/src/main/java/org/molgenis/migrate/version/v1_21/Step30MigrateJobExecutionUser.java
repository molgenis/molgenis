package org.molgenis.migrate.version.v1_21;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * <ul>
 * <li>Changes JobExecution user columns from foreign key to MolgenisUser to a simple userName.</li>
 * </ul>
 */
public class Step30MigrateJobExecutionUser extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step30MigrateJobExecutionUser.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public Step30MigrateJobExecutionUser(DataSource dataSource)
	{
		super(29, 30);
		requireNonNull(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		if (checkPreviouslyUpgraded())
		{
			LOG.info(
					"Skipping the upgrading of JobExecution entities because it has already been done in a previous version.");
		}
		else
		{
			LOG.info("Upgrade user attribute in JobExecution entities...");
			updateDataType("JobExecution", "user", "string");

			getJobExecutionEntityNames().forEach(this::dropForeignKey);

			LOG.info("Done.");
		}
	}

	private List<String> getJobExecutionEntityNames()
	{
		return jdbcTemplate.query("SELECT fullName FROM entities WHERE extends = 'JobExecution'",
				(rs, rowNum) -> rs.getString("fullName"));
	}

	/**
	 * Checks if this migration step needs to be executed for the current version. There is the possibility a previous
	 * version has already done this upgrade because this migration step and its corresponding fix might be added to
	 * older versions.
	 */
	private boolean checkPreviouslyUpgraded()
	{
		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM entities_attributes ea JOIN attributes a ON ea.attributes = a.identifier WHERE ea.fullName = 'JobExecution' AND a.name = 'user' AND dataType = 'xref'",
				Integer.class);
		return (count == 0);
	}

	private void updateDataType(String entityFullName, String attributeName, String newDataType)
	{
		LOG.info("Update data type of {}.{} to {}...", entityFullName, attributeName, newDataType);
		String attributeId = jdbcTemplate.queryForObject(
				"SELECT a.identifier FROM entities_attributes ea JOIN attributes a ON ea.attributes = a.identifier WHERE ea.fullName = '"
						+ entityFullName + "' AND a.name='" + attributeName + "'", String.class);
		jdbcTemplate.update(
				"UPDATE attributes SET dataType = '" + newDataType + "', refEntity = NULL WHERE identifier = '"
						+ attributeId + "'");
	}

	private void dropForeignKey(String entityFullName)
	{
		LOG.info("Drop foreign key and index from {} to MolgenisUser...", entityFullName);
		jdbcTemplate.update("ALTER TABLE `" + entityFullName + "` DROP FOREIGN KEY `" + entityFullName + "_ibfk_1`");
		jdbcTemplate.update("DROP INDEX user ON `" + entityFullName + "`");
		jdbcTemplate.update("UPDATE `" + entityFullName
				+ "` SET user = (SELECT userName from MolgenisUser WHERE MolgenisUser.ID = `" + entityFullName
				+ "`.user)");
	}
}
