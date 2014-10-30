package org.molgenis.data.meta;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Lists;

public class MetaDataSearchServiceImpl implements MetaDataSearchService
{
	private final ManageableCrudRepositoryCollection metaDataRepositoryCollection;
	private final MetaDataService metaDataService;

	public MetaDataSearchServiceImpl(ManageableCrudRepositoryCollection metaDataRepositoryCollection,
			MetaDataService metaDataService)
	{
		this.metaDataRepositoryCollection = metaDataRepositoryCollection;
		this.metaDataService = metaDataService;
	}

	@Override
	public List<PackageSearchResultItem> findRootPackages(String searchTerm)
	{
		List<PackageSearchResultItem> results = Lists.newArrayList();

		if (StringUtils.isBlank(searchTerm))
		{
			for (Package p : metaDataService.getRootPackages())
			{
				results.add(new PackageSearchResultItem(p));
			}
		}
		else
		{
			// Search in packages
			Query q = new QueryImpl().search(searchTerm);
			for (Entity packageEntity : getPackageRepo().findAll(q))
			{
				Package p = metaDataService.getPackage(packageEntity.getString(PackageMetaData.FULL_NAME));
				if ((p != null) && (p.getParent() == null))
				{
					String matchDesc = "Matched: package '" + p.getName() + "'";
					results.add(new PackageSearchResultItem(p, matchDesc));
				}
			}

			// Search in entities
			for (Entity entityMetaData : getEntityMetaDataRepo().findAll(q))
			{
				Package p = getRootPackage(entityMetaData);
				if (p != null)
				{
					String matchDesc = "Matched: entity '"
							+ entityMetaData.getString(EntityMetaDataMetaData.SIMPLE_NAME) + "'";
					PackageSearchResultItem item = new PackageSearchResultItem(p.getRootPackage(), matchDesc);
					if ((p != null) && !results.contains(item)) results.add(item);
				}
			}

			// Search in attributes
			for (Entity attributeMetaData : getAttributeMetaDataRepo().findAll(q))
			{
				Entity entityMetaData = attributeMetaData.getEntity(AttributeMetaDataMetaData.ENTITY);
				if (entityMetaData != null)
				{
					Package p = getRootPackage(entityMetaData);
					if (p != null)
					{
						String matchDesc = "Matched: attribute '"
								+ attributeMetaData.getString(AttributeMetaDataMetaData.NAME) + "' of entity '"
								+ entityMetaData.getString(EntityMetaDataMetaData.SIMPLE_NAME) + "'";
						PackageSearchResultItem item = new PackageSearchResultItem(p.getRootPackage(), matchDesc);
						if ((p != null) && !results.contains(item)) results.add(item);
					}
				}
			}
		}

		// Remove default package
		ListIterator<PackageSearchResultItem> it = results.listIterator();
		while (it.hasNext())
		{
			PackageSearchResultItem item = it.next();
			if (item.getPackageFound().getName().equals("default"))
			{
				it.remove();
			}
		}

		return results;
	}

	// Get the root package of an entity
	private Package getRootPackage(Entity entityMetaData)
	{
		Entity packageEntity = entityMetaData.getEntity(EntityMetaDataMetaData.PACKAGE);
		if (packageEntity != null)
		{
			String packageName = packageEntity.getString(PackageMetaData.FULL_NAME);
			if (packageName != null)
			{
				Package p = metaDataService.getPackage(packageName);
				if (p != null)
				{
					return p.getRootPackage();
				}
			}
		}

		return null;
	}

	private Queryable getPackageRepo()
	{
		return (Queryable) metaDataRepositoryCollection.getRepositoryByEntityName(PackageMetaData.ENTITY_NAME);
	}

	private Queryable getEntityMetaDataRepo()
	{
		return (Queryable) metaDataRepositoryCollection.getRepositoryByEntityName(EntityMetaDataMetaData.ENTITY_NAME);
	}

	private Queryable getAttributeMetaDataRepo()
	{
		return (Queryable) metaDataRepositoryCollection
				.getRepositoryByEntityName(AttributeMetaDataMetaData.ENTITY_NAME);
	}
}
