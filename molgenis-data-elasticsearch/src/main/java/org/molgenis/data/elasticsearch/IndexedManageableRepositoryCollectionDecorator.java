package org.molgenis.data.elasticsearch;

import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

/**
 * Decorates a ManageableRepositoryCollection so it is indexed. Removes, creates ES mappings
 */
public class IndexedManageableRepositoryCollectionDecorator extends IndexedRepositoryCollectionDecorator implements
		ManageableRepositoryCollection
{

	public IndexedManageableRepositoryCollectionDecorator(SearchService searchService,
			ManageableRepositoryCollection delegate)
	{
		super(searchService, delegate);
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		getManageableRepositoryCollection().deleteEntityMeta(entityName);
		getSearchService().delete(entityName);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		getManageableRepositoryCollection().addAttribute(entityName, attribute);
		EntityMetaData meta = new EntityMetaData(getManageableRepositoryCollection().getRepository(entityName).getEntityMetaData());
		meta.addAttribute(attribute);
		getSearchService().createMappings(meta);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		getManageableRepositoryCollection().deleteAttribute(entityName, attributeName);

		EntityMetaData meta = new EntityMetaData(getManageableRepositoryCollection().getRepository(entityName).getEntityMetaData());

		AttributeMetaData attr = meta.getAttribute(attributeName);
		if (attr != null)
		{
			meta.removeAttribute(attr);
			getSearchService().createMappings(meta);
		}
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		getManageableRepositoryCollection().addAttributeSync(entityName, attribute);
		EntityMetaData meta = new EntityMetaData(getManageableRepositoryCollection().getRepository(entityName).getEntityMetaData());
		meta.addAttribute(attribute);
		getSearchService().createMappings(meta);
	}

	protected ManageableRepositoryCollection getManageableRepositoryCollection()
	{
		return (ManageableRepositoryCollection) getDelegate();
	}
}
