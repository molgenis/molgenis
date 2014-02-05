package org.molgenis.annotationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
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
 * This class...
 * </p>
 * 
 * <p>
 * EBI returns: {target={targetType, chemblId, geneNames, description, compoundCount, bioactivityCount,
 * proteinAccession, synonyms, organism, preferredName}}
 * </p>
 * 
 * @author mdehaan
 * @param Iterator<Entity>
 * @return Iterator<Entity>
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
			HttpGet httpGet = new HttpGet(EBI_CHEMBLWS_URL + entity.get(UNIPROT_ID)+".json");

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
					HashMap<String, Object> rootMap = jsonStringToMap(result);
					HashMap<String, Object> resultMap = (HashMap) rootMap.get("target");
					resultMap.put(UNIPROT_ID, entity.get(UNIPROT_ID));
					results.add(new MapEntity(resultMap));
				}
			}
			catch (RuntimeException e)
			{
				httpGet.abort();
				throw e;
			}
			catch (IOException e)
			{
				httpGet.abort();
				throw new RuntimeException(e);
			}
		}
		return results.iterator();
	}

	private static HashMap<String, Object> jsonStringToMap(String result) throws IOException
	{
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>(){};
		return mapper.readValue(result, typeRef);
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		String[] chembleFeatures = new String[]
				{ "targetType", "chemblId", "geneNames", "description", "compoundCount", "bioactivityCount",
						"proteinAccession", "synonyms", "organism", "preferredName" };
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		
		for(String attribute : chembleFeatures){
			metadata.addAttributeMetaData(new DefaultAttributeMetaData(attribute, FieldTypeEnum.STRING));
		}
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
	public Boolean canAnnotate()
	{
		//TODO implement!
		return true;
	}
}