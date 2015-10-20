package org.molgenis.app.promise;

import static java.util.Objects.requireNonNull;
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
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_DISEASE_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_GENDER_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_JURISTIC_PERSONS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_MATERIAL_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_OMICS_DATA_TYPES;
import static org.molgenis.app.promise.BbmriNlCheatSheet.REF_PERSONS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.SAMPLE_COLLECTIONS_ENTITY;
import static org.molgenis.app.promise.BbmriNlCheatSheet.SEX;
import static org.molgenis.app.promise.BbmriNlCheatSheet.TYPE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.WEBSITE;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.molgenis.app.promise.MappingReport.Status;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
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
	public MappingReport map(String projectName) throws IOException
	{
		Entity project = dataService.findOne(PromiseMappingProjectMetaData.FULLY_QUALIFIED_NAME, projectName);
		if (project == null) throw new MolgenisDataException("Project is null");

		MappingReport report = new MappingReport();

		try
		{
			LOG.info("Getting data from ProMISe for " + projectName);
			Iterable<Entity> promiseBiobankEntities = promiseDataParser.parse(project, 0);

			EntityMetaData targetEntityMetaData = requireNonNull(
					dataService.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY));

			for (Entity promiseBiobankEntity : promiseBiobankEntities)
			{
				Entity targetEntity = dataService.findOne(SAMPLE_COLLECTIONS_ENTITY, project.getString("biobank_id"));

				boolean biobankExists = true;
				if (targetEntity == null)
				{
					targetEntity = new MapEntity(targetEntityMetaData);

					// fill hand coded fields with dummy data the first time this biobank is added
					targetEntity.set(CONTACT_PERSON, getTempPerson());
					targetEntity.set(PRINCIPAL_INVESTIGATORS, getTempPerson());
					targetEntity.set(INSTITUTES, getTempJuristicPerson());
					targetEntity.set(DISEASE, getTempDisease());
					targetEntity.set(OMICS, getTempOmics());
					targetEntity.set(DATA_CATEGORIES, getTempDataCategories());

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
				targetEntity.set(TYPE, toTypes(promiseBiobankEntity.getString("COLLECTION_TYPE")));
				targetEntity.set(MATERIALS, toMaterialTypes(promiseBiobankEntity.getString("MATERIAL_TYPES")));
				targetEntity.set(SEX, toGenders(promiseBiobankEntity.getString("SEX")));
				targetEntity.set(AGE_LOW, promiseBiobankEntity.getString("AGE_LOW")); // nillable
				targetEntity.set(AGE_HIGH, promiseBiobankEntity.getString("AGE_HIGH")); // nillable
				targetEntity.set(AGE_UNIT, promiseBiobankEntity.getString("AGE_UNIT")); // nillable
				targetEntity.set(NUMBER_OF_DONORS, promiseBiobankEntity.getString("NUMBER_DONORS")); // nillable

				if (biobankExists)
				{
					dataService.update(SAMPLE_COLLECTIONS_ENTITY, targetEntity);
				}
				else
				{
					dataService.add(SAMPLE_COLLECTIONS_ENTITY, targetEntity);
				}

				report.setStatus(Status.SUCCESS);
			}

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
		EntityMetaData dataCategoriesMetaData = requireNonNull(dataService.getEntityMetaData(REF_DATA_CATEGORY_TYPES));
		Entity dataCategories = new MapEntity(dataCategoriesMetaData);

		dataCategories.set("id", "TEMP");

		return dataCategories;
	}

	private Entity getTempOmics()
	{
		EntityMetaData omicsMetaData = requireNonNull(dataService.getEntityMetaData(REF_OMICS_DATA_TYPES));
		Entity omics = new MapEntity(omicsMetaData);

		omics.set("id", "TEMP");

		return omics;
	}

	private Entity getTempDisease()
	{
		EntityMetaData diseasesMetaData = requireNonNull(dataService.getEntityMetaData(REF_DISEASE_TYPES));
		Entity disease = new MapEntity(diseasesMetaData);

		disease.set("id", "TEMP");

		return disease;
	}

	private String toAcronym(String acronym)
	{
		return acronym.substring(0, acronym.lastIndexOf("_"));
	}

	private Entity getTempJuristicPerson()
	{
		// TODO: check if TEMP juristic person already exists (and then do nothing)

		EntityMetaData juristicPersonsMetaData = requireNonNull(dataService.getEntityMetaData(REF_JURISTIC_PERSONS));
		Entity juristicPerson = new MapEntity(juristicPersonsMetaData);

		juristicPerson.set("id", "TEMP");
		juristicPerson.set("name", "TEMP");
		juristicPerson.set("country", "NL");

		return juristicPerson;
	}

	private Entity getTempPerson()
	{
		// TODO: check if TEMP contact person already exists (and then do nothing)

		EntityMetaData contactPersonsMetaData = requireNonNull(dataService.getEntityMetaData(REF_PERSONS));
		Entity contactPerson = new MapEntity(contactPersonsMetaData);

		contactPerson.set("id", "TEMP");
		contactPerson.set("country", "NL");

		return contactPerson;
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

	private Iterable<Entity> toTypes(String promiseTypes)
	{
		Object[] types = promiseTypes.split(",");
		Iterable<Object> ids = Arrays.asList(types);

		Iterable<Entity> collectionTypes = dataService.findAll(REF_COLLECTION_TYPES, ids);

		// FIXME fix mysqlrepo returning [null] instead of empty iterable
		if (collectionTypes.iterator().next() == null)
		{
			throw new RuntimeException("Unknown '" + REF_COLLECTION_TYPES + "' [" + promiseTypes + "]");
		}
		return collectionTypes;
	}

	private Iterable<Entity> toMaterialTypes(String promiseTypes)
	{
		Object[] types = promiseTypes.split(",");
		Iterable<Object> ids = Arrays.asList(types);

		Iterable<Entity> materialTypes = dataService.findAll(REF_MATERIAL_TYPES);

		// FIXME fix mysqlrepo returning [null] instead of empty iterable
		if (materialTypes.iterator().next() == null)
		{
			throw new RuntimeException("Unknown '" + REF_MATERIAL_TYPES + "' [" + promiseTypes + "]");
		}
		return materialTypes;
	}

}
