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
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.mysql.meta.EntityMetaDataMetaData;
import org.molgenis.data.mysql.meta.MysqlMetaDataRepositories;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public abstract class MysqlRepositoryCollection implements RepositoryCollection
{
	public final DataSource ds;
	private final DataService dataService;
	private Map<String, MysqlRepository> repositories;
	// temporary workaround for module dependencies
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final MysqlMetaDataRepositories metaDataRepositories;

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			MysqlMetaDataRepositories metaDataRepositories)
	{
		this(ds, dataService, metaDataRepositories, null);
	}

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			MysqlMetaDataRepositories metaDataRepositories, RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.ds = ds;
		this.dataService = dataService;
		this.metaDataRepositories = metaDataRepositories;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
		this.metaDataRepositories.setRepositoryCollection(this);
		System.out.println("metaDataRepositories != null" + this);
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
		metaDataRepositories.createAndUpgradeMetaDataTables();

		Set<EntityMetaData> metadata = metaDataRepositories.getAllEntityMetaDataIncludingAbstract();

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

	public boolean tableExists(String table)
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

	public boolean columnExists(String table, String column)
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
				dataService.addRepository(getDecoratedRepository(repository));
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
			dataService.addRepository(getDecoratedRepository(repository));
		}

		// Add to entities and attributes tables, this should be done AFTER the creation of new tables because create
		// table statements are ddl statements and when these are executed mysql does an implicit commit. So when the
		// create table fails a rollback does not work anymore
		metaDataRepositories.registerEntityMetaData(emd);

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

		return getDecoratedRepository(repo);
	}

	public MysqlRepository getUndecoratedRepository(String name)
	{
		return repositories.get(name);
	}

	public Entity getEntityMetaDataEntity(String name)
	{
		Query q = new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, name);
		return metaDataRepositories.entityMetaDataRepository.findOne(q);
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

		metaDataRepositories.dropEntityMetaData(name);
	}

	public void dropAttributeMetaData(String entityName, String attributeName)
	{
		MysqlRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.dropAttribute(attributeName);
		}

		metaDataRepositories.dropAttributeMetaData(entityName, attributeName);

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
	 * Returns an optionally decorated repository (e.g. security, indexing, validation) for the given repository
	 * 
	 * @param repository
	 * @return
	 */
	private Repository getDecoratedRepository(CrudRepository repository)
	{
		return repositoryDecoratorFactory != null ? repositoryDecoratorFactory.createDecoratedRepository(repository) : repository;
	}

	/**
	 * Returns an optionally decorated attribute meta data repository (e.g. security, indexing, validation) for the
	 * given repository
	 * 
	 * @return
	 */
	private AttributeMetaDataRepository getAttributeMetaDataRepository()
	{
		return metaDataRepositories.attributeMetaDataRepositoryDecoratorFactory != null ? metaDataRepositories.attributeMetaDataRepositoryDecoratorFactory
				.createDecoratedRepository(metaDataRepositories.attributeMetaDataRepository) : metaDataRepositories.attributeMetaDataRepository;
	}

	/**
	 * Returns an optionally decorated entity meta data repository (e.g. security, indexing, validation) for the given
	 * repository
	 * 
	 * @return
	 */
	public EntityMetaDataRepository getEntityMetaDataRepository()
	{
		return metaDataRepositories.entityMetaDataRepositoryDecoratorFactory != null ? metaDataRepositories.entityMetaDataRepositoryDecoratorFactory
				.createDecoratedRepository(metaDataRepositories.entityMetaDataRepository) : metaDataRepositories.entityMetaDataRepository;
	}
}
