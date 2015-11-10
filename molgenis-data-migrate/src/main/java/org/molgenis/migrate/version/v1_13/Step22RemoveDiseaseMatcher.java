package org.molgenis.migrate.version.v1_13;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step22RemoveDiseaseMatcher extends MolgenisUpgrade
{
	private final JdbcTemplate template;

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
		// remove disease matcher entities + attributes
		template.execute("DROP TABLE IF EXISTS Disease;");
		template.execute("DROP TABLE IF EXISTS DiseaseMapping;");
		template.execute(
				"DELETE FROM attributes WHERE identifier IN (SELECT attributes FROM entities_attributes WHERE fullName = 'Disease' OR fullName = 'DiseaseMapping')");
		template.execute("DELETE FROM entities WHERE fullName = 'Disease' OR fullName = 'DiseaseMapping'");

		// remove settings
		template.execute("ALTER TABLE settings_dataexplorer DROP COLUMN mod_diseasematcher");
		template.execute("DELETE FROM attributes WHERE name = 'mod_diseasematcher'");
	}
}
