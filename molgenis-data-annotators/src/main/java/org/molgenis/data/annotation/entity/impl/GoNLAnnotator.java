package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.CHROMOSOMES;
import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.FILEPATTERN;
import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES;
import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.ROOT_DIRECTORY;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.GoNLMultiAllelicResultFilter;
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
public class GoNLAnnotator
{
	public static final String NAME = "gonl";

	public static final String GONL_GENOME_AF = "GoNL_AF";
	public static final String GONL_GENOME_GTC = "GoNL_GTC";
	public static final String GONL_AF_LABEL = "Genome of the netherlands allele frequency";
	public static final String GONL_GTC_LABEL = "Genome of the netherlands Genotype counts frequency";
	public static final String INFO_AF = "AF";
	public static final String INFO_GTC = "GTC";
	public static final String INFO_AN = "AN";
	public static final String INFO_AC = "AC";

	public static final String GONL_MULTI_FILE_RESOURCE = "gonlresources";

	// Backwards capabilities properties from the old annotator
	public static final String BC_GONL_MAF_LABEL = "GONLMAF";
	public static final String BC_GONL_MAF = BC_GONL_MAF_LABEL;

	@Autowired
	private Entity goNLAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator gonl()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();
		DefaultAttributeMetaData goNlAfAttribute = new DefaultAttributeMetaData(GONL_GENOME_AF, FieldTypeEnum.STRING)
				.setDescription("The allele frequency for variants seen in the population used for the GoNL project")
				.setLabel(GONL_AF_LABEL);

		DefaultAttributeMetaData goNlGtcAttribute = new DefaultAttributeMetaData(GONL_GENOME_GTC, FieldTypeEnum.STRING)
				.setDescription("The allele frequency for variants seen in the population used for the GoNL project")
				.setLabel(GONL_GTC_LABEL);

		attributes.add(goNlGtcAttribute);
		attributes.add(goNlAfAttribute);

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo
				.create(Status.READY,
						AnnotatorInfo.Type.POPULATION_REFERENCE,
						NAME,
						"What genetic variation is to be found in the Dutch indigenous population? "
								+ "Detailed knowledge about this is not only interesting in itself, "
								+ "it also helps to extract useful biomedical information from Dutch biobanks. "
								+ "The Dutch biobank collaboration BBMRI-NL has initiated the extensive Rainbow Project “Genome of the Netherlands” (GoNL) "
								+ "because it offers unique opportunities for science and for the development of new treatments and diagnostic techniques. "
								+ "A close-up look at the DNA of 750 Dutch people-250 trio’s of two parents and an adult child-plus a "
								+ "global genetic profile of large numbers of Dutch will disclose a wealth of new information, new insights, "
								+ "and possible applications.", attributes);

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();

		GoNLMultiAllelicResultFilter goNLMultiAllelicResultFilter = new GoNLMultiAllelicResultFilter();

		EntityAnnotator entityAnnotator = new AnnotatorImpl(GONL_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, goNLMultiAllelicResultFilter, dataService, resources,
				(annotationSourceFileName) -> {
					goNLAnnotatorSettings.set(ROOT_DIRECTORY, annotationSourceFileName);
					goNLAnnotatorSettings.set(FILEPATTERN, "gonl.chr%s.snps_indels.r5.vcf.gz");
					goNLAnnotatorSettings.set(OVERRIDE_CHROMOSOME_FILES, "X:gonl.chrX.release4.gtc.vcf.gz");
					goNLAnnotatorSettings
							.set(CHROMOSOMES, "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X");
				})
		{
			@Override
			protected Object getResourceAttributeValue(AttributeMetaData attr, Entity entity)
			{
				String attrName = null;

				if (GONL_GENOME_AF.equals(attr.getName()))
				{
					attrName = INFO_AF;
				}
				else if (GONL_GENOME_GTC.equals(attr.getName()))
				{
					attrName = INFO_GTC;
				}
				else
				{
					attrName = attr.getName();
				}

				return entity.get(attrName);
			}
		};

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource gonlresources()
	{
		MultiResourceConfig goNLConfig = new MultiResourceConfigImpl(CHROMOSOMES, FILEPATTERN, ROOT_DIRECTORY,
				OVERRIDE_CHROMOSOME_FILES, goNLAnnotatorSettings);

		return new MultiFileResource(GONL_MULTI_FILE_RESOURCE, goNLConfig, new TabixVcfRepositoryFactory(
				GONL_MULTI_FILE_RESOURCE));
	}
}