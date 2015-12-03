package org.molgenis.migrate.version.v1_5;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Migrates MySQL MREF tables for ordinary entities from molgenis 1.4 to 1.5
 */
public class Step3AddOrderColumnToMrefTables extends MolgenisUpgrade
{
	private JdbcTemplate template;

	private DataServiceImpl dataService;

	private EntityManager entityResolver;

	private MySqlEntityFactory mySqlEntityFactory;

	private DataSource dataSource;

	private static final Logger LOG = LoggerFactory.getLogger(Step3AddOrderColumnToMrefTables.class);

	private MetaDataService metaData;

	public Step3AddOrderColumnToMrefTables(DataSource dataSource)
	{
		super(2, 3);
		this.template = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Migrating MySQL MREF columns...");

		// Get the undecorated repos
		MysqlRepositoryCollection undecoratedMySQL = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(dataService, mySqlEntityFactory, dataSource,
						new AsyncJdbcTemplate(new JdbcTemplate(dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};

		dataService = new DataServiceImpl();
		entityResolver = new EntityManagerImpl(dataService);
		mySqlEntityFactory = new MySqlEntityFactory(entityResolver, dataService);
		metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(undecoratedMySQL));
		addOrderColumnToMREFTables();
		LOG.info("Migrating MySQL MREF columns DONE.");
	}

	private void addOrderColumnToMREFTables()
	{
		LOG.info("Add order column to MREF tables...");
		for (EntityMetaData emd : metaData.getEntityMetaDatas())
		{
			LOG.info("Entity {}", emd.getName());
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				if (amd.getDataType() instanceof MrefField)
				{
					LOG.info("Add order column to MREF attribute table {}.{} .", emd.getName(), amd.getName());
					String mrefUpdateSql = getMrefUpdateSql(emd, amd);
					try
					{
						template.execute(mrefUpdateSql);
					}
					catch (DataAccessException dae)
					{
						LOG.error("Error migrating {}.{} . {}", emd.getName(), amd.getName(), dae.getMessage());
					}
				}
			}
		}
		LOG.info("Add order column to MREF tables DONE.");
	}

	private static String getMrefUpdateSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format("ALTER TABLE `%s_%s` ADD COLUMN `order` INT;", emd.getName(), att.getName());
	}
}
