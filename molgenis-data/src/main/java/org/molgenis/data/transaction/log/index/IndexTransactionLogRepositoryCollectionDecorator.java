package org.molgenis.data.transaction.log.index;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.transaction.log.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.log.index.IndexTransactionLogEntryMetaData.DataType;

public class IndexTransactionLogRepositoryCollectionDecorator implements ManageableRepositoryCollection
{
	private final ManageableRepositoryCollection decorated;
	private final IndexTransactionLogService indexTransactionLogService;

	public IndexTransactionLogRepositoryCollectionDecorator(ManageableRepositoryCollection decorated,
			IndexTransactionLogService indexTransactionLogService)
	{
		this.decorated = requireNonNull(decorated);
		this.indexTransactionLogService = requireNonNull(indexTransactionLogService);
	}

	@Override
	public String getName()
	{
		return this.decorated.getName();
	}

	public boolean hasRepository(String name)
	{
		return this.decorated.hasRepository(name);
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		this.indexTransactionLogService.log(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.DELETE, DataType.METADATA, null);
		this.decorated.deleteEntityMeta(entityName);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		this.indexTransactionLogService.log(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.UPDATE,
				DataType.METADATA, null);
		this.decorated.addAttribute(entityName, attribute);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		this.indexTransactionLogService.log(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.UPDATE, DataType.METADATA, null);
		this.decorated.deleteAttribute(entityName, attributeName);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		this.indexTransactionLogService.log(this.decorated.getRepository(entityName).getEntityMetaData(),
				CudType.UPDATE,
				DataType.METADATA, null);
		this.decorated.addAttribute(entityName, attribute);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
	{
		this.indexTransactionLogService.log(entityMeta, CudType.ADD,
				DataType.METADATA, null);
		return this.decorated.addEntityMeta(entityMeta);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return this.decorated.getEntityNames();
	}

	@Override
	public Repository getRepository(String name)
	{
		return this.decorated.getRepository(name);
	}
}
