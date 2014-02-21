package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Iterator;

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
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
public class EbiServiceAnnotator implements RepositoryAnnotator, ApplicationListener<ContextRefreshedEvent>
{
    // Web url to call the EBI web service
	private static final String EBI_CHEMBLWS_URL = "https://www.ebi.ac.uk/chemblws/targets/uniprot/";

	// EBI service is dependant on this ID when the web service is called
	// If Uniprot ID is not in a data set, the web service cannot be used
	public static final String UNIPROT_ID = "uniprot_id";
    public static final String EBI_CHE_MBL = "EBI-CHeMBL";

    @Autowired
	DataService dataService;

	@Autowired
	AnnotationService annotatorService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
	{
		return EBI_CHE_MBL;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNIPROT_ID, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public boolean canAnnotate(EntityMetaData inputMetaData)
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
	@Transactional
	public Iterator<Entity> annotate(final Iterator<Entity> source)
	{
        final HttpClient httpClient = new DefaultHttpClient();
        return new Iterator<Entity>() {
            @Override
            public boolean hasNext() {
                return source.hasNext();  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Entity next() {
                return annotateEntity(httpClient, source.next());  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void remove() {
                //
            }
        };
	}

    private Entity annotateEntity(HttpClient httpClient, Entity entity) {
        HttpGet httpGet = new HttpGet(getServiceUri(entity));
        Entity resultEntity = null;
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
            resultEntity = parseResult(entity, result);
        }
        catch (Exception e)
        {
            httpGet.abort();
            // TODO: how to handle exceptions at this point
            throw new RuntimeException(e);
        }
        return resultEntity;
    }

    private String getServiceUri(Entity entity)
	{
		return EBI_CHEMBLWS_URL + entity.get(UNIPROT_ID) + ".json";
	}

	private Entity parseResult(Entity entity, String json) throws IOException
	{
        Entity result = new MapEntity();
		if (!"".equals(json))
		{
			Map<String, Object> rootMap = jsonStringToMap(json);
			Map<String, Object> resultMap = (Map) rootMap.get("target");
			resultMap.put(UNIPROT_ID, entity.get(UNIPROT_ID));
			result = new MapEntity(resultMap);
		}
		return result;
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
}