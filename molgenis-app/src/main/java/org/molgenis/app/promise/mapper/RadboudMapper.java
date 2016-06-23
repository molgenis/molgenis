package org.molgenis.app.promise.mapper;

import org.molgenis.app.promise.client.PromiseDataParser;
import org.molgenis.app.promise.mapper.MappingReport.Status;
import org.molgenis.app.promise.model.PromiseMappingProjectMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.hash.Hashing.md5;
import static java.nio.charset.Charset.forName;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.join;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.*;

@Component
public class RadboudMapper implements PromiseMapper, ApplicationListener<ContextRefreshedEvent>
{
	private final String MAPPER_ID = "RADBOUD";

	private final PromiseMapperFactory promiseMapperFactory;
	private final PromiseDataParser promiseDataParser;
	private final DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(RadboudMapper.class);

	@Autowired
	public RadboudMapper(PromiseDataParser promiseDataParser, DataService dataService,
			PromiseMapperFactory promiseMapperFactory)
	{
		this.promiseDataParser = Objects.requireNonNull(promiseDataParser);
		this.dataService = Objects.requireNonNull(dataService);
		this.promiseMapperFactory = Objects.requireNonNull(promiseMapperFactory);
	}

	@Override
	public String getId()
	{
		return MAPPER_ID;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0)
	{
		promiseMapperFactory.registerMapper(MAPPER_ID, this);
	}

	@Override
	public MappingReport map(Entity project)
	{
		requireNonNull(project);

		MappingReport report = new MappingReport();

		List<Entity> sampleCollectionsToAdd = newArrayList();
		List<Entity> sampleCollectionsToUpdate = newArrayList();

		Map<String, List<Entity>> sampleIdMap = newHashMap();

		try
		{
			Entity credentials = project.getEntity(PromiseMappingProjectMetaData.CREDENTIALS);

			LOG.info("Generating RADBOUD sample map");
			promiseDataParser.parse(credentials, 1, sampleEntity -> {
				String samplesId = sampleEntity.getString(XML_ID) + sampleEntity.getString(XML_IDAA);
				sampleIdMap.putIfAbsent(samplesId, newArrayList());
				sampleIdMap.get(samplesId).add(sampleEntity);
			});

			LOG.info("Downloading biobank data for " + project.getString(NAME));
			promiseDataParser.parse(credentials, 0, biobankEntity -> {
				LOG.info("Mapping biobank: " + biobankEntity.getString(XML_TITLE));

				EntityMetaData targetEntityMetaData = requireNonNull(
						dataService.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY));

				String biobankId = biobankEntity.getString(XML_ID) + biobankEntity.getString(XML_IDAA);
				Iterable<Entity> biobankSamplesEntities = sampleIdMap.get(biobankId);

				Entity targetEntity = dataService.findOne(SAMPLE_COLLECTIONS_ENTITY, biobankId);

				boolean biobankExists = true;
				if (targetEntity == null)
				{
					targetEntity = new MapEntity(targetEntityMetaData);

					// fill hand coded fields with dummy data the first time this biobank is added
					targetEntity.set(ACRONYM, null);
					targetEntity.set(PUBLICATIONS, null);
					targetEntity.set(BIOBANK_SAMPLE_ACCESS_URI,
							"http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/");
					targetEntity.set(WEBSITE, "http://www.radboudbiobank.nl/");
					targetEntity.set(BIOBANK_DATA_ACCESS_URI,
							"http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/");
					targetEntity.set(DISEASE, getMrefEntities(REF_DISEASE_TYPES, "NAV"));
					targetEntity.set(PRINCIPAL_INVESTIGATORS, toPrincipalInvestigators());
					targetEntity.set(INSTITUTES, getMrefEntities(REF_JURISTIC_PERSONS, "83"));

					biobankExists = false;
				}

				targetEntity.set(ID, biobankId);
				targetEntity.set(NAME, biobankEntity.getString(XML_TITLE));
				targetEntity.set(TYPE, toTypes(biobankEntity.getString("TYPEBIOBANK")));
				targetEntity.set(DATA_CATEGORIES, toDataCategories(biobankEntity, biobankSamplesEntities));
				targetEntity.set(MATERIALS, toMaterials(biobankSamplesEntities));
				targetEntity.set(OMICS, toOmics(biobankSamplesEntities));
				targetEntity.set(SEX, toSex(biobankSamplesEntities));
				targetEntity.set(AGE_LOW, toAgeMinOrMax(biobankSamplesEntities, true));
				targetEntity.set(AGE_HIGH, toAgeMinOrMax(biobankSamplesEntities, false));
				targetEntity.set(AGE_UNIT, getXrefEntity(REF_AGE_TYPES, "YEAR"));
				targetEntity.set(NUMBER_OF_DONORS, size(biobankSamplesEntities));
				targetEntity.set(DESCRIPTION, biobankEntity.getString(XML_DESCRIPTION));
				targetEntity.set(CONTACT_PERSON, getContactPersons(biobankEntity));
				targetEntity.set(BIOBANKS, getMrefEntities(REF_BIOBANKS, "RBB"));
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_FEE, true);
				targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, true);
				targetEntity.set(BIOBANK_DATA_SAMPLE_ACCESS_DESCRIPTION, null);  // Don't fill in
				targetEntity.set(BIOBANK_DATA_ACCESS_FEE, true);
				targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, true);
				targetEntity.set(BIOBANK_DATA_ACCESS_DESCRIPTION, null);  // Don't fill in

				if (biobankExists) sampleCollectionsToUpdate.add(targetEntity);
				else sampleCollectionsToAdd.add(targetEntity);
			});

			LOG.info("Adding {} entities to {}", sampleCollectionsToAdd.size(), SAMPLE_COLLECTIONS_ENTITY);
			dataService.add(SAMPLE_COLLECTIONS_ENTITY, sampleCollectionsToAdd.stream());

			LOG.info("Updating {} entities in {}", sampleCollectionsToUpdate.size(), SAMPLE_COLLECTIONS_ENTITY);
			dataService.update(SAMPLE_COLLECTIONS_ENTITY, sampleCollectionsToUpdate.stream());

			LOG.info("Finished mapping RADBOUD biobanks");
			report.setStatus(Status.SUCCESS);
		}
		catch (Exception e)
		{
			report.setStatus(Status.ERROR);
			report.setMessage(e.getMessage());

			LOG.warn("Error in mapping response to entities", e);
		}

		sampleIdMap.clear();
		return report;
	}

	private Iterable<Entity> getMrefEntities(String entityName, String value)
	{
		Entity entity = dataService.findOne(entityName, value);
		if (entity == null)
		{
			throw new RuntimeException("Unknown '" + entityName + "' [" + value + "]");
		}
		return singletonList(entity);
	}

	private Entity getXrefEntity(String entityName, String value)
	{
		Entity entity = dataService.findOne(entityName, value);
		if (entity == null)
		{
			throw new RuntimeException("Unknown '" + entityName + "' [" + value + "]");
		}
		return entity;
	}

	private Iterable<Entity> toPrincipalInvestigators()
	{
		MapEntity principalInvestigators = new MapEntity(dataService.getEntityMetaData("bbmri_nl_persons"));
		principalInvestigators.set("id", new UuidGenerator().generateId());
		Entity countryNl = dataService.findOne("bbmri_nl_countries", "NL");
		if (countryNl == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_countries' [NL]");
		}
		principalInvestigators.set("country", countryNl);
		dataService.add("bbmri_nl_persons", principalInvestigators);
		return singletonList(principalInvestigators);
	}

	public Iterable<Entity> getContactPersons(Entity biobankEntity)
	{
		String[] contactPerson = biobankEntity.getString("CONTACTPERS").split(",");
		String address1 = biobankEntity.getString("ADRES1");
		String address2 = biobankEntity.getString("ADRES2");
		String postalCode = biobankEntity.getString("POSTCODE");
		String city = biobankEntity.getString("PLAATS");
		String[] email = biobankEntity.getString("EMAIL").split(" ");
		String phoneNumber = biobankEntity.getString("TELEFOON");

		List<Entity> persons = newArrayList();
		for (int i = 0; i < contactPerson.length; i++)
		{
			StringBuilder contentBuilder = new StringBuilder();
			if (contactPerson[i] != null && !contactPerson[i].isEmpty()) contentBuilder.append(contactPerson[i]);
			if (address1 != null && !address1.isEmpty()) contentBuilder.append(address1);
			if (address2 != null && !address2.isEmpty()) contentBuilder.append(address2);
			if (postalCode != null && !postalCode.isEmpty()) contentBuilder.append(postalCode);
			if (city != null && !city.isEmpty()) contentBuilder.append(city);
			if (email[i] != null && !email[i].isEmpty()) contentBuilder.append(email[i]);
			if (phoneNumber != null && !phoneNumber.isEmpty()) contentBuilder.append(phoneNumber);

			String personId = md5().newHasher().putString(contentBuilder, forName("UTF-8")).hash().toString();
			Entity person = dataService.findOne("bbmri_nl_persons", personId);

			if (person != null)
			{
				persons.add(person);
			}
			else
			{
				MapEntity newPerson = new MapEntity(dataService.getEntityMetaData(REF_PERSONS));
				newPerson.set("id", personId);
				newPerson.set("first_name", contactPerson[i]);
				newPerson.set("last_name", contactPerson[i]);
				newPerson.set("phone", phoneNumber);
				newPerson.set("email", email[i]);

				StringBuilder addressBuilder = new StringBuilder();
				if (address1 != null && !address1.isEmpty()) addressBuilder.append(address1);
				if (address2 != null && !address2.isEmpty())
				{
					if (address1 != null && !address1.isEmpty()) addressBuilder.append(' ');
					addressBuilder.append(address2);
				}
				if (addressBuilder.length() > 0)
				{
					newPerson.set("address", addressBuilder.toString());
				}
				newPerson.set("zip", postalCode);
				newPerson.set("city", city);
				Entity countryNl = dataService.findOne("bbmri_nl_countries", "NL");
				if (countryNl == null)
				{
					throw new RuntimeException("Unknown 'bbmri_nl_countries' [NL]");
				}

				// TODO what to put here, this is a required attribute?
				newPerson.set("country", countryNl);

				dataService.add("bbmri_nl_persons", newPerson);
				persons.add(newPerson);
			}

		}
		return persons;
	}

	private Integer toAgeMinOrMax(Iterable<Entity> promiseBiobankSamplesEntities, boolean lowest)
	{
		Long ageMinOrMax = null;
		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			String birthDate = promiseBiobankSamplesEntity.getString("GEBOORTEDATUM");
			if (birthDate != null && !birthDate.isEmpty())
			{
				LocalDate start = LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE_TIME);
				LocalDate end = LocalDate.now();
				long age = ChronoUnit.YEARS.between(start, end);
				if (ageMinOrMax == null || (lowest && age < ageMinOrMax) || (!lowest && age > ageMinOrMax))
				{
					ageMinOrMax = age;
				}
			}
		}
		return ageMinOrMax != null ? ageMinOrMax.intValue() : null;
	}

	private Iterable<Entity> toSex(Iterable<Entity> promiseBiobankSamplesEntities) throws RuntimeException
	{
		Set<Object> genderTypeIds = new LinkedHashSet<Object>();

		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			if ("1".equals(promiseBiobankSamplesEntity.getString("GESLACHT")))
			{
				genderTypeIds.add("FEMALE");
			}
			if ("2".equals(promiseBiobankSamplesEntity.getString("GESLACHT")))
			{
				genderTypeIds.add("MALE");
			}
			if ("3".equals(promiseBiobankSamplesEntity.getString("GESLACHT")))
			{
				genderTypeIds.add("UNKNOWN");
			}
		}

		if (genderTypeIds.isEmpty())
		{
			genderTypeIds.add("NAV");
		}
		Iterable<Entity> genderTypes = dataService.findAll("bbmri_nl_gender_types", genderTypeIds.stream())
				.collect(toList());
		if (!genderTypeIds.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_gender_types' [" + join(genderTypeIds, ',') + "]");
		}
		return genderTypes;
	}

	private Iterable<Entity> toTypes(String promiseTypeBiobank)
	{
		String collectionTypeId;
		if (promiseTypeBiobank == null || promiseTypeBiobank.isEmpty())
		{
			collectionTypeId = "OTHER";
		}
		else
		{
			switch (promiseTypeBiobank)
			{
				case "0":
					collectionTypeId = "OTHER";
					break;
				case "1":
					collectionTypeId = "DISEASE_SPECIFIC";
					break;
				case "2":
					collectionTypeId = "POPULATION_BASED";
					break;
				default:
					throw new RuntimeException("Unknown biobank type [" + promiseTypeBiobank + "]");
			}
		}
		Entity collectionType = dataService.findOne("bbmri_nl_collection_types", collectionTypeId);
		if (collectionType == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_collection_types' [" + collectionTypeId + "]");
		}
		return asList(collectionType);
	}

	private Iterable<Entity> toDataCategories(Entity promiseBiobankEntity,
			Iterable<Entity> promiseBiobankSamplesEntities)
	{
		Set<Object> dataCategoryTypeIds = new LinkedHashSet<Object>();

		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			if (promiseBiobankSamplesEntity != null)
			{
				String deelbiobanks = promiseBiobankSamplesEntity.getString("DEELBIOBANKS");
				if (deelbiobanks != null && Integer.valueOf(deelbiobanks) >= 1)
				{
					dataCategoryTypeIds.add("BIOLOGICAL_SAMPLES");
				}
			}

			if ("1".equals(promiseBiobankEntity.getString("VOORGESCH")))
			{
				dataCategoryTypeIds.add("OTHER");
			}

			if ("1".equals(promiseBiobankEntity.getString("FAMANAM")))
			{
				dataCategoryTypeIds.add("GENEALOGICAL_RECORDS");
			}

			if ("1".equals(promiseBiobankEntity.getString("BEHANDEL")))
			{
				dataCategoryTypeIds.add("MEDICAL_RECORDS");
			}

			if ("1".equals(promiseBiobankEntity.getString("FOLLOWUP")))
			{
				dataCategoryTypeIds.add("OTHER");
			}

			if ("1".equals(promiseBiobankEntity.getString("BEELDEN")))
			{
				dataCategoryTypeIds.add("IMAGING_DATA");
			}

			if ("1".equals(promiseBiobankEntity.getString("VRAGENLIJST")))
			{
				dataCategoryTypeIds.add("SURVEY_DATA");
			}

			if ("1".equals(promiseBiobankEntity.getString("OMICS")))
			{
				dataCategoryTypeIds.add("PHYSIOLOGICAL_BIOCHEMICAL_MEASUREMENTS");
			}

			if ("1".equals(promiseBiobankEntity.getString("ROUTINEBEP")))
			{
				dataCategoryTypeIds.add("PHYSIOLOGICAL_BIOCHEMICAL_MEASUREMENTS");
			}

			if ("1".equals(promiseBiobankEntity.getString("GWAS")))
			{
				dataCategoryTypeIds.add("OTHER");
			}

			if ("1".equals(promiseBiobankEntity.getString("HISTOPATH")))
			{
				dataCategoryTypeIds.add("OTHER");
			}

			if ("1".equals(promiseBiobankEntity.getString("OUTCOME")))
			{
				dataCategoryTypeIds.add("NATIONAL_REGISTRIES");
			}

			if ("1".equals(promiseBiobankEntity.getString("ANDERS")))
			{
				dataCategoryTypeIds.add("OTHER");
			}
		}

		if (dataCategoryTypeIds.isEmpty())
		{
			dataCategoryTypeIds.add("NAV");
		}

		Iterable<Entity> dataCategoryTypes = dataService
				.findAll("bbmri_nl_data_category_types", dataCategoryTypeIds.stream()).collect(toList());
		if (!dataCategoryTypes.iterator().hasNext())
		{
			throw new RuntimeException(
					"Unknown 'bbmri_nl_data_category_types' [" + join(dataCategoryTypeIds, ',') + "]");
		}
		return dataCategoryTypes;
	}

	private Iterable<Entity> toMaterials(Iterable<Entity> promiseBiobankSamplesEntities)
	{
		Set<Object> materialTypeIds = new LinkedHashSet<Object>();

		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			if ("1".equals(promiseBiobankSamplesEntity.getString("DNA")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("DNABEENMERG")))
			{
				materialTypeIds.add("DNA");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("BLOED")))
			{
				materialTypeIds.add("WHOLE_BLOOD");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("BLOEDPLASMA")))
			{
				materialTypeIds.add("PLASMA");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("BLOEDSERUM")))
			{
				materialTypeIds.add("SERUM");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("WEEFSELSOORT")))
			{
				materialTypeIds.add("TISSUE_PARAFFIN_EMBEDDED");
			}
			else if ("2".equals(promiseBiobankSamplesEntity.getString("WEEFSELSOORT")))
			{
				materialTypeIds.add("TISSUE_FROZEN");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("URINE")))
			{
				materialTypeIds.add("URINE");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("SPEEKSEL")))
			{
				materialTypeIds.add("SALIVA");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("FECES")))
			{
				materialTypeIds.add("FECES");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("RNA")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("RNABEENMERG")))
			{
				materialTypeIds.add("MICRO_RNA");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("GASTROINTMUC")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("LIQUOR")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("CELLBEENMERG")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("MONONUCLBLOED")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("MONONUCMERG")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("GRANULOCYTMERG")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("MONOCYTMERG")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("MICROBIOOM")))
			{
				materialTypeIds.add("OTHER");
			}
		}

		if (materialTypeIds.isEmpty())
		{
			materialTypeIds.add("NAV");
		}
		Iterable<Entity> materialTypes = dataService.findAll("bbmri_nl_material_types", materialTypeIds.stream())
				.collect(toList());
		if (!materialTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_material_types' [" + join(materialTypeIds, ',') + "]");
		}

		return materialTypes;
	}

	private Iterable<Entity> toOmics(Iterable<Entity> promiseBiobankSamplesEntities)
	{
		Set<Object> omicsTypeIds = new LinkedHashSet<Object>();

		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			if ("1".equals(promiseBiobankSamplesEntity.getString("GWASOMNI")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("GWAS370CNV")) || "1"
					.equals(promiseBiobankSamplesEntity.getString("EXOOMCHIP")))
			{
				omicsTypeIds.add("GENOMICS");
			}
		}

		if (omicsTypeIds.isEmpty())
		{
			omicsTypeIds.add("NAV");
		}
		Iterable<Entity> omicsTypes = dataService.findAll("bbmri_nl_omics_data_types", omicsTypeIds.stream())
				.collect(toList());
		if (!omicsTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_omics_data_types' [" + join(omicsTypeIds, ',') + "]");
		}
		return omicsTypes;
	}
}
