package org.molgenis.data.annotation.entity.impl;

import java.util.Collections;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.MultiFileResource;
import org.molgenis.data.annotation.resources.impl.MultiResourceConfigImpl;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThousandGenomeAnnotator
{
	// TODO Write test
	public static final String THOUSAND_GENOME_AF = "1000G_AF";
	public static final String THOUSAND_GENOME_AF_LABEL = "Thousand genome allele frequency";
	public static final String THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME = VcfRepository.getInfoPrefix() + "AF";
	public static final String THOUSAND_GENOME_CHROMOSOME_PROPERTY = "thousand_genome_chromosomes";
	public static final String THOUSAND_GENOME_FILE_PATTERN_PROPERTY = "thousand_genome_file_pattern";
	public static final String THOUSAND_GENOME_FOLDER_PROPERTY = "thousand_genome_root_directory";
	public static final String THOUSAND_GENOME_MULTI_FILE_RESOURCE = "thousandGenomeResources";

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator thousandGenome()
	{
		DefaultAttributeMetaData outputAttribute = new DefaultAttributeMetaData(THOUSAND_GENOME_AF,
				FieldTypeEnum.STRING).setDescription(
				"The allele frequency for variants seen in the population used for the thousand genomes project")
				.setLabel(THOUSAND_GENOME_AF_LABEL);

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo
				.create(Status.BETA,
						AnnotatorInfo.Type.POPULATION_REFERENCE,
						"Thousand Genomes",
						"The 1000 Genomes Project is an international collaboration to produce an "
								+ "extensive public catalog of human genetic variation, including SNPs and structural variants, "
								+ "and their haplotype contexts. This resource will support genome-wide association studies and other "
								+ "medical research studies. "
								+ "The genomes of about 2500 unidentified people from about 25 populations around the world will be"
								+ "sequenced using next-generation sequencing technologies. "
								+ "The results of the study will be freely and publicly accessible to researchers worldwide. "
								+ "Further information about the project is available in the About tab. Information about downloading, "
								+ "browsing or using the 1000 Genomes data is available in the Data tab.",
						Collections.singletonList(outputAttribute));

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();

		// TODO: properly test multiAllelicFresultFilter
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData(THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME,
						FieldTypeEnum.DECIMAL)));
		EntityAnnotator entityAnnotator = new AnnotatorImpl(THOUSAND_GENOME_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, multiAllelicResultFilter, dataService, resources)
		{
			@Override
			protected String getResourceAttributeName(AttributeMetaData attr)
			{
				if (THOUSAND_GENOME_AF.equals(attr.getName()))
				{
					return THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME;
				}
				return attr.getName();
			}
		};

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource thousandGenomeSources()
	{
		MultiResourceConfig thousandGenomeConfig = new MultiResourceConfigImpl(THOUSAND_GENOME_CHROMOSOME_PROPERTY,
				THOUSAND_GENOME_FILE_PATTERN_PROPERTY, THOUSAND_GENOME_FOLDER_PROPERTY, molgenisSettings);

		return new MultiFileResource(THOUSAND_GENOME_MULTI_FILE_RESOURCE, thousandGenomeConfig,
				new TabixVcfRepositoryFactory(THOUSAND_GENOME_MULTI_FILE_RESOURCE));
	}
}
