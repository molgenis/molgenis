package org.molgenis.app.promise;

import static org.molgenis.app.promise.BbmriNlCheatSheet.AGE_HIGH;
import static org.molgenis.app.promise.BbmriNlCheatSheet.AGE_LOW;
import static org.molgenis.app.promise.BbmriNlCheatSheet.AGE_UNIT;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANKS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_FEE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_JOINT_PROJECTS;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_URI;
import static org.molgenis.app.promise.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_FEE;
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
import static org.molgenis.app.promise.BbmriNlCheatSheet.SAMPLE_COLLECTIONS_ENTITY;
import static org.molgenis.app.promise.BbmriNlCheatSheet.SEX;
import static org.molgenis.app.promise.BbmriNlCheatSheet.TYPE;
import static org.molgenis.app.promise.BbmriNlCheatSheet.WEBSITE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.molgenis.app.promise.MappingReport.Status;
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

import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;

@Component
public class RadboudMapper implements PromiseMapper, ApplicationListener<ContextRefreshedEvent>
{
	private final String ID = "RADBOUD";

	private final ProMiseDataParser promiseDataParser;
	private final DataService dataService;
	private final PromiseMapperFactory promiseMapperFactory;

	private static final Logger LOG = LoggerFactory.getLogger(RadboudMapper.class);

	@Autowired
	public RadboudMapper(ProMiseDataParser promiseDataParser, DataService dataService,
			PromiseMapperFactory promiseMapperFactory)
	{
		this.promiseDataParser = Objects.requireNonNull(promiseDataParser);
		this.dataService = Objects.requireNonNull(dataService);
		this.promiseMapperFactory = Objects.requireNonNull(promiseMapperFactory);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0)
	{
		promiseMapperFactory.registerMapper(ID, this);
	}

	public MappingReport map(String biobankId) throws IOException
	{
		LOG.info("Downloading data for " + biobankId);
		MappingReport report = new MappingReport();
		
		try{
	
			Iterable<Entity> promiseBiobankEntities = promiseDataParser.parse(biobankId, 0);
			Iterable<Entity> promiseSampleEntities = promiseDataParser.parse(biobankId, 1);
	
			EntityMetaData targetEntityMetaData = dataService.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY);
	
			for (Entity promiseBiobankEntity : promiseBiobankEntities)
			{
				Iterable<Entity> promiseBiobankSamplesEntities = getPromiseBiobankSamples(promiseBiobankEntity,
						promiseSampleEntities);
	
				MapEntity targetEntity = new MapEntity(targetEntityMetaData);
				targetEntity.set(BbmriNlCheatSheet.ID, "promise_" + promiseBiobankEntity.getString("DEELBIOBANK"));
				targetEntity.set(NAME, promiseBiobankEntity.getString("TITEL"));
				// targetEntity.set(ACRONYM, null); //TODO Vaste mapping op basis van ID
				targetEntity.set(TYPE, toTypes(promiseBiobankEntity.getString("TYPEBIOBANK")));
				targetEntity.set(DISEASE, toDiseases()); // TODO discuss with DvE
				targetEntity.set(DATA_CATEGORIES, toDataCategories(promiseBiobankEntity, promiseBiobankSamplesEntities));
				targetEntity.set(MATERIALS, toMaterials(promiseBiobankSamplesEntities));
				targetEntity.set(OMICS, toOmics(promiseBiobankSamplesEntities));
				targetEntity.set(SEX, toSex(promiseBiobankSamplesEntities));
				targetEntity.set(AGE_LOW, toAgeMinOrMax(promiseBiobankSamplesEntities, true));
				targetEntity.set(AGE_HIGH, toAgeMinOrMax(promiseBiobankSamplesEntities, false));
				targetEntity.set(AGE_UNIT, toAgeUnit());
				targetEntity.set(NUMBER_OF_DONORS, Iterables.size(promiseBiobankSamplesEntities));
				targetEntity.set(DESCRIPTION, promiseBiobankEntity.getString("OMSCHRIJVING"));
				// targetEntity.set(PUBLICATIONS, null);
				targetEntity.set(CONTACT_PERSON, getCreatePersons(promiseBiobankEntity));
				targetEntity.set(PRINCIPAL_INVESTIGATORS, toPrincipalInvestigators());
				targetEntity.set(INSTITUTES, toInstitutes());
				targetEntity.set(BIOBANKS, toBiobanks());
				targetEntity.set(WEBSITE, "http://www.radboudbiobank.nl/");
				// targetEntity.set(SAMPLE_ACCESS, null);
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_FEE, false);
				targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, true);
				// targetEntity.set(BIOBANK_DATA_SAMPLE_ACCESS_DESCRIPTION", null);
				targetEntity.set(BIOBANK_SAMPLE_ACCESS_URI,
						"http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/");
				// targetEntity.set(DATA_ACCESS, null);
				targetEntity.set(BIOBANK_DATA_ACCESS_FEE, false);
				targetEntity.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, true);
				// targetEntity.set(BIOBANK_DATA_ACCESS_DESCRIPTION, null);
				targetEntity.set(BIOBANK_DATA_ACCESS_URI, "http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/");
	
				dataService.add("bbmri_nl_sample_collections", targetEntity);
			}
			
			report.setStatus(Status.SUCCESS);
		}catch(Exception e){
			report.setStatus(Status.ERROR);
			report.setMessage(e.getMessage());

			LOG.warn(ExceptionUtils.getStackTrace(e));
		}
		return report;
	}

	private Iterable<Entity> toBiobanks()
	{
		Entity biobank = dataService.findOne("bbmri_nl_biobanks", "RBB");
		if (biobank == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_biobanks' [RBB]");
		}
		return Collections.singletonList(biobank);
	}

	private Iterable<Entity> toInstitutes()
	{
		Entity juristicPerson = dataService.findOne("bbmri_nl_juristic_persons", "83");
		if (juristicPerson == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_juristic_persons' [83]");
		}
		return Collections.singletonList(juristicPerson);
	}

	private Entity toAgeUnit()
	{
		Entity ageUnit = dataService.findOne("bbmri_nl_age_types", "YEAR");
		if (ageUnit == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_age_types' [YEAR]");
		}
		return ageUnit;
	}

	private Iterable<Entity> toPrincipalInvestigators()
	{
		// Entity newPerson = dataService.findOne("bbmri_nl_persons", "612");
		// if (newPerson == null)
		// {
		// throw new RuntimeException("Unknown 'bbmri_nl_persons' [612]");
		// }
		// return Collections.singletonList(newPerson);

		MapEntity principalInvestigators = new MapEntity(dataService.getEntityMetaData("bbmri_nl_persons"));
		principalInvestigators.set("id", new UuidGenerator().generateId());
		Entity countryNl = dataService.findOne("bbmri_nl_countries", "NL");
		if (countryNl == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_countries' [NL]");
		}
		principalInvestigators.set("country", countryNl);
		dataService.add("bbmri_nl_persons", principalInvestigators);
		return Collections.singletonList(principalInvestigators);
	}

	private Iterable<Entity> getCreatePersons(Entity promiseBiobankEntity)
	{
		// TODO what if all fields are null?
		String contactPerson = promiseBiobankEntity.getString("CONTACTPERS");
		String address1 = promiseBiobankEntity.getString("ADRES1");
		String address2 = promiseBiobankEntity.getString("ADRES2");
		String postalCode = promiseBiobankEntity.getString("POSTCODE");
		String city = promiseBiobankEntity.getString("PLAATS");
		String email = promiseBiobankEntity.getString("EMAIL");
		String phoneNumber = promiseBiobankEntity.getString("TELEFOON");

		StringBuilder contentBuilder = new StringBuilder();
		if (contactPerson != null && !contactPerson.isEmpty()) contentBuilder.append(contactPerson);
		if (address1 != null && !address1.isEmpty()) contentBuilder.append(address1);
		if (address2 != null && !address2.isEmpty()) contentBuilder.append(address2);
		if (postalCode != null && !postalCode.isEmpty()) contentBuilder.append(postalCode);
		if (city != null && !city.isEmpty()) contentBuilder.append(city);
		if (email != null && !email.isEmpty()) contentBuilder.append(email);
		if (phoneNumber != null && !phoneNumber.isEmpty()) contentBuilder.append(phoneNumber);

		String personId = Hashing.md5().newHasher().putString(contentBuilder, Charset.forName("UTF-8")).hash()
				.toString();
		Entity person = dataService.findOne("bbmri_nl_persons", personId);
		if (person != null)
		{
			return Collections.singletonList(person);
		}
		else
		{
			MapEntity newPerson = new MapEntity(dataService.getEntityMetaData("bbmri_nl_persons"));
			newPerson.set("id", personId);
			// entity.set("first_name", );
			newPerson.set("last_name", contactPerson); // TODO how to split name into first and last name?
			newPerson.set("phone", phoneNumber);
			newPerson.set("email", email);

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
			newPerson.set("country", countryNl); // TODO what to put here, this is
			// a required attribute?
			dataService.add("bbmri_nl_persons", newPerson);

			return Collections.singletonList(newPerson);
		}
	}

	private Integer toAgeMinOrMax(Iterable<Entity> promiseBiobankSamplesEntities, boolean lowest)
	{
		Long ageMinOrMax = null;
		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			String geboorteDatum = promiseBiobankSamplesEntity.getString("GEBOORTEDATUM");
			if (geboorteDatum != null && !geboorteDatum.isEmpty())
			{
				LocalDate start = LocalDate.parse(geboorteDatum, DateTimeFormatter.ISO_DATE_TIME);
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

	// Mapping, meerdere waarden:
	// 1 = FEMALE
	// 2 = MALE
	// 3 = UNKNOWN
	private Iterable<Entity> toSex(Iterable<Entity> promiseBiobankSamplesEntities)
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
		Iterable<Entity> genderTypes = dataService.findAll("bbmri_nl_gender_types", genderTypeIds);
		if (!genderTypeIds.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_gender_types' [" + StringUtils.join(genderTypeIds, ',') + "]");
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
		return Arrays.asList(collectionType);
	}

	private Iterable<Entity> toDiseases()
	{
		Entity diseaseType = dataService.findOne("bbmri_nl_disease_types", "NAV");
		if (diseaseType == null)
		{
			throw new RuntimeException("Unknown 'bbmri_nl_disease_types' [NAV]");
		}
		return Arrays.asList(diseaseType); // FIXME
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

		Iterable<Entity> dataCategoryTypes = dataService.findAll("bbmri_nl_data_category_types", dataCategoryTypeIds);
		if (!dataCategoryTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_data_category_types' ["
					+ StringUtils.join(dataCategoryTypeIds, ',') + "]");
		}
		return dataCategoryTypes;
	}

	// Mapping, meerdere waarden voor velden waar de waarde 1 / ja is:
	// (DNA|DNABEENMERG) = DNA
	// CDNA
	// MICRO_RNA
	// BLOED = WHOLE_BLOOD
	// PERIPHERAL_BLOOD_CELLS
	// BLOEDPLASMA = PLASMA
	// BLOEDSERUM = SERUM
	// WEEFSELSOORT==2 = TISSUE_FROZEN
	// WEEFSELSOORT==1 = TISSUE_PARAFFIN_EMBEDDED
	// CELL_LINES
	// URINE = URINE
	// SPEEKSEL = SALIVA
	// FECES = FECES
	// PATHOGEN
	// (RNA|RNABEENMERG) = RNA
	// (GASTROINTMUC|LIQUOR|CELLBEENMERG|MONONUCLBLOED|MONONUCMERG|GRANULOCYTMERG|MONOCYTMERG|MICROBIOOM) = OTHER
	private Iterable<Entity> toMaterials(Iterable<Entity> promiseBiobankSamplesEntities)
	{
		Set<Object> materialTypeIds = new LinkedHashSet<Object>();

		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			if ("1".equals(promiseBiobankSamplesEntity.getString("DNA"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("DNABEENMERG")))
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

			if ("1".equals(promiseBiobankSamplesEntity.getString("RNA"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("RNABEENMERG")))
			{
				materialTypeIds.add("MICRO_RNA");
			}

			if ("1".equals(promiseBiobankSamplesEntity.getString("GASTROINTMUC"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("LIQUOR"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("CELLBEENMERG"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("MONONUCLBLOED"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("MONONUCMERG"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("GRANULOCYTMERG"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("MONOCYTMERG"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("MICROBIOOM")))
			{
				materialTypeIds.add("OTHER");
			}
		}

		if (materialTypeIds.isEmpty())
		{
			materialTypeIds.add("NAV");
		}
		Iterable<Entity> materialTypes = dataService.findAll("bbmri_nl_material_types", materialTypeIds);
		if (!materialTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_material_types' [" + StringUtils.join(materialTypeIds, ',')
					+ "]");
		}

		return materialTypes;
	}

	// Mapping, meerdere waarden:
	// GWAS=1 GENOMICS
	// VOOR
	private Iterable<Entity> toOmics(Iterable<Entity> promiseBiobankSamplesEntities)
	{
		Set<Object> omicsTypeIds = new LinkedHashSet<Object>();

		for (Entity promiseBiobankSamplesEntity : promiseBiobankSamplesEntities)
		{
			if ("1".equals(promiseBiobankSamplesEntity.getString("GWASOMNI"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("GWAS370CNV"))
					|| "1".equals(promiseBiobankSamplesEntity.getString("EXOOMCHIP")))
			{
				omicsTypeIds.add("GENOMICS");
			}
		}

		if (omicsTypeIds.isEmpty())
		{
			omicsTypeIds.add("NAV");
		}
		Iterable<Entity> omicsTypes = dataService.findAll("bbmri_nl_omics_data_types", omicsTypeIds);
		if (!omicsTypes.iterator().hasNext())
		{
			throw new RuntimeException("Unknown 'bbmri_nl_omics_data_types' [" + StringUtils.join(omicsTypeIds, ',')
					+ "]");
		}
		return omicsTypes;
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
