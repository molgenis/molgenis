package org.molgenis.migrate.version.v1_6;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
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
import org.molgenis.fieldtypes.CategoricalField;
import org.molgenis.fieldtypes.CategoricalMrefField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Part of migration of MOLGENIS 1.5 to 1.6. Changes all entities having MySQL as a back-end. Attributes of FieldType
 * String are changed to TEXT, and UNIQUE constraints are dropped when present for these columns.
 * 
 * This is a partial copy of Step4 and is needed again because in the previous version the actual back-end changes
 * weren't implemented, possibly resulting in inconsistent tables. Also, Step4 didn't work.
 */
public class Step8VarcharToTextRepeated extends MolgenisUpgrade
{
	private JdbcTemplate template;

	private DataSource dataSource;

	private static final Logger LOG = LoggerFactory.getLogger(Step8VarcharToTextRepeated.class);

	public Step8VarcharToTextRepeated(DataSource dataSource)
	{
		super(7, 8);
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

		// find all entities with backend = MySQL
		Iterable<Entity> entities = dataService.findAll("entities");
		entities.forEach(entity -> {
			if (entity.get("backend").equals("MySQL"))
			{
				String mysqlEntityName = entity.get("fullName").toString();

				EntityMetaData emd = metaData.getEntityMetaData(mysqlEntityName);

				if (!emd.isAbstract())
				{
					for (AttributeMetaData amd : emd.getAtomicAttributes())
					{
						FieldType fieldType = amd.getDataType();
						if (fieldType instanceof StringField)
						{
							// mysql keys cannot be text so leave those columns untouched
							if (!(amd.isIdAtrribute() || fieldType instanceof CategoricalMrefField
									|| fieldType instanceof CategoricalField || fieldType instanceof MrefField
									|| fieldType instanceof XrefField))
							{
								if (amd.isUnique())
								{
									// TEXT columns don't like UNIQUE, remove the constraint
									LOG.info("Removing UNIQUE constraint for {}.{}", emd.getName(), amd.getName());

									try
									{
										// get the unique key name for this column
										List<Map<String, Object>> rows = template
												.queryForList(getShowIndexSql(emd, amd));
										// should be first and only row
										if (rows.iterator().hasNext())
										{
											String keyName = rows.iterator().next().get("Key_name").toString();
											template.execute(getRemoveUniqueConstraintSql(emd, keyName));
										}
									}
									catch (Throwable t)
									{
										LOG.error("Error removing UNIQUE constraint for {}.{} ", emd.getName(),
												amd.getName(), t);
									}
								}

								String columnModifySql = getModifyColumnSql(emd, amd);
								try
								{
									LOG.info("Changing column {}.{} to TEXT", emd.getName(), amd.getName());
									template.execute(columnModifySql);
								}
								catch (DataAccessException dae)
								{
									LOG.error("Error changing column {}.{} ", emd.getName(), amd.getName(), dae);
								}
							}
						}
					}
				}
			}
		});
		LOG.info("Migrating String columns DONE.");
	}

	private static String getModifyColumnSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format("ALTER TABLE `%s` MODIFY COLUMN `%s` TEXT;", emd.getName(), att.getName());
	}

	private static String getRemoveUniqueConstraintSql(EntityMetaData emd, String keyName)
	{
		return String.format("ALTER TABLE `%s` DROP INDEX `%s`;", emd.getName(), keyName);
	}

	private static String getShowIndexSql(EntityMetaData emd, AttributeMetaData att)
	{
		return String.format("SHOW INDEX FROM `%s` WHERE Column_name = '%s';", emd.getName(), att.getName());
	}
}
