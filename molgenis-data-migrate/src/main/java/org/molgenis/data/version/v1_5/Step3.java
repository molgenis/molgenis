package org.molgenis.data.version.v1_5;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
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

	private String backend;

	private DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(Step3.class);

	public Step3(DataSource dataSource, String backend, DataService dataService)
	{
		super(2, 3);
		this.template = new JdbcTemplate(dataSource);
		this.backend = backend;
		this.dataService = dataService;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Migrating backend {} ...", backend);
		for (EntityMetaData emd : dataService.getMeta().getEntityMetaDatas())
		{
			if (backend.equalsIgnoreCase(emd.getBackend()))
			{
				for (AttributeMetaData amd : emd.getAtomicAttributes())
				{
					if (amd.getDataType() instanceof MrefField) try
					{
						String mrefExistenceSql = getMrefExistenceSql(emd, amd);
						LOG.debug(mrefExistenceSql);
						Integer exists = template.queryForObject(mrefExistenceSql, Integer.class);
						if (exists > 0)
						{
							LOG.debug("MREF attribute table {}.{}.{} already has an order column.", backend,
									emd.getName(), amd.getName());
						}
						else
						{
							LOG.info("Add order column to MREF attribute table {}.{}.{} .", backend, emd.getName(),
									amd.getName());
							String mrefUpdateSql = getMrefUpdateSql(emd, amd);
							LOG.debug(mrefUpdateSql);
							template.execute(mrefUpdateSql);
						}
					}
					catch (DataAccessException daoe)
					{
						LOG.error("Error migrating {}.{}.{} .", backend, emd.getName(), amd.getName(), daoe);
					}
				}
			}
		}
		LOG.info("Migrating backend {} done.", backend);
	}

	private static String getMrefExistenceSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '%s_%s' AND column_name = 'order'",
				emd.getName().toLowerCase(), att.getName().toLowerCase());
	}

	private static String getMrefUpdateSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format("ALTER TABLE `%s_%s` ADD COLUMN `order` INT;", emd.getName().toLowerCase(), att.getName()
				.toLowerCase());
	}
}
