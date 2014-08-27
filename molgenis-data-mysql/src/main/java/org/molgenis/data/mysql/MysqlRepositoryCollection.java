package org.molgenis.data.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.data.AggregateableCrudRepositorySecurityDecorator;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.model.MolgenisModelException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public abstract class MysqlRepositoryCollection implements RepositoryCollection
{
	private final DataSource ds;
	private final DataService dataService;
	private Map<String, MysqlRepository> repositories;
	private final EntityMetaDataRepository entityMetaDataRepository;
	private final AttributeMetaDataRepository attributeMetaDataRepository;

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			EntityMetaDataRepository entityMetaDataRepository, AttributeMetaDataRepository attributeMetaDataRepository)
	{
		this.ds = ds;
		this.dataService = dataService;
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.attributeMetaDataRepository = attributeMetaDataRepository;
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
		entityMetaDataRepository.setRepositoryCollection(this);
		attributeMetaDataRepository.setRepositoryCollection(this);

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

		Map<String, DefaultEntityMetaData> metadata = new LinkedHashMap<String, DefaultEntityMetaData>();

		// read the entity meta data
		for (DefaultEntityMetaData entityMetaData : entityMetaDataRepository.getEntityMetaDatas())
		{
			metadata.put(entityMetaData.getName(), entityMetaData);

			// add the attribute meta data of the entity
			for (AttributeMetaData attributeMetaData : attributeMetaDataRepository
					.getEntityAttributeMetaData(entityMetaData.getName()))
			{
				entityMetaData.addAttributeMetaData(attributeMetaData);
			}
		}

		// read the refEntity
		for (Entity attribute : attributeMetaDataRepository)
		{
			if (attribute.getString(AttributeMetaDataMetaData.REF_ENTITY) != null)
			{
				DefaultEntityMetaData entityMetaData = metadata.get(attribute
						.getString(AttributeMetaDataMetaData.ENTITY));
				DefaultAttributeMetaData attributeMetaData = (DefaultAttributeMetaData) entityMetaData
						.getAttribute(attribute.getString(AttributeMetaDataMetaData.NAME));
				EntityMetaData ref = metadata.get(attribute.getString(AttributeMetaDataMetaData.REF_ENTITY));
				if (ref == null) throw new RuntimeException("refEntity '" + attribute.getString("refEntity")
						+ "' missing for " + entityMetaData.getName() + "." + attributeMetaData.getName());
				attributeMetaData.setRefEntity(ref);
			}
		}

		// instantiate the repos
		for (EntityMetaData emd : metadata.values())
		{
			if (!emd.isAbstract())
			{
				MysqlRepository repo = createMysqlRepsitory();
				repo.setMetaData(emd);
				repositories.put(emd.getName(), repo);
			}
		}
	}

	private void upgradeMetaDataTables()
	{
		// Update attributes table if needed

		if (!columnExists(attributeMetaDataRepository.getName(), AttributeMetaDataMetaData.AGGREGATEABLE))
		{
			String sql;
			try
			{
				sql = attributeMetaDataRepository.getAlterSql(AttributeMetaDataRepository.META_DATA
						.getAttribute(AttributeMetaDataMetaData.AGGREGATEABLE));
			}
			catch (MolgenisModelException e)
			{
				throw new RuntimeException(e);
			}

			new JdbcTemplate(ds).execute(sql);
		}

		if (!columnExists(attributeMetaDataRepository.getName(), AttributeMetaDataMetaData.RANGE_MIN))
		{
			String sql;
			try
			{
				sql = attributeMetaDataRepository.getAlterSql(AttributeMetaDataRepository.META_DATA
						.getAttribute(AttributeMetaDataMetaData.RANGE_MIN));
			}
			catch (MolgenisModelException e)
			{
				throw new RuntimeException(e);
			}

			new JdbcTemplate(ds).execute(sql);
		}

		if (!columnExists(attributeMetaDataRepository.getName(), AttributeMetaDataMetaData.RANGE_MAX))
		{
			String sql;
			try
			{
				sql = attributeMetaDataRepository.getAlterSql(AttributeMetaDataRepository.META_DATA
						.getAttribute(AttributeMetaDataMetaData.RANGE_MAX));
			}
			catch (MolgenisModelException e)
			{
				throw new RuntimeException(e);
			}

			new JdbcTemplate(ds).execute(sql);
		}

		if (!columnExists(attributeMetaDataRepository.getName(), AttributeMetaDataMetaData.ENUM_OPTIONS))
		{
			String sql;
			try
			{
				sql = attributeMetaDataRepository.getAlterSql(AttributeMetaDataRepository.META_DATA
						.getAttribute(AttributeMetaDataMetaData.ENUM_OPTIONS));
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
				dataService.addRepository(new AggregateableCrudRepositorySecurityDecorator(repository));
			}

			return repository;
		}

		// if not abstract add to repositories
		if (!emd.isAbstract())
		{
			repository = createMysqlRepsitory();
			repository.setMetaData(emd);
			repository.create();

			repositories.put(emd.getName(), repository);
			dataService.addRepository(new AggregateableCrudRepositorySecurityDecorator(repository));
		}

		// Add to entities and attributes tables, this should be done AFTER the creation of new tables because create
		// table statements are ddl statements and when these are executed mysql does an implicit commit. So when the
		// create table fails a rollback does not work anymore
		entityMetaDataRepository.addEntityMetaData(emd);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
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

		return new AggregateableCrudRepositorySecurityDecorator(repo);
	}

	public Entity getEntityMetaDataEntity(String name)
	{
		return entityMetaDataRepository.findOne(name);
	}

	public void drop(EntityMetaData md)
	{
		assert md != null;
		drop(md.getName());
	}

	public void drop(String name)
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

	@Transactional
	public void update(EntityMetaData sourceEntityMetaData)
	{
		MysqlRepository repository = repositories.get(sourceEntityMetaData.getName());
		EntityMetaData existingEntityMetaData = repository.getEntityMetaData();

		for (AttributeMetaData attr : sourceEntityMetaData.getAttributes())
		{
			AttributeMetaData currentAttribute = existingEntityMetaData.getAttribute(attr.getName());

			if (currentAttribute != null)
			{
				if (!currentAttribute.getDataType().equals(attr.getDataType()))
				{
					throw new MolgenisDataException("Changing type for existing attributes is not currently supported");
				}
			}
			else if (!attr.isNillable())
			{
				throw new MolgenisDataException("Adding non-nillable attributes is not currently supported");
			}
			else
			{
				attributeMetaDataRepository.addAttributeMetaData(sourceEntityMetaData.getName(), attr);
				DefaultEntityMetaData defaultEntityMetaData = (DefaultEntityMetaData) repository.getEntityMetaData();
				defaultEntityMetaData.addAttributeMetaData(attr);
				repository.addAttribute(attr);
			}
		}
	}
}
