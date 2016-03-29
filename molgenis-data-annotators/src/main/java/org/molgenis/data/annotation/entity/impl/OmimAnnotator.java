package org.molgenis.data.annotation.entity.impl;

import static java.util.Arrays.asList;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.data.annotator.websettings.OmimAnnotatorSettings.Meta.OMIM_LOCATION;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

@Configuration
public class OmimAnnotator
{
	public static final String NAME = "OMIM";
	public static final char SEPARATOR = '\t';

	public static final String PHENOTYPE = "OMIM_Phenotypes";
	public static final String MIM_NUMBER = "OMIM_MIM_Numbers";
	public static final String CYTO_LOCATIONS = "OMIM_Cyto_Locations";

	public static final String OMIM_RESOURCE = "OMIMResource";

	@Autowired
	private Entity omimAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator omim()
	{
		List<AttributeMetaData> outputAttributes = new ArrayList<>();
		DefaultAttributeMetaData omim_phenotype = new DefaultAttributeMetaData(PHENOTYPE, TEXT)
				.setDescription("OMIM phenotype").setLabel("OMIM Phenotype");
		DefaultAttributeMetaData omim_mim_number = new DefaultAttributeMetaData(MIM_NUMBER, TEXT)
				.setDescription("Number that represents the MIM database identifier").setLabel("OMIM MIM Number");
		DefaultAttributeMetaData omim_cyto_location = new DefaultAttributeMetaData(CYTO_LOCATIONS, TEXT)
				.setDescription("Cytogenic location associated with an OMIM phenotype").setLabel("OMIM Cyto Location");

		outputAttributes.addAll(asList(omim_phenotype, omim_mim_number, omim_cyto_location));

		AnnotatorInfo omimInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.PHENOTYPE_ASSOCIATION, NAME,
				"OMIM is a comprehensive, authoritative compendium of human genes and genetic phenotypes that is "
						+ "freely available and updated daily. The full-text, referenced overviews in OMIM contain information on all "
						+ "known mendelian disorders and over 15,000 genes. OMIM focuses on the relationship between phenotype and genotype.",
				outputAttributes);

		EntityAnnotator entityAnnotator = new AnnotatorImpl(OMIM_RESOURCE, omimInfo, new GeneNameQueryCreator(),
				new OmimResultFilter(), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(OMIM_LOCATION, omimAnnotatorSettings));

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	public Resource omimResource()
	{
		return new ResourceImpl(OMIM_RESOURCE, new SingleResourceConfig(OMIM_LOCATION, omimAnnotatorSettings),
				new RepositoryFactory()
				{
					@Override
					public Repository createRepository(File file) throws IOException
					{
						return new OmimRepository(file);
					}
				});
	}

	public static class OmimResultFilter implements ResultFilter
	{
		@Override
		public Collection<AttributeMetaData> getRequiredAttributes()
		{
			return Collections.emptyList();
		}

		@Override
		public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
		{
			Optional<Entity> firstResult = FluentIterable.from(results).first();
			return firstResult.transform(e -> {
				Entity result = new MapEntity();
				result.set(PHENOTYPE, e.get(OmimRepository.OMIM_PHENOTYPE_COL_NAME));
				result.set(MIM_NUMBER, e.get(OmimRepository.OMIM_MIM_NUMBER_COL_NAME));
				result.set(CYTO_LOCATIONS, e.get(OmimRepository.OMIM_CYTO_LOCATION_COL_NAME));
				return result;
			});

		}
	}
}
