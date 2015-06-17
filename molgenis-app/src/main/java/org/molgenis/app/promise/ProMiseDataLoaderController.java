package org.molgenis.app.promise;

import static org.molgenis.app.promise.ProMiseDataLoaderController.URI;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.hash.Hashing;

@Controller
@RequestMapping(URI)
public class ProMiseDataLoaderController extends MolgenisPluginController
{
	public static final String ID = "promiseloader";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ProMiseDataParser promiseDataParser;
	private final DataService dataService;

	@Autowired
	public ProMiseDataLoaderController(ProMiseDataParser proMiseDataParser, DataService dataService)
	{
		super(URI);
		this.promiseDataParser = proMiseDataParser;
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-promiseloader";
	}

	@RequestMapping(value = "map", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public void map() throws IOException
	{
		EntityMetaData targetEntityMetaData = dataService.getEntityMetaData("bbmri_nl_sample_collections");

		Iterable<Entity> promiseBiobankEntities = promiseDataParser.parse(0);
		Iterable<Entity> promiseSampleEntities = promiseDataParser.parse(1);
		for (Entity promiseBiobankEntity : promiseBiobankEntities)
		{
			Entity promiseBiobankSamplesEntity = getPromiseBiobankSamples(promiseBiobankEntity, promiseSampleEntities);

			MapEntity targetEntity = new MapEntity(targetEntityMetaData);
			targetEntity.set("id", "promise_" + promiseBiobankEntity.getString("DEELBIOBANK"));
			targetEntity.set("name", promiseBiobankEntity.getString("TITEL"));
			// targetEntity.set("acronym", null); //TODO Vaste mapping op basis van ID
			targetEntity.set("type", toTypes(promiseBiobankEntity.getString("TYPEBIOBANK")));
			targetEntity.set("disease", toDiseases()); // TODO discuss with DvE
			targetEntity.set("data_categories", toDataCategories(promiseBiobankEntity, promiseBiobankSamplesEntity));
			targetEntity.set("materials", toMaterials(promiseBiobankSamplesEntity));
			targetEntity.set("omics", toOmics(promiseBiobankSamplesEntity));
			targetEntity.set("sex", toSex(promiseBiobankSamplesEntity));
			targetEntity.set("age_low", toAgeLow(promiseBiobankSamplesEntity));
			targetEntity.set("age_high", toAgeHigh(promiseBiobankSamplesEntity));
			targetEntity.set("age_unit", dataService.findOne("bbmri_nl_age_types", "YEAR"));
			targetEntity.set("numberOfDonors", 1); // FIXME
			targetEntity.set("description", promiseBiobankEntity.getString("OMSCHRIJVING"));
			// targetEntity.set("publications", null);
			targetEntity.set("contact_person", getCreatePersons(promiseBiobankEntity));
			// targetEntity.set("principal_investigators", null);
			// targetEntity.set("institutes", null);
			targetEntity.set("biobanks", "Radboud Biobank");
			// targetEntity.set("website", null);
			// targetEntity.set("sample_access", null);
			targetEntity.set("biobankSampleAccessFee", false);
			targetEntity.set("biobankSampleAccessJointProjects", true);
			// targetEntity.set("biobankSampleAccessDescription", null);
			targetEntity
					.set("biobankSampleAccessURI", "http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/");
			// targetEntity.set("data_access", null);
			targetEntity.set("biobankDataAccessFee", false);
			targetEntity.set("biobankDataAccessJointProjects", true);
			// targetEntity.set("biobankDataAccessDescription", null);
			targetEntity.set("biobankDataAccessURI", "http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/");

			dataService.add("bbmri_nl_sample_collections", targetEntity);
		}
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
			return null;
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
			newPerson.set("country", dataService.findOne("bbmri_nl_countries", "NL")); // TODO what to put here, this is
																						// a required attribute?
			dataService.add("bbmri_nl_persons", newPerson);

			return Collections.singletonList(newPerson);
		}
	}

	private Integer toAgeLow(Entity promiseBiobankSamplesEntity)
	{
		Integer ageLow;
		String geboorteDatum = promiseBiobankSamplesEntity.getString("GEBOORTEDATUM");
		if (geboorteDatum != null && !geboorteDatum.isEmpty())
		{
			LocalDate start = LocalDate.parse(geboorteDatum, DateTimeFormatter.ISO_DATE_TIME);
			LocalDate end = LocalDate.now();
			return (int) ChronoUnit.YEARS.between(start, end);
		}
		else
		{
			ageLow = null;
		}
		return ageLow;
	}

	private Integer toAgeHigh(Entity promiseBiobankSamplesEntity)
	{
		Integer ageHigh;
		String geboorteDatum = promiseBiobankSamplesEntity.getString("GEBOORTEDATUM");
		if (geboorteDatum != null && !geboorteDatum.isEmpty())
		{
			LocalDate start = LocalDate.parse(geboorteDatum, DateTimeFormatter.ISO_DATE_TIME);
			LocalDate end = LocalDate.now();
			return (int) ChronoUnit.YEARS.between(start, end);
		}
		else
		{
			ageHigh = null;
		}
		return ageHigh;
	}

	// Mapping, meerdere waarden:
	// 1 = FEMALE
	// 2 = MALE
	// 3 = UNKNOWN
	private Iterable<Entity> toSex(Entity promiseBiobankSamplesEntity)
	{
		Set<Object> genderTypeIds = new LinkedHashSet<Object>();

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

		if (genderTypeIds.isEmpty())
		{
			genderTypeIds.add("NAV");
		}
		return dataService.findAll("bbmri_nl_gender_types", genderTypeIds);
	}

	private Iterable<Entity> toTypes(String promiseTypeBiobank)
	{
		if (promiseTypeBiobank == null || promiseTypeBiobank.isEmpty())
		{
			return Arrays.asList(dataService.findOne("bbmri_nl_collection_types", "OTHER"));
		}

		switch (promiseTypeBiobank)
		{
			case "0":
				return Arrays.asList(dataService.findOne("bbmri_nl_collection_types", "OTHER"));
			case "1":
				return Arrays.asList(dataService.findOne("bbmri_nl_collection_types", "DISEASE_SPECIFIC"));
			case "2":
				return Arrays.asList(dataService.findOne("bbmri_nl_collection_types", "POPULATION_BASED"));
			default:
				throw new RuntimeException("Unknown biobank type [" + promiseTypeBiobank + "]");
		}
	}

	private Iterable<Entity> toDiseases()
	{
		return Arrays.asList(dataService.findOne("bbmri_nl_disease_types", "NAV")); // FIXME
	}

	private Iterable<Entity> toDataCategories(Entity promiseBiobankEntity, Entity promiseBiobankSamplesEntity)
	{
		Set<Object> dataCategoryTypeIds = new LinkedHashSet<Object>();

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

		if (dataCategoryTypeIds.isEmpty())
		{
			dataCategoryTypeIds.add("NAV");
		}
		return dataService.findAll("bbmri_nl_data_category_types", dataCategoryTypeIds);
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
	private Iterable<Entity> toMaterials(Entity promiseBiobankSampleEntity)
	{
		Set<Object> materialTypeIds = new LinkedHashSet<Object>();

		if ("1".equals(promiseBiobankSampleEntity.getString("DNA"))
				|| "1".equals(promiseBiobankSampleEntity.getString("DNABEENMERG")))
		{
			materialTypeIds.add("DNA");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("BLOED")))
		{
			materialTypeIds.add("WHOLE_BLOOD");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("BLOEDPLASMA")))
		{
			materialTypeIds.add("PLASMA");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("BLOEDSERUM")))
		{
			materialTypeIds.add("SERUM");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("WEEFSELSOORT")))
		{
			materialTypeIds.add("TISSUE_PARAFFIN_EMBEDDED");
		}
		else if ("2".equals(promiseBiobankSampleEntity.getString("WEEFSELSOORT")))
		{
			materialTypeIds.add("TISSUE_FROZEN");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("URINE")))
		{
			materialTypeIds.add("URINE");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("SPEEKSEL")))
		{
			materialTypeIds.add("SALIVA");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("FECES")))
		{
			materialTypeIds.add("FECES");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString("RNA"))
				|| "1".equals(promiseBiobankSampleEntity.getString("RNABEENMERG")))
		{
			materialTypeIds.add("RNA");
		}

		if ("1".equals(promiseBiobankSampleEntity.getString(""))
				|| "1".equals(promiseBiobankSampleEntity.getString("LIQUOR"))
				|| "1".equals(promiseBiobankSampleEntity.getString("CELLBEENMERG"))
				|| "1".equals(promiseBiobankSampleEntity.getString("MONONUCLBLOED"))
				|| "1".equals(promiseBiobankSampleEntity.getString("MONONUCMERG"))
				|| "1".equals(promiseBiobankSampleEntity.getString("GRANULOCYTMERG"))
				|| "1".equals(promiseBiobankSampleEntity.getString("LIQUOR"))
				|| "1".equals(promiseBiobankSampleEntity.getString("MONOCYTMERG"))
				|| "1".equals(promiseBiobankSampleEntity.getString("GASTROINTMUC"))
				|| "1".equals(promiseBiobankSampleEntity.getString("MICROBIOOM")))
		{
			materialTypeIds.add("OTHER");
		}

		if (materialTypeIds.isEmpty())
		{
			materialTypeIds.add("NAV");
		}
		return dataService.findAll("bbmri_nl_material_types", materialTypeIds);
	}

	// Mapping, meerdere waarden:
	// GWAS=1 GENOMICS
	// VOOR
	private Iterable<Entity> toOmics(Entity promiseBiobankSampleEntity)
	{
		Set<Object> omicsTypeIds = new LinkedHashSet<Object>();

		if ("1".equals(promiseBiobankSampleEntity.getString("GWASOMNI"))
				|| "1".equals(promiseBiobankSampleEntity.getString("GWAS370CNV"))
				|| "1".equals(promiseBiobankSampleEntity.getString("EXOOMCHIP")))
		{
			omicsTypeIds.add("GENOMICS");
		}

		if (omicsTypeIds.isEmpty())
		{
			omicsTypeIds.add("NAV");
		}
		return dataService.findAll("bbmri_nl_omics_data_types", omicsTypeIds);
	}

	private Entity getPromiseBiobankSamples(Entity promiseBiobankEntity, Iterable<Entity> promiseSampleEntities)
	{
		String biobankId = promiseBiobankEntity.getString("ID") + promiseBiobankEntity.getString("IDAA");
		for (Entity promiseSampleEntity : promiseSampleEntities)
		{
			String biobankSamplesId = promiseSampleEntity.getString("ID") + promiseSampleEntity.getString("IDAA");
			if (biobankId.equals(biobankSamplesId))
			{
				return promiseSampleEntity;
			}
		}
		return null;
	}

	@RequestMapping(value = "load", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public void load() throws IOException
	{
		Map<Integer, String> seqNrMap = new LinkedHashMap<Integer, String>();
		seqNrMap.put(0, "Biobanks");
		seqNrMap.put(1, "Samples");
		seqNrMap.put(10, "Queries");
		seqNrMap.put(11, "Tables");
		seqNrMap.put(12, "Items");
		seqNrMap.put(13, "Headers");
		seqNrMap.put(14, "Labels");
		seqNrMap.put(15, "Centers");
		seqNrMap.put(16, "Users");
		seqNrMap.put(17, "Logins");
		seqNrMap.put(18, "Contact");

		for (Map.Entry<Integer, String> entry : seqNrMap.entrySet())
		{
			load(entry.getKey(), entry.getValue());
		}
	}

	private void load(Integer seqNr, String label) throws IOException
	{
		Iterable<Entity> entities = promiseDataParser.parse(seqNr);

		String promiseEntityName = "promise" + '_' + label.toLowerCase();
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(promiseEntityName);
		entityMetaData.setLabel("ProMISe " + label);
		entityMetaData.addAttribute("_id").setIdAttribute(true).setAuto(true).setVisible(false).setNillable(false);

		Set<String> attrNames = new HashSet<String>();
		for (Entity entity : entities)
		{
			for (String attrName : entity.getAttributeNames())
			{
				if (!attrNames.contains(attrName))
				{
					entityMetaData.addAttribute(attrName).setDataType(MolgenisFieldTypes.TEXT).setNillable(true);
					attrNames.add(attrName);
				}
			}
		}

		if (dataService.getMeta().getEntityMetaData(promiseEntityName) != null)
		{
			dataService.getMeta().deleteEntityMeta(promiseEntityName);
		}
		dataService.getMeta().addEntityMeta(entityMetaData);
		dataService.add(promiseEntityName, entities);
	}
}
