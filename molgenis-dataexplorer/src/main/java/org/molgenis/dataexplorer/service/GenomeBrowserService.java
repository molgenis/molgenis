package org.molgenis.dataexplorer.service;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.GenomicDataSettings;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Service implements genomeBrowser specific business logic.
 */
@Component
public class GenomeBrowserService
{

	private DataService dataService;

	private GenomicDataSettings genomicDataSettings;

	public GenomeBrowserService(DataService dataService, GenomicDataSettings genomicDataSettings)
	{
		this.dataService = requireNonNull(dataService);
		this.genomicDataSettings = requireNonNull(genomicDataSettings);
	}

	//TODO Improve performance by rewriting to query that returns all genomic entities instead of retrieving all entities and determining which one is genomic

	/**
	 * Fetch all non abstract genomeBrowser entities
	 * these are defined as being non abstract and having a ATTRS_POS and ATTRS_CHROM attribute.
	 *
	 * @return from entity name to entityLabel
	 */
	public Stream<EntityType> getGenomeBrowserEntities()
	{
		return dataService.getMeta()
						  .getEntityTypes()
						  .filter(entityType -> !entityType.isAbstract())
						  .filter(this::isGenomeBrowserEntity);
	}

	private boolean isGenomeBrowserEntity(EntityType entityType)
	{
		Attribute attributeStartPosition = genomicDataSettings.getAttributeMetadataForAttributeNameArray(
				GenomicDataSettings.Meta.ATTRS_POS, entityType);
		Attribute attributeChromosome = genomicDataSettings.getAttributeMetadataForAttributeNameArray(
				GenomicDataSettings.Meta.ATTRS_CHROM, entityType);
		return attributeStartPosition != null && attributeChromosome != null;
	}
}
