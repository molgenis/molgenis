package org.molgenis.app.promise.mapper;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.hash.Hashing.md5;
import static java.nio.charset.Charset.forName;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.app.promise.mapper.RadboudMapper.XML_IDAA;
import static org.molgenis.app.promise.mapper.RadboudMapper.getBiobankId;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.*;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_DESCRIPTION;

class RadboudBiobankMapper
{
	private static final String XML_TITLE = "TITEL";
	private static final String XML_DESCRIPTION = "OMSCHRIJVING";
	private static final String XML_CONTACT_PERSON = "CONTACTPERS";
	private static final String XML_ADDRESS1 = "ADRES1";
	private static final String XML_ADDRESS2 = "ADRES2";
	private static final String XML_ZIP_CODE = "POSTCODE";
	private static final String XML_LOCATION = "PLAATS";
	private static final String XML_EMAIL = "EMAIL";
	private static final String XML_PHONE = "TELEFOON";
	private static final String XML_TYPEBIOBANK = "TYPEBIOBANK";

	private static final String ACCESS_URI = "http://www.radboudbiobank.nl/nl/collecties/materiaal-opvragen/";

	private DataService dataService;

	private Entity countryNl;
	private EntityMetaData personMetaData;

	RadboudBiobankMapper(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);

		// cache entities that will be used more than once
		countryNl = requireNonNull(dataService.findOne(REF_COUNTRIES, "NL"));
		personMetaData = requireNonNull(dataService.getEntityMetaData(REF_PERSONS));
	}

	/**
	 * Creates a new BBMRI Sample Collection entity from a Radboud Biobank entity.
	 *
	 * @param radboudBiobankEntity the Radboud Biobank Entity to turn into a BBMRI NL Sample Collection
	 * @param samples              a map of Radboud Samples
	 * @param diseases             a map of Radboud Disease Types
	 * @return a mapped BBMRI Sample Collection entity
	 */
	Entity mapNewBiobank(Entity radboudBiobankEntity, RadboudSampleMap samples, RadboudDiseaseMap diseases)
	{
		EntityMetaData targetEntityMetaData = requireNonNull(dataService.getEntityMetaData(SAMPLE_COLLECTIONS_ENTITY));

		Entity newSampleCollection = new DefaultEntity(targetEntityMetaData, dataService);

		// these fields are only set on first create, users will update them manually
		newSampleCollection.set(ACRONYM, null);
		newSampleCollection.set(PUBLICATIONS, null);
		newSampleCollection.set(BIOBANK_SAMPLE_ACCESS_URI, ACCESS_URI);
		newSampleCollection.set(WEBSITE, "http://www.radboudbiobank.nl/");
		newSampleCollection.set(BIOBANK_DATA_ACCESS_URI, ACCESS_URI);
		newSampleCollection.set(PRINCIPAL_INVESTIGATORS, getPrincipalInvestigator(getBiobankId(radboudBiobankEntity)));
		newSampleCollection.set(INSTITUTES, getMrefEntities(REF_JURISTIC_PERSONS, "83"));

		return mapExistingBiobank(radboudBiobankEntity, samples, diseases, newSampleCollection);
	}

	/**
	 * Updates an existing BBMRI Sample Collection entity with data from a Radboud Biobank Entity.
	 *
	 * @param radboudBiobankEntity     the entity from which to collect data
	 * @param samples                  a map of Radboud Samples
	 * @param diseases                 a map of Radboud Disease Types
	 * @param existingSampleCollection a BBMRI Sample Collection entity
	 * @return a mapped BBMRI Sample Collection entity
	 */
	Entity mapExistingBiobank(Entity radboudBiobankEntity, RadboudSampleMap samples, RadboudDiseaseMap diseases,
			Entity existingSampleCollection)
	{
		String biobankId = getBiobankId(radboudBiobankEntity);

		// these fields are fetched from Promise and will always be overwritten
		existingSampleCollection.set(ID, biobankId);
		existingSampleCollection.set(NAME, radboudBiobankEntity.getString(XML_TITLE));
		existingSampleCollection.set(TYPE, getTypes(radboudBiobankEntity.getString(XML_TYPEBIOBANK)));
		existingSampleCollection.set(DATA_CATEGORIES, samples.getDataCategories(radboudBiobankEntity));
		existingSampleCollection.set(MATERIALS, samples.getMaterials(biobankId));
		existingSampleCollection.set(OMICS, samples.getOmics(biobankId));
		existingSampleCollection.set(SEX, samples.getSex(biobankId));
		existingSampleCollection.set(AGE_LOW, samples.getAgeMin(biobankId));
		existingSampleCollection.set(AGE_HIGH, samples.getAgeMax(biobankId));
		existingSampleCollection.set(AGE_UNIT, getXrefEntity(REF_AGE_TYPES, "YEAR"));
		existingSampleCollection.set(DISEASE, diseases.getDiseaseTypes(radboudBiobankEntity.getString(XML_IDAA)));
		existingSampleCollection.set(NUMBER_OF_DONORS, samples.getSize(biobankId));
		existingSampleCollection.set(DESCRIPTION, radboudBiobankEntity.getString(XML_DESCRIPTION));
		existingSampleCollection.set(CONTACT_PERSON, getContactPersons(radboudBiobankEntity));
		existingSampleCollection.set(BIOBANKS, getMrefEntities(REF_BIOBANKS, "RBB"));
		existingSampleCollection.set(BIOBANK_SAMPLE_ACCESS_FEE, true);
		existingSampleCollection.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, true);
		existingSampleCollection.set(BIOBANK_DATA_SAMPLE_ACCESS_DESCRIPTION, null);  // Don't fill in
		existingSampleCollection.set(BIOBANK_DATA_ACCESS_FEE, true);
		existingSampleCollection.set(BIOBANK_DATA_ACCESS_JOINT_PROJECTS, true);
		existingSampleCollection.set(BIOBANK_DATA_ACCESS_DESCRIPTION, null);  // Don't fill in

		return existingSampleCollection;
	}

	private Iterable<Entity> getPrincipalInvestigator(String biobankId)
	{
		Entity principalInvestigatorEntity = dataService.findOne(REF_PERSONS, biobankId);
		if (principalInvestigatorEntity == null)
		{
			principalInvestigatorEntity = new MapEntity(dataService.getEntityMetaData(REF_PERSONS));
			principalInvestigatorEntity.set(ID, biobankId);
			principalInvestigatorEntity.set(COUNTRY, countryNl);
			dataService.add(REF_PERSONS, principalInvestigatorEntity);
		}
		return singletonList(principalInvestigatorEntity);
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

	private Iterable<Entity> getContactPersons(Entity biobankEntity)
	{
		String[] contactPerson = biobankEntity.getString(XML_CONTACT_PERSON).split(",");
		String address1 = biobankEntity.getString(XML_ADDRESS1);
		String address2 = biobankEntity.getString(XML_ADDRESS2);
		String postalCode = biobankEntity.getString(XML_ZIP_CODE);
		String city = biobankEntity.getString(XML_LOCATION);
		String[] email = biobankEntity.getString(XML_EMAIL).split(" ");
		String phoneNumber = biobankEntity.getString(XML_PHONE);

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
			Entity person = dataService.findOne(REF_PERSONS, personId);

			if (person != null)
			{
				persons.add(person);
			}
			else
			{
				DefaultEntity newPerson = new DefaultEntity(personMetaData, dataService);
				newPerson.set(ID, personId);
				newPerson.set(FIRST_NAME, contactPerson[i]);
				newPerson.set(LAST_NAME, contactPerson[i]);
				newPerson.set(PHONE, phoneNumber);
				newPerson.set(EMAIL, email[i]);

				StringBuilder addressBuilder = new StringBuilder();
				if (address1 != null && !address1.isEmpty()) addressBuilder.append(address1);
				if (address2 != null && !address2.isEmpty())
				{
					if (address1 != null && !address1.isEmpty()) addressBuilder.append(' ');
					addressBuilder.append(address2);
				}
				if (addressBuilder.length() > 0)
				{
					newPerson.set(ADDRESS, addressBuilder.toString());
				}
				newPerson.set(ZIP, postalCode);
				newPerson.set(CITY, city);
				newPerson.set(COUNTRY, countryNl);
				dataService.add(REF_PERSONS, newPerson);
				persons.add(newPerson);
			}

		}
		return persons;
	}

	private Iterable<Entity> getTypes(String radboudTypeBiobank)
	{
		String collectionTypeId;
		if (radboudTypeBiobank == null || radboudTypeBiobank.isEmpty())
		{
			collectionTypeId = "OTHER";
		}
		else
		{
			switch (radboudTypeBiobank)
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
					throw new RuntimeException("Unknown biobank type [" + radboudTypeBiobank + "]");
			}
		}
		Entity collectionType = dataService.findOne(REF_COLLECTION_TYPES, collectionTypeId);
		if (collectionType == null)
		{
			throw new RuntimeException("Unknown '" + REF_COLLECTION_TYPES + "' [" + collectionTypeId + "]");
		}
		return singletonList(collectionType);
	}
}
