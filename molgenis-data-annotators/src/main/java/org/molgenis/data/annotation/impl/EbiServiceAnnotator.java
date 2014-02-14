package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
public class EbiServiceAnnotator implements RepositoryAnnotator
{
	// Web url to call the EBI web service
	private static final String EBI_CHEMBLWS_URL = "https://www.ebi.ac.uk/chemblws/targets/uniprot/";

	// EBI service is dependant on this ID when the web service is called
	// If Uniprot ID is not in a data set, the web service cannot be used
	private static final String UNIPROT_ID = "uniprot_id";

	@Autowired
	DataService dataService;

	@Override
	@Transactional
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		HttpClient httpClient = new DefaultHttpClient();
		List<Entity> results = new ArrayList<Entity>();

		while (source.hasNext())
		{
			Entity entity = source.next();
			HttpGet httpGet = new HttpGet(EBI_CHEMBLWS_URL + entity.get(UNIPROT_ID) + ".json");

			try
			{
				HttpResponse response = httpClient.execute(httpGet);
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

				String output;
				String result = "";

				while ((output = br.readLine()) != null)
				{
					result += output;
				}

				if (!"".equals(result))
				{
					Map<String, Object> rootMap = jsonStringToMap(result);
					Map<String, Object> resultMap = (Map) rootMap.get("target");
					resultMap.put(UNIPROT_ID, entity.get(UNIPROT_ID));
					results.add(new MapEntity(resultMap));
				}
			}
			catch (Exception e)
			{
				httpGet.abort();
				// TODO: how to handle exceptions at this point
				throw new RuntimeException(e);
			}
		}
		return results.iterator();
	}

	private static Map<String, Object> jsonStringToMap(String result) throws IOException
	{
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(result, new TypeToken<Map<String, Object>>() {}.getType());
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
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

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNIPROT_ID, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public Boolean canAnnotate(EntityMetaData inputMetaData)
	{
		boolean canAnnotate = true;
		Iterable<AttributeMetaData> inputAttributes = getInputMetaData().getAttributes();
		for (AttributeMetaData attribute : inputAttributes)
		{
			if (inputMetaData.getAttribute(attribute.getName()) == null) canAnnotate = false;
			else if (!inputMetaData.getAttribute(attribute.getName()).getDataType().equals(attribute.getDataType()))
			{
				canAnnotate = false;
			}
		}
		return canAnnotate;
	}

	@Override
	public String getName()
	{
		return "EBI-CHeMBL";
	}

}
