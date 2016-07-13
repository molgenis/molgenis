package org.molgenis.data.annotation.core.entity.impl.omim;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AnnotatorImpl;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.web.settings.OmimAnnotatorSettings;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.annotation.web.settings.OmimAnnotatorSettings.Meta.OMIM_LOCATION;

@Configuration
public class OmimAnnotator implements AnnotatorConfig
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

	@Autowired
	GeneNameQueryCreator geneNameQueryCreator;

	private RepositoryAnnotatorImpl annotator;

	public AttributeMetaData getPhenotypeAttr()
	{
		return attributeMetaDataFactory.create().setName(OMIM_DISORDER).setDataType(TEXT)
				.setDescription("OMIM phenotype").setLabel("OMIM_Disorders");
	}

	public AttributeMetaData getMimNumberAttr()
	{
		return attributeMetaDataFactory.create().setName(OMIM_CAUSAL_IDENTIFIER).setDataType(TEXT)
				.setDescription("Number that represents the MIM database dataType for the Locus / Gene")
				.setLabel("OMIM_Causal_ID");
	}

	public AttributeMetaData getEntryAttr()
	{
		return attributeMetaDataFactory.create().setName(OMIM_CYTO_LOCATIONS).setDataType(TEXT)
				.setDescription("Cytogenic location associated with an OMIM phenotype")
				.setLabel("OMIM_Cytogenic_Location");
	}

	public AttributeMetaData getTypeAttr()
	{
		return attributeMetaDataFactory.create().setName(OMIM_ENTRY).setDataType(TEXT)
				.setDescription("Number that represents the MIM database dataType for the phenotype")
				.setLabel("OMIM_Entry");
	}

	public AttributeMetaData getOmimLocationAttr()
	{
		return attributeMetaDataFactory.create().setName(OMIM_TYPE).setDataType(TEXT)
				.setDescription("Phenotype Mapping key: 1 - the disorder is placed on the map based on its "
						+ "association witha gene, but the underlying defect is not known. 2 - the disorder "
						+ "has been placed on the map by linkage or other statistical method; no mutation has "
						+ "been found. 3 - the molecular basis for the disorder is known; a mutation has been "
						+ "found in the gene. 4 - a contiguous gene deletion or duplication syndrome, multiple "
						+ "genes are deleted or duplicated causing the phenotype.").setLabel("OMIM_Type");
	}

	@Bean
	public RepositoryAnnotator omim()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<AttributeMetaData> outputAttributes = new ArrayList<>();
		outputAttributes.addAll(Arrays
				.asList(getPhenotypeAttr(), getMimNumberAttr(), getOmimLocationAttr(), getEntryAttr(), getTypeAttr()));

		AnnotatorInfo omimInfo = AnnotatorInfo
				.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.PHENOTYPE_ASSOCIATION, NAME,
						"OMIM is a comprehensive, authoritative compendium of human genes and genetic phenotypes that is "
								+ "freely available and updated daily. The full-text, referenced overviews in OMIM contain information on all "
								+ "known mendelian disorders and over 15,000 genes. OMIM focuses on the relationship between phenotype and genotype.",
						outputAttributes);

		EntityAnnotator entityAnnotator = new AnnotatorImpl(OMIM_RESOURCE, omimInfo, geneNameQueryCreator,
				new OmimResultFilter(entityMetaDataFactory, attributeMetaDataFactory, this), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(OMIM_LOCATION, omimAnnotatorSettings));

		annotator.init(entityAnnotator);
	}

	@Bean
	public Resource omimResource()
	{
		return new ResourceImpl(OMIM_RESOURCE, new SingleResourceConfig(OMIM_LOCATION, omimAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new RepositoryFactory()
				{
					@Override
					public Repository<Entity> createRepository(File file) throws IOException
					{
						return new OmimRepository(file, entityMetaDataFactory, attributeMetaDataFactory);
					}
				};
			}
		};

	}

}
