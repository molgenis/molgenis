package org.molgenis.data.version.v1_8;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step11dEntitiesAttributesConstraints
{
	private static final Logger LOG = LoggerFactory.getLogger(Step11cAttributeMappingAddSourceAttributeMetaDatas.class);

	private final DataSource dataSource;

	public Step11dEntitiesAttributesConstraints(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	public void upgrade()
	{
		LOG.info("Updating metadata from version 11.2 to 11.3 ...");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		// see https://github.com/molgenis/molgenis/issues/3869
		jdbcTemplate.execute(
				"ALTER TABLE entities_attributes ADD FOREIGN KEY (`fullName`) REFERENCES `entities` (`fullName`) ON DELETE CASCADE;");
		jdbcTemplate.execute(
				"ALTER TABLE entities_attributes ADD FOREIGN KEY (`attributes`) REFERENCES `attributes` (`identifier`) ON DELETE CASCADE;");

		LOG.info("Added DELETE CASCADE foreign key constraints to entities_attributes");
	}
}
