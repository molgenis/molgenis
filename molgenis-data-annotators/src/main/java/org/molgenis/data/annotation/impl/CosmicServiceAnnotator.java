package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.impl.datastructures.CosmicData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component("cosmicService")
public class CosmicServiceAnnotator extends AbstractRepositoryAnnotator implements RepositoryAnnotator,
		ApplicationListener<ContextRefreshedEvent>
{
	// Web url to call the EBI web service
	private static final String SERVICE_URL = "http://beta.rest.ensembl.org/feature/id/";
	private static final String SERVICE_POSTFIX = ".json?feature=somatic_variation";
	// EBI service is dependant on this ID when the web service is called
	// If Uniprot ID is not in a data set, the web service cannot be used
	public static final String ENSEMBLE_ID = "ensemblId";
	public static final String NAME = "Ensemble2Cosmic";
	private final DefaultHttpClient httpClient;

	@Autowired
	DataService dataService;

	public CosmicServiceAnnotator()
	{
		httpClient = new DefaultHttpClient();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENSEMBLE_ID, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		HttpGet httpGet = new HttpGet(getServiceUri(entity));
		ArrayList<Entity> resultEntities = new ArrayList<Entity>();
		List<Object> annotatedInput = new ArrayList<Object>();
		if (!annotatedInput.contains(entity.get(ENSEMBLE_ID)))
		{
			annotatedInput.add(entity.get(ENSEMBLE_ID));
			try
			{
				HttpResponse response = httpClient.execute(httpGet);
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

				String output;
				StringBuilder result = new StringBuilder();

				while ((output = br.readLine()) != null)
				{
					result.append(output);
				}
				resultEntities.addAll(parseResult(entity, result.toString()));
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
		uriStringBuilder.append(SERVICE_URL);
		uriStringBuilder.append(entity.get(ENSEMBLE_ID));
		uriStringBuilder.append(SERVICE_POSTFIX);

		return uriStringBuilder.toString();
	}

	private List<Entity> parseResult(Entity entity, String json) throws IOException
	{
		List<Entity> result = new ArrayList<Entity>();
		if (!"".equals(json))
		{
			Collection<CosmicData> resultCollection = jsonStringToCollection(json);
			for (CosmicData data : resultCollection)
			{
				Entity resultEntity = new MapEntity();
				resultEntity.set("ID", data.getID());
				resultEntity.set("feature_type", data.getFeature_type());
				resultEntity.set("alt_alleles", data.getAlt_alleles()[0] + "," + data.getAlt_alleles()[1]);
				resultEntity.set("end", data.getEnd());
				resultEntity.set("seq_region_name", data.getSeq_region_name());
				resultEntity.set("consequence_type", data.getConsequence_type());
				resultEntity.set("strand", data.getStrand());
				resultEntity.set("start", data.getStart());
				resultEntity.set(ENSEMBLE_ID, entity.get(ENSEMBLE_ID));
				result.add(resultEntity);
			}
		}
		return result;
	}

	private static Collection<CosmicData> jsonStringToCollection(String result) throws IOException
	{
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<CosmicData>>()
		{
		}.getType();
		Collection<CosmicData> results = gson.fromJson(result, collectionType);

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("ID", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("feature_type", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("alt_alleles", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("end", FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("seq_region_name", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("consequence_type", FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("strand", FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("start", FieldTypeEnum.INT));
		return metadata;
	}
}
