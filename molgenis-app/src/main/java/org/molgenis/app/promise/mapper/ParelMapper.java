package org.molgenis.app.promise.mapper;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.ACRONYM;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.AGE_HIGH;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.AGE_LOW;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.AGE_UNIT;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANKS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_DESCRIPTION;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_FEE;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_JOINT_PROJECTS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_URI;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_DESCRIPTION;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_FEE;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_URI;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.CONTACT_PERSON;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.DATA_CATEGORIES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.DESCRIPTION;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.DISEASE;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.INSTITUTES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.MATERIALS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.NAME;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.NUMBER_OF_DONORS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.OMICS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.PRINCIPAL_INVESTIGATORS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.PUBLICATIONS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_AGE_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_COLLECTION_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_COUNTRIES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_DATA_CATEGORY_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_DISEASE_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_GENDER_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_JURISTIC_PERSONS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_MATERIAL_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_OMICS_DATA_TYPES;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_PERSONS;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.SAMPLE_COLLECTIONS_ENTITY;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.SEX;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.TYPE;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.WEBSITE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
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

import com.google.common.collect.Lists;

import autovalue.shaded.com.google.common.common.collect.Iterables;

@Component
public class ParelMapper implements PromiseMapper, ApplicationListener<ContextRefreshedEvent>
{
	private final String ID = "PAREL";

	private PromiseMapperFactory promiseMapperFactory;
	private PromiseDataParser promiseDataParser;
	private DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(ParelMapper.class);

	private static final HashMap<String, String> materialTypesMap;

	static
	{
		materialTypesMap = new HashMap<>();
		materialTypesMap.put("bloed", "WHOLE BLOOD");
		materialTypesMap.put("bloedplasma", "PLASMA");
		materialTypesMap.put("bloedplasma (EDTA)", "PLASMA");
		materialTypesMap.put("bloedserum", "SERUM");
		materialTypesMap.put("DNA uit beenmergcellen", "DNA");
		materialTypesMap.put("DNA uit bloedcellen", "DNA");
		materialTypesMap.put("feces", "FECES");
		materialTypesMap.put("gastrointestinale mucosa", "TISSUE_FROZEN");
		materialTypesMap.put("liquor (CSF)", "OTHER");
		materialTypesMap.put("mononucleaire celfractie uit beenmerg", "OTHER");
		materialTypesMap.put("mononucleaire celfractie uit bloed", "PERIPHERAL_BLOOD_CELLS");
		materialTypesMap.put("RNA uit beenmergcellen", "RNA");
		materialTypesMap.put("RNA uit bloecellen", "RNA");
		materialTypesMap.put("serum", "SERUM");
		materialTypesMap.put("urine", "URINE");
		materialTypesMap.put("weefsel", "TISSUE_FROZEN");
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
		promiseMapperFactory.registerMapper(ID, this);
	}

	@Override
	public String getId()
	{
		return ID;
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

			Iterable<Entity> promiseBiobankEntities = promiseDataParser.parse(credentials, 0);
			Iterable<Entity> promiseSampleEntities = promiseDataParser.parse(credentials, 1);

			EntityMetaData targetEntityMetaData = requireNonNull(
					dataService.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY));

			// parels should only have one biobank entity
			Entity promiseBiobankEntity = promiseBiobankEntities.iterator().next();

			// find out if a sample collection with this id already exists
			Entity targetEntity = dataService.findOne(SAMPLE_COLLECTIONS_ENTITY, project.getString("biobank_id"));

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
				targetEntity.set(DESCRIPTION, null); // nillable
				targetEntity.set(PUBLICATIONS, null); // nillable
				targetEntity.set(BIOBANKS, null); // nillable
				targetEntity.set(WEBSITE, "http://www.parelsnoer.org/"); // nillable
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_FEE, null); // nillable
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS, null); // nillable
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_DESCRIPTION, null); // nillable
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_URI, "http://www.parelsnoer.org/page/Onderzoeker"); // nillable
				targetEntity.set(BIOBANK_DATA_ACCESS_FEE, null); // nillable
				targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, null); // nillable
				targetEntity.set(BIOBANK_DATA_ACCESS_DESCRIPTION, null); // nillable
				targetEntity.set(BIOBANK_DATA_ACCESS_URI, "http://www.parelsnoer.org/page/Onderzoeker"); // nillable

				biobankExists = false;
			}

			// map data from ProMISe
			targetEntity.set(BbmriNlCheatSheet.ID, project.getString("biobank_id"));
			targetEntity.set(ACRONYM, promiseBiobankEntity.getString("ACRONYM")); // nillable
			targetEntity.set(TYPE, toTypes(promiseBiobankEntity.getString("COLLECTION_TYPE"))); // mref
			targetEntity.set(MATERIALS, toMaterialTypes(promiseSampleEntities)); // mref
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

		}
		catch (Exception e)
		{
			report.setStatus(Status.ERROR);
			report.setMessage(e.getMessage());

			LOG.warn(ExceptionUtils.getStackTrace(e));
		}

		return report;
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
		Entity person = dataService.findOne(targetRefEntity, "TEMP");
		if (person == null)
		{
			EntityMetaData personsMetaData = requireNonNull(dataService.getEntityMetaData(targetRefEntity));
			person = new MapEntity(personsMetaData);

			person.set("id", "TEMP");
			person.set("name", "TEMP");
			person.set("country", dataService.findOne(REF_COUNTRIES, "NL"));
			dataService.add(targetRefEntity, person);
		}

		return person;
	}

	private Entity toAgeType(String ageType)
	{
		return dataService.findOne(REF_AGE_TYPES, ageType);
	}

	private String toAcronym(String acronym)
	{
		return acronym.substring(0, acronym.lastIndexOf("_"));
	}

	private Iterable<Entity> toGenders(String promiseSex)
	{
		Object[] sexes = promiseSex.split(",");
		Iterable<Object> ids = Arrays.asList(sexes);

		Iterable<Entity> genderTypes = dataService.findAll(REF_GENDER_TYPES, ids);
		if (!genderTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown '" + REF_GENDER_TYPES + "' [" + ids.toString() + "]");
		}
		return genderTypes;
	}

	private Iterable<Entity> toTypes(String promiseTypes)
	{
		Object[] types = promiseTypes.split(",");
		Iterable<Object> ids = Arrays.asList(types);

		Iterable<Entity> collectionTypes = dataService.findAll(REF_COLLECTION_TYPES, ids);

		if (!collectionTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown '" + REF_COLLECTION_TYPES + "' [" + promiseTypes + "]");
		}
		return collectionTypes;
	}

	private Iterable<Entity> toMaterialTypes(Iterable<Entity> promiseSampleEntities)
	{
		List<Object> ids = Lists.newArrayList();
		List<String> unknown = Lists.newArrayList();
		for (Entity sample : promiseSampleEntities)
		{
			System.out.println(sample);
			String type = sample.getString("MATERIAL_TYPES");
			if (materialTypesMap.containsKey(type))
			{
				ids.add(materialTypesMap.get(type));
			}
			else
			{
				unknown.add(type);
			}
		}

		if (!unknown.isEmpty())
		{
			throw new RuntimeException("Unknown ProMISe material types: [" + String.join(",", unknown) + "]");
		}

		Iterable<Entity> materialTypes = dataService.findAll(REF_MATERIAL_TYPES, ids);

		if (!materialTypes.iterator().hasNext() || Iterables.size(materialTypes) != Iterables.size(ids))
		{
			List<String> bbmriMaterials = Lists.newArrayList();
			materialTypes.forEach(type -> bbmriMaterials.add(type.getString("id")));

			List<String> promiseMaterials = Lists.newArrayList();
			materialTypes.forEach(type -> promiseMaterials.add(type.getString("MATERIAL_TYPES")));

			throw new RuntimeException("ProMISe material types [" + String.join(",", promiseMaterials)
					+ "] resulted in incomplete mapping to BBMRI material types [" + String.join(",", bbmriMaterials)
					+ "]");
		}
		return materialTypes;
	}

}
