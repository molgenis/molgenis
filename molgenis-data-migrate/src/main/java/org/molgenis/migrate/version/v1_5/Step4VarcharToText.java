package org.molgenis.migrate.version.v1_5;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Part of migration of MOLGENIS 1.4 to 1.5. Changes all entities having MySQL as a back-end. Attributes of FieldType
 * String are changed to TEXT.
 */
public class Step4VarcharToText extends MolgenisUpgrade
{
	private JdbcTemplate template;

	private RepositoryCollection mysql;

	private static final Logger LOG = LoggerFactory.getLogger(Step4VarcharToText.class);

	public Step4VarcharToText(DataSource dataSource, RepositoryCollection mysql)
	{
		super(3, 4);
		this.template = new JdbcTemplate(dataSource);
		this.mysql = mysql;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Migrating {} String columns...", mysql.getName());
		for (Repository repo : mysql)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				if (amd.getDataType() instanceof StringField)
				{
					LOG.info("Changing column {}.{}.{} to TEXT.", mysql.getName(), emd.getName(), amd.getName());
					String columnModifySql = getModifyColumnSql(emd, amd);
					LOG.debug(columnModifySql);
					try
					{
						template.execute(columnModifySql);
					}
					catch (DataAccessException dae)
					{
						LOG.error("Error migrating {}.{}.{} .", mysql.getName(), emd.getName(), amd.getName(), dae);
					}
				}
			}
		}
		LOG.info("Migrating {} String columns DONE.", mysql.getName());
	}

	private static String getModifyColumnSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format("ALTER TABLE %s MODIFY COLUMN %s TEXT;", emd.getName(), att.getName());
	}
}
