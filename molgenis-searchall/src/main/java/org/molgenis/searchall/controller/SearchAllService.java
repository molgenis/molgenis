package org.molgenis.searchall.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.searchall.model.AttributeResult;
import org.molgenis.searchall.model.EntityTypeResult;
import org.molgenis.searchall.model.PackageResult;
import org.molgenis.searchall.model.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class SearchAllService
{
	private final DataService dataService;

	public SearchAllService(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public Result searchAll(String searchterm)
	{
		List<PackageResult> packages = dataService.findAll(PackageMetadata.PACKAGE, Package.class)
												  .filter(aPackage -> !isSystemPackage(aPackage))
												  .filter(aPackage -> isMatchingMetadata(aPackage, searchterm))
												  .map(pack -> PackageResult.create(pack.getId(), pack.getLabel(),
														  pack.getDescription()))
												  .collect(toList());
		List<EntityTypeResult> entityTypeMatches = new ArrayList<>();

		Stream<EntityType> entityTypeStream = dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA,
				EntityType.class).filter(entityType -> !isSystemEntity(entityType));

		entityTypeStream.forEach(entityType -> searchSingleEntityType(searchterm, entityTypeMatches, entityType));

		return Result.create(entityTypeMatches, packages);
	}

	private void searchSingleEntityType(String searchterm, List<EntityTypeResult> entityTypeMatches,
			EntityType entityType)
	{
		boolean isLabelMatch = entityType.getLabelValue().toString().contains(searchterm);
		boolean isDescMatch = entityType.getString(DESCRIPTION) != null ? entityType.getString(DESCRIPTION)
																					.contains(searchterm) : false;
		List<AttributeResult> matchingAttributes = StreamSupport.stream(entityType.getAllAttributes().spliterator(),
				false)
																.filter(attribute -> isMatchingMetadata(attribute,
																		searchterm))
																.map(attr -> AttributeResult.create(attr.getLabel(),
																		attr.getDescription(),
																		attr.getDataType().toString()))
																.collect(toList());
		long nrOfMatchingEntities = dataService.count(entityType.getId().toString(),
				new QueryImpl<>().search(searchterm));
		if (isLabelMatch || isDescMatch || matchingAttributes.size() > 0 || nrOfMatchingEntities > 0)
		{
			entityTypeMatches.add(
					EntityTypeResult.create(entityType.getId(), entityType.getLabel(), entityType.getDescription(),
							entityType.getPackage().getId(), isLabelMatch, isDescMatch, matchingAttributes,
							nrOfMatchingEntities));
		}
	}

	private boolean isMatchingMetadata(Entity metadataEntity, String term)
	{
		boolean isLabelMatch = metadataEntity.getLabelValue().toString().contains(term);
		boolean isDescMatch = metadataEntity.getString(DESCRIPTION) != null ? metadataEntity.getString(DESCRIPTION)
																							.contains(term) : false;
		return isDescMatch || isLabelMatch;
	}

	private static boolean isSystemEntity(EntityType entityType)
	{
		return isSystemPackage(entityType.getPackage());
	}

	private static boolean isSystemPackage(Package package_)
	{
		if (package_ == null)
		{
			return false;
		}
		if (package_.getId().equals(PACKAGE_SYSTEM))
		{
			return true;
		}
		Package rootPackage = package_.getRootPackage();
		return rootPackage != null && rootPackage.getId().equals(PACKAGE_SYSTEM);
	}
}
