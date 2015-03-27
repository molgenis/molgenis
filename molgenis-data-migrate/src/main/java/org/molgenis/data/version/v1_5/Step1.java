package org.molgenis.data.version.v1_5;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sql.DataSource;

import org.molgenis.data.version.MetaDataUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

/**
 * Upgrades the metadata repositories in MySQL.
 */
public class Step1 extends MetaDataUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step1.class);
	private JdbcTemplate jdbcTemplate;

	public Step1(DataSource dataSource)
	{
		super(0, 1);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		updateMetaDataDatabaseTables();
	}

	private void updateMetaDataDatabaseTables()
	{
		InputStream in = getClass().getResourceAsStream("/2582.sql");
		String script;
		try
		{
			script = FileCopyUtils.copyToString(new InputStreamReader(in));
		}
		catch (IOException e)
		{
			LOG.error("Failed to read upgrade script", e);
			throw new RuntimeException(e);
		}
		for (String statement : script.split(";"))
		{
			String trimmed = statement.trim();
			try
			{
				LOG.info(trimmed);
				jdbcTemplate.execute(trimmed);
			}
			catch (DataAccessException e)
			{
				LOG.error(e.getMessage());
			}

		}
	}

}
