package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class IdCardBiobankServiceImpl implements IdCardBiobankService
{
	private final DataService dataService;
	private final IdCardBiobankIndexerSettings idCardBiobankIndexerSettings;

	@Autowired
	public IdCardBiobankServiceImpl(DataService dataService, IdCardBiobankIndexerSettings idCardBiobankIndexerSettings)
	{
		this.dataService = requireNonNull(dataService);
		this.idCardBiobankIndexerSettings = requireNonNull(idCardBiobankIndexerSettings);
	}

	private JsonObject getResourceAsJsonObject(String url)
	{
		try
		{
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse result = httpClient.execute(request);
			String toExtract = EntityUtils.toString(result.getEntity(), "UTF-8");
			JsonParser parser = new JsonParser();
			return parser.parse(toExtract).getAsJsonObject();
		}
		catch (IOException ex)
		{
			throw new MolgenisDataException("Hackathon error message", ex);
		}
	}

	private JsonArray getResourceAsJsonArray(String url)
	{
		try
		{
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse result = httpClient.execute(request);
			String toExtract = EntityUtils.toString(result.getEntity(), "UTF-8");
			JsonParser parser = new JsonParser();
			return parser.parse(toExtract).getAsJsonArray();
		}
		catch (IOException ex)
		{
			throw new MolgenisDataException("Hackathon error message");
		}
	}

	private Set<String> getIdCardBiobanksOrganizationIds()
	{
		String regbbsEndpoint = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
				+ idCardBiobankIndexerSettings.getBiobankCollectionResource();
		JsonArray resource = this.getResourceAsJsonArray(regbbsEndpoint);
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
		JsonArray jsonArray = getResourceAsJsonArray(uri);
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
		regbbMapEntity.set("also_listed_in", this.parseToListMapEntity("rdconnect_also_listed_in", "also_listed_in",
				jsonObject.getAsJsonArray("also listed in")));
		regbbMapEntity.set("url", this.parseToListMapEntity("rdconnect_url", "url", jsonObject.getAsJsonArray("url")));

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
		regbbMapEntity.set("type_of_host_institution",
				jsonObject.getAsJsonPrimitive("type of host institution").getAsString());
		regbbMapEntity.set("target_population", jsonObject.getAsJsonPrimitive("target population").getAsString());

		return regbbMapEntity;
	}

	@Override
	public Entity getIdCardBiobank(String id)
	{
		String uri = idCardBiobankIndexerSettings.getApiBaseUri() + '/'
				+ idCardBiobankIndexerSettings.getBiobankResource() + '/' + id;
		JsonObject root = this.getResourceAsJsonObject(uri);
		return toEntity(root);
	}

	private List<MapEntity> parseToListMapEntity(String entityName, String attributeName, JsonArray jsonArray)
	{
		EntityMetaData emd = dataService.getEntityMetaData(entityName);
		List<MapEntity> mapEntityList = new ArrayList<MapEntity>();
		jsonArray.spliterator()
				.forEachRemaining(e -> mapEntityList.add(this.parseToMapEntity(emd, attributeName, e.getAsString())));
		return mapEntityList;
	}

	private MapEntity parseToMapEntity(EntityMetaData entityMetaData, String attributeName, String value)
	{
		MapEntity entity = new MapEntity(entityMetaData);
		entity.set(attributeName, value);
		return entity;
	}
}
