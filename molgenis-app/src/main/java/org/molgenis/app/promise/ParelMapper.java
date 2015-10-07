package org.molgenis.app.promise;

import static org.molgenis.app.promise.BbmriNlCheatSheet.ACRONYM;
import static org.molgenis.app.promise.BbmriNlCheatSheet.AGE_HIGH;
import static org.molgenis.app.promise.BbmriNlCheatSheet.AGE_LOW;
import static org.molgenis.app.promise.BbmriNlCheatSheet.AGE_UNIT;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANKS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_DESCRIPTION;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_FEE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_JOINT_PROJECTS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_URI;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_DESCRIPTION;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_FEE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_URI;
import static org.molgenis.app.promise.BbmriNlCheatSheet.CONTACT_PERSON;
import static org.molgenis.app.promise.BbmriNlCheatSheet.DATA_ACCESS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.DATA_CATEGORIES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.DESCRIPTION;
import static org.molgenis.app.promise.BbmriNlCheatSheet.DISEASE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.INSTITUTES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.MATERIALS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.NAME;
import static org.molgenis.app.promise.BbmriNlCheatSheet.NUMBER_OF_DONORS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.OMICS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.PRINCIPAL_INVESTIGATORS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.PUBLICATIONS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_COLLECTION_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_DATA_CATEGORY_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_GENDER_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_OMICS_DATA_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.SAMPLE_ACCESS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.SAMPLE_COLLECTIONS_ENTITY;
import static org.molgenis.app.promise.BbmriNlCheatSheet.SEX;
import static org.molgenis.app.promise.BbmriNlCheatSheet.TYPE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.WEBSITE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.molgenis.app.promise.MappingReport.Status;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ParelMapper implements PromiseMapper, ApplicationListener<ContextRefreshedEvent>
{
	private final String ID = "PAREL";

	private PromiseMapperFactory promiseMapperFactory;

	private ProMiseDataParser promiseDataParser;

	private DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(ParelMapper.class);

	@Autowired
	public ParelMapper(PromiseMapperFactory promiseMapperFactory, ProMiseDataParser promiseDataParser,
			DataService dataService)
	{
		this.promiseMapperFactory = Objects.requireNonNull(promiseMapperFactory);
		this.promiseDataParser = Objects.requireNonNull(promiseDataParser);
		this.dataService = Objects.requireNonNull(dataService);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0)
	{
		promiseMapperFactory.registerMapper(ID, this);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public MappingReport map(String projectName) throws IOException
	{
		MappingReport report = new MappingReport();

		LOG.info("Getting data from ProMISe for " + projectName);

		try
		{
			Iterable<Entity> promiseBiobankEntities = promiseDataParser.parse(projectName, 0);
			Iterable<Entity> promiseSampleEntities = promiseDataParser.parse(projectName, 1);

			EntityMetaData targetEntityMetaData = Objects.requireNonNull(dataService
					.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY));

			for (Entity promiseBiobankEntity : promiseBiobankEntities)
			{
				Iterable<Entity> promiseBiobankSamplesEntities = getPromiseBiobankSamples(promiseBiobankEntity,
						promiseSampleEntities);

				MapEntity targetEntity = new MapEntity(targetEntityMetaData);

				targetEntity.set(BbmriNlCheatSheet.ID, null);
				targetEntity.set(NAME, null);
				targetEntity.set(ACRONYM, null);
				targetEntity.set(TYPE, toTypes(promiseBiobankEntity.getString("COLLECTION_TYPE")));
				targetEntity.set(DISEASE, null);
				targetEntity.set(DATA_CATEGORIES, toDataCategories(promiseBiobankEntity.getString("DATA_CATEGORIES")));
				targetEntity.set(MATERIALS, null);
				targetEntity.set(OMICS, toOmics(promiseBiobankSamplesEntities));
				targetEntity.set(SEX, toGenders(promiseBiobankEntity.getString("SEX")));
				targetEntity.set(AGE_LOW, null);
				targetEntity.set(AGE_HIGH, null);
				targetEntity.set(AGE_UNIT, null);
				targetEntity.set(NUMBER_OF_DONORS, null);
				targetEntity.set(DESCRIPTION, null);
				targetEntity.set(PUBLICATIONS, null);
				targetEntity.set(CONTACT_PERSON, null);
				targetEntity.set(PRINCIPAL_INVESTIGATORS, null);
				targetEntity.set(INSTITUTES, null);
				targetEntity.set(BIOBANKS, null);
				targetEntity.set(WEBSITE, null);
				targetEntity.set(SAMPLE_ACCESS, null);
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_FEE, null);
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS, null);
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_DESCRIPTION, null);
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_URI, null);
				targetEntity.set(DATA_ACCESS, null);
				targetEntity.set(BIOBANK_DATA_ACCESS_FEE, null);
				targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, null);
				targetEntity.set(BIOBANK_DATA_ACCESS_DESCRIPTION, null);
				targetEntity.set(BIOBANK_DATA_ACCESS_URI, null);

				dataService.add("bbmri_nl_sample_collections", targetEntity);
			}

			report.setStatus(Status.SUCCESS);
		}
		catch (Exception e)
		{
			report.setStatus(Status.ERROR);
			report.setMessage(e.getMessage());

			LOG.warn(ExceptionUtils.getStackTrace(e));
		}

		return report;
	}

	private Iterable<Entity> toGenders(String promiseSex)
	{
		// TODO should this come from biobank or sample entities?
		Object[] sexes = promiseSex.split(",");
		Iterable<Object> ids = Arrays.asList(sexes);

		Iterable<Entity> genderTypes = dataService.findAll(REF_GENDER_TYPES);
		if (!genderTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown '" + REF_GENDER_TYPES + "' [" + ids.toString() + "]");
		}
		return genderTypes;
	}

	private Iterable<Entity> toOmics(Iterable<Entity> promiseBiobankSamplesEntities)
	{
		// TODO implement when mapping is known
		Iterable<Object> ids = Arrays.asList("NAV");

		Iterable<Entity> omicsTypes = dataService.findAll(REF_OMICS_DATA_TYPES, ids);
		if (!omicsTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown '" + REF_OMICS_DATA_TYPES + "' [" + ids.toString() + "]");
		}
		return omicsTypes;
	}

	private Iterable<Entity> toDataCategories(String promiseDataCategories)
	{
		Object[] categories = promiseDataCategories.split(",");
		Iterable<Object> ids = Arrays.asList(categories);

		// TODO replace with actual values when we know how they should be mapped
		ids = Arrays.asList("OTHER");
		Iterable<Entity> categoryTypes = dataService.findAll(REF_DATA_CATEGORY_TYPES, ids);

		// FIXME fix mysqlrepo returning [null] instead of empty iterable
		if (categoryTypes.iterator().next() == null)
		{
			throw new RuntimeException("Unknown '" + REF_DATA_CATEGORY_TYPES + "' [" + promiseDataCategories + "]");
		}

		return categoryTypes;
	}

	private Iterable<Entity> toTypes(String promiseType)
	{
		// TODO how are multiple types stored in PSI?
		Iterable<Object> ids = Arrays.asList(promiseType);

		Iterable<Entity> collectionTypes = dataService.findAll(REF_COLLECTION_TYPES, ids);

		// FIXME fix mysqlrepo returning [null] instead of empty iterable
		if (collectionTypes.iterator().next() == null)
		{
			throw new RuntimeException("Unknown '" + REF_COLLECTION_TYPES + "' [" + promiseType + "]");
		}
		return collectionTypes;
	}

	private Iterable<Entity> getPromiseBiobankSamples(Entity promiseBiobankEntity,
			Iterable<Entity> promiseSampleEntities)
	{
		List<Entity> promiseBiobankSampleEntities = new ArrayList<Entity>();
		String biobankId = promiseBiobankEntity.getString("ID") + promiseBiobankEntity.getString("IDAA");
		for (Entity promiseSampleEntity : promiseSampleEntities)
		{
			String biobankSamplesId = promiseSampleEntity.getString("ID") + promiseSampleEntity.getString("IDAA");

			if (biobankId.equals(biobankSamplesId))
			{
				promiseBiobankSampleEntities.add(promiseSampleEntity);
			}
		}
		return promiseBiobankSampleEntities;
	}
}
