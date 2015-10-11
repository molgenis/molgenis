package org.molgenis.rdconnect;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.molgenis.rdconnect.IdCardBiobank.ALSO_LISTED_IN;
import static org.molgenis.rdconnect.IdCardBiobank.CITY;
import static org.molgenis.rdconnect.IdCardBiobank.COUNTRY;
import static org.molgenis.rdconnect.IdCardBiobank.DATE_OF_INCLUSION;
import static org.molgenis.rdconnect.IdCardBiobank.EMAIL;
import static org.molgenis.rdconnect.IdCardBiobank.FIRST_NAME;
import static org.molgenis.rdconnect.IdCardBiobank.ID;
import static org.molgenis.rdconnect.IdCardBiobank.LAST_ACTIVITIES;
import static org.molgenis.rdconnect.IdCardBiobank.LAST_NAME;
import static org.molgenis.rdconnect.IdCardBiobank.NAME;
import static org.molgenis.rdconnect.IdCardBiobank.NAME_OF_HOST_INSTITUTION;
import static org.molgenis.rdconnect.IdCardBiobank.ORGANIZATION_ID;
import static org.molgenis.rdconnect.IdCardBiobank.PHONE;
import static org.molgenis.rdconnect.IdCardBiobank.STREET1;
import static org.molgenis.rdconnect.IdCardBiobank.STREET2;
import static org.molgenis.rdconnect.IdCardBiobank.TARGET_POPULATION;
import static org.molgenis.rdconnect.IdCardBiobank.TITLE;
import static org.molgenis.rdconnect.IdCardBiobank.TYPE;
import static org.molgenis.rdconnect.IdCardBiobank.TYPE_OF_HOST_INSTITUTION;
import static org.molgenis.rdconnect.IdCardBiobank.URL;
import static org.molgenis.rdconnect.IdCardBiobank.ZIP;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

@Service
public class IdCardBiobankServiceImpl implements IdCardBiobankService
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankServiceImpl.class);

	private static final int ID_CARD_CONNECT_TIMEOUT = 2000;
	private static final int ID_CARD_CONNECTION_REQUEST_TIMEOUT = 2000;
	private static final int ID_CARD_SOCKET_TIMEOUT = 2000;

	private final DataService dataService;
	private final HttpClient httpClient;
	private final IdCardBiobankIndexerSettings idCardBiobankIndexerSettings;
	private final RequestConfig requestConfig;

	@Autowired
	public IdCardBiobankServiceImpl(DataService dataService, HttpClient httpClient,
			IdCardBiobankIndexerSettings idCardBiobankIndexerSettings)
	{
		this.dataService = requireNonNull(dataService);
		this.httpClient = requireNonNull(httpClient);
		this.idCardBiobankIndexerSettings = requireNonNull(idCardBiobankIndexerSettings);

		this.requestConfig = RequestConfig.custom().setConnectTimeout(ID_CARD_CONNECT_TIMEOUT)
				.setConnectionRequestTimeout(ID_CARD_CONNECTION_REQUEST_TIMEOUT)
				.setSocketTimeout(ID_CARD_SOCKET_TIMEOUT).build();
	}

	public IdCardBiobank getIdCardBiobank(String id)
	{
		// Construct uri
		StringBuilder uriBuilder = new StringBuilder().append(idCardBiobankIndexerSettings.getApiBaseUri()).append('/')
				.append(idCardBiobankIndexerSettings.getBiobankResource()).append('/').append(id);

		return getIdCardResource(uriBuilder.toString(), new JsonResponseHandler<IdCardBiobank>()
		{
			@Override
			public IdCardBiobank deserialize(JsonReader jsonReader) throws IOException
			{
				return toIdCardBiobank(jsonReader);
			}
		});
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids)
	{
		String value = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining(",", "[", "]"));
		try
		{
			value = URLEncoder.encode(value, UTF_8.name());
		}
		catch (UnsupportedEncodingException e1)
		{
			throw new RuntimeException(e1);
		}
		StringBuilder uriBuilder = new StringBuilder().append(idCardBiobankIndexerSettings.getApiBaseUri()).append('/')
				.append(idCardBiobankIndexerSettings.getBiobankCollectionSelectionResource()).append('/').append(value);

		return getIdCardResource(uriBuilder.toString(), new JsonResponseHandler<Iterable<Entity>>()
		{
			@Override
			public Iterable<Entity> deserialize(JsonReader jsonReader) throws IOException
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
		});
	}

	/**
	 * Deserializes ID-Card JSON resource into an entity
	 * 
	 * @param jsonReader
	 * @return
	 * @throws IOException
	 */
	private IdCardBiobank toIdCardBiobank(JsonReader jsonReader) throws IOException
	{
		IdCardBiobank idCardBiobank = new IdCardBiobank(dataService);

		// e.g. "Mon Jan 05 18:02:13 GMT 2015"
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

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
								idCardBiobank.set(TITLE, jsonReader.nextString());
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

	private <T> T getIdCardResource(String url, ResponseHandler<T> responseHandler)
	{
		HttpGet request = new HttpGet(url);
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		request.setConfig(requestConfig);
		try
		{
			LOG.info("Retrieving [" + url + "]");
			return httpClient.execute(request, responseHandler);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	private static abstract class JsonResponseHandler<T> implements ResponseHandler<T>
	{
		@Override
		public T handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
		{
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() < 100 || statusLine.getStatusCode() >= 300)
			{
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity == null)
			{
				throw new ClientProtocolException("Response contains no content");
			}

			JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
			try
			{
				return deserialize(jsonReader);
			}
			finally
			{
				jsonReader.close();
			}
		}

		public abstract T deserialize(JsonReader jsonReader) throws IOException;
	}

	private JsonElement getResourceAsJson(String url)
	{
		// Create a custom response handler
		ResponseHandler<JsonElement> responseHandler = new ResponseHandler<JsonElement>()
		{
			@Override
			public JsonElement handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
			{
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() < 100 || statusLine.getStatusCode() >= 300)
				{
					throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
				}

				HttpEntity entity = response.getEntity();
				if (entity == null)
				{
					throw new ClientProtocolException("Response contains no content");
				}

				InputStreamReader inputStreamReader = new InputStreamReader(entity.getContent(), UTF_8);
				try
				{
					return new JsonParser().parse(inputStreamReader);
				}
				finally
				{
					inputStreamReader.close();
				}
			}
		};

		HttpGet request = new HttpGet(url);
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		request.setConfig(requestConfig);
		try
		{
			LOG.info("Retrieving [" + url + "]");
			return httpClient.execute(request, responseHandler);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	private Set<String> getIdCardBiobanksOrganizationIdsOld()
	{
		String regbbsEndpoint = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
				+ idCardBiobankIndexerSettings.getBiobankCollectionResource();
		JsonArray resource = this.getResourceAsJson(regbbsEndpoint).getAsJsonArray();
		return StreamSupport.stream(resource.spliterator(), false)
				.map(j -> j.getAsJsonObject().get("OrganizationID").getAsString()).collect(Collectors.toSet());
	}

	// @Override
	// public Iterable<Entity> getIdCardBiobanksOld(Iterable<String> ids)
	// {
	// String value = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining(",", "[", "]"));
	// try
	// {
	// value = URLEncoder.encode(value, "UTF-8");
	// }
	// catch (UnsupportedEncodingException e1)
	// {
	// throw new RuntimeException(e1);
	// }
	// String uri = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
	// + idCardBiobankIndexerSettings.getBiobankCollectionSelectionResource() + '/' + value;
	// JsonArray jsonArray = getResourceAsJson(uri).getAsJsonArray();
	// return StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElement -> {
	// return toEntity(jsonElement.getAsJsonObject());
	//
	// }).collect(Collectors.toList());
	// }

	@Override
	public Iterable<Entity> getIdCardBiobanks()
	{
		return this.getIdCardBiobanks(this.getIdCardBiobanksOrganizationIdsOld());
	}
	//
	// private Entity toEntity(JsonObject jsonObject)
	// {
	// EntityMetaData emd = dataService.getEntityMetaData("rdconnect_regbb");
	//
	// MapEntity regbbMapEntity = new MapEntity(emd);
	//
	// regbbMapEntity.set("OrganizationID", jsonObject.getAsJsonPrimitive("OrganizationID").getAsInt());
	// regbbMapEntity.set("type", jsonObject.getAsJsonPrimitive("type").getAsString());
	// regbbMapEntity.set("also_listed_in", mapJsonArrayToCsvString(jsonObject.getAsJsonArray("also listed in")));
	// regbbMapEntity.set("url", mapJsonArrayToCsvString(jsonObject.getAsJsonArray("url")));
	//
	// /**
	// * "main contact" entity
	// */
	// regbbMapEntity.set("title",
	// jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("title").getAsString());
	// regbbMapEntity.set("first_name",
	// jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("first name").getAsString());
	// regbbMapEntity.set("email",
	// jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("email").getAsString());
	// regbbMapEntity.set("last_name",
	// jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("last name").getAsString());
	//
	// // Example format "Mon Jan 05 18:02:13 GMT 2015"
	// final String datetimePattern = "EEE MMM dd HH:mm:ss z yyyy";
	// SimpleDateFormat datetimeFormat = new SimpleDateFormat(datetimePattern);
	//
	// try
	// {
	// regbbMapEntity.set("last_activities",
	// datetimeFormat.parseObject(jsonObject.getAsJsonPrimitive("last activities").getAsString()));
	// }
	// catch (ParseException e)
	// {
	// throw new MolgenisDataException("failed to parse the 'last activities' property", e);
	// }
	//
	// try
	// {
	// regbbMapEntity.set("date_of_inclusion",
	// datetimeFormat.parseObject(jsonObject.getAsJsonPrimitive("date of inclusion").getAsString()));
	// }
	// catch (ParseException e)
	// {
	// throw new MolgenisDataException("failed to parse the 'last activities' property", e);
	// }
	//
	// regbbMapEntity.set("email", jsonObject.getAsJsonObject("address").getAsJsonPrimitive("street2").getAsString());
	// regbbMapEntity.set("name_of_host_institution",
	// jsonObject.getAsJsonObject("address").getAsJsonPrimitive("name of host institution").getAsString());
	// regbbMapEntity.set("zip", jsonObject.getAsJsonObject("address").getAsJsonPrimitive("zip").getAsString());
	// regbbMapEntity.set("street1",
	// jsonObject.getAsJsonObject("address").getAsJsonPrimitive("street1").getAsString());
	// regbbMapEntity.set("country",
	// jsonObject.getAsJsonObject("address").getAsJsonPrimitive("country").getAsString());
	// regbbMapEntity.set("city", jsonObject.getAsJsonObject("address").getAsJsonPrimitive("city").getAsString());
	// regbbMapEntity.set("name", jsonObject.getAsJsonPrimitive("name").getAsString());
	// regbbMapEntity.set("ID", jsonObject.getAsJsonPrimitive("ID").getAsString());
	// regbbMapEntity.set("type_of_host_institution", jsonObject.getAsJsonPrimitive("type of host institution") != null
	// ? jsonObject.getAsJsonPrimitive("type of host institution").getAsString() : null);
	// regbbMapEntity.set("target_population", jsonObject.getAsJsonPrimitive("target population") != null
	// ? jsonObject.getAsJsonPrimitive("target population").getAsString() : null);
	//
	// return regbbMapEntity;
	// }
	//
	// private String mapJsonArrayToCsvString(JsonArray jsonArray)
	// {
	// return jsonArray != null ? StreamSupport.stream(jsonArray.spliterator(), false).map(JsonElement::getAsString)
	// .collect(Collectors.joining(",")) : null;
	// }

	// public Entity getIdCardBiobankOld(String id)
	// {
	// String uri = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
	// + idCardBiobankIndexerSettings.getBiobankResource() + '/' + id;
	// JsonObject root = this.getResourceAsJson(uri).getAsJsonObject();
	// return toEntity(root);
	// }
}
