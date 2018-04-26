package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.QueryAnnotatorImpl;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.annotation.web.settings.GoNLAnnotatorSettings.Meta.*;
import static org.molgenis.data.meta.AttributeType.STRING;

@Configuration
public class GoNLAnnotator implements AnnotatorConfig
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

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	private RepositoryAnnotatorImpl annotator;

	@Bean
	public RepositoryAnnotator gonl()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<Attribute> attributes = createGoNlOutputAttributes();

		AnnotatorInfo thousandGenomeInfo = AnnotatorInfo.create(AnnotatorInfo.Status.READY,
				AnnotatorInfo.Type.POPULATION_REFERENCE, NAME,
				"What genetic variation is to be found in the Dutch indigenous population? "
						+ "Detailed knowledge about this is not only interesting in itself, "
						+ "it also helps to extract useful biomedical information from Dutch biobanks. "
						+ "The Dutch biobank collaboration BBMRI-NL has initiated the extensive Rainbow Project “Genome of the Netherlands” (GoNL) "
						+ "because it offers unique opportunities for science and for the development of new treatments and diagnostic techniques. "
						+ "A close-up look at the DNA of 750 Dutch people-250 trio’s of two parents and an adult child-plus a "
						+ "global genetic profile of large numbers of Dutch will disclose a wealth of new information, new insights, "
						+ "and possible applications.", attributes);

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator(vcfAttributes);

		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(GONL_MULTI_FILE_RESOURCE, thousandGenomeInfo,
				locusQueryCreator, dataService, resources, (annotationSourceFileName) ->
		{
			goNLAnnotatorSettings.set(ROOT_DIRECTORY, annotationSourceFileName);
			goNLAnnotatorSettings.set(FILEPATTERN, "gonl.chr%s.snps_indels.r5.vcf.gz");
			goNLAnnotatorSettings.set(OVERRIDE_CHROMOSOME_FILES, "X:gonl.chrX.release4.gtc.vcf.gz");
			goNLAnnotatorSettings.set(CHROMOSOMES, "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X");
		})
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createGoNlOutputAttributes();
			}

			@Override
			protected void processQueryResults(Entity entity, Iterable<Entity> annotationSourceEntities,
					boolean updateMode)
			{
				if (updateMode)
				{
					throw new MolgenisDataException("This annotator/filter does not support updating of values");
				}

				List<Entity> refMatches = determineRefMatches(entity, annotationSourceEntities);
				setGoNLFrequencies(entity, refMatches);
			}
		};
		annotator.init(entityAnnotator);

	}

	private List<Attribute> createGoNlOutputAttributes()
	{
		List<Attribute> attributes = new ArrayList<>();
		Attribute goNlAfAttribute = attributeFactory.create()
													.setName(GONL_GENOME_AF)
													.setDataType(STRING)
													.setDescription(
															"The allele frequency for variants seen in the population used for the GoNL project")
													.setLabel(GONL_AF_LABEL);

		Attribute goNlGtcAttribute = attributeFactory.create()
													 .setName(GONL_GENOME_GTC)
													 .setDataType(STRING)
													 .setDescription(
															 "GenoType Counts. For each ALT allele in the same order as listed = 0/0,0/1,1/1,0/2,1/2,2/2,0/3,1/3,2/3,3/3,etc. Phasing is ignored; hence 1/0, 0|1 and 1|0 are all counted as 0/1. When one or more alleles is not called for a genotype in a specific sample (./., ./0, ./1, ./2, etc.), that sample's genotype is completely discarded for calculating GTC.")
													 .setLabel(GONL_GTC_LABEL);

		attributes.add(goNlGtcAttribute);
		attributes.add(goNlAfAttribute);
		return attributes;
	}

	@Bean
	Resource gonlresources()
	{
		MultiResourceConfig goNLConfig = new MultiResourceConfigImpl(CHROMOSOMES, FILEPATTERN, ROOT_DIRECTORY,
				OVERRIDE_CHROMOSOME_FILES, goNLAnnotatorSettings);

		return new MultiFileResource(GONL_MULTI_FILE_RESOURCE, goNLConfig)
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new TabixVcfRepositoryFactory(GONL_MULTI_FILE_RESOURCE, vcfAttributes, entityTypeFactory,
						attributeFactory);
			}
		};
	}

	private void setGoNLFrequencies(Entity entity, List<Entity> refMatches)
	{
		String afs = null;
		String gtcs = null;
		if (hasAltAttribute(entity))
		{
			List<Entity> alleleMatches = computeAlleleMatches(entity, refMatches);

			if (alleleMatches.stream().anyMatch(Objects::nonNull))
			{
				afs = alleleMatches.stream()
								   .map(gonl -> gonl == null ? "." : Double.toString(
										   Double.valueOf(gonl.getString(INFO_AC)) / gonl.getInt(INFO_AN)))
								   .collect(Collectors.joining(","));
				//update GTC field to separate allele combinations by pipe instead of comma, since we use comma to separate alt allele info
				gtcs = alleleMatches.stream()
									.map(gonl -> gonl == null ? "." : gonl.getString(INFO_GTC).replace(",", "|"))
									.collect(Collectors.joining(","));
			}

		}
		entity.set(GONL_GENOME_AF, afs);
		entity.set(GONL_GENOME_GTC, gtcs);
	}

	private List<Entity> determineRefMatches(Entity entity, Iterable<Entity> annotationSourceEntities)
	{
		List<Entity> refMatches = newArrayList();
		for (Entity resourceEntity : annotationSourceEntities)
		{
			//situation example: input A, GoNL A
			if (refMatches(resourceEntity))
			{
				refMatches.add(resourceEntity);
			}
			//situation example: input ATC/TTC, GoNL A/T
			//we then match on A (leaving TC), lengthen the GoNL ref to A+TC, and alt to T+TC
			//now it has a match to ATC/TTC (as it should, but was not obvious due to notation)
			else if (shouldLengthenGoNLRef(resourceEntity))
			{
				lengthenGoNLRef(entity, resourceEntity);
				refMatches.add(resourceEntity);
			}
			//situation example: input T/G, GoNL TCT/GCT
			//we then match on T (leaving CT), and shorten the GoNL ref to T (-CT), and alt to G (-CT)
			//now it has a match to T/G (as it should, but was not obvious due to notation)
			else if (shouldShortenGoNLRef(entity, resourceEntity))
			{
				shortenGoNLRef(entity, resourceEntity);
				refMatches.add(resourceEntity);
			}
		}
		return refMatches;
	}

	private List<Entity> computeAlleleMatches(Entity entity, List<Entity> refMatches)
	{
		List<Entity> alleleMatches = newArrayList();
		for (String alt : entity.getString(vcfAttributes.getAltAttribute().getName()).split(","))
		{
			alleleMatches.add(refMatches.stream()
										.filter(gonl -> (alt).equals(
												(gonl.getString(vcfAttributes.getAltAttribute().getName()))))
										.findFirst()
										.orElse(null));
		}
		return alleleMatches;
	}

	private boolean hasAltAttribute(Entity entity)
	{
		return entity.getString(vcfAttributes.getAltAttribute().getName()) != null;
	}

	private void shortenGoNLRef(Entity entity, Entity resourceEntity)
	{
		int postFixInputLength = resourceEntity.getString(vcfAttributes.getRefAttribute().getName())
											   .substring(entity.getString(vcfAttributes.getRefAttribute().getName())
																.length())
											   .length();
		//bugfix: matching A/G to ACT/A results in postFixInputLength=2, correctly updating ref from ACT to A,
		//but then tries to substring the alt allele A to length -1 (1 minus 2) which is not allowed.
		//added a check to prevent this: alt.length() > postFixInputLength ? trim the alt : change to 'n/a' because we cannot use this alt.
		resourceEntity.set(vcfAttributes.getRefAttribute().getName(),
				resourceEntity.getString(vcfAttributes.getRefAttribute().getName())
							  .substring(0,
									  (resourceEntity.getString(vcfAttributes.getRefAttribute().getName()).length()
											  - postFixInputLength)));
		String newAltString = Arrays.stream(
				resourceEntity.getString(vcfAttributes.getAltAttribute().getName()).split(","))
									.map(alt -> alt.length() > postFixInputLength ? alt.substring(0,
											(alt.length() - postFixInputLength)) : "n/a")
									.collect(Collectors.joining(","));
		resourceEntity.set(vcfAttributes.getAltAttribute().getName(), newAltString);
	}

	private boolean shouldShortenGoNLRef(Entity entity, Entity resourceEntity)
	{
		return resourceEntity.getString(vcfAttributes.getRefAttribute().getName())
							 .indexOf(entity.getString(vcfAttributes.getRefAttribute().getName())) == 0;
	}

	private void lengthenGoNLRef(Entity entity, Entity resourceEntity)
	{
		String postFixResource = entity.getString(vcfAttributes.getRefAttribute().getName())
									   .substring(resourceEntity.getString(vcfAttributes.getRefAttribute().getName())
																.length());
		resourceEntity.set(vcfAttributes.getRefAttribute().getName(),
				resourceEntity.getString(vcfAttributes.getRefAttribute().getName()) + postFixResource);
		String newAltString = Arrays.stream(
				resourceEntity.getString(vcfAttributes.getAltAttribute().getName()).split(","))
									.map(alt -> alt + postFixResource)
									.collect(Collectors.joining(","));
		resourceEntity.set(vcfAttributes.getAltAttribute().getName(), newAltString);
	}

	private boolean shouldLengthenGoNLRef(Entity resourceEntity)
	{
		return vcfAttributes.getRefAttribute()
							.getName()
							.indexOf(resourceEntity.getString(vcfAttributes.getRefAttribute().getName())) == 0;
	}

	private boolean refMatches(Entity resourceEntity)
	{
		return resourceEntity.get(vcfAttributes.getRefAttribute().getName())
							 .equals(vcfAttributes.getRefAttribute().getName());
	}

}