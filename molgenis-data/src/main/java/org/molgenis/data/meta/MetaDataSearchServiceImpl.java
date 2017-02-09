package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
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
			Query<Entity> q = new QueryImpl<>().search(searchTerm);
			// for (Entity packageEntity : dataService.findAllAsIterable(PackageMetadata.GROUP_MEMBER, q))
			dataService.findAll(PACKAGE, q).forEach(packageEntity ->
			{
				Package p = metaDataService.getPackage(packageEntity.getString(PackageMetadata.FULL_NAME));
				if ((p != null) && (p.getParent() == null))
				{
					String matchDesc = "Matched: package '" + p.getFullyQualifiedName() + "'";
					results.add(new PackageSearchResultItem(p, matchDesc));
				}
			});

			// Search in entities
			dataService.findAll(ENTITY_TYPE_META_DATA, q).forEach(EntityType ->
			{
				Package p = getRootPackage(EntityType);
				if (p != null)
				{
					String matchDesc =
							"Matched: entity '" + EntityType.getString(EntityTypeMetadata.SIMPLE_NAME) + "'";
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
			if (item.getPackageFound().getFullyQualifiedName().equals("default"))
			{
				it.remove();
			}
		}

		return results;
	}

	// Get the root package of an entity
	private Package getRootPackage(Entity EntityType)
	{
		Entity packageEntity = EntityType.getEntity(EntityTypeMetadata.PACKAGE);
		if (packageEntity != null)
		{
			String packageName = packageEntity.getString(PackageMetadata.FULL_NAME);
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
