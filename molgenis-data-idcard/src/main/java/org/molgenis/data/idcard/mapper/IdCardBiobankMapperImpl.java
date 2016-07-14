package org.molgenis.data.idcard.mapper;

import com.google.gson.stream.JsonReader;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.model.IdCardBiobankFactory;
import org.molgenis.data.idcard.model.IdCardOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.idcard.model.IdCardBiobankMetaData.*;

@Component
public class IdCardBiobankMapperImpl implements IdCardBiobankMapper
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankMapperImpl.class);

	private final DataService dataService;
	private final IdCardBiobankFactory idCardBiobankFactory;

	@Autowired
	public IdCardBiobankMapperImpl(DataService dataService, IdCardBiobankFactory idCardBiobankFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.idCardBiobankFactory = requireNonNull(idCardBiobankFactory);
	}

	@Override
	public IdCardBiobank toIdCardBiobank(JsonReader jsonReader) throws IOException
	{
		IdCardBiobank idCardBiobank = idCardBiobankFactory.create();

		// e.g. "Mon Jan 05 18:02:13 GMT 2015"
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

		jsonReader.beginObject();
		while (jsonReader.hasNext())
		{
			String name = jsonReader.nextName();
			switch (name)
			{
				case "Collections":
					jsonReader.skipValue(); // not used at the moment
					break;
				case "OrganizationID":
					idCardBiobank.set(ORGANIZATION_ID, jsonReader.nextInt());
					break;
				case "type":
					idCardBiobank.set(TYPE, jsonReader.nextString());
					break;
				case "also listed in":
					List<String> alsoListedInValues = new ArrayList<>();

					jsonReader.beginArray();
					while (jsonReader.hasNext())
					{
						alsoListedInValues.add(jsonReader.nextString());
					}
					jsonReader.endArray();

					idCardBiobank.set(ALSO_LISTED_IN, alsoListedInValues.stream().collect(Collectors.joining(",")));
					break;
				case "url":
					List<String> urlValues = new ArrayList<>();

					jsonReader.beginArray();
					while (jsonReader.hasNext())
					{
						urlValues.add(jsonReader.nextString());
					}
					jsonReader.endArray();

					idCardBiobank.set(URL, urlValues.stream().collect(Collectors.joining(",")));
					break;
				case "main contact":
					jsonReader.beginObject();
					while (jsonReader.hasNext())
					{
						String mainContactName = jsonReader.nextName();
						switch (mainContactName)
						{
							case "title":
								idCardBiobank.set(SALUTATION, jsonReader.nextString());
								break;
							case "first name":
								idCardBiobank.set(FIRST_NAME, jsonReader.nextString());
								break;
							case "email":
								idCardBiobank.set(EMAIL, jsonReader.nextString());
								break;
							case "last name":
								idCardBiobank.set(LAST_NAME, jsonReader.nextString());
								break;
							case "phone":
								idCardBiobank.set(PHONE, jsonReader.nextString());
								break;
							default:
								LOG.warn("unknown property [{}] in object [main contact]", mainContactName);
								jsonReader.skipValue();
								break;

						}
					}
					jsonReader.endObject();
					break;
				case "last activities":
					try
					{
						idCardBiobank.set(LAST_ACTIVITIES, dateTimeFormat.parse(jsonReader.nextString()));
					}
					catch (ParseException e)
					{
						throw new IOException(e);
					}
					break;
				case "date of inclusion":
					try
					{
						idCardBiobank.set(DATE_OF_INCLUSION, dateTimeFormat.parse(jsonReader.nextString()));
					}
					catch (ParseException e)
					{
						throw new IOException(e);
					}
					break;
				case "address":
					jsonReader.beginObject();
					while (jsonReader.hasNext())
					{
						switch (jsonReader.nextName())
						{
							case "street2":
								idCardBiobank.set(STREET2, jsonReader.nextString());
								break;
							case "name of host institution":
								idCardBiobank.set(NAME_OF_HOST_INSTITUTION, jsonReader.nextString());
								break;
							case "zip":
								idCardBiobank.set(ZIP, jsonReader.nextString());
								break;
							case "street1":
								idCardBiobank.set(STREET1, jsonReader.nextString());
								break;
							case "country":
								idCardBiobank.set(COUNTRY, jsonReader.nextString());
								break;
							case "city":
								idCardBiobank.set(CITY, jsonReader.nextString());
								break;
							default:
								jsonReader.skipValue();
								break;
						}
					}
					jsonReader.endObject();
					break;
				case "name":
					idCardBiobank.set(NAME, jsonReader.nextString());
					break;
				case "ID":
					idCardBiobank.set(ID, jsonReader.nextString());
					break;
				case "type of host institution":
					idCardBiobank.set(TYPE_OF_HOST_INSTITUTION, jsonReader.nextString());
					break;
				case "target population":
					idCardBiobank.set(TARGET_POPULATION, jsonReader.nextString());
					break;
				default:
					LOG.warn("unknown property [{}] in root object", name);
					jsonReader.skipValue();
					break;
			}
		}
		jsonReader.endObject();
		return idCardBiobank;

	}

	@Override
	public Iterable<Entity> toIdCardBiobanks(JsonReader jsonReader) throws IOException
	{
		List<Entity> idCardBiobanks = new ArrayList<>();

		jsonReader.beginArray();
		while (jsonReader.hasNext())
		{
			idCardBiobanks.add(toIdCardBiobank(jsonReader));
		}
		jsonReader.endArray();

		return idCardBiobanks;
	}

	@Override
	public IdCardOrganization toIdCardOrganization(JsonReader jsonReader) throws IOException
	{
		IdCardOrganization idCardOrganization = new IdCardOrganization();

		jsonReader.beginObject();
		while (jsonReader.hasNext())
		{
			String name = jsonReader.nextName();
			switch (name)
			{
				case "Collections":
					jsonReader.skipValue(); // not used at the moment
					break;
				case "name":
					idCardOrganization.setName(jsonReader.nextString());
					break;
				case "ID":
					idCardOrganization.setId(jsonReader.nextString());
					break;
				case "OrganizationID":
					idCardOrganization.setOrganizationId(jsonReader.nextString());
					break;
				case "type":
					idCardOrganization.setType(jsonReader.nextString());
					break;
				default:
					LOG.warn("unknown property [{}] in root object", name);
					jsonReader.skipValue();
					break;
			}
		}
		jsonReader.endObject();

		return idCardOrganization;
	}

	@Override
	public Iterable<IdCardOrganization> toIdCardOrganizations(JsonReader jsonReader) throws IOException
	{
		List<IdCardOrganization> idCardOrganizations = new ArrayList<>();

		jsonReader.beginArray();
		while (jsonReader.hasNext())
		{
			idCardOrganizations.add(toIdCardOrganization(jsonReader));
		}
		jsonReader.endArray();

		return idCardOrganizations;
	}
}
