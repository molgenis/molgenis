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
public class ThousandGenomesAnnotator
{

	public static final String THOUSAND_GENOMES_AF = "THOUSAND_GENOMES_AF";
	public static final String THOUSAND_GENOMES_AF_LABEL = "ThousandGenomes allele frequency";
	public static final String THOUSAND_GENOMES_AF_ResourceAttributeName = VcfRepository.getInfoPrefix() + "AF";

	public static final String THOUSAND_GENOMES_FILE_LOCATION_PROPERTY = "thousandgenomesc_location";
	public static final String THOUSAND_GENOMES_TABIX_RESOURCE = "ThousandGenomesTabixResource";

	@Autowired
	private MolgenisSettings molgenisSettings;
	@Autowired
	private DataService dataService;
	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator ThousandGenomes()
	{
		// TODO: description
		DefaultAttributeMetaData outputAttribute = new DefaultAttributeMetaData(THOUSAND_GENOMES_AF,
				FieldTypeEnum.STRING).setDescription("TODO").setLabel(THOUSAND_GENOMES_AF_LABEL);

		AnnotatorInfo thousandGenomesInfo = AnnotatorInfo.create(Status.BETA, AnnotatorInfo.Type.POPULATION_REFERENCE,
				"ThousandGenomes", "TODO", Collections.singletonList(outputAttribute));

		// TODO: properly test multiAllelicResultFilter
		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData(THOUSAND_GENOMES_AF_ResourceAttributeName,
						FieldTypeEnum.DECIMAL)));
		EntityAnnotator entityAnnotator = new AnnotatorImpl(THOUSAND_GENOMES_TABIX_RESOURCE, thousandGenomesInfo,
				locusQueryCreator, multiAllelicResultFilter, dataService, resources)
		{
			@Override
			protected String getResourceAttributeName(AttributeMetaData attr)
			{
				if (THOUSAND_GENOMES_AF.equals(attr.getName()))
				{
					return THOUSAND_GENOMES_AF_ResourceAttributeName;
				}
				return attr.getName();
			}
		};

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	MultiFileResource thousandGenomesResources()
	{
		MultiResourceConfig thousandGenomesConfig = new MultiResourceConfigImpl("1000g_chromosomes",
				"1000g_chromosome_file_pattern", "1000g_location", molgenisSettings);

		MultiFileResource thousandGenomesMultiResource = new MultiFileResource("1000g_annotator",
				thousandGenomesConfig, new TabixVcfRepositoryFactory(THOUSAND_GENOMES_TABIX_RESOURCE));

		return thousandGenomesMultiResource;
	}
}
