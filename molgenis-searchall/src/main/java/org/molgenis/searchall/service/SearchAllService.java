package org.molgenis.searchall.service;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.searchall.model.AttributeResult;
import org.molgenis.searchall.model.EntityTypeResult;
import org.molgenis.searchall.model.PackageResult;
import org.molgenis.searchall.model.Result;
import org.molgenis.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

@Component
public class SearchAllService
{
	private final DataService dataService;
	private final LanguageService languageService;

	public SearchAllService(DataService dataService, LanguageService languageService)
	{
		this.dataService = requireNonNull(dataService);
		this.languageService = requireNonNull(languageService);
	}

	public Result searchAll(String searchterm)
	{
		final String lang = languageService.getCurrentUserLanguageCode();
		return Result.builder()
					 .setEntityTypes(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)
												.filter(not(EntityUtils::isSystemEntity))
												.map(entityType -> toEntityTypeResult(searchterm, entityType, lang))
												.filter(EntityTypeResult::isMatch)
												.collect(toList()))
					 .setPackages(dataService.findAll(PACKAGE, Package.class)
											 .filter(not(EntityUtils::isSystemPackage))
											 .map(PackageResult::create)
											 .filter(packageResult -> packageResult.isLabelOrDescriptionMatch(
													 searchterm))
											 .collect(toList()))
					 .build();
	}

	private EntityTypeResult toEntityTypeResult(String searchterm, EntityType entityType, String lang)
	{
		return EntityTypeResult.builder()
							   .setId(entityType.getId())
							   .setLabel(entityType.getLabel(lang))
							   .setDescription(entityType.getDescription(lang))
							   .setPackageId(entityType.getPackage().getId())
							   .setLabelMatch(containsIgnoreCase(entityType.getLabel(lang), searchterm))
							   .setDescriptionMatch(containsIgnoreCase(entityType.getDescription(lang), searchterm))
							   .setAttributes(matchingAttributes(searchterm, entityType.getAllAttributes(), lang))
							   .setNrOfMatchingEntities(
									   dataService.count(entityType.getId(), new QueryImpl<>().search(searchterm)))
							   .build();
	}

	private List<AttributeResult> matchingAttributes(String searchterm, Iterable<Attribute> allAttributes, String lang)
	{
		return stream(allAttributes).map(attribute -> AttributeResult.create(attribute, lang))
									.filter(attributeResult -> attributeResult.isLabelOrDescriptionMatch(searchterm))
									.collect(toList());
	}

}