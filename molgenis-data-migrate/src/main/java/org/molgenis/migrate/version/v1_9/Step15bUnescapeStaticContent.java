package org.molgenis.migrate.version.v1_9;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Migration step for https://github.com/molgenis/molgenis/issues/3463
 */
public class Step15bUnescapeStaticContent
{
	private static final Logger LOG = LoggerFactory.getLogger(Step15bUnescapeStaticContent.class);

	private final DataSource dataSource;

	public Step15bUnescapeStaticContent(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	public void upgrade()
	{
		LOG.info("Updating metadata from version 15 to 15b");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		String prefix = "app.";
		List<String> pluginIds = Arrays.asList("background", "contact", "feedback", "home", "news", "references");
		for (String pluginId : pluginIds)
		{
			String name = prefix + pluginId;
			List<String> values = jdbcTemplate.queryForList("SELECT Value FROM RuntimeProperty WHERE name = ?",
					String.class, name);
			if (values != null && values.size() == 1) // Row might not exist
			{
				String value = values.get(0);
				if (!value.equals("null")) // RuntimeProperty.Value can't be null, the string "null" is used instead
				{
					LOG.info("Migrating RuntimeProperty [" + name + "]");
					String newValue = StringEscapeUtils.unescapeXml(value);
					jdbcTemplate.update("UPDATE RuntimeProperty SET Value = ? WHERE Name = ?", newValue, name);
				}
			}
		}
		LOG.info("Migrated static content values");
	}
}
