package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AbstractAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.core.query.LocusQueryCreator;
import org.molgenis.data.annotation.core.resources.MultiResourceConfig;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.MultiFileResource;
import org.molgenis.data.annotation.core.resources.impl.MultiResourceConfigImpl;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixVcfRepositoryFactory;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.molgenis.data.annotation.web.settings.ThousendGenomesAnnotatorSettings.Meta.*;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.STRING;

@Configuration
public class ThousandGenomesAnnotator implements AnnotatorConfig
{
	public static final String NAME = "thousandgenomes";

	public static final String THOUSAND_GENOME_AF = "Thousand_Genomes_AF";
	public static final String THOUSAND_GENOME_AF_LABEL = "Thousand genome allele frequency";
	public static final String THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME = "AF";
	public static final String THOUSAND_GENOME_MULTI_FILE_RESOURCE = "thousandGenomesSources";

	@Autowired
	private Entity thousendGenomesAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	private RepositoryAnnotatorImpl annotator;

	@Bean
	public RepositoryAnnotator thousandGenomes()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<Attribute> attributes = createThousandGenomesOutputAttributes();

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.POPULATION_REFERENCE,
				NAME, "The 1000 Genomes Project is an international collaboration to produce an "
						+ "extensive public catalog of human genetic variation, including SNPs and structural variants, "
						+ "and their haplotype contexts. This resource will support genome-wide association studies and other "
						+ "medical research studies. "
						+ "The genomes of about 2500 unidentified people from about 25 populations around the world will be"
						+ "sequenced using next-generation sequencing technologies. "
						+ "The results of the study will be freely and publicly accessible to researchers worldwide. "
						+ "Further information about the project is available in the About tab. Information about downloading, "
						+ "browsing or using the 1000 Genomes data is available at: http://www.1000genomes.org/ ",
				attributes);

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator(vcfAttributes);

		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(singletonList(
				attributeFactory.create().setName(THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME).setDataType(DECIMAL)),
				vcfAttributes);

		EntityAnnotator entityAnnotator = new AbstractAnnotator(THOUSAND_GENOME_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, multiAllelicResultFilter, dataService, resources, (annotationSourceFileName) ->
		{
			thousendGenomesAnnotatorSettings.set(ROOT_DIRECTORY, annotationSourceFileName);
			thousendGenomesAnnotatorSettings.set(FILEPATTERN,
					"ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
			thousendGenomesAnnotatorSettings.set(CHROMOSOMES,
					"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22");
		})
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createThousandGenomesOutputAttributes();
			}

			@Override
			protected Object getResourceAttributeValue(Attribute attr, Entity entityType)
			{
				String attrName = THOUSAND_GENOME_AF.equals(
						attr.getName()) ? THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME : attr.getName();
				return entityType.get(attrName);
			}
		};

		annotator.init(entityAnnotator);
	}

	private List<Attribute> createThousandGenomesOutputAttributes()
	{
		return singletonList(attributeFactory.create()
											 .setName(THOUSAND_GENOME_AF)
											 .setDataType(STRING)
											 .setDescription(
													 "The allele frequency for variants seen in the population used for the thousand genomes project")
											 .setLabel(THOUSAND_GENOME_AF_LABEL));
	}

	@Bean
	Resource thousandGenomesSources()
	{
		MultiResourceConfig thousandGenomeConfig = new MultiResourceConfigImpl(CHROMOSOMES, FILEPATTERN, ROOT_DIRECTORY,
				OVERRIDE_CHROMOSOME_FILES, thousendGenomesAnnotatorSettings);

		return new MultiFileResource(THOUSAND_GENOME_MULTI_FILE_RESOURCE, thousandGenomeConfig)
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new TabixVcfRepositoryFactory(THOUSAND_GENOME_MULTI_FILE_RESOURCE, vcfAttributes,
						entityTypeFactory, attributeFactory);
			}
		};
	}
}
