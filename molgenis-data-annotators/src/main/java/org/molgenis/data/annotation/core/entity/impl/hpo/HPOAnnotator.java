package org.molgenis.data.annotation.core.entity.impl.hpo;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AbstractAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.data.annotation.web.settings.HPOAnnotatorSettings.Meta.HPO_LOCATION;
import static org.molgenis.data.meta.AttributeType.TEXT;

/**
 * Typical HPO terms for a gene dataType (already present via SnpEff) Source:
 * http://compbio.charite.de/hudson/job/hpo. annotations.monthly/lastStableBuild/artifact/annotation/
 * ALL_SOURCES_TYPICAL_FEATURES_diseases_to_genes_to_phenotypes .txt
 * <p>
 * Add resource file path to RuntimeProperty 'hpo_location'
 */
@Configuration
public class HPOAnnotator implements AnnotatorConfig
{
	public static final String NAME = "hpo";

	public static final String HPO_IDS = "HPOIDS";
	public static final String HPO_TERMS = "HPOTERMS";

	private static final String HPO_RESOURCE = "HPOResource";

	@Autowired
	private Entity HPOAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	GeneNameQueryCreator geneNameQueryCreator;

	@Autowired
	private AttributeFactory attributeFactory;
	private RepositoryAnnotatorImpl annotator;

	@Bean
	public RepositoryAnnotator hpo()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	public Attribute getIdsAttr()
	{
		return attributeFactory.create().setName(HPO_IDS).setDataType(TEXT).setDescription("HPO identifiers");
	}

	public Attribute getTermsAttr()
	{
		return attributeFactory.create().setName(HPO_TERMS).setDataType(TEXT).setDescription("HPO terms");
	}

	@Override
	public void init()
	{
		List<Attribute> attributes = createHpoOutputAttributes();

		AnnotatorInfo info = AnnotatorInfo.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.PHENOTYPE_ASSOCIATION,
				NAME,
				"The Human Phenotype Ontology (HPO) aims to provide a standardized vocabulary of phenotypic abnormalities encountered in human disease."
						+ "Terms in the HPO describes a phenotypic abnormality, such as atrial septal defect.The HPO is currently being developed using the medical literature, Orphanet, DECIPHER, and OMIM. HPO currently contains approximately 11,000 terms and over 115,000 annotations to hereditary diseases."
						+ "Please note that if SnpEff was used to annotate in order to add the gene symbols to the variants, than this annotator should be used on the result entity rather than the variant entity itself.",
				attributes);

		EntityAnnotator entityAnnotator = new AbstractAnnotator(HPO_RESOURCE, info, geneNameQueryCreator,
				new HpoResultFilter(entityTypeFactory, this), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(HPO_LOCATION, HPOAnnotatorSettings))
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createHpoOutputAttributes();
			}
		};

		annotator.init(entityAnnotator);
	}

	private List<Attribute> createHpoOutputAttributes()
	{
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(getIdsAttr());
		attributes.add(getTermsAttr());
		return attributes;
	}

	@Bean
	public Resource hpoResource()
	{
		return new ResourceImpl(HPO_RESOURCE, new SingleResourceConfig(HPO_LOCATION, HPOAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return file -> new HPORepository(file, entityTypeFactory, attributeFactory);
			}
		};
	}
}
