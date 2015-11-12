package org.molgenis.bbmri.eric.service;

import static java.util.Objects.requireNonNull;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.ACRONYM;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.AGE_HIGH;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.AGE_LOW;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.AGE_UNIT;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANKS;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_DESCRIPTION;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_FEE;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_JOINT_PROJECTS;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_DATA_ACCESS_URI;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_DESCRIPTION;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_FEE;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.BIOBANK_SAMPLE_ACCESS_URI;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.CONTACT_PERSON;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.DATA_CATEGORIES;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.DESCRIPTION;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.DISEASE;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.ID;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.LATITUDE;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.LONGITUDE;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.MATERIALS;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.NAME;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.NUMBER_OF_DONORS;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.SAMPLE_COLLECTIONS_ENTITY;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.SEX;
import static org.molgenis.bbmri.eric.model.BbmriNlCheatSheet.TYPE;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Translates BBMRI-NL Sample Collections and Biobanks to BBMRI-ERIC collections and biobanks.
 */
@Service
public class NlToEricConverter
{
	private final DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(NlToEricConverter.class);

	private final String ERIC_COLLECTIONS = "eu_bbmri_eric_collections";
	private final String ERIC_BIOBANKS = "eu_bbmri_eric_biobanks";
	private final String ERIC_CONTACTS = "eu_bbmri_eric_contacts";

	private final String defaultContactEmail;

	/**
	 * Constructor.
	 */
	@Autowired
	public NlToEricConverter(DataService dataService, @Value("${default_contact_email}") String defaultContactEmail)
	{
		this.defaultContactEmail = requireNonNull(defaultContactEmail,
				"property default_contact_email not set in molgenis-server.properties");
		this.dataService = requireNonNull(dataService, "dataService is null");
	}

	/**
	 * Converts the BBMRI-NL Sample Collections and Biobanks entities. New entities are added, old entities are updated.
	 * It is also detected when a Collection, Biobank or Person was removed so it can be removed from the corresponding
	 * BBMRI-ERIC entities.
	 * 
	 * Executed around every midnight.
	 */
	@Scheduled(cron = "0 5 0 * * *")
	@Transactional
	@RunAsSystem
	public void convertNlToEric()
	{

		try
		{
			int collectionsAdded = 0;
			int collectionsUpdated = 0;
			int biobanksAdded = 0;
			int biobanksUpdated = 0;

			LOG.info("Start mapping BBMRI-NL data to BBMRI-ERIC schema");
			Iterable<Entity> nlSampleCollections = dataService.findAll(SAMPLE_COLLECTIONS_ENTITY);
			EntityMetaData ericBiobanksMeta = dataService.getEntityMetaData(ERIC_BIOBANKS);
			EntityMetaData ericCollectionsMeta = dataService.getEntityMetaData(ERIC_COLLECTIONS);

			for (Entity nlSampleCollection : nlSampleCollections)
			{
				Entity ericCollection = toEricCollection(nlSampleCollection, ericCollectionsMeta, ericBiobanksMeta);

				if (dataService.findOne(ERIC_COLLECTIONS, ericCollection.getIdValue().toString()) == null)
				{
					dataService.add(ERIC_COLLECTIONS, ericCollection);
					collectionsAdded++;
				}
				else
				{
					dataService.update(ERIC_COLLECTIONS, ericCollection);
					collectionsUpdated++;
				}

				Iterator<Entity> biobanks = nlSampleCollection.getEntities(BIOBANKS).iterator();
				List<Entity> ericBiobanks = Lists.newArrayList();
				if (biobanks.hasNext())
				{
					biobanks.forEachRemaining(biobank -> ericBiobanks.add(toEricBiobank(biobank, ericBiobanksMeta)));
				}
				else
				{
					ericBiobanks.add(toDummyEricBiobank(nlSampleCollection, ericBiobanksMeta));
				}

				for (Entity biobank : ericBiobanks)
				{
					if (dataService.findOne(ERIC_BIOBANKS, biobank.getIdValue().toString()) == null)
					{
						dataService.add(ERIC_BIOBANKS, biobank);
						biobanksAdded++;
					}
					else
					{
						dataService.update(ERIC_BIOBANKS, biobank);
						biobanksUpdated++;
					}
				}

				LOG.info(String.format(
						"Finished mapping. Added %d sample collections. Updated %d sample collections. Added %d biobanks. Updated %d biobanks.",
						collectionsAdded, collectionsUpdated, biobanksAdded, biobanksUpdated));
			}
		}
		catch (Exception e)
		{
			LOG.error("Error mapping BBMRI-NL data to BBMRI-ERIC schema", e);
		}
	}

	/**
	 * Maps a BBMRI-NL Sample Collections entity to a BBMRI-ERIC collections entity
	 */
	private Entity toEricCollection(Entity nlSampleCollection, EntityMetaData ericCollectionsMeta,
			EntityMetaData ericBiobanksMeta)
	{
		Entity ericCollection = new MapEntity(ericCollectionsMeta);

		ericCollection.set(ID, toEricCollectionId(nlSampleCollection.getString(ID)));
		ericCollection.set(ACRONYM, nlSampleCollection.getString(ACRONYM));
		ericCollection.set(NAME, toEricName(nlSampleCollection.getString(NAME)));
		ericCollection.set(DESCRIPTION, nlSampleCollection.getString(DESCRIPTION));
		ericCollection.set(SEX, toEricRefEntities("eu_bbmri_eric_sex_types", nlSampleCollection.getEntities(SEX)));
		ericCollection.set(AGE_LOW, nlSampleCollection.getInt(AGE_LOW));
		ericCollection.set(AGE_HIGH, nlSampleCollection.getInt(AGE_HIGH));
		ericCollection.set(AGE_UNIT,
				toEricRefEntity("eu_bbmri_eric_age_units", nlSampleCollection.getEntity(AGE_UNIT), true));
		ericCollection.set(DATA_CATEGORIES,
				toEricRefEntities("eu_bbmri_eric_data_types", nlSampleCollection.getEntities(DATA_CATEGORIES)));
		ericCollection.set(MATERIALS,
				toEricRefEntities("eu_bbmri_eric_material_types", nlSampleCollection.getEntities(MATERIALS)));
		ericCollection.set("storage_temperatures", null);
		ericCollection.set(TYPE,
				toEricRefEntities("eu_bbmri_eric_collection_types", nlSampleCollection.getEntities(TYPE)));
		ericCollection.set(DISEASE,
				toEricRefEntities("eu_bbmri_eric_disease_types", nlSampleCollection.getEntities(DISEASE)));
		ericCollection.set("head", null);
		ericCollection.set("head_role", null);
		ericCollection.set("sample_access_fee", nlSampleCollection.getBoolean(BIOBANK_SAMPLE_ACCESS_FEE));
		ericCollection.set("sample_access_joint_project",
				nlSampleCollection.getBoolean(BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS));
		ericCollection.set("sample_access_description",
				nlSampleCollection.getString(BIOBANK_SAMPLE_ACCESS_DESCRIPTION));
		ericCollection.set("sample_access_uri", nlSampleCollection.getString(BIOBANK_SAMPLE_ACCESS_URI));
		ericCollection.set("data_access_fee", nlSampleCollection.getBoolean(BIOBANK_DATA_ACCESS_FEE));
		ericCollection.set("data_access_joint_project",
				nlSampleCollection.getBoolean(BIOBANK_DATA_ACCESS_JOINT_PROJECTS));
		ericCollection.set("data_access_description", nlSampleCollection.getString(BIOBANK_DATA_ACCESS_DESCRIPTION));
		ericCollection.set("data_access_uri", nlSampleCollection.getString(BIOBANK_DATA_ACCESS_URI));
		ericCollection.set("size", nlSampleCollection.getInt(NUMBER_OF_DONORS));
		ericCollection.set("order_of_magnitude", toOrderOfMagnitude(nlSampleCollection.getInt(NUMBER_OF_DONORS)));
		ericCollection.set("timestamp", new Date());
		ericCollection.set("collaboration_commercial", null);
		ericCollection.set("collaboration_non_for_profit", null);
		ericCollection.set("contact", toEricContact(nlSampleCollection.getEntities(CONTACT_PERSON).iterator().next()));
		ericCollection.set("contact_priority", 0);
		ericCollection.set("bioresource_reference", null);
		ericCollection.set("latitude", null);
		ericCollection.set("longitude", null);
		ericCollection.set("network", null);

		return ericCollection;
	}

	private Entity toDummyEricBiobank(Entity nlSampleCollection, EntityMetaData ericBiobanksMeta)
	{
		Entity ericBiobank = new MapEntity(ericBiobanksMeta);

		ericBiobank.set("contact", getDummyContact());
		ericBiobank.set("contact_priority", 0);
		ericBiobank.set("latitude", null);
		ericBiobank.set("longitude", null);
		ericBiobank.set("collaboration_commercial", null);
		ericBiobank.set("collaboration_non_for_profit", null);
		ericBiobank.set("id", toEricBiobankId(nlSampleCollection.getString("id")));
		ericBiobank.set("name", toEricName(nlSampleCollection.getString("name")));
		ericBiobank.set("juridical_person", "N/A");
		ericBiobank.set("country", dataService.findOne("eu_bbmri_eric_countries", "NL"));
		ericBiobank.set("it_support_available", null);
		ericBiobank.set("it_staff_size", null);
		ericBiobank.set("is_available", null);
		ericBiobank.set("his_available", null);
		ericBiobank.set("partner_charter_signed", true);
		ericBiobank.set("acronym", nlSampleCollection.getString("acronym"));
		ericBiobank.set("description", nlSampleCollection.getString("description"));
		ericBiobank.set("url", nlSampleCollection.getString("website"));
		ericBiobank.set("head", null);
		ericBiobank.set("head_role", null);
		ericBiobank.set("type", null);
		ericBiobank.set("bioresource_reference", null);

		return ericBiobank;
	}

	/**
	 * Maps a BBMRI-NL Biobank entity to a BBMRI-ERIC biobank entity and adds it to the repository
	 */
	private Entity toEricBiobank(Entity nlBiobank, EntityMetaData ericBiobanksMeta)
	{
		Entity ericBiobank = new MapEntity(ericBiobanksMeta);

		Iterator<Entity> contacts = nlBiobank.getEntities("contact_person").iterator();
		ericBiobank.set("contact", contacts.hasNext() ? toEricContact(contacts.next()) : getDummyContact());

		ericBiobank.set("contact_priority", 0);
		ericBiobank.set("latitude", nlBiobank.getString(LATITUDE));
		ericBiobank.set("longitude", nlBiobank.getString(LONGITUDE));
		ericBiobank.set("collaboration_commercial", null);
		ericBiobank.set("collaboration_non_for_profit", null);
		ericBiobank.set("id", toEricBiobankId(nlBiobank.getString("id")));
		ericBiobank.set("name", toEricName(nlBiobank.getString("name")));

		Iterator<Entity> juristic_persons = nlBiobank.getEntities("juristic_person").iterator();
		String name;
		Entity country;
		if (juristic_persons.hasNext())
		{
			Entity person = juristic_persons.next();
			name = person.getString("name");
			country = toEricRefEntity("eu_bbmri_eric_countries", person.getEntity("country"), false);
		}
		else
		{
			name = "N/A";
			country = dataService.findOne("eu_bbmri_eric_countries", "NL");
		}

		ericBiobank.set("juridical_person", name);
		ericBiobank.set("country", country);

		ericBiobank.set("it_support_available", nlBiobank.getBoolean("biobankITSupportAvailable"));
		ericBiobank.set("it_staff_size", nlBiobank.getInt("biobankITStaffSize"));
		ericBiobank.set("is_available", nlBiobank.getBoolean("biobankISAvailable"));
		ericBiobank.set("his_available", nlBiobank.getBoolean("biobankHISAvailable"));
		ericBiobank.set("partner_charter_signed", true);
		ericBiobank.set("acronym", nlBiobank.getString("acronym"));
		ericBiobank.set("description", nlBiobank.getString("description"));
		ericBiobank.set("url", nlBiobank.getString("website"));
		ericBiobank.set("head", null);
		ericBiobank.set("head_role", null);
		ericBiobank.set("type", null);
		ericBiobank.set("bioresource_reference", null);

		return ericBiobank;
	}

	/**
	 * Maps an mref or categorical_mref of BBMRI-NL to an mref or categorical_mref of BBMRI-ERIC.
	 */
	private Iterable<Entity> toEricRefEntities(String refEntityName, Iterable<Entity> nlEntities)
	{
		List<Object> ids = Lists.newArrayList();
		nlEntities.forEach(e -> ids.add(e.getIdValue()));
		return dataService.findAll(refEntityName, ids);
	}

	/**
	 * Returns the name or "N/A" as null flavor
	 */
	private String toEricName(String name)
	{
		return name == null ? "N/A" : name;
	}

	/**
	 * Maps xref or categoricals from BBMRI-NL to BBMRI-ERIC.
	 */
	private Entity toEricRefEntity(String refEntityName, Entity nlEntity, boolean nillable)
	{
		if (!nillable) requireNonNull(nlEntity);
		if (nlEntity == null) return null;
		return dataService.findOne(refEntityName, nlEntity.getIdValue().toString());
	}

	/**
	 * Maps BBMRI-NL Persons to BBMRI-ERIC contacts. New contacts get added, existing contacts get updated.
	 */
	private Entity toEricContact(Entity nlContactPerson)
	{
		Entity ericContact = new MapEntity(dataService.getEntityMetaData(ERIC_CONTACTS));

		ericContact.set("id", nlContactPerson.getString("id"));
		ericContact.set("first_name", nlContactPerson.getString("first_name"));
		ericContact.set("last_name", nlContactPerson.getString("last_name"));
		ericContact.set("phone", nlContactPerson.getString("phone"));

		String email = nlContactPerson.getString("email");
		ericContact.set("email", email == null ? defaultContactEmail : email);

		ericContact.set("address", nlContactPerson.getString("address"));
		ericContact.set("zip", nlContactPerson.getString("zip"));
		ericContact.set("city", nlContactPerson.getString("city"));
		ericContact.set("country",
				toEricRefEntity("eu_bbmri_eric_countries", nlContactPerson.getEntity("country"), false));

		if (dataService.findOne(ERIC_CONTACTS, ericContact.getIdValue().toString()) == null)
		{
			dataService.add(ERIC_CONTACTS, ericContact);
		}
		else
		{
			dataService.update(ERIC_CONTACTS, ericContact);
		}

		return ericContact;
	}

	/**
	 * Returns a dummy contact. Needed because not all NL biobank entities are guaranteed to have a contact person, but
	 * is is a required field in the ERIC model.
	 */
	private Entity getDummyContact()
	{
		Entity ericContact = new MapEntity(dataService.getEntityMetaData(ERIC_CONTACTS));

		String na = "N/A";
		ericContact.set("id", "N/A");
		ericContact.set("first_name", na);
		ericContact.set("last_name", na);
		ericContact.set("phone", na);
		ericContact.set("email", defaultContactEmail);
		ericContact.set("address", na);
		ericContact.set("zip", na);
		ericContact.set("city", na);
		ericContact.set("country", dataService.findOne("eu_bbmri_eric_countries", "NL"));

		if (dataService.findOne(ERIC_CONTACTS, ericContact.getIdValue().toString()) == null)
		{
			dataService.add(ERIC_CONTACTS, ericContact);
		}
		else
		{
			dataService.update(ERIC_CONTACTS, ericContact);
		}

		return ericContact;
	}

	/**
	 * Generates BBMRI ERIC identifier.
	 * 
	 * Unique collection ID within BBMRI-ERIC based on MIABIS 2.0 standard, constructed from biobankID prefix +
	 * :collection: + local collection ID string
	 */
	private String toEricCollectionId(String id)
	{
		return new StringBuilder().append("nl_").append(id).append(":collection:").append("nl_").append(id).toString();
	}

	/**
	 * Generates BBMRI ERIC identifier.
	 * 
	 * Unique biobank ID withing BBMRI-ERIC based on MIABIS 2.0 standard (ISO 3166-1 alpha-2 + underscore + biobank
	 * national ID or name), prefixed with bbmri-eric:ID: string
	 */
	private String toEricBiobankId(String id)
	{
		return new StringBuilder().append("bbmri-eric:ID:").append("nl").append('_').append(id).toString();
	}

	/**
	 * Returns the order of magnitude of a number
	 */
	private int toOrderOfMagnitude(int numberOfDonors)
	{
		if (numberOfDonors == 0) throw new IllegalArgumentException("can't take log10 of 0");
		return (int) Math.log10(numberOfDonors);
	}
}
