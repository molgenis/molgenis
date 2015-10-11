package org.molgenis.rdconnect;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

				InputStream is = entity.getContent();

				InputStreamReader reader = new InputStreamReader(is, UTF_8);
				try
				{
					JsonParser parser = new JsonParser();
					return parser.parse(reader);
				}
				finally
				{
					reader.close();
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

	private Set<String> getIdCardBiobanksOrganizationIds()
	{
		String regbbsEndpoint = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
				+ idCardBiobankIndexerSettings.getBiobankCollectionResource();
		JsonArray resource = this.getResourceAsJson(regbbsEndpoint).getAsJsonArray();
		return StreamSupport.stream(resource.spliterator(), false)
				.map(j -> j.getAsJsonObject().get("OrganizationID").getAsString()).collect(Collectors.toSet());
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids)
	{
		String value = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining(",", "[", "]"));
		try
		{
			value = URLEncoder.encode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{
			throw new RuntimeException(e1);
		}
		String uri = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
				+ idCardBiobankIndexerSettings.getBiobankCollectionSelectionResource() + '/' + value;
		JsonArray jsonArray = getResourceAsJson(uri).getAsJsonArray();
		return StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElement -> {
			return toEntity(jsonElement.getAsJsonObject());

		}).collect(Collectors.toList());
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks()
	{
		return this.getIdCardBiobanks(this.getIdCardBiobanksOrganizationIds());
	}

	private Entity toEntity(JsonObject jsonObject)
	{
		EntityMetaData emd = dataService.getEntityMetaData("rdconnect_regbb");

		MapEntity regbbMapEntity = new MapEntity(emd);

		regbbMapEntity.set("OrganizationID", jsonObject.getAsJsonPrimitive("OrganizationID").getAsInt());
		regbbMapEntity.set("type", jsonObject.getAsJsonPrimitive("type").getAsString());
		regbbMapEntity.set("also_listed_in", mapJsonArrayToCsvString(jsonObject.getAsJsonArray("also listed in")));
		regbbMapEntity.set("url", mapJsonArrayToCsvString(jsonObject.getAsJsonArray("url")));

		/**
		 * "main contact" entity
		 */
		regbbMapEntity.set("title",
				jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("title").getAsString());
		regbbMapEntity.set("first_name",
				jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("first name").getAsString());
		regbbMapEntity.set("email",
				jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("email").getAsString());
		regbbMapEntity.set("last_name",
				jsonObject.getAsJsonObject("main contact").getAsJsonPrimitive("last name").getAsString());

		// Example format "Mon Jan 05 18:02:13 GMT 2015"
		final String datetimePattern = "EEE MMM dd HH:mm:ss z yyyy";
		SimpleDateFormat datetimeFormat = new SimpleDateFormat(datetimePattern);

		try
		{
			regbbMapEntity.set("last_activities",
					datetimeFormat.parseObject(jsonObject.getAsJsonPrimitive("last activities").getAsString()));
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException("failed to parse the 'last activities' property", e);
		}

		try
		{
			regbbMapEntity.set("date_of_inclusion",
					datetimeFormat.parseObject(jsonObject.getAsJsonPrimitive("date of inclusion").getAsString()));
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException("failed to parse the 'last activities' property", e);
		}

		regbbMapEntity.set("email", jsonObject.getAsJsonObject("address").getAsJsonPrimitive("street2").getAsString());
		regbbMapEntity.set("name_of_host_institution",
				jsonObject.getAsJsonObject("address").getAsJsonPrimitive("name of host institution").getAsString());
		regbbMapEntity.set("zip", jsonObject.getAsJsonObject("address").getAsJsonPrimitive("zip").getAsString());
		regbbMapEntity.set("street1",
				jsonObject.getAsJsonObject("address").getAsJsonPrimitive("street1").getAsString());
		regbbMapEntity.set("country",
				jsonObject.getAsJsonObject("address").getAsJsonPrimitive("country").getAsString());
		regbbMapEntity.set("city", jsonObject.getAsJsonObject("address").getAsJsonPrimitive("city").getAsString());
		regbbMapEntity.set("name", jsonObject.getAsJsonPrimitive("name").getAsString());
		regbbMapEntity.set("ID", jsonObject.getAsJsonPrimitive("ID").getAsString());
		regbbMapEntity.set("type_of_host_institution", jsonObject.getAsJsonPrimitive("type of host institution") != null
				? jsonObject.getAsJsonPrimitive("type of host institution").getAsString() : null);
		regbbMapEntity.set("target_population", jsonObject.getAsJsonPrimitive("target population") != null
				? jsonObject.getAsJsonPrimitive("target population").getAsString() : null);

		return regbbMapEntity;
	}

	private String mapJsonArrayToCsvString(JsonArray jsonArray)
	{
		return jsonArray != null ? StreamSupport.stream(jsonArray.spliterator(), false).map(JsonElement::getAsString)
				.collect(Collectors.joining(",")) : null;
	}

	@Override
	public Entity getIdCardBiobank(String id)
	{
		String uri = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
				+ idCardBiobankIndexerSettings.getBiobankResource() + '/' + id;
		JsonObject root = this.getResourceAsJson(uri).getAsJsonObject();
		return toEntity(root);
	}

	private MapEntity parseToMapEntity(EntityMetaData entityMetaData, String attributeName, String value)
	{
		MapEntity entity = new MapEntity(entityMetaData);
		entity.set(attributeName, value);
		return entity;
	}
}
