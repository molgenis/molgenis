package org.molgenis.data.mysql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public abstract class MysqlRepositoryCollection implements ManageableCrudRepositoryCollection
{
	public static final String NAME = "MySQL";
	private final DataSource ds;
	final private Map<String, MysqlRepository> repositories = new LinkedHashMap<String, MysqlRepository>();
	// temporary workaround for module dependencies
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	public MysqlRepositoryCollection(DataSource ds)
	{
		this(ds, null);
	}

	public MysqlRepositoryCollection(DataSource ds, RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.ds = ds;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	public DataSource getDataSource()
	{
		return ds;
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract MysqlRepository createMysqlRepository();

	@Override
	@Transactional
	public CrudRepository addEntityMeta(EntityMetaData emd)
	{
		MysqlRepository repository = createMysqlRepository();
		repository.setMetaData(emd);
		repository.create();
		repositories.put(emd.getName(), repository);

		return getDecoratedRepository(repository);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public CrudRepository getCrudRepository(String name)
	{
		MysqlRepository repo = repositories.get(name);
		if (repo == null)
		{
			return null;
		}

		return getDecoratedRepository(repo);
	}

	@Override
	public Repository getRepository(String name)
	{
		return getCrudRepository(name);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		MysqlRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.addAttribute(attribute);
		}
	}

	public MysqlRepository getUndecoratedRepository(String name)
	{
		return repositories.get(name);
	}

	public void drop(EntityMetaData md)
	{
		assert md != null;
		deleteEntityMeta(md.getName());
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		// remove the repo
		MysqlRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.drop();
			repositories.remove(entityName);
		}
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		MysqlRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.dropAttribute(attributeName);
		}
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		MysqlRepository repository = repositories.get(entityName);
		repository.addAttributeSync(attribute);
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