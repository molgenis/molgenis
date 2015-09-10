package org.molgenis.data.version.v1_9;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Preconditions;

/**
 * Migration for the AttributeMapping table
 * <ol>
 * <li>Add a column `algorithmState` enum('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS') to AttributeMapping</li>
 * </ol>
 * 
 */
public class Step17AddAlgorithmStateDiscuss extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step17AddAlgorithmStateDiscuss.class);
	private final JdbcTemplate jdbcTemplate;

	public Step17AddAlgorithmStateDiscuss(DataSource dataSource)
	{
		super(16, 17);
		this.jdbcTemplate = new JdbcTemplate(Preconditions.checkNotNull(dataSource));
	}

	@Override
	public void upgrade()
	{
		LOG.info("Update metadata of the AttributeMapping from version 1.8 to 1.9");
		LOG.info("Alter AttributeMapping --- add column `algorithmState` enum('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS');");
		jdbcTemplate
				.execute("Use `omx`; ALTER TABLE AttributeMapping ADD `algorithmState` enum('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS');");
	}
}
