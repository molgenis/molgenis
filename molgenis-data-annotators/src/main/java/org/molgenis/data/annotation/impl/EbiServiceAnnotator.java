package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * <p>
 * This class calls the EBI CHeMBL webservice with a uniprot ID. The webservice returns a map with information on the
 * submitted protein ID.
 * </p>
 * 
 * <p>
 * <b>EBI returns:</b> {target={targetType, chemblId, geneNames, description, compoundCount, bioactivityCount,
 * proteinAccession, synonyms, organism, preferredName}}
 * </p>
 * 
 * @author mdehaan
 * 
 * @version EBI: database version 17, prepared on 29th August 2013
 * 
 * */
@Component("ebiService")
public class EbiServiceAnnotator extends AbstractRepositoryAnnotator implements RepositoryAnnotator,
		ApplicationListener<ContextRefreshedEvent>
{
	// Web url to call the EBI web service
	private static final String EBI_CHEMBLWS_URL = "https://www.ebi.ac.uk/chemblws/targets/uniprot/";

	// EBI service is dependant on this ID when the web service is called
	// If Uniprot ID is not in a data set, the web service cannot be used
	public static final String UNIPROT_ID = "uniprot_id";

	public static final String NAME = "EBI-CHeMBL";
	private final HttpClient httpClient;
	private final List<Object> annotatedInput = new ArrayList<Object>();

	public EbiServiceAnnotator()
	{
		httpClient = new DefaultHttpClient();
	}

	public EbiServiceAnnotator(HttpClient client)
	{
		httpClient = client;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNIPROT_ID, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public boolean annotationDataExists()
	{
		boolean dataExists = true;

		// TODO Check if the webservice is up

		return dataExists;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		HttpGet httpGet = new HttpGet(getServiceUri(entity));
		List<Entity> resultEntities = new ArrayList<>();

		if (!annotatedInput.contains(entity.get(UNIPROT_ID)))
		{
			annotatedInput.add(entity.get(UNIPROT_ID));
			try
			{
				HttpResponse response = httpClient.execute(httpGet);
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()),
						Charset.forName("UTF-8")));
				StringBuilder result = new StringBuilder();
				try
				{
					String output;

					while ((output = br.readLine()) != null)
					{
						result.append(output);
					}
				}
				finally
				{
					br.close();
				}
				resultEntities = parseResult(entity, result.toString());
			}
			catch (Exception e)
			{
				httpGet.abort();
				// TODO: how to handle exceptions at this point
				throw new RuntimeException(e);
			}
		}
		return resultEntities;
	}

	private String getServiceUri(Entity entity)
	{
		StringBuilder uriStringBuilder = new StringBuilder();
		uriStringBuilder.append(EBI_CHEMBLWS_URL);
		uriStringBuilder.append(entity.get(UNIPROT_ID));
		uriStringBuilder.append(".json");

		return uriStringBuilder.toString();
	}

	private List<Entity> parseResult(Entity entity, String json) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<>();
		if (!"".equals(json))
		{
			Map<String, Object> rootMap = jsonStringToMap(json);
			resultMap = (Map<String, Object>) rootMap.get("target");
			resultMap.put(UNIPROT_ID, entity.get(UNIPROT_ID));
		}
		return Collections.singletonList(getAnnotatedEntity(entity, resultMap));
	}

	private static Map<String, Object> jsonStringToMap(String result) throws IOException
	{
		Gson gson = new Gson();
		Map<String, Object> resultMap = gson.fromJson(result, new TypeToken<Map<String, Object>>()
		{
		}.getType());
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("targetType", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("chemblId", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("geneNames", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("description", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("compoundCount", FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("bioactivityCount", FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("proteinAccession", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("synonyms", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("organism", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("preferredName", FieldTypeEnum.STRING));
		return metadata;
	}

}
