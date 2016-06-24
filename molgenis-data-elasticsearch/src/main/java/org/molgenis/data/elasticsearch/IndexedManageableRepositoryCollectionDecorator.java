package org.molgenis.data.elasticsearch;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;

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
		DefaultEntityMetaData meta = new DefaultEntityMetaData(getManageableRepositoryCollection().getRepository(
				entityName).getEntityMetaData());
		meta.addAttributeMetaData(attribute);
		getSearchService().createMappings(meta);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		getManageableRepositoryCollection().deleteAttribute(entityName, attributeName);

		DefaultEntityMetaData meta = new DefaultEntityMetaData(getManageableRepositoryCollection().getRepository(
				entityName).getEntityMetaData());

		AttributeMetaData attr = meta.getAttribute(attributeName);
		if (attr != null)
		{
			meta.removeAttributeMetaData(attr);
			getSearchService().createMappings(meta);
		}
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		getManageableRepositoryCollection().addAttributeSync(entityName, attribute);
		DefaultEntityMetaData meta = new DefaultEntityMetaData(getManageableRepositoryCollection().getRepository(
				entityName).getEntityMetaData());
		meta.addAttributeMetaData(attribute);
		getSearchService().createMappings(meta);
	}

	protected ManageableRepositoryCollection getManageableRepositoryCollection()
	{
		return (ManageableRepositoryCollection) getDelegate();
	}
}
