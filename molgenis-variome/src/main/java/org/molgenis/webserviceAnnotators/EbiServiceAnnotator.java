package org.molgenis.webserviceAnnotators;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * <p>
 * This class calls the EBI Chembl web service with UniProt ID's located in the repository that is
 * passed on from the controller. The features that Chembl returns are used to create a new protocol. 
 * Values for each observed set are added to the repository
 * </p>
 * 
 * <p>
 * EBI returns: {target={targetType, chemblId, geneNames, description, compoundCount, bioactivityCount,
 * proteinAccession, synonyms, organism, preferredName}}
 * </p>
 * 
 * @author mdehaan
 * @param Repository
 * @return Repository
 * @version EBI: database version 17, prepared on 29th August 2013
 * 
 * */
@Component
public class EbiServiceAnnotator implements RepositoryAnnotator
{

	// EBI service is dependant on this ID when the web service is called
	// If Uniprot ID is not in a data set, the web service cannot be used
	private static final String UNIPROT_ID = "uniprot_id";

	@Autowired
	DataService dataService;

	@Override
	@Transactional
	public Repository annotate(Repository source)
	{

		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, source.getName()), DataSet.class);

		HttpClient httpClient = new DefaultHttpClient();
		String serviceUrl = "";

		// add new protocol to store the results of the web service
		Protocol resultProtocol = new Protocol();
		resultProtocol.setIdentifier("chembl_results");
		resultProtocol.setName("chembl_results");
		dataService.add(Protocol.ENTITY_NAME, resultProtocol);

		// create a list with all the feature names returned by Chembl
		List<String> featureNames = new ArrayList<String>();
		featureNames = ChemblFeatureNames(featureNames);

		// create features to store the results of the web service
		for (String name : featureNames)
		{

			ObservableFeature newFeature = new ObservableFeature();
			newFeature.setIdentifier(name + "_id");
			newFeature.setName(name);
			dataService.add(ObservableFeature.ENTITY_NAME, newFeature);

			resultProtocol.getFeatures().add(newFeature);
		}

		dataService.update(Protocol.ENTITY_NAME, resultProtocol);

		// add resultProtocol to the protocol_used of the data set
		Protocol repositoryProtocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, dataSet.getProtocolUsed().getIdentifier()), Protocol.class);

		repositoryProtocol.getSubprotocols().add(resultProtocol);
		dataService.update(Protocol.ENTITY_NAME, repositoryProtocol);

		Iterable<ObservationSet> osSet = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);

		for (ObservationSet os : osSet)
		{
			CrudRepository valueRepo = (CrudRepository) dataService
					.getRepositoryByEntityName(ObservedValue.ENTITY_NAME);

			// TODO solve hard coded column name on which the called annotation
			// source is depending
			ObservableFeature f = dataService.findOne(ObservableFeature.ENTITY_NAME,
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, UNIPROT_ID), ObservableFeature.class);

			ObservedValue value = valueRepo.findOne(
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os).eq(ObservedValue.FEATURE, f),
					ObservedValue.class);

			serviceUrl = "https://www.ebi.ac.uk/chemblws/targets/uniprot/" + value.getValue().getString("value")
					+ ".json";

			HttpGet httpGet = new HttpGet(serviceUrl);

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

					for (String columnName : featureNames)
					{
						StringValue sv = new StringValue();
						sv.setValue(resultMap.get(columnName).toString());
						dataService.add(StringValue.ENTITY_NAME, sv);

						ObservedValue ov = new ObservedValue();

						ObservableFeature thisFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
								new QueryImpl().eq(ObservableFeature.IDENTIFIER, columnName + "_id"),
								ObservableFeature.class);

						ov.setFeature(thisFeature);
						ov.setObservationSet(os);
						ov.setValue(sv);
						dataService.add(ObservedValue.ENTITY_NAME, ov);
					}
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

		return null;
	}

	private static HashMap<String, Object> jsonStringToMap(String result) throws IOException
	{
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>()
		{
		};

		return mapper.readValue(result, typeRef);
	}

	private List<String> ChemblFeatureNames(List<String> featureNames)
	{
		String[] chembleFeatures = new String[]
		{ "targetType", "chemblId", "geneNames", "description", "compoundCount", "bioactivityCount",
				"proteinAccession", "synonyms", "organism", "preferredName" };

		for (String feature : chembleFeatures)
		{
			featureNames.add(feature);
		}

		return featureNames;
	}
}