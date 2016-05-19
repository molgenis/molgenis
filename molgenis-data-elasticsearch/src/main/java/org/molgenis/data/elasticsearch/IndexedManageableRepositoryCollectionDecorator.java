package org.molgenis.data.elasticsearch;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;

/**
 * Decorates a RepositoryCollection so it is indexed. Removes, creates ES mappings
 */
public class IndexedRepositoryCollectionDecorator extends IndexedRepositoryCollectionDecorator implements
		RepositoryCollection
{

	public IndexedRepositoryCollectionDecorator(SearchService searchService,
			RepositoryCollection delegate)
	{
		super(searchService, delegate);
	}

	@Override
	public void deleteRepository(String entityName)
	{
		getRepositoryCollection().deleteRepository(entityName);
		getSearchService().delete(entityName);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		getRepositoryCollection().addAttribute(entityName, attribute);
		EntityMetaData meta = new EntityMetaDataImpl(getRepositoryCollection().getRepository(entityName).getEntityMetaData());
		meta.addAttribute(attribute);
		getSearchService().createMappings(meta);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		getRepositoryCollection().deleteAttribute(entityName, attributeName);

		EntityMetaData meta = new EntityMetaDataImpl(getRepositoryCollection().getRepository(entityName).getEntityMetaData());

		AttributeMetaData attr = meta.getAttribute(attributeName);
		if (attr != null)
		{
			meta.removeAttribute(attr);
			getSearchService().createMappings(meta);
		}
	}

	protected RepositoryCollection getRepositoryCollection()
	{
		return (RepositoryCollection) getDelegate();
	}

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return hasRepository(entityMeta.getName());
	}
}
