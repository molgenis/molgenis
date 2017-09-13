package org.molgenis.migrate.version.v1_22;

import org.molgenis.data.populate.IdGenerator;
import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static java.util.Objects.requireNonNull;

public class Step31UpdateApplicationSettings extends MolgenisUpgrade
{
	private final Logger LOG = LoggerFactory.getLogger(Step31UpdateApplicationSettings.class);

	private final JdbcTemplate jdbcTemplate;
	private final IdGenerator idGenerator;

	public Step31UpdateApplicationSettings(DataSource dataSource, IdGenerator idGenerator)
	{
		super(30, 31);
		this.jdbcTemplate = new JdbcTemplate(requireNonNull(dataSource));
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating application settings ...");

		// add custom javascript headers setting
		String customJavascriptHeadersId = idGenerator.generateId();
		boolean googleSignInIdDefaultValue = true;
		jdbcTemplate.update(
				"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`visible`,`label`,`description`,`isAggregatable`,`enumOptions`,`rangeMin`,`rangeMax`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`,`defaultValue`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				customJavascriptHeadersId, "custom_javascript", "text", null, null, true, false, true,
				"Custom javascript headers",
				"Custom javascript headers, specified as comma separated list. These headers will be included in the molgenis header before the applications own javascript headers.",
				false, null, null, null, false, false, null, null, "");

		jdbcTemplate.update("INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 4,
				"settings_app", customJavascriptHeadersId);

		// update existing settings table
		jdbcTemplate.execute("ALTER TABLE settings_app ADD COLUMN `custom_javascript` text");

		LOG.debug("Updated application settings");
	}
}
