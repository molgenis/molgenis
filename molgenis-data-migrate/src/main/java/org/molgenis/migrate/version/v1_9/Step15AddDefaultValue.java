package org.molgenis.migrate.version.v1_9;

import static java.util.stream.StreamSupport.stream;

import javax.sql.DataSource;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Preconditions;

/**
 * Migration for the introduction of attributes attribute "defaultValue".
 * <ol>
 * <li>Creates the column in the database</li>
 * <li>Iterates over the already imported JPA entities to write their defaultValue to the newly created column</li>
 * <li>Sets the defaultValue for the generateToken attribute of the Script entity</li>
 * <li>Recreates the attributes index in elasticsearch to reflect these changes</li>
 * </ol>
 * 
 * @author fkelpin
 */
public class Step15AddDefaultValue extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step15AddDefaultValue.class);
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	private final SearchService searchService;
	private final RepositoryCollection jpaRepositoryCollection;

	public Step15AddDefaultValue(DataSource dataSource, SearchService searchService,
			RepositoryCollection jpaRepositoryCollection)
	{
		super(14, 15);
		this.dataSource = Preconditions.checkNotNull(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.searchService = Preconditions.checkNotNull(searchService);
		this.jpaRepositoryCollection = Preconditions.checkNotNull(jpaRepositoryCollection);
	}

	private void executeUpgradeSql(String entityName, String attributeName, String defaultValue)
	{
		jdbcTemplate.update("UPDATE attributes SET defaultValue = ? WHERE name = ? "
				+ "AND identifier IN (SELECT attributes FROM entities_attributes ea WHERE ea.fullName = ?);",
				defaultValue, attributeName, entityName);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating metadata from version 14 to 15");
		addColumnDefaultValue();
		insertDefaultValuesForJPAEntities();
		insertDefaultValueForScriptEntity();
		reindexAttributesRepository();
	}

	protected void insertDefaultValueForScriptEntity()
	{
		executeUpgradeSql("Script", "generateToken", "false");
	}

	protected void insertDefaultValuesForJPAEntities()
	{
		jpaRepositoryCollection
				.stream()
				.map(Repository::getEntityMetaData)
				.forEach(
						emd -> {
							stream(emd.getAtomicAttributes().spliterator(), false).filter(
									amd -> amd.getDefaultValue() != null).forEach(
									amd -> executeUpgradeSql(emd.getName(), amd.getName(), amd.getDefaultValue()));
						});
	}

	protected void addColumnDefaultValue()
	{
		try
		{
			executeSql("ALTER TABLE attributes ADD COLUMN defaultValue " + new TextField().getMysqlType());
		}
		catch (MolgenisModelException e)
		{
			throw new MolgenisDataException("Failed to figure out the MySQL data type for TEXT fields");
		}
	}

	protected void reindexAttributesRepository()
	{
		DataServiceImpl dataService = new DataServiceImpl();

		// Get the undecorated attribute repo
		MysqlRepositoryCollection undecoratedMySQL = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(dataService, dataSource, new AsyncJdbcTemplate(new JdbcTemplate(dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};
		MetaDataService metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(undecoratedMySQL));

		searchService.delete(AttributeMetaDataMetaData.ENTITY_NAME);
		searchService.createMappings(AttributeMetaDataMetaData.INSTANCE);

		searchService.rebuildIndex(undecoratedMySQL.getRepository(AttributeMetaDataMetaData.ENTITY_NAME),
				AttributeMetaDataMetaData.INSTANCE);
	}

	protected void executeSql(String sql)
	{
		LOG.info(sql);
		jdbcTemplate.execute(sql);
	}
}
