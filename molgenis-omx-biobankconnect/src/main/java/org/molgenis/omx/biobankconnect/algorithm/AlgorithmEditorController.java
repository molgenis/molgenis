package org.molgenis.omx.biobankconnect.algorithm;

import static org.molgenis.omx.biobankconnect.algorithm.AlgorithmEditorController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyServiceRequest;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
@RequestMapping(URI)
public class AlgorithmEditorController extends MolgenisPluginController
{
	public static final String ID = "algorithm";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyMatcher ontologyMatcher;
	@Autowired
	private UserAccountService userAccountService;
	@Autowired
	private CurrentUserStatus currentUserStatus;
	@Autowired
	private AlgorithmGenerator algorithmGenerator;
	@Autowired
	private ApplyAlgorithms applyAlgorithms;
	@Autowired
	private SearchService searchService;
	@Autowired
	private DataService dataService;

	public AlgorithmEditorController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createmapping", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult createMappings(@RequestBody
	OntologyMatcherRequest request)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		if (selectedDataSetIds.size() > 0)
		{
			return ontologyMatcher.generateMapping(userName, request.getFeatureId(), request.getTargetDataSetId(),
					selectedDataSetIds.get(0));
		}
		return new SearchResult(0, Collections.<Hit> emptyList());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/suggestscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> suggestScript(@RequestBody
	OntologyMatcherRequest request)
	{
		Map<String, Object> jsonResults = new HashMap<String, Object>();
		String userName = userAccountService.getCurrentUser().getUsername();
		String suggestedScript = algorithmGenerator.generateAlgorithm(userName, request);
		jsonResults.put("suggestedScript", suggestedScript);
		return jsonResults;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> testScrpit(@RequestBody
	OntologyMatcherRequest request)
	{
		Map<String, Object> jsonResults = new HashMap<String, Object>();
		String algorithm = request.getAlgorithmScript();
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		if (selectedDataSetIds.size() != 0 && !StringUtils.isEmpty(algorithm))
		{
			Integer sourceDataSetId = selectedDataSetIds.get(0);
			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, request.getFeatureId(),
					ObservableFeature.class);
			String message = applyAlgorithms.validateAlgorithmInputs(sourceDataSetId, algorithm);
			Collection<Object> results = StringUtils.isEmpty(message) ? applyAlgorithms.createValueFromAlgorithm(
					feature.getDataType(), sourceDataSetId, algorithm).values() : Collections.emptyList();
			jsonResults.put("results", results);
			jsonResults.put("message", message);
			jsonResults.put("totalCounts", countRowsByDataSet(sourceDataSetId));
		}
		return jsonResults;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/savescript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, String> saveScript(@RequestBody
	OntologyMatcherRequest request)
	{
		Integer targetDataSetId = request.getTargetDataSetId();
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		// target data set and selected datasets need to be not null
		if (targetDataSetId == null || selectedDataSetIds == null || request.getFeatureId() == null)
		{
			return Collections.<String, String> emptyMap();
		}
		// at least one of following values need to be not null (algorithm
		// script or featureIds)
		if (request.getAlgorithmScript() == null && request.getMappedFeatureIds() == null)
		{
			return Collections.<String, String> emptyMap();
		}
		if (selectedDataSetIds.size() > 0)
		{
			return ontologyMatcher.updateScript(userAccountService.getCurrentUser().getUsername(), request);
		}
		return Collections.<String, String> emptyMap();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/allattributes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult getAllAttributes(@RequestBody
	Map<String, Object> request)
	{
		if (request.get("dataSetId") == null) return new SearchResult("dataSetId cannot be null!");
		Object dataSetId = request.get("dataSetId");
		Object queryString = request.get("queryString");
		Object approximate = (request.get("approximate") == null) ? false : request.get("approximate");
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, dataSetId, DataSet.class);

		Query query = new QueryImpl();
		if (request.get("query") != null)
		{
			query = new Gson().fromJson(request.get("query").toString(), QueryImpl.class);
		}
		query.eq("type", "observablefeature");

		if (queryString != null && !queryString.toString().isEmpty())
		{
			if ((boolean) approximate)
			{
				query.and().like(ObservableFeature.NAME.toLowerCase(), queryString.toString());
			}
			else query.and().search(queryString.toString());
		}
		return searchService
				.search(new SearchRequest("protocolTree-" + dataSet.getProtocolUsed().getId(), query, null));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/attribute", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult getOneAttribute(@RequestBody
	String featureId)
	{
		return searchService.search(new SearchRequest(null, new QueryImpl().eq(ObservableFeature.ID, featureId).and()
				.eq("type", "observablefeature"), null));
	}

	@RequestMapping(method = POST, value = "/ontologyterm", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult query(@RequestBody
	OntologyServiceRequest ontologyTermRequest)
	{
		String ontologyIri = ontologyTermRequest.getOntologyIri();
		String queryString = ontologyTermRequest.getQueryString();
		if (queryString == null) return new SearchResult(0, Collections.<Hit> emptyList());
		return ontologyService.search(ontologyIri, queryString);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getmapping", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult getMappings(@RequestBody
	Map<String, Object> request)
	{
		if (request.get("dataSetIdentifier") == null) return new SearchResult("dataSetId cannot be null!");
		Object dataSetIdentifier = request.get("dataSetIdentifier");
		Object featureIds = request.get("featureIds");

		Query query = new QueryImpl();
		if (featureIds != null && !featureIds.toString().isEmpty())
		{
			if (query.getRules().size() > 0) query.or();
			for (Object featureId : (List<?>) featureIds)
			{
				query.eq(AsyncOntologyMatcher.STORE_MAPPING_FEATURE, featureId.toString());
			}
		}
		return searchService.search(new SearchRequest(dataSetIdentifier.toString(), query, null));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getstores", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, List<DataSet>> getDataSetsForMappings(@RequestBody
	String dataSetId)
	{
		Map<String, List<DataSet>> results = new HashMap<String, List<DataSet>>();
		List<DataSet> dataSets = new ArrayList<DataSet>();
		if (dataSetId != null)
		{
			StringBuilder prefixForStoreIdentifiers = new StringBuilder();
			prefixForStoreIdentifiers.append(userAccountService.getCurrentUser().getUsername()).append('-')
					.append(dataSetId);
			Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME,
					new QueryImpl().like(DataSet.IDENTIFIER, prefixForStoreIdentifiers.toString()), DataSet.class);
			for (DataSet dataSet : allDataSets)
			{
				if (dataSet.getProtocolUsed().getIdentifier().equals(AsyncOntologyMatcher.PROTOCOL_IDENTIFIER))
				{
					dataSets.add(dataSet);
				}
			}
		}
		results.put("results", dataSets);
		return results;
	}

	/**
	 * Helper class to count the number of observationSets in a dataset, in
	 * another word the number of rows in a dataset
	 * 
	 * @param sourceDataSetId
	 * @return
	 */
	private long countRowsByDataSet(Integer sourceDataSetId)
	{
		DataSet sourceDataSet = dataService.findOne(DataSet.ENTITY_NAME, sourceDataSetId, DataSet.class);
		return dataService.count(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, sourceDataSet));
	}
}