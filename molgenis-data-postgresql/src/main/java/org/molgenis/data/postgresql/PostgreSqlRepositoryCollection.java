package org.molgenis.data.postgresql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public abstract class PostgreSqlRepositoryCollection implements ManageableRepositoryCollection
{
	public static final String NAME = "PostgreSQL";
	private final Map<String, PostgreSqlRepository> repositories = new LinkedHashMap<>();

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		PostgreSqlRepository repository = createPostgreSqlRepository();
		repository.setMetaData(entityMeta);
		repository.create();
		repositories.put(entityMeta.getName(), repository);

		return repository;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return Iterators.transform(repositories.values().iterator(), new Function<PostgreSqlRepository, Repository>()
		{
			@Override
			public Repository apply(PostgreSqlRepository repo)
			{
				return repo;
			}
		});
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		PostgreSqlRepository repo = repositories.get(entityName);
		if (repo != null)
		{
			repo.drop();
			repositories.remove(entityName);
		}
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		PostgreSqlRepository repo = repositories.get(entityName);
		if (repo == null)
		{
			throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		}
		repo.addAttribute(attribute);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		PostgreSqlRepository repo = repositories.get(entityName);
		if (repo == null)
		{
			throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		}
		repo.dropAttribute(attributeName);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		PostgreSqlRepository repo = repositories.get(entityName);
		if (repo == null)
		{
			throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		}
		repo.addAttribute(attribute);
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract PostgreSqlRepository createPostgreSqlRepository();
}