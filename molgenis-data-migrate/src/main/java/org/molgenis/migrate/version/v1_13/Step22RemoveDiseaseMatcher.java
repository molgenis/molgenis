package org.molgenis.migrate.version.v1_13;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step22RemoveDiseaseMatcher extends MolgenisUpgrade
{
	private final JdbcTemplate template;
	private final Logger LOG = LoggerFactory.getLogger(Step22RemoveDiseaseMatcher.class);

	@Autowired
	public Step22RemoveDiseaseMatcher(DataSource dataSource)
	{
		super(21, 22);
		requireNonNull(dataSource);
		this.template = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Removing Disease Matcher entities");
		template.execute("DROP TABLE IF EXISTS Disease;");
		template.execute("DROP TABLE IF EXISTS DiseaseMapping;");
		template.execute(
				"DELETE FROM attributes WHERE identifier IN (SELECT attributes FROM entities_attributes WHERE fullName = 'Disease' OR fullName = 'DiseaseMapping')");
		template.execute("DELETE FROM entities WHERE fullName = 'Disease' OR fullName = 'DiseaseMapping'");

		LOG.info("Removing Disease Matcher settings");
		template.execute("ALTER TABLE settings_dataexplorer DROP COLUMN mod_diseasematcher");
		template.execute("DELETE FROM attributes WHERE name = 'mod_diseasematcher'");

		LOG.info("Removing Disease Matcher permissions");
		template.execute(
				"DELETE FROM UserAuthority WHERE role ='ROLE_ENTITY_READ_DISEASE' OR role = 'ROLE_ENTITY_COUNT_DISEASE' OR role = 'ROLE_ENTITY_WRITE_DISEASE';");
		template.execute(
				"DELETE FROM UserAuthority WHERE role ='ROLE_ENTITY_READ_DISEASEMAPPING' OR role = 'ROLE_ENTITY_COUNT_DISEASEMAPPING' OR role = 'ROLE_ENTITY_WRITE_DISEASEMAPPING';");
		template.execute(
				"DELETE FROM GroupAuthority WHERE role ='ROLE_ENTITY_READ_DISEASE' OR role = 'ROLE_ENTITY_COUNT_DISEASE' OR role = 'ROLE_ENTITY_WRITE_DISEASE';");
		template.execute(
				"DELETE FROM GroupAuthority WHERE role ='ROLE_ENTITY_READ_DISEASEMAPPING' OR role = 'ROLE_ENTITY_COUNT_DISEASEMAPPING' OR role = 'ROLE_ENTITY_WRITE_DISEASEMAPPING';");
	}
}
