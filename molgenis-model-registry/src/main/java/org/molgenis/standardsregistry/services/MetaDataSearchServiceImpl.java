package org.molgenis.standardsregistry.services;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.standardsregistry.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

@Service
public class MetaDataSearchServiceImpl implements MetaDataSearchService
{
	private final DataService dataService;
	private final MetaDataService metaDataService;
	private final TagService<LabeledResource, LabeledResource> tagService;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public MetaDataSearchServiceImpl(DataService dataService, MetaDataService metaDataService,
			TagService<LabeledResource, LabeledResource> tagService,
			MolgenisPermissionService molgenisPermissionService)
	{
		this.dataService = dataService;
		this.metaDataService = metaDataService;
		this.tagService = tagService;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@Override
	public PackageSearchResponse search(String searchQuery, int offSet, int number)
	{
		List<PackageResponse> packageResponses = Lists.newArrayList();

		List<PackageSearchResultItem> searchResults = findRootPackages(searchQuery);
		for (PackageSearchResultItem searchResult : searchResults)
		{
			Package p = searchResult.getPackageFound();
			List<StandardRegistryEntity> entitiesInPackageUnfiltered = getEntitiesInPackage(p.getId());
			List<StandardRegistryEntity> entitiesInPackageFiltered = Lists.newArrayList(
					entitiesInPackageUnfiltered.stream().filter(entity ->
					{
						if (entity.isAbtract())
						{
							return false;
						}

						String entityTypeId = entity.getName();

						// Check read permission
						if (!molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ))
						{
							return false;
						}

						// Check has data
						if (!dataService.hasRepository(entityTypeId)
								|| dataService.count(entityTypeId, new QueryImpl<>()) == 0)
						{
							return false;
						}

						return true;
					}).collect(Collectors.toList()));

			PackageResponse pr = new PackageResponse(p.getId(), p.getLabel(), p.getDescription(),
					searchResult.getMatchDescription(), entitiesInPackageFiltered, getTagsForPackage(p));
			packageResponses.add(pr);
		}

		int total = packageResponses.size();
		if (total > 0)
		{
			packageResponses = packageResponses.subList(offSet, packageResponses.size());

			if (packageResponses.size() > number)
			{
				packageResponses = packageResponses.subList(0, number);
			}
		}

		int offset = offSet;
		int num = number != 0 ? number : packageResponses.size();

		PackageSearchResponse packageSearchResponse = new PackageSearchResponse(searchQuery, offset, num, total, packageResponses);

		return packageSearchResponse;
	}

	@Override
	public List<StandardRegistryTag> getTagsForPackage(Package p)
	{
		List<StandardRegistryTag> tags = Lists.newArrayList();

		for (SemanticTag<Package, LabeledResource, LabeledResource> tag : tagService.getTagsForPackage(p))
		{
			tags.add(new StandardRegistryTag(tag.getObject().getLabel(), tag.getObject().getIri(),
					tag.getRelation().toString()));
		}

		return tags;
	}

	@Override
	public List<StandardRegistryEntity> getEntitiesInPackage(String packageName)
	{
		List<StandardRegistryEntity> entiesForThisPackage = new ArrayList<>();
		Package aPackage = metaDataService.getPackage(packageName);
		getEntitiesInPackageRec(aPackage, entiesForThisPackage);
		return entiesForThisPackage;
	}

	private void getEntitiesInPackageRec(Package aPackage, List<StandardRegistryEntity> entiesForThisPackage)
	{
		for (EntityType emd : aPackage.getEntityTypes())
		{
			entiesForThisPackage.add(new StandardRegistryEntity(emd.getId(), emd.getLabel(), emd.isAbstract()));
		}
		Iterable<Package> subPackages = aPackage.getChildren();
		if (subPackages != null)
		{
			for (Package subPackage : subPackages)
			{
				getEntitiesInPackageRec(subPackage, entiesForThisPackage);
			}
		}
	}

	private List<PackageSearchResultItem> findRootPackages(String searchTerm)
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
