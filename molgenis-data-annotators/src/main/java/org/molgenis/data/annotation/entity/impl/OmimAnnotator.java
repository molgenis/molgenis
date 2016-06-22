package org.molgenis.data.annotation.entity.impl;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.AttributeMetaDataFactory;
import org.molgenis.data.meta.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.annotator.websettings.OmimAnnotatorSettings.Meta.OMIM_LOCATION;

@Configuration
public class OmimAnnotator
{
	public static final String NAME = "OMIM";
	public static final char SEPARATOR = '\t';

	public static final String OMIM_DISORDER = "OMIM_Disorders";
	public static final String OMIM_CAUSAL_IDENTIFIER = "OMIM_MIM_Numbers";
	public static final String OMIM_CYTO_LOCATIONS = "OMIM_Cyto_Locations";
	public static final String OMIM_ENTRY = "OMIM_Entry";
	public static final String OMIM_TYPE = "OMIM_Type";

	public static final String OMIM_RESOURCE = "OMIMResource";

	@Autowired
	private Entity omimAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Bean
	public RepositoryAnnotator omim()
	{
		List<AttributeMetaData> outputAttributes = new ArrayList<>();
		AttributeMetaData omim_phenotype = attributeMetaDataFactory.create().setName(OMIM_DISORDER).setDataType(TEXT)
				.setDescription("OMIM phenotype").setLabel("OMIM_Disorders");
		AttributeMetaData omim_mim_number = attributeMetaDataFactory.create().setName(OMIM_CAUSAL_IDENTIFIER)
				.setDataType(TEXT)
				.setDescription("Number that represents the MIM database dataType for the Locus / Gene")
				.setLabel("OMIM_Causal_ID");
		AttributeMetaData omim_cyto_location = attributeMetaDataFactory.create().setName(OMIM_CYTO_LOCATIONS)
				.setDataType(TEXT).setDescription("Cytogenic location associated with an OMIM phenotype")
				.setLabel("OMIM_Cytogenic_Location");
		AttributeMetaData omim_entry = attributeMetaDataFactory.create().setName(OMIM_ENTRY).setDataType(TEXT)
				.setDescription("Number that represents the MIM database dataType for the phenotype")
				.setLabel("OMIM_Entry");
		AttributeMetaData omim_type = attributeMetaDataFactory.create().setName(OMIM_TYPE).setDataType(TEXT)
				.setDescription("Phenotype Mapping key: 1 - the disorder is placed on the map based on its "
						+ "association witha gene, but the underlying defect is not known. 2 - the disorder "
						+ "has been placed on the map by linkage or other statistical method; no mutation has "
						+ "been found. 3 - the molecular basis for the disorder is known; a mutation has been "
						+ "found in the gene. 4 - a contiguous gene deletion or duplication syndrome, multiple "
						+ "genes are deleted or duplicated causing the phenotype.").setLabel("OMIM_Type");

		outputAttributes
				.addAll(Arrays.asList(omim_phenotype, omim_mim_number, omim_cyto_location, omim_entry, omim_type));

		AnnotatorInfo omimInfo = AnnotatorInfo
				.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.PHENOTYPE_ASSOCIATION, NAME,
						"OMIM is a comprehensive, authoritative compendium of human genes and genetic phenotypes that is "
								+ "freely available and updated daily. The full-text, referenced overviews in OMIM contain information on all "
								+ "known mendelian disorders and over 15,000 genes. OMIM focuses on the relationship between phenotype and genotype.",
						outputAttributes);

		EntityAnnotator entityAnnotator = new AnnotatorImpl(OMIM_RESOURCE, omimInfo, new GeneNameQueryCreator(),
				new OmimResultFilter(), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(OMIM_LOCATION, omimAnnotatorSettings));

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	public Resource omimResource()
	{
		return new ResourceImpl(OMIM_RESOURCE, new SingleResourceConfig(OMIM_LOCATION, omimAnnotatorSettings),
				new RepositoryFactory()
				{
					@Override
					public Repository<Entity> createRepository(File file) throws IOException
					{
						return new OmimRepository(file);
					}
				});
	}

	public static class OmimResultFilter implements ResultFilter
	{
		@Override
		public Collection<AttributeMetaData> getRequiredAttributes()
		{
			return Collections.emptyList();
		}

		@Override
		public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
		{
			Optional<Entity> firstResult = FluentIterable.from(results).first();
			return firstResult.transform(e -> {
				Entity result = new DynamicEntity(null); // FIXME pass entity meta data instead of null
				result.set(OMIM_DISORDER, e.get(OmimRepository.OMIM_PHENOTYPE_COL_NAME));
				result.set(OMIM_CAUSAL_IDENTIFIER, e.get(OmimRepository.OMIM_MIM_NUMBER_COL_NAME));
				result.set(OMIM_CYTO_LOCATIONS, e.get(OmimRepository.OMIM_CYTO_LOCATION_COL_NAME));
				result.set(OMIM_TYPE, e.get(OmimRepository.OMIM_TYPE_COL_NAME).toString());
				result.set(OMIM_ENTRY, e.get(OmimRepository.OMIM_ENTRY_COL_NAME).toString());

				return result;
			});

		}
	}

}
