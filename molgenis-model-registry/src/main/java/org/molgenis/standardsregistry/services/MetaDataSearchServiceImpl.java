package org.molgenis.standardsregistry.services;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.standardsregistry.model.PackageSearchResultItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ListIterator;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

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
	@RunAsSystem
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
			Query<Package> packageQuery = new QueryImpl<Package>().search(searchTerm);
			dataService.findAll(PACKAGE, packageQuery, Package.class).forEach(packageEntity ->
			{
				if ((packageEntity != null) && (packageEntity.getParent() == null))
				{
					String matchDesc = "Matched: package '" + packageEntity.getId() + "'";
					results.add(new PackageSearchResultItem(packageEntity, matchDesc));
				}
			});

			// Search in entities
			Query<EntityType> entityTypeQuery = new QueryImpl<EntityType>().search(searchTerm);

			dataService.findAll(ENTITY_TYPE_META_DATA, entityTypeQuery, EntityType.class).forEach(entityType ->
			{
				Package p = getRootPackage(entityType);
				if (p != null)
				{
					String matchDesc = "Matched: entity '" + entityType.getString(EntityTypeMetadata.ID) + "'";
					PackageSearchResultItem item = new PackageSearchResultItem(p.getRootPackage(), matchDesc);
					if ((p != null) && !results.contains(item)) results.add(item);
				}
			});

			// Search in attributes no longer needed since the entities contain the attribute documents.
			// Change this if the searching for tags becomes needed and/or the results need to reflect which attribute
			// got matched.
		}

		// Remove default package
		ListIterator<PackageSearchResultItem> it = results.listIterator();
		while (it.hasNext())
		{
			PackageSearchResultItem item = it.next();
			if (item.getPackageFound().getId().equals("default"))
			{
				it.remove();
			}
		}

		return results;
	}

	// Get the root package of an entity
	private Package getRootPackage(EntityType entityType)
	{
		Package packageEntity = entityType.getEntity(EntityTypeMetadata.PACKAGE, Package.class);

		if (packageEntity != null)
		{
			String packageName = packageEntity.getId();
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
