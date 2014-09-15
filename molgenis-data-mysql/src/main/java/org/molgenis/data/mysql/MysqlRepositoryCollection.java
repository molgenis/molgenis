package org.molgenis.data.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedCrudRepositorySecurityDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.meta.ElasticsearchAttributeMetaDataRepository;
import org.molgenis.data.elasticsearch.meta.ElasticsearchEntityMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.IndexedRepositoryValidationDecorator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.model.MolgenisModelException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class MysqlRepositoryCollection implements RepositoryCollection
{
	private final DataSource ds;
	private final DataService dataService;
	private Map<String, MysqlRepository> repositories;
	private final MysqlEntityMetaDataRepository entityMetaDataRepository;
	private final MysqlAttributeMetaDataRepository attributeMetaDataRepository;
	private final SearchService elasticSearchService;

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			MysqlEntityMetaDataRepository entityMetaDataRepository,
			MysqlAttributeMetaDataRepository attributeMetaDataRepository)
	{
		this(ds, dataService, entityMetaDataRepository, attributeMetaDataRepository, null);
	}

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			MysqlEntityMetaDataRepository entityMetaDataRepository,
			MysqlAttributeMetaDataRepository attributeMetaDataRepository, SearchService elasticSearchService)
	{
		this.ds = ds;
		this.dataService = dataService;
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.attributeMetaDataRepository = attributeMetaDataRepository;
		this.elasticSearchService = elasticSearchService;

		entityMetaDataRepository.setRepositoryCollection(this);
		attributeMetaDataRepository.setRepositoryCollection(this);

		refreshRepositories();
	}

	public DataSource getDataSource()
	{
		return ds;
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract MysqlRepository createMysqlRepsitory();

	public void refreshRepositories()
	{
		repositories = new LinkedHashMap<String, MysqlRepository>();

		// create meta data table
		if (!tableExists(EntityMetaDataMetaData.ENTITY_NAME))
		{
			entityMetaDataRepository.create();

			if (!tableExists(AttributeMetaDataMetaData.ENTITY_NAME))
			{
				attributeMetaDataRepository.create();
			}
		}
		else if (attributeMetaDataRepository.count() == 0)
		{
			// Update table structure to prevent errors is apps that don't use emx
			attributeMetaDataRepository.drop();
			entityMetaDataRepository.drop();
			entityMetaDataRepository.create();
			attributeMetaDataRepository.create();
		}

		// Upgrade old databases
		upgradeMetaDataTables();

		Set<EntityMetaData> metadata = getAllEntityMetaDataIncludingAbstract();

		// instantiate the repos
		for (EntityMetaData emd : metadata)
		{
			if (!emd.isAbstract())
			{
				MysqlRepository repo = createMysqlRepsitory();
				repo.setMetaData(emd);
				repositories.put(emd.getName(), repo);
			}
		}

		registerMysqlRepos();
	}

	public void registerMysqlRepos()
	{
		for (String name : getEntityNames())
		{
			if (dataService.hasRepository(name))
			{
				dataService.removeRepository(name);
			}

			Repository repo = getRepositoryByEntityName(name);
			dataService.addRepository(repo);
		}
	}

	private void upgradeMetaDataTables()
	{
		// Update attributes table if needed
		addAttributeToTable(AttributeMetaDataMetaData.AGGREGATEABLE);
		addAttributeToTable(AttributeMetaDataMetaData.RANGE_MIN);
		addAttributeToTable(AttributeMetaDataMetaData.RANGE_MAX);
		addAttributeToTable(AttributeMetaDataMetaData.ENUM_OPTIONS);
		addAttributeToTable(AttributeMetaDataMetaData.LABEL_ATTRIBUTE);
		addAttributeToTable(AttributeMetaDataMetaData.READ_ONLY);
	}

	private void addAttributeToTable(String attributeName)
	{
		if (!columnExists(attributeMetaDataRepository.getName(), attributeName))
		{
			String sql;
			try
			{
				sql = attributeMetaDataRepository.getAlterSql(MysqlAttributeMetaDataRepository.META_DATA
						.getAttribute(attributeName));
			}
			catch (MolgenisModelException e)
			{
				throw new RuntimeException(e);
			}

			new JdbcTemplate(ds).execute(sql);
		}
	}

	private boolean tableExists(String table)
	{
		Connection conn = null;
		try
		{

			conn = ds.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, table, null);
			return tables.next();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}

	private boolean columnExists(String table, String column)
	{
		Connection conn = null;
		try
		{

			conn = ds.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet columns = dbm.getColumns(null, null, table, column);
			return columns.next();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}

	@Transactional
	public MysqlRepository add(EntityMetaData emd)
	{
		MysqlRepository repository = null;

		if (getEntityMetaDataEntity(emd.getName()) != null)
		{
			if (emd.isAbstract())
			{
				return null;
			}

			repository = repositories.get(emd.getName());
			if (repository == null) throw new IllegalStateException("Repository [" + emd.getName()
					+ "] registered in entities table but missing in the MysqlRepositoryCollection");

			if (!dataService.hasRepository(emd.getName()))
			{
				dataService.addRepository(getSecuredAndOptionallyIndexedRepository(repository));
			}

			return repository;
		}

		if (dataService.hasRepository(emd.getName()))
		{
			throw new MolgenisDataException("Entity with name [" + emd.getName() + "] already exists.");
		}

		// if not abstract add to repositories
		if (!emd.isAbstract())
		{
			repository = createMysqlRepsitory();
			repository.setMetaData(emd);
			repository.create();

			repositories.put(emd.getName(), repository);
			dataService.addRepository(getSecuredAndOptionallyIndexedRepository(repository));
		}

		// Add to entities and attributes tables, this should be done AFTER the creation of new tables because create
		// table statements are ddl statements and when these are executed mysql does an implicit commit. So when the
		// create table fails a rollback does not work anymore
		getEntityMetaDataRepository().addEntityMetaData(emd);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
			// do not use getAttributeMetaDataRepository(), actions already take place during addEntityMetaData
			attributeMetaDataRepository.addAttributeMetaData(emd.getName(), att);
		}

		return repository;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		MysqlRepository repo = repositories.get(name);
		if (repo == null)
		{
			return null;
		}

		return getSecuredAndOptionallyIndexedRepository(repo);
	}

	public Set<EntityMetaData> getAllEntityMetaDataIncludingAbstract()
	{
		Map<String, EntityMetaData> metadata = Maps.newLinkedHashMap();

		// read the entity meta data
		for (EntityMetaData entityMetaData : entityMetaDataRepository.getEntityMetaDatas())
		{
			DefaultEntityMetaData entityMetaDataWithAttributes = new DefaultEntityMetaData(entityMetaData);
			metadata.put(entityMetaDataWithAttributes.getName(), entityMetaDataWithAttributes);

			// add the attribute meta data of the entity
			for (AttributeMetaData attributeMetaData : attributeMetaDataRepository
					.getEntityAttributeMetaData(entityMetaDataWithAttributes.getName()))
			{
				entityMetaDataWithAttributes.addAttributeMetaData(attributeMetaData);
			}
		}

		// read the refEntity
		for (Entity attribute : attributeMetaDataRepository)
		{
			if (attribute.getString(AttributeMetaDataMetaData.REF_ENTITY) != null)
			{
				EntityMetaData entityMetaData = metadata.get(attribute.getString(AttributeMetaDataMetaData.ENTITY));
				DefaultAttributeMetaData attributeMetaData = (DefaultAttributeMetaData) entityMetaData
						.getAttribute(attribute.getString(AttributeMetaDataMetaData.NAME));
				EntityMetaData ref = metadata.get(attribute.getString(AttributeMetaDataMetaData.REF_ENTITY));
				if (ref == null) throw new RuntimeException("refEntity '" + attribute.getString("refEntity")
						+ "' missing for " + entityMetaData.getName() + "." + attributeMetaData.getName());
				attributeMetaData.setRefEntity(ref);
			}
		}

		Set<EntityMetaData> metadataSet = Sets.newLinkedHashSet();
		for (String name : metadata.keySet())
		{
			metadataSet.add(metadata.get(name));
		}

		return metadataSet;
	}

	public Entity getEntityMetaDataEntity(String name)
	{
		return entityMetaDataRepository.findOne(name);
	}

	public void drop(EntityMetaData md)
	{
		assert md != null;
		dropEntityMetaData(md.getName());
	}

	public void dropEntityMetaData(String name)
	{
		// remove the repo
		MysqlRepository r = repositories.get(name);
		if (r != null)
		{
			r.drop();
			repositories.remove(name);
			dataService.removeRepository(r.getName());
		}

		// delete metadata
		attributeMetaDataRepository.delete(attributeMetaDataRepository.findAll(new QueryImpl().eq(
				AttributeMetaDataMetaData.ENTITY, name)));
		entityMetaDataRepository.delete(entityMetaDataRepository.findAll(new QueryImpl().eq(
				EntityMetaDataMetaData.NAME, name)));
	}

	public void dropAttributeMetaData(String entityName, String attributeName)
	{
		MysqlRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.dropAttribute(attributeName);
		}

		// Update AttributeMetaDataRepository
		attributeMetaDataRepository.removeAttributeMetaData(entityName, attributeName);

		refreshRepositories();
	}

	/**
	 * Add new AttributeMetaData to an existing EntityMetaData. Trying to edit an exiting AttributeMetaData will throw
	 * an exception
	 * 
	 * @param sourceEntityMetaData
	 * @return the names of the added AttributeMetaData
	 */
	@Transactional
	public List<String> update(EntityMetaData sourceEntityMetaData)
	{
		MysqlRepository repository = repositories.get(sourceEntityMetaData.getName());
		EntityMetaData existingEntityMetaData = repository.getEntityMetaData();
		List<String> addedAttributes = Lists.newArrayList();

		for (AttributeMetaData attr : existingEntityMetaData.getAttributes())
		{
			if (sourceEntityMetaData.getAttribute(attr.getName()) == null)
			{
				throw new MolgenisDataException(
						"Removing of existing attributes is currently not sypported. You tried to remove attribute ["
								+ attr.getName() + "]");
			}
		}

		for (AttributeMetaData attr : sourceEntityMetaData.getAttributes())
		{
			AttributeMetaData currentAttribute = existingEntityMetaData.getAttribute(attr.getName());
			if (currentAttribute != null)
			{
				if (!currentAttribute.isSameAs(attr))
				{
					throw new MolgenisDataException(
							"Changing existing attributes is not currently supported. You tried to alter attribute ["
									+ attr.getName() + "] of entity [" + sourceEntityMetaData.getName()
									+ "]. Only adding of new atrtibutes to existing entities is supported.");
				}
			}
			else if (!attr.isNillable())
			{
				throw new MolgenisDataException("Adding non-nillable attributes is not currently supported");
			}
			else
			{
				getAttributeMetaDataRepository().addAttributeMetaData(sourceEntityMetaData.getName(), attr);
				DefaultEntityMetaData defaultEntityMetaData = (DefaultEntityMetaData) repository.getEntityMetaData();
				defaultEntityMetaData.addAttributeMetaData(attr);
				repository.addAttribute(attr);
				addedAttributes.add(attr.getName());
			}
		}

		return addedAttributes;
	}

	/**
	 * Returns a secured and optionally indexed repository for the given repository
	 * 
	 * @param repository
	 * @return
	 */
	private Repository getSecuredAndOptionallyIndexedRepository(CrudRepository repository)
	{
		CrudRepository decoratedRepository = repository;
		if (elasticSearchService != null)
		{
			// 1. security decorator
			// 2. index decorator
			// 3. validation decorator
			// 4. repository
			decoratedRepository = new IndexedCrudRepositorySecurityDecorator(new IndexedRepositoryValidationDecorator(
					new ElasticsearchRepositoryDecorator(decoratedRepository, elasticSearchService),
					new EntityAttributesValidator()));
		}
		else
		{
			// 1. security decorator
			// 2. validation decorator
			// 3. repository
			decoratedRepository = new CrudRepositorySecurityDecorator(new RepositoryValidationDecorator(
					decoratedRepository, new EntityAttributesValidator()));
		}
		return decoratedRepository;
	}

	/**
	 * Returns an optionally indexed meta data repository for attributes
	 * 
	 * @return
	 */
	private AttributeMetaDataRepository getAttributeMetaDataRepository()
	{
		if (elasticSearchService != null)
		{
			return new ElasticsearchAttributeMetaDataRepository(attributeMetaDataRepository, dataService,
					elasticSearchService);
		}
		return attributeMetaDataRepository;
	}

	/**
	 * Returns an optionally indexed meta data repository for entities
	 * 
	 * @return
	 */
	private EntityMetaDataRepository getEntityMetaDataRepository()
	{
		if (elasticSearchService != null)
		{
			return new ElasticsearchEntityMetaDataRepository(entityMetaDataRepository, elasticSearchService);
		}
		return entityMetaDataRepository;
	}
}
