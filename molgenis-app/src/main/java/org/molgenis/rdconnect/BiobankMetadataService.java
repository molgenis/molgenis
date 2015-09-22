package org.molgenis.rdconnect;

import java.io.IOException;
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
public class BiobankMetadataService
{
	public final static String REGBBS_URL = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbbs";
	public final static String REGBB_URL_PREFIX = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbb/organization-id/";
	public final static String REGBBS_ORGANIZATIONID = "OrganizationID";
	
	// public static void main(String args[])
	// {
	// BiobankMetadataService biobankMetadataService = new BiobankMetadataService();
	// Map<String, Object> biobankMetadata = biobankMetadataService
	// .getBiobankMetadata("http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbb/organization-id/10779");
	// System.out.println(biobankMetadata.toString());
	// EntityMetaData emd =
	// MapEntity regbbMapEntity = new MapEntity();
	// }

	private DataService dataService;

	@Autowired
	public BiobankMetadataService(DataService dataService)
	{
		this.dataService = dataService;
	}

	// public static void main(String args[])
	// {
	// BiobankMetadataService biobankMetadataService = new BiobankMetadataService();
	// Set<String> orgnizationIds = biobankMetadataService.getIdCardBiobanksOrgnizationIds();
	// orgnizationIds.forEach(e -> System.out.println("OrgnizationId: " + e));
	// }

	public JsonObject getResourceAsJsonObject(String url)
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
			throw new MolgenisDataException("Hackathon error message");
		}
	}

	public JsonArray getResourceAsJsonArray(String url)
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

	public Set<String> getIdCardBiobanksOrgnizationIds()
	{
		JsonArray resource = this.getResourceAsJsonArray(REGBBS_URL);
		return StreamSupport.stream(resource.spliterator(), false)
				.map(j -> j.getAsJsonObject().get(REGBBS_ORGANIZATIONID).getAsString()).collect(Collectors.toSet());
	}

	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids)
	{
		return StreamSupport.stream(ids.spliterator(), false).map(e -> this.getIdCardBiobank(e))
				.collect(Collectors.toList());
	}

	public Iterable<Entity> getIdCardBiobanks()
	{
		return this.getIdCardBiobanks(this.getIdCardBiobanksOrgnizationIds());
	}

	public Entity getIdCardBiobank(String id)
	{
		JsonObject root = this.getResourceAsJsonObject(REGBB_URL_PREFIX + "10779");
		EntityMetaData emd = dataService.getEntityMetaData("rdconnect_regbb");

		MapEntity regbbMapEntity = new MapEntity(emd);

		regbbMapEntity.set("OrganizationID", root.get("OrganizationID"));
		regbbMapEntity.set("type", root.get("type"));

		// First upload this one
		// Escaped due to demo purposes
		// EntityMetaData emdAlsoListedIn = dataService.getEntityMetaData("lso_listed_in");
		// JsonArray alsoListedInsJson = root.getAsJsonArray("lso_listed_in");
		// List<MapEntity> mapEntityList = new ArrayList<MapEntity>();
		// alsoListedInsJson.spliterator().forEachRemaining(
		// e -> mapEntityList.add(this.createUrlMapEntity(emdAlsoListedIn, e.getAsString())));
		// regbbMapEntity.set("lso_listed_in", mapEntityList);

		// First upload this one
		// Escaped due to demo purposes
		// EntityMetaData emdUrl = dataService.getEntityMetaData("url");
		// JsonArray urlsJson = root.getAsJsonArray("url");
		// List<MapEntity> mapEntityList = new ArrayList<MapEntity>();
		// urlsJson.spliterator().forEachRemaining(
		// e -> mapEntityList.add(this.createUrlMapEntity(emdUrl, e.getAsString())));
		// regbbMapEntity.set("url", mapEntityList);

		/**
		 * "main contact" entity
		 */
		regbbMapEntity.set("title", root.getAsJsonObject("main contact").getAsJsonObject("title").getAsString());
		regbbMapEntity.set("first_name", root.getAsJsonObject("main contact").getAsJsonObject("first name")
				.getAsString());
		regbbMapEntity.set("email", root.getAsJsonObject("main contact").getAsJsonObject("email")
				.getAsString());
		regbbMapEntity
				.set("last_name", root.getAsJsonObject("main contact").getAsJsonObject("last name")
				.getAsString());

		// TODO DateTime
		// regbbMapEntity.set("last_activities", root.getAsJsonObject("last activities").getAsString());

		// TODO
		// @SerializedName("date of inclusion")
		// private DateTime dateOfInclusion;
		//
		// private Address address;
		// private String name;
		//
		// @SerializedName("ID")
		// private String id;
		//
		// @SerializedName("type of host institution")
		// private String typeOfHostInstitutionS;
		//
		// @SerializedName("target population")
		// private String targetPopulation;

		return regbbMapEntity;
	}
	
	private MapEntity createUrlMapEntity(EntityMetaData entityMetaData, String value)
	{
		final String attributeName = "url";
		MapEntity entity = new MapEntity(entityMetaData);
		entity.set(attributeName, value);
		return entity;
	}
}
