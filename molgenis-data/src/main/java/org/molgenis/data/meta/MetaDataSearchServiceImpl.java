package org.molgenis.data.meta;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class MetaDataSearchServiceImpl implements MetaDataSearchService
{
	private final DataService dataService;
	private final MetaDataService metaDataService;

	@Autowired
	public MetaDataSearchServiceImpl(DataService dataService, MetaDataService metaDataService)
	{
		this.dataService = dataService;
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
			for (Entity packageEntity : dataService.findAll(PackageMetaData.ENTITY_NAME, q))
			{
				Package p = metaDataService.getPackage(packageEntity.getString(PackageMetaData.FULL_NAME));
				if ((p != null) && (p.getParent() == null))
				{
					String matchDesc = "Matched: package '" + p.getName() + "'";
					results.add(new PackageSearchResultItem(p, matchDesc));
				}
			}

			// Search in entities
			for (Entity entityMetaData : dataService.findAll(EntityMetaDataMetaData.ENTITY_NAME, q))
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

			// TODO: Search in attributes?
			// This used to be necessary when entities didn't yet have an mref to attributes.
			// Now that they do, the attribute and all of its properties are listed in the entity document in
			// elasticsearch and will be found in the above query.
			// But the match does not know any longer which attribute it has matched, though, and won't be able to
			// find tags of attributes of the entity when we implement them.
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

}
