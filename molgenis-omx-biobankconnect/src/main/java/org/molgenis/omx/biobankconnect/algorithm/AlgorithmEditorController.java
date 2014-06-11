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

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyServiceRequest;
import org.molgenis.omx.biobankconnect.wizard.BiobankConnectWizard;
import org.molgenis.omx.biobankconnect.wizard.ChooseCataloguePage;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.biobankconnect.wizard.OntologyAnnotatorPage;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;

@Controller
@RequestMapping(URI)
public class AlgorithmEditorController extends AbstractWizardController
{

	public static final String ID = "algorithm";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyMatcher ontologyMatcher;
	@Autowired
	private OntologyAnnotator ontologyAnnotator;
	@Autowired
	private UserAccountService userAccountService;
	@Autowired
	private CurrentUserStatus currentUserStatus;
	@Autowired
	private AlgorithmUnitConverter algorithmUnitConverter;
	@Autowired
	private AlgorithmScriptLibrary algorithmScriptLibrary;
	@Autowired
	private AlgorithmGenerator algorithmGenerator;
	@Autowired
	private ApplyAlgorithms applyAlgorithms;
	@Autowired
	private SearchService searchService;

	private BiobankConnectWizard wizard;
	private final DataService dataService;
	private final ChooseBiobankPage chooseBiobanksPage;
	private final OntologyAnnotatorPage ontologyAnnotatorPage;
	private final ChooseCataloguePage chooseCataloguePage;
	private final AlgorithmEditorPage algorithmEditorPage;
	private final AlgorithmGeneratorPage algorithmGeneratorPage;

	@Autowired
	public AlgorithmEditorController(ChooseBiobankPage chooseBiobanksPage, OntologyAnnotatorPage ontologyAnnotatorPage,
			ChooseCataloguePage chooseCataloguePage, AlgorithmEditorPage algorithmEditorPage,
			AlgorithmGeneratorPage algorithmGeneratorPage, DataService dataService)
	{
		super(URI, ID);
		if (algorithmEditorPage == null) throw new IllegalArgumentException("algorithmEditorPage is null!");
		if (chooseBiobanksPage == null) throw new IllegalArgumentException("chooseBiobanksPage is null!");
		if (chooseCataloguePage == null) throw new IllegalArgumentException("chooseCataloguePage is null!");
		if (ontologyAnnotatorPage == null) throw new IllegalArgumentException("ontologyAnnotatorPage is null!");
		if (algorithmGeneratorPage == null) throw new IllegalArgumentException("algorithmGeneratorPage is null!");
		if (dataService == null) throw new IllegalArgumentException("dataService is null!");
		this.chooseBiobanksPage = chooseBiobanksPage;
		this.ontologyAnnotatorPage = ontologyAnnotatorPage;
		this.chooseCataloguePage = chooseCataloguePage;
		this.algorithmEditorPage = algorithmEditorPage;
		this.algorithmGeneratorPage = algorithmGeneratorPage;
		this.dataService = dataService;
	}

	@Override
	public void onInit(HttpServletRequest request)
	{
		wizard.setDataSets(getBiobankDataSets());
		currentUserStatus.setUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
				request.getRequestedSessionId());
	}

	@Override
	protected Wizard createWizard()
	{
		wizard = new BiobankConnectWizard();
		wizard.setDataSets(getBiobankDataSets());
		wizard.setUserName(userAccountService.getCurrentUser().getUsername());
		wizard.addPage(chooseCataloguePage);
		wizard.addPage(ontologyAnnotatorPage);
		wizard.addPage(chooseBiobanksPage);
		wizard.addPage(algorithmEditorPage);
		wizard.addPage(algorithmGeneratorPage);
		return wizard;
	}

	private List<DataSet> getBiobankDataSets()
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : allDataSets)
		{
			if (dataSet.getProtocolUsed().getIdentifier().equals(AsyncOntologyMatcher.PROTOCOL_IDENTIFIER)) continue;
			if (dataSet.getIdentifier().matches("^" + userAccountService.getCurrentUser().getUsername() + ".*derived$")) continue;
			dataSets.add(dataSet);
		}
		return dataSets;
	}

	@RequestMapping(value = "/annotate", method = RequestMethod.POST)
	public String annotate(HttpServletRequest request)
	{
		ontologyAnnotator.removeAnnotations(wizard.getSelectedDataSet().getId());
		if (request.getParameter("selectedOntologies") != null)
		{
			List<String> documentTypes = new ArrayList<String>();
			for (String ontologyUri : request.getParameter("selectedOntologies").split(","))
			{
				documentTypes.add("ontologyTerm-" + ontologyUri);
			}
			ontologyAnnotator.annotate(wizard.getSelectedDataSet().getId(), documentTypes);
		}
		return init(request);
	}

	@RequestMapping(value = "/annotate/remove", method = RequestMethod.POST)
	public String removeAnnotations(HttpServletRequest request) throws Exception
	{
		ontologyAnnotator.removeAnnotations(wizard.getSelectedDataSet().getId());
		return init(request);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/annotate/update", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateDocument(@RequestBody
	UpdateIndexRequest request)
	{
		ontologyAnnotator.updateIndex(request);
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
		if (selectedDataSetIds.size() != 0 && !algorithm.isEmpty())
		{
			Integer sourceDataSetId = selectedDataSetIds.get(0);
			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, request.getFeatureId(),
					ObservableFeature.class);
			String message = applyAlgorithms.validateAlgorithmInputs(sourceDataSetId, algorithm);
			Collection<Object> results = message.isEmpty() ? applyAlgorithms.createValueFromAlgorithm(
					feature.getDataType(), sourceDataSetId, algorithm).values() : Collections.emptyList();
			jsonResults.put("results", results);
			jsonResults.put("message", message);
			jsonResults.put("totalCounts", countRowsByDataSet(sourceDataSetId));
		}
		return jsonResults;
	}

	private Integer countRowsByDataSet(Integer sourceDataSetId)
	{
		DataSet sourceDataSet = dataService.findOne(DataSet.ENTITY_NAME, sourceDataSetId, DataSet.class);
		Iterable<ObservationSet> observationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, sourceDataSet), ObservationSet.class);
		return Iterables.size(observationSets);
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

	@RequestMapping(method = RequestMethod.GET, value = "/progress", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> progress(@RequestBody
	OntologyMatcherRequest request)
	{
		Map<String, Object> jsonResults = new HashMap<String, Object>();
		String userName = userAccountService.getCurrentUser().getUsername();
		jsonResults.put("isRunning", currentUserStatus.isUserMatching(userName));
		jsonResults.put("percentage", currentUserStatus.getPercentageOfProcessForUser(userName));
		return jsonResults;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/allattributes")
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

	@RequestMapping(method = RequestMethod.POST, value = "/attribute")
	@ResponseBody
	public SearchResult getOneAttribute(@RequestBody
	String featureId)
	{
		return searchService.search(new SearchRequest(null, new QueryImpl().eq(ObservableFeature.ID, featureId).and()
				.eq("type", "observablefeature"), null));
	}

	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, value = "/ontologyterm")
	@ResponseBody
	public SearchResult query(@RequestBody
	OntologyServiceRequest ontologyTermRequest)
	{
		String ontologyUrl = ontologyTermRequest.getOntologyUrl();
		String queryString = ontologyTermRequest.getQueryString();
		if (queryString == null) return new SearchResult(0, Collections.<Hit> emptyList());
		return ontologyService.search(ontologyUrl, queryString);
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
}