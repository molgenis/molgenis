package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.data.annotator.websettings.ThousendGenomesAnnotatorSettings.Meta.CHROMOSOMES;
import static org.molgenis.data.annotator.websettings.ThousendGenomesAnnotatorSettings.Meta.FILEPATTERN;
import static org.molgenis.data.annotator.websettings.ThousendGenomesAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES;
import static org.molgenis.data.annotator.websettings.ThousendGenomesAnnotatorSettings.Meta.ROOT_DIRECTORY;

import java.util.Collections;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.MultiFileResource;
import org.molgenis.data.annotation.resources.impl.MultiResourceConfigImpl;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThousandGenomesAnnotator
{
	public static final String NAME = "thousand_genomes";

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

	@Bean
	public RepositoryAnnotator thousandGenomes()
	{
		DefaultAttributeMetaData outputAttribute = new DefaultAttributeMetaData(THOUSAND_GENOME_AF,
				FieldTypeEnum.STRING).setDescription(
				"The allele frequency for variants seen in the population used for the thousand genomes project")
				.setLabel(THOUSAND_GENOME_AF_LABEL);

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo
				.create(Status.READY,
						AnnotatorInfo.Type.POPULATION_REFERENCE,
						NAME,
						"The 1000 Genomes Project is an international collaboration to produce an "
								+ "extensive public catalog of human genetic variation, including SNPs and structural variants, "
								+ "and their haplotype contexts. This resource will support genome-wide association studies and other "
								+ "medical research studies. "
								+ "The genomes of about 2500 unidentified people from about 25 populations around the world will be"
								+ "sequenced using next-generation sequencing technologies. "
								+ "The results of the study will be freely and publicly accessible to researchers worldwide. "
								+ "Further information about the project is available in the About tab. Information about downloading, "
								+ "browsing or using the 1000 Genomes data is available at: http://www.1000genomes.org/ ",
						Collections.singletonList(outputAttribute));

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();

		// TODO: properly test multiAllelicResultFilter
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData(THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME,
						FieldTypeEnum.DECIMAL)));

		EntityAnnotator entityAnnotator = new AnnotatorImpl(THOUSAND_GENOME_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, multiAllelicResultFilter, dataService, resources, (annotationSourceFileName) -> {
					thousendGenomesAnnotatorSettings.set(ROOT_DIRECTORY, annotationSourceFileName);
					thousendGenomesAnnotatorSettings.set(FILEPATTERN,
							"ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
					thousendGenomesAnnotatorSettings.set(CHROMOSOMES,
							"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22");
				})
		{
			@Override
			protected Object getResourceAttributeValue(AttributeMetaData attr, Entity entity)
			{
				String attrName = THOUSAND_GENOME_AF.equals(attr.getName()) ? THOUSAND_GENOME_AF_RESOURCE_ATTRIBUTE_NAME : attr
						.getName();
				return entity.get(attrName);
			}
		};

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource thousandGenomesSources()
	{
		MultiResourceConfig thousandGenomeConfig = new MultiResourceConfigImpl(CHROMOSOMES, FILEPATTERN,
				ROOT_DIRECTORY, OVERRIDE_CHROMOSOME_FILES, thousendGenomesAnnotatorSettings);

		return new MultiFileResource(THOUSAND_GENOME_MULTI_FILE_RESOURCE, thousandGenomeConfig,
				new TabixVcfRepositoryFactory(THOUSAND_GENOME_MULTI_FILE_RESOURCE));
	}
}
