package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
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
			// for (Entity packageEntity : dataService.findAllAsIterable(PackageMetadata.GROUP_MEMBER, q))
			dataService.findAll(PACKAGE, packageQuery, Package.class).forEach(p ->
			{
				if ((p != null) && (p.getParent() == null))
				{
					String matchDesc = "Matched: package '" + p.getName() + "'";
					results.add(new PackageSearchResultItem(p, matchDesc));
				}
			});

			// Search in entities
			Query<EntityType> entityTypeQuery = new QueryImpl<EntityType>().search(searchTerm);
			dataService.findAll(ENTITY_TYPE_META_DATA, entityTypeQuery, EntityType.class).forEach(EntityType ->
			{
				Package p = getRootPackage(EntityType);
				if (p != null)
				{
					String matchDesc = "Matched: entity '" + EntityType.getString(EntityTypeMetadata.SIMPLE_NAME) + "'";
					PackageSearchResultItem item = new PackageSearchResultItem(p.getRootPackage(), matchDesc);
					if (!results.contains(item)) results.add(item);
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
			if (item.getPackageFound().getName().equals("default"))
			{
				it.remove();
			}
		}

		return results;
	}

	// Get the root package of an entity
	private Package getRootPackage(EntityType entityType)
	{
		Package package_ = entityType.getPackage();
		if (package_ != null)
		{
			return package_.getRootPackage();
		}
		return null;
	}
}
