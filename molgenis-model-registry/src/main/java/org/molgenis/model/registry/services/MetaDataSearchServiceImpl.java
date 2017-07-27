package org.molgenis.model.registry.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.model.registry.model.*;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
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
	public ModelRegistrySearch search(String searchQuery, int offSet, int number)
	{
		List<ModelRegistryPackage> modelRegistryPackages = Lists.newArrayList();

		List<PackageSearchResultItem> searchResults = findRootPackages(searchQuery);
		for (PackageSearchResultItem searchResult : searchResults)
		{
			Package p = searchResult.getPackageFound();
			List<ModelRegistryEntity> entitiesInPackageUnfiltered = getEntitiesInPackage(p.getId());
			List<ModelRegistryEntity> entitiesInPackageFiltered = Lists.newArrayList(
					entitiesInPackageUnfiltered.stream().filter(entity ->
					{
						if (entity.isAbstract())
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

			modelRegistryPackages.add(ModelRegistryPackage.create(p.getId(), p.getLabel(), p.getDescription(),
					searchResult.getMatchDescription(), entitiesInPackageFiltered, getTagsForPackage(p)));
		}

		int total = modelRegistryPackages.size();
		if (total > 0)
		{
			modelRegistryPackages = modelRegistryPackages.subList(offSet, modelRegistryPackages.size());

			if (modelRegistryPackages.size() > number)
			{
				modelRegistryPackages = modelRegistryPackages.subList(0, number);
			}
		}

		int offset = offSet;
		int num = number != 0 ? number : modelRegistryPackages.size();

		return ModelRegistrySearch.create(searchQuery, offset, num, total, modelRegistryPackages);
	}

	@Override
	public List<ModelRegistryTag> getTagsForPackage(Package p)
	{
		return ImmutableList.copyOf(stream(tagService.getTagsForPackage(p).spliterator(), false).map(
				tag -> ModelRegistryTag.create(tag.getObject().getLabel(), tag.getObject().getIri(),
						tag.getRelation().toString())).iterator());
	}

	@Override
	public List<ModelRegistryEntity> getEntitiesInPackage(String packageName)
	{
		List<ModelRegistryEntity> entriesForThisPackage = new ArrayList<>();
		Package aPackage = metaDataService.getPackage(packageName);
		getEntitiesInPackageRec(aPackage, entriesForThisPackage);
		return entriesForThisPackage;
	}

	private void getEntitiesInPackageRec(Package pkg, List<ModelRegistryEntity> entriesForThisPackage)
	{
		entriesForThisPackage.addAll(ImmutableList.copyOf(stream(pkg.getEntityTypes().spliterator(), false).map(
				emd -> ModelRegistryEntity.create(emd.getId(), emd.getLabel(), emd.isAbstract())).iterator()));

		Iterable<Package> subPackages = pkg.getChildren();
		if (subPackages != null)
		{
			for (Package subPackage : subPackages)
			{
				getEntitiesInPackageRec(subPackage, entriesForThisPackage);
			}
		}
	}

	private List<PackageSearchResultItem> findRootPackages(String searchTerm)
	{
		List<PackageSearchResultItem> results = Lists.newArrayList();

		if (StringUtils.isBlank(searchTerm))
		{
			results.addAll(ImmutableList.copyOf(stream(metaDataService.getRootPackages().spliterator(), false).map(
					pkg -> new PackageSearchResultItem(pkg)).iterator()));
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
					if ((p != null) && !results.contains(item))
					{
						results.add(item);
					}
				}
			});

			// Search in attributes no longer needed since the entities contain the attribute documents.
			// Change this if the searching for tags becomes needed and/or the results need to reflect which attribute
			// got matched.
		}

		// Remove default package
		return ImmutableList.copyOf(results.stream()
										   .filter(item -> !item.getPackageFound().getId().equalsIgnoreCase("default"))
										   .iterator());
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
