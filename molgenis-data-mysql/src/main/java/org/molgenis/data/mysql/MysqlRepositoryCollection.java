package org.molgenis.data.mysql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultEntityMetaData;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public abstract class MysqlRepositoryCollection implements ManageableRepositoryCollection
{
	public static final String NAME = "MySQL";
	private final Map<String, MysqlRepository> repositories = new LinkedHashMap<>();

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		MysqlRepository repository = createMysqlRepository();
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
		return Iterators.transform(repositories.values().iterator(), new Function<MysqlRepository, Repository>()
		{
			@Override
			public Repository apply(MysqlRepository repo)
			{
				return repo;
			}
		});
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		MysqlRepository repo = repositories.get(entityName);
		if (repo == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		repo.drop();
		repositories.remove(entityName);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		MysqlRepository repo = repositories.get(entityName);
		if (repo == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		repo.addAttribute(attribute);
		DefaultEntityMetaData meta = new DefaultEntityMetaData(repo.getEntityMetaData());
		meta.addAttributeMetaData(attribute);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		MysqlRepository repo = repositories.get(entityName);
		if (repo == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		repo.dropAttribute(attributeName);
		DefaultEntityMetaData meta = new DefaultEntityMetaData(repo.getEntityMetaData());

		AttributeMetaData attr = meta.getAttribute(entityName);
		if (attr != null) meta.removeAttributeMetaData(attr);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		MysqlRepository repo = repositories.get(entityName);
		if (repo == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));
		repo.addAttributeSync(attribute);
		DefaultEntityMetaData meta = new DefaultEntityMetaData(repo.getEntityMetaData());
		meta.addAttributeMetaData(attribute);
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract MysqlRepository createMysqlRepository();

}