package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.CHROMOSOMES;
import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.FILEPATTERN;
import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES;
import static org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings.Meta.ROOT_DIRECTORY;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.REF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.MultiFileResource;
import org.molgenis.data.annotation.resources.impl.MultiResourceConfigImpl;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

@Configuration
public class GoNLAnnotator
{
	public static final String NAME = "gonl";

	public static final String GONL_GENOME_AF = "GoNL_AF";
	public static final String GONL_GENOME_GTC = "GoNL_GTC";
	public static final String GONL_AF_LABEL = "Genome of the netherlands allele frequency";
	public static final String GONL_GTC_LABEL = "Genome of the netherlands Genotype counts frequency";
	public static final String INFO_GTC = "GTC";
	public static final String INFO_AN = "AN";
	public static final String INFO_AC = "AC";

	public static final String GONL_MULTI_FILE_RESOURCE = "gonlresources";

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
				.setDescription(
						"GenoType Counts. For each ALT allele in the same order as listed = 0/0,0/1,1/1,0/2,1/2,2/2,0/3,1/3,2/3,3/3,etc. Phasing is ignored; hence 1/0, 0|1 and 1|0 are all counted as 0/1. When one or more alleles is not called for a genotype in a specific sample (./., ./0, ./1, ./2, etc.), that sample's genotype is completely discarded for calculating GTC.")
				.setLabel(GONL_GTC_LABEL);

		attributes.add(goNlGtcAttribute);
		attributes.add(goNlAfAttribute);

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.POPULATION_REFERENCE,
				NAME,
				"What genetic variation is to be found in the Dutch indigenous population? "
						+ "Detailed knowledge about this is not only interesting in itself, "
						+ "it also helps to extract useful biomedical information from Dutch biobanks. "
						+ "The Dutch biobank collaboration BBMRI-NL has initiated the extensive Rainbow Project “Genome of the Netherlands” (GoNL) "
						+ "because it offers unique opportunities for science and for the development of new treatments and diagnostic techniques. "
						+ "A close-up look at the DNA of 750 Dutch people-250 trio’s of two parents and an adult child-plus a "
						+ "global genetic profile of large numbers of Dutch will disclose a wealth of new information, new insights, "
						+ "and possible applications.",
				attributes);

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();

		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(GONL_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, dataService, resources, (annotationSourceFileName) -> {
					goNLAnnotatorSettings.set(ROOT_DIRECTORY, annotationSourceFileName);
					goNLAnnotatorSettings.set(FILEPATTERN, "gonl.chr%s.snps_indels.r5.vcf.gz");
					goNLAnnotatorSettings.set(OVERRIDE_CHROMOSOME_FILES, "X:gonl.chrX.release4.gtc.vcf.gz");
					goNLAnnotatorSettings.set(CHROMOSOMES,
							"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X");
				})
		{
			public String postFixResource = "";

			@Override
			protected void processQueryResults(Entity inputEntity, Iterable<Entity> annotationSourceEntities,
					Entity resultEntity)
			{
				String afs = null;
				String gtcs = null;
				List<Entity> refMatches = Lists.newArrayList();
				for (Entity resourceEntity : annotationSourceEntities)
				{
					if (resourceEntity.get(VcfRepository.REF).equals(inputEntity.get(VcfRepository.REF)))
					{
						refMatches.add(resourceEntity);
					}
					else if (inputEntity.getString(VcfRepository.REF)
							.indexOf(resourceEntity.getString(VcfRepository.REF)) == 0)
					{
						postFixResource = inputEntity.getString(VcfRepository.REF)
								.substring(resourceEntity.getString(VcfRepository.REF).length());
						resourceEntity.set(VcfRepository.REF,
								resourceEntity.getString(VcfRepository.REF) + postFixResource);
						String newAltString = Arrays.asList(resourceEntity.getString(ALT).split(",")).stream()
								.map(alt -> alt + postFixResource).collect(Collectors.joining(","));
						resourceEntity.set(VcfRepository.ALT, newAltString);
						refMatches.add(resourceEntity);
					}
					else if (resourceEntity.getString(VcfRepository.REF)
							.indexOf(inputEntity.getString(VcfRepository.REF)) == 0)
					{
						int postFixInputLength = resourceEntity.getString(VcfRepository.REF)
								.substring(inputEntity.getString(VcfRepository.REF).length()).length();
						resourceEntity.set(VcfRepository.REF, resourceEntity.getString(VcfRepository.REF).substring(0,
								(resourceEntity.getString(VcfRepository.REF).length() - postFixInputLength)));
						String newAltString = Arrays.asList(resourceEntity.getString(ALT).split(",")).stream()
								.map(alt -> alt.substring(0, (alt.length() - postFixInputLength)))
								.collect(Collectors.joining(","));
						resourceEntity.set(VcfRepository.ALT, newAltString);
						refMatches.add(resourceEntity);
					}
				}
				if (inputEntity.getString(ALT) != null)
				{
					List<Entity> alleleMatches = Lists.newArrayList();
					for (String alt : inputEntity.getString(ALT).split(","))
					{
						alleleMatches
								.add(Iterables.find(refMatches, gonl -> (alt).equals((gonl.getString(ALT))), null));
					}

					if (!Iterables.all(alleleMatches, Predicates.isNull()))
					{
						afs = alleleMatches.stream()
								.map(gonl -> gonl == null ? ""
										: Double.toString(gonl.getDouble(INFO_AC) / gonl.getDouble(INFO_AN)))
								.collect(Collectors.joining("|"));

						gtcs = alleleMatches.stream().map(gonl -> gonl == null ? "" : gonl.getString(INFO_GTC))
								.collect(Collectors.joining("|"));
					}

				}
				resultEntity.set(GONL_GENOME_AF, afs);
				resultEntity.set(GONL_GENOME_GTC, gtcs);
			}
		};
		return new RepositoryAnnotatorImpl(entityAnnotator);

	}

	@Bean
	Resource gonlresources()
	{
		MultiResourceConfig goNLConfig = new MultiResourceConfigImpl(CHROMOSOMES, FILEPATTERN, ROOT_DIRECTORY,
				OVERRIDE_CHROMOSOME_FILES, goNLAnnotatorSettings);

		return new MultiFileResource(GONL_MULTI_FILE_RESOURCE, goNLConfig,
				new TabixVcfRepositoryFactory(GONL_MULTI_FILE_RESOURCE));
	}
}