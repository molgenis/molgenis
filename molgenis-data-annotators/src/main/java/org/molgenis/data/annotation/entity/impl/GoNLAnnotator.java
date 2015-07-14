package org.molgenis.data.annotation.entity.impl;

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
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoNLAnnotator
{
	// TODO Write test
	public static final String GONL_GENOME_AF = "GoNL_AF";
	public static final String GONL_AF_LABEL = "Genome of the netherlands allele frequency";
	public static final String GONL_AF_RESOURCE_ATTRIBUTE_NAME = VcfRepository.getInfoPrefix() + "AF";
	public static final String GONL_CHROMOSOME_PROPERTY = "gonl_genome_chromosomes";
	public static final String GONL_FILE_PATTERN_PROPERTY = "gonl_genome_file_pattern";
	public static final String GONL_FOLDER_PROPERTY = "gonl_genome_root_directory";
	public static final String GONL_MULTI_FILE_RESOURCE = "gonlresources";

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator gonl()
	{
		DefaultAttributeMetaData outputAttribute = new DefaultAttributeMetaData(GONL_GENOME_AF, FieldTypeEnum.STRING)
				.setDescription("The allele frequency for variants seen in the population used for the GoNL project")
				.setLabel(GONL_AF_LABEL);

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo
				.create(Status.READY,
						AnnotatorInfo.Type.POPULATION_REFERENCE,
						"gonl",
						"What genetic variation is to be found in the Dutch indigenous population? "
								+ "Detailed knowledge about this is not only interesting in itself, "
								+ "it also helps to extract useful biomedical information from Dutch biobanks. "
								+ "The Dutch biobank collaboration BBMRI-NL has initiated the extensive Rainbow Project “Genome of the Netherlands” (GoNL) "
								+ "because it offers unique opportunities for science and for the development of new treatments and diagnostic techniques. "
								+ "A close-up look at the DNA of 750 Dutch people-250 trio’s of two parents and an adult child-plus a "
								+ "global genetic profile of large numbers of Dutch will disclose a wealth of new information, new insights, "
								+ "and possible applications.", Collections.singletonList(outputAttribute));

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();

		// TODO: properly test multiAllelicFresultFilter
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData(GONL_AF_RESOURCE_ATTRIBUTE_NAME,
						FieldTypeEnum.DECIMAL)));
		EntityAnnotator entityAnnotator = new AnnotatorImpl(GONL_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, multiAllelicResultFilter, dataService, resources)
		{
			
			
			@Override
			protected Object getResourceAttributeValue(AttributeMetaData attr, Entity entity)
			{
				String attrName = GONL_GENOME_AF.equals(attr.getName()) ? GONL_AF_RESOURCE_ATTRIBUTE_NAME : attr.getName();
				return entity.get(attrName);
			}
		};

	
		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource gonlresources()
	{
		MultiResourceConfig goNLConfig = new MultiResourceConfigImpl(GONL_CHROMOSOME_PROPERTY,
				GONL_FILE_PATTERN_PROPERTY, GONL_FOLDER_PROPERTY, molgenisSettings);		
		
		return new MultiFileResource(GONL_MULTI_FILE_RESOURCE, goNLConfig, new TabixVcfRepositoryFactory(
				GONL_MULTI_FILE_RESOURCE));
	}
}