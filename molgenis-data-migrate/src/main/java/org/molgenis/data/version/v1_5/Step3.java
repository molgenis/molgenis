package org.molgenis.data.version.v1_5;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.version.MetaDataUpgrade;
import org.molgenis.fieldtypes.MrefField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Migrates MySQL MREF tables from molgenis 1.4 to 1.5
 */
public class Step3 extends MetaDataUpgrade
{
	private JdbcTemplate template;

	private RepositoryCollection mysql;

	private static final Logger LOG = LoggerFactory.getLogger(Step3.class);

	public Step3(DataSource dataSource, RepositoryCollection mysql)
	{
		super(2, 3);
		this.template = new JdbcTemplate(dataSource);
		this.mysql = mysql;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Migrating {} MREF columns...", mysql.getName());
		for (Repository repo : mysql)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				if (amd.getDataType() instanceof MrefField)
				{
					LOG.info("Add order column to MREF attribute table {}.{}.{} .", mysql.getName(), emd.getName(),
							amd.getName());
					String mrefUpdateSql = getMrefUpdateSql(emd, amd);
					LOG.debug(mrefUpdateSql);
					try
					{
						template.execute(mrefUpdateSql);
					}
					catch (DataAccessException dae)
					{
						LOG.error("Error migrating {}.{}.{} .", mysql.getName(), emd.getName(), amd.getName(), dae);
					}
				}
			}
		}
		LOG.info("Migrating {} MREF columns DONE.", mysql.getName());
	}

	private static String getMrefUpdateSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format("ALTER TABLE `%s_%s` ADD COLUMN `order` INT;", emd.getName().toLowerCase(), att.getName()
				.toLowerCase());
	}
}
