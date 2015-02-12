package org.molgenis.data.mysql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public abstract class MysqlRepositoryCollection implements ManageableCrudRepositoryCollection, InitializingBean
{
	private final DataSource ds;
	private final DataService dataService;
	final private Map<String, MysqlRepository> repositories = new LinkedHashMap<String, MysqlRepository>();
	// temporary workaround for module dependencies
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final WritableMetaDataService metaDataRepositories;

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			WritableMetaDataService metaDataRepositories)
	{
		this(ds, dataService, metaDataRepositories, null);
	}

	public MysqlRepositoryCollection(DataSource ds, DataService dataService,
			WritableMetaDataService metaDataRepositories, RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.ds = ds;
		this.dataService = dataService;
		this.metaDataRepositories = metaDataRepositories;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		refreshRepositories();
	}

	public DataSource getDataSource()
	{
		return ds;
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract MysqlRepository createMysqlRepository();

	public void refreshRepositories()
	{
		Iterable<EntityMetaData> metadata = metaDataRepositories.getEntityMetaDatas();

		// instantiate the repos
		for (EntityMetaData emd : metadata)
		{
			if (!emd.isAbstract())
			{
				MysqlRepository repo = createMysqlRepository();
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

	@Override
	@Transactional
	public CrudRepository add(EntityMetaData emd)
	{
		CrudRepository result = null;

		if (metaDataRepositories.getEntityMetaData(emd.getName()) != null)
		{
			if (emd.isAbstract())
			{
				return null;
			}

			result = repositories.get(emd.getName());
			if (result == null) throw new IllegalStateException("Repository [" + emd.getName()
					+ "] registered in entities table but missing in the MysqlRepositoryCollection");

			result = getDecoratedRepository(result);
			if (!dataService.hasRepository(emd.getName()))
			{
				dataService.addRepository(result);
			}

			return result;
		}

		if (dataService.hasRepository(emd.getName()))
		{
			throw new MolgenisDataException("Entity with name [" + emd.getName() + "] already exists.");
		}

		// if not abstract add to repositories
		if (!emd.isAbstract())
		{
			MysqlRepository repository = createMysqlRepository();
			repository.setMetaData(emd);
			repository.create();
			repositories.put(emd.getName(), repository);
			result = getDecoratedRepository(repository);
			dataService.addRepository(result);
		}

		// Add to entities and attributes tables, this should be done AFTER the creation of new tables because create
		// table statements are ddl statements and when these are executed mysql does an implicit commit. So when the
		// create table fails a rollback does not work anymore
		metaDataRepositories.addEntityMetaData(emd);

		return result;
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
			metaDataRepositories.removeEntityMetaData(name);
		}
	}

	public void dropAttributeMetaData(String entityName, String attributeName)
	{
		MysqlRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.dropAttribute(attributeName);
			metaDataRepositories.removeAttributeMetaData(entityName, attributeName);
		}
		refreshRepositories();
	}

	/**
	 * Add new AttributeMetaData to an existing EntityMetaData. Trying to edit an existing AttributeMetaData will throw
	 * an exception
	 * 
	 * @param sourceEntityMetaData
	 * @return the added AttributeMetaData
	 */
	@Override
	@Transactional
	public List<AttributeMetaData> update(EntityMetaData sourceEntityMetaData)
	{
		MysqlRepository repository = repositories.get(sourceEntityMetaData.getName());
		EntityMetaData existingEntityMetaData = repository.getEntityMetaData();
		List<AttributeMetaData> addedAttributes = Lists.newArrayList();

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
				// TODO: use decorated repository!
				metaDataRepositories.addAttributeMetaData(sourceEntityMetaData.getName(), attr);
				DefaultEntityMetaData defaultEntityMetaData = (DefaultEntityMetaData) repository.getEntityMetaData();
				defaultEntityMetaData.addAttributeMetaData(attr);
				if (attr.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
				{
					for (AttributeMetaData attrPart : attr.getAttributeParts())
					{
						repository.addAttribute(attrPart);
					}
				}
				else
				{
					repository.addAttribute(attr);
				}
				addedAttributes.add(attr);
			}
		}

		return addedAttributes;
	}

    @Transactional
    public List<AttributeMetaData> updateSync(EntityMetaData sourceEntityMetaData)
    {
        MysqlRepository repository = repositories.get(sourceEntityMetaData.getName());
        EntityMetaData existingEntityMetaData = repository.getEntityMetaData();
        List<AttributeMetaData> addedAttributes = Lists.newArrayList();

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
                // TODO: use decorated repository!
                metaDataRepositories.addAttributeMetaData(sourceEntityMetaData.getName(), attr);
                DefaultEntityMetaData defaultEntityMetaData = (DefaultEntityMetaData) repository.getEntityMetaData();
                defaultEntityMetaData.addAttributeMetaData(attr);
                if (attr.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
                {
                    for (AttributeMetaData attrPart : attr.getAttributeParts())
                    {
                        repository.addAttributeSync(attrPart);
                    }
                }
                else
                {
                    repository.addAttributeSync(attr);
                }
                addedAttributes.add(attr);
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
	private CrudRepository getDecoratedRepository(CrudRepository repository)
	{
		return repositoryDecoratorFactory != null ? (CrudRepository) repositoryDecoratorFactory
				.createDecoratedRepository(repository) : repository;
	}

	@Override
	public Iterator<CrudRepository> iterator()
	{
		return Iterators.transform(repositories.values().iterator(), new Function<MysqlRepository, CrudRepository>()
		{
			@Override
			public CrudRepository apply(MysqlRepository input)
			{
				return input;
			}
		});
	}

}