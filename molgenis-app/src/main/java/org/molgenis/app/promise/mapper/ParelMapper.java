package org.molgenis.app.promise.mapper;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import org.molgenis.app.promise.client.PromiseDataParser;
import org.molgenis.app.promise.mapper.MappingReport.Status;
import org.molgenis.app.promise.model.BbmriNlCheatSheet;
import org.molgenis.app.promise.model.PromiseMappingProjectMetaData;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.*;

@Component
public class ParelMapper implements PromiseMapper, ApplicationListener<ContextRefreshedEvent>
{
	private final String MAPPER_ID = "PAREL";

	private PromiseMapperFactory promiseMapperFactory;
	private PromiseDataParser promiseDataParser;
	private DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(ParelMapper.class);

	private static final HashMap<String, List<String>> materialTypesMap;
	private static final List<String> unknownMaterialTypes = newArrayList();
	private static final List<String> materialTypeIds = newArrayList();

	static
	{
		materialTypesMap = new HashMap<>();
		materialTypesMap.put("bloed", asList("WHOLE BLOOD"));
		materialTypesMap.put("bloedplasma", asList("PLASMA"));
		materialTypesMap.put("bloedplasma (EDTA)", asList("PLASMA"));
		materialTypesMap.put("bloedserum", asList("SERUM"));
		materialTypesMap.put("DNA uit beenmergcellen", asList("DNA"));
		materialTypesMap.put("DNA uit bloedcellen", asList("DNA"));
		materialTypesMap.put("feces", asList("FECES"));
		materialTypesMap.put("gastrointestinale mucosa", asList("TISSUE_FROZEN"));
		materialTypesMap.put("liquor (CSF)", asList("OTHER"));
		materialTypesMap.put("mononucleaire celfractie uit beenmerg", asList("OTHER"));
		materialTypesMap.put("mononucleaire celfractie uit bloed", asList("PERIPHERAL_BLOOD_CELLS"));
		materialTypesMap.put("RNA uit beenmergcellen", asList("RNA"));
		materialTypesMap.put("RNA uit bloedcellen", asList("RNA"));
		materialTypesMap.put("serum", asList("SERUM"));
		materialTypesMap.put("urine", asList("URINE"));
		materialTypesMap.put("weefsel", asList("TISSUE_FROZEN", "TISSUE_PARAFFIN_EMBEDDED"));
	}

	private static final HashMap<String, List<String>> tissueTypesMap;

	static
	{
		tissueTypesMap = new HashMap<>();
		tissueTypesMap.put("1", asList("TISSUE_PARAFFIN_EMBEDDED"));
		tissueTypesMap.put("2", asList("TISSUE_FROZEN"));
		tissueTypesMap.put("9", asList("OTHER"));
	}

	@Autowired
	public ParelMapper(PromiseMapperFactory promiseMapperFactory, PromiseDataParser promiseDataParser,
			DataService dataService)
	{
		this.promiseMapperFactory = requireNonNull(promiseMapperFactory);
		this.promiseDataParser = requireNonNull(promiseDataParser);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0)
	{
		promiseMapperFactory.registerMapper(MAPPER_ID, this);
	}

	@Override
	public String getId()
	{
		return MAPPER_ID;
	}

	@Override
	public MappingReport map(Entity project)
	{
		requireNonNull(project);
		MappingReport report = new MappingReport();

		try
		{
			LOG.info("Getting data from ProMISe for " + project.getString("name"));
			Entity credentials = project.getEntity(PromiseMappingProjectMetaData.CREDENTIALS);
			EntityMetaData targetEntityMetaData = requireNonNull(
					dataService.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY));

			// Parse biobanks
			promiseDataParser.parse(credentials, 0, promiseBiobankEntity -> {

				// find out if a sample collection with this id already exists
				Entity targetEntity = dataService.findOne(SAMPLE_COLLECTIONS_ENTITY, project.getString(BIOBANK_ID));

				boolean biobankExists = true;
				if (targetEntity == null)
				{
					targetEntity = new MapEntity(targetEntityMetaData);

					// fill hand coded fields with dummy data the first time this biobank is added
					targetEntity.set(CONTACT_PERSON, asList(getTempPerson(REF_PERSONS))); // mref
					targetEntity.set(PRINCIPAL_INVESTIGATORS, asList(getTempPerson(REF_PERSONS))); // mref
					targetEntity.set(INSTITUTES, asList(getTempPerson(REF_JURISTIC_PERSONS))); // mref
					targetEntity.set(DISEASE, asList(getTempDisease())); // mref
					targetEntity.set(OMICS, asList(getTempOmics())); // mref
					targetEntity.set(DATA_CATEGORIES, asList(getTempDataCategories())); // mref

					targetEntity.set(NAME, null); // nillable
					targetEntity.set(ACRONYM, null); // nillable
					targetEntity.set(DESCRIPTION, null); // nillable
					targetEntity.set(PUBLICATIONS, null); // nillable
					targetEntity.set(BIOBANKS, null); // nillable
					targetEntity.set(WEBSITE, "http://www.parelsnoer.org/"); // nillable
					targetEntity.set(BIOBANK_SAMPLE_ACCESS_FEE, null); // nillable
					targetEntity.set(BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS, null); // nillable
					targetEntity.set(BIOBANK_SAMPLE_ACCESS_DESCRIPTION, null); // nillable
					targetEntity
							.set(BIOBANK_SAMPLE_ACCESS_URI, "http://www.parelsnoer.org/page/Onderzoeker"); // nillable
					targetEntity.set(BIOBANK_DATA_ACCESS_FEE, null); // nillable
					targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, null); // nillable
					targetEntity.set(BIOBANK_DATA_ACCESS_DESCRIPTION, null); // nillable
					targetEntity.set(BIOBANK_DATA_ACCESS_URI, "http://www.parelsnoer.org/page/Onderzoeker"); // nillable

					biobankExists = false;
				}

				// map data from ProMISe
				targetEntity.set(BbmriNlCheatSheet.ID, project.getString(BIOBANK_ID));
				targetEntity.set(TYPE, toTypes(promiseBiobankEntity.getString("COLLECTION_TYPE"))); // mref
				targetEntity.set(MATERIALS, getMaterialTypes(credentials)); // mref
				targetEntity.set(SEX, toGenders(promiseBiobankEntity.getString("SEX"))); // mref
				targetEntity.set(AGE_LOW, promiseBiobankEntity.getString("AGE_LOW")); // nillable
				targetEntity.set(AGE_HIGH, promiseBiobankEntity.getString("AGE_HIGH")); // nillable
				targetEntity.set(AGE_UNIT, toAgeType(promiseBiobankEntity.getString("AGE_UNIT")));
				targetEntity.set(NUMBER_OF_DONORS, promiseBiobankEntity.getString("NUMBER_DONORS")); // nillable

				if (biobankExists)
				{
					LOG.info("Updating Sample Collection with id " + targetEntity.getIdValue());
					dataService.update(SAMPLE_COLLECTIONS_ENTITY, targetEntity);
				}
				else
				{
					LOG.info("Adding new Sample Collection with id " + targetEntity.getIdValue());
					dataService.add(SAMPLE_COLLECTIONS_ENTITY, targetEntity);
				}

				report.setStatus(Status.SUCCESS);
			});
		}
		catch (Exception e)
		{
			report.setStatus(Status.ERROR);
			report.setMessage(e.getMessage());

			LOG.error("Something went wrong: {}", e);
		}

		return report;
	}

	private Iterable<Entity> getMaterialTypes(Entity credentials)
	{
		try
		{
			// Parse samples
			promiseDataParser.parse(credentials, 1,
					promiseSampleEntity -> toMaterialTypes(promiseSampleEntity.getString("MATERIAL_TYPES"),
							promiseSampleEntity.getString("MATERIAL_TYPES_SUB")));
		}
		catch (IOException e)
		{
			LOG.error("Something went wrong: {}", e);
		}

		if (!unknownMaterialTypes.isEmpty()) throw new RuntimeException(
				"Unknown ProMISe material types: [" + String.join(",", unknownMaterialTypes) + "]");

		Iterable<Entity> materialTypes = dataService
				.findAll(REF_MATERIAL_TYPES, transform(materialTypeIds, id -> (Object) id).stream())
				.collect(Collectors.toList());

		if (Iterables.isEmpty(materialTypes))
			throw new RuntimeException("Couldn't find mappings for some of the material types in:" + materialTypeIds);
		return materialTypes;
	}

	private Object getTempDataCategories()
	{
		return dataService.findOne(REF_DATA_CATEGORY_TYPES, "NAV");
	}

	private Entity getTempOmics()
	{
		return dataService.findOne(REF_OMICS_DATA_TYPES, "NAV");
	}

	private Entity getTempDisease()
	{
		return dataService.findOne(REF_DISEASE_TYPES, "NI");
	}

	private Entity getTempPerson(String targetRefEntity)
	{
		Entity person = dataService.findOne(targetRefEntity, "Unknown");
		if (person == null)
		{
			EntityMetaData personsMetaData = requireNonNull(dataService.getEntityMetaData(targetRefEntity));
			person = new MapEntity(personsMetaData);

			person.set("id", "Unknown");
			person.set("name", "Unknown");
			person.set("country", dataService.findOne(REF_COUNTRIES, "NL"));
			dataService.add(targetRefEntity, person);
		}

		return person;
	}

	private Entity toAgeType(String ageType)
	{
		return dataService.findOne(REF_AGE_TYPES, ageType);
	}

	private Iterable<Entity> toGenders(String promiseSex)
	{
		Object[] sexes = promiseSex.split(",");
		Stream<Object> ids = asList(sexes).stream();

		Iterable<Entity> genderTypes = dataService.findAll(REF_GENDER_TYPES, ids).collect(toList());
		if (!genderTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown '" + REF_GENDER_TYPES + "' [" + ids.toString() + "]");
		}
		return genderTypes;
	}

	private Iterable<Entity> toTypes(String promiseTypes)
	{
		Object[] types = promiseTypes.split(",");
		Stream<Object> ids = asList(types).stream();

		Iterable<Entity> collectionTypes = dataService.findAll(REF_COLLECTION_TYPES, ids).collect(toList());

		if (!collectionTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown '" + REF_COLLECTION_TYPES + "' [" + promiseTypes + "]");
		}
		return collectionTypes;
	}

	private void toMaterialTypes(String type, String tissue)
	{
		if (type.equals("weefsel") && tissue != null)
		{
			if (tissueTypesMap.containsKey(tissue)) tissueTypesMap.get(tissue).forEach(t -> materialTypeIds.add(t));
			else unknownMaterialTypes.add(tissue);
		}
		else
		{
			if (materialTypesMap.containsKey(type)) materialTypesMap.get(type).forEach(t -> materialTypeIds.add(t));
			else unknownMaterialTypes.add(type);
		}
	}
}
