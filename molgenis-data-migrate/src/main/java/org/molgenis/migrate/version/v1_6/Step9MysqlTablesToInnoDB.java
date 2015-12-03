package org.molgenis.migrate.version.v1_6;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.migrate.version.MigrationUtils;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Part of migration of MOLGENIS 1.5 to 1.6. Changes all MySQL tables that do not use the InnoDB engine to use that
 * engine.
 * 
 * Some servers have MyISAM as default engine, which caused inconsistent behavior between local and on-server MOLGENIS
 * instances. MysqlRepository creates all tables with InnoDB as of MOLGENIS 1.6, but old databases may need to be
 * migrated in this step.
 */
public class Step9MysqlTablesToInnoDB extends MolgenisUpgrade
{
	private JdbcTemplate template;

	private DataSource dataSource;

	private static final Logger LOG = LoggerFactory.getLogger(Step9MysqlTablesToInnoDB.class);

	public Step9MysqlTablesToInnoDB(DataSource dataSource)
	{
		super(8, 9);
		this.template = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}

	@Override
	public void upgrade()
	{
		DataServiceImpl dataService = new DataServiceImpl();
		EntityManager entityResolver = new EntityManagerImpl(dataService);
		MySqlEntityFactory mySqlEntityFactory = new MySqlEntityFactory(entityResolver, dataService);

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
		MetaDataService metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(undecoratedMySQL));

		LOG.info("Migrating non-InnoDB tables to InnoDB...");

		try
		{
			List<Map<String, Object>> rows = template
					.queryForList(getSelectEngineSql(MigrationUtils.getDatabaseName()));

			for (Map<String, Object> table : rows)
			{
				if (!table.get("ENGINE").equals("InnoDB"))
				{
					template.execute(getAlterTableSql(table.get("TABLE_NAME").toString()));
					LOG.info(String.format("Changed engine to InnoDB for table `%s`", table.get("TABLE_NAME")));
				}
			}
		}
		catch (Throwable t)
		{
			LOG.error("Error migrating non-InnoDB tables!", t);
		}

		LOG.info("Migration of non-InnoDB tables finished.");
	}

	private static String getAlterTableSql(String tableName)
	{
		return String.format("ALTER TABLE %s ENGINE = InnoDB;", tableName);
	}

	private static String getSelectEngineSql(String databaseName)
	{
		return String.format("SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%s'",
				databaseName);
	}
}
