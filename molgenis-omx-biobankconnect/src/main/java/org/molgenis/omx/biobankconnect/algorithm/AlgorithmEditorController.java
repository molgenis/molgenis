package org.molgenis.omx.biobankconnect.algorithm;

import static org.molgenis.omx.biobankconnect.algorithm.AlgorithmEditorController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.js.ScriptEvaluator;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.biobankconnect.wizard.BiobankConnectWizard;
import org.molgenis.omx.biobankconnect.wizard.ChooseCataloguePage;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.biobankconnect.wizard.OntologyAnnotatorPage;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchResult;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.mozilla.javascript.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class AlgorithmEditorController extends AbstractWizardController
{

	public static final String ID = "algorithm";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

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

	private BiobankConnectWizard wizard;
	private final DataService dataService;
	private final AlgorithmEditorPage algorithmEditorPage;
	private final ChooseBiobankPage chooseBiobanksPage;
	private final ChooseCataloguePage chooseCataloguePage;
	private final OntologyAnnotatorPage ontologyAnnotatorPage;

	@Autowired
	public AlgorithmEditorController(AlgorithmEditorPage algorithmEditorPage, ChooseBiobankPage chooseBiobanksPage,
			ChooseCataloguePage chooseCataloguePage, OntologyAnnotatorPage ontologyAnnotatorPage,
			DataService dataService)
	{
		super(URI, ID);
		if (algorithmEditorPage == null) throw new IllegalArgumentException("algorithmEditorPage is null!");
		if (chooseBiobanksPage == null) throw new IllegalArgumentException("chooseBiobanksPage is null!");
		if (chooseCataloguePage == null) throw new IllegalArgumentException("chooseCataloguePage is null!");
		if (ontologyAnnotatorPage == null) throw new IllegalArgumentException("ontologyAnnotatorPage is null!");
		if (dataService == null) throw new IllegalArgumentException("dataService is null!");
		this.algorithmEditorPage = algorithmEditorPage;
		this.chooseBiobanksPage = chooseBiobanksPage;
		this.chooseCataloguePage = chooseCataloguePage;
		this.ontologyAnnotatorPage = ontologyAnnotatorPage;
		this.dataService = dataService;
	}

	@Override
	public void onInit(HttpServletRequest request)
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();

		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		wizard.setDataSets(dataSets);
		currentUserStatus.setUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
				request.getRequestedSessionId());
	}

	@Override
	protected Wizard createWizard()
	{
		wizard = new BiobankConnectWizard();
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		wizard.setDataSets(dataSets);
		wizard.setUserName(userAccountService.getCurrentUser().getUsername());
		wizard.addPage(chooseCataloguePage);
		wizard.addPage(ontologyAnnotatorPage);
		wizard.addPage(chooseBiobanksPage);
		wizard.addPage(algorithmEditorPage);
		return wizard;
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
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		if (selectedDataSetIds.size() > 0)
		{
			SearchResult searchResult = ontologyMatcher.generateMapping(userName, request.getFeatureId(),
					request.getTargetDataSetId(), selectedDataSetIds.get(0));
			ObservableFeature standardFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
					request.getFeatureId(), ObservableFeature.class);
			String scriptTemplate = algorithmScriptLibrary.findScriptTemplate(standardFeature);
			if (scriptTemplate.isEmpty())
			{
				if (searchResult.getTotalHitCount() > 0)
				{
					Hit hit = searchResult.getSearchHits().get(0);
					ObservableFeature customFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
							Integer.parseInt(hit.getColumnValueMap().get("id").toString()), ObservableFeature.class);

					String conversionScript = algorithmUnitConverter.convert(standardFeature.getUnit(),
							customFeature.getUnit());
					StringBuilder suggestedScript = new StringBuilder();
					suggestedScript.append("$('").append(customFeature.getName()).append("')").append(conversionScript);
					jsonResults.put("suggestedScript", suggestedScript.toString());
				}
			}
			else
			{
				for (String standardFeatureName : extractFeatureName(scriptTemplate))
				{
					SearchResult result = algorithmScriptLibrary.findOntologyTerm(Arrays.asList(standardFeatureName));
					if (result.getTotalHitCount() > 0)
					{
						for (String synonyms : algorithmScriptLibrary.findOntologyTermSynonyms(result.getSearchHits()
								.get(0)))
						{

						}
					}

					QueryImpl query = new QueryImpl();
					for (Hit hit : searchResult)
					{
						if (query.getRules().size() > 0) query.addRule(new QueryRule(Operator.OR));
						query.addRule(new QueryRule("id", Operator.EQUALS, hit.getColumnValueMap().get("id")));
					}
					query.pageSize(100);
				}
			}
		}
		return jsonResults;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> testScrpit(@RequestBody
	OntologyMatcherRequest request)
	{
		if (request.getSelectedDataSetIds().size() == 0 || request.getAlgorithmScript().isEmpty()) return Collections
				.emptyMap();

		DataSet sourceDataSet = dataService.findOne(DataSet.ENTITY_NAME, request.getSelectedDataSetIds().get(0),
				DataSet.class);
		Iterable<ObservationSet> observationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, sourceDataSet), ObservationSet.class);
		List<String> featureNames = extractFeatureName(request.getAlgorithmScript());
		Iterable<ObservableFeature> featureIterators = dataService.findAll(ObservableFeature.ENTITY_NAME,
				new QueryImpl().in(ObservableFeature.NAME, featureNames), ObservableFeature.class);
		Iterable<ObservedValue> observedValueIterators = dataService.findAll(
				ObservedValue.ENTITY_NAME,
				new QueryImpl().and().in(ObservedValue.FEATURE, featureIterators).and()
						.in(ObservedValue.OBSERVATIONSET, observationSets), ObservedValue.class);

		Map<Integer, MapEntity> eachIndividualValues = new HashMap<Integer, MapEntity>();
		for (ObservedValue value : observedValueIterators)
		{
			ObservationSet observationSet = value.getObservationSet();
			Integer observationSetId = observationSet.getId();
			if (!eachIndividualValues.containsKey(observationSetId)) eachIndividualValues.put(observationSetId,
					new MapEntity());
			Object valueObject = value.getValue().get("value");
			if (valueObject instanceof Integer) eachIndividualValues.get(observationSetId).set(
					value.getFeature().getName(), Integer.parseInt(value.getValue().get("value").toString()));

			if (valueObject instanceof Double) eachIndividualValues.get(observationSetId).set(
					value.getFeature().getName(), Double.parseDouble(value.getValue().get("value").toString()));

			if (valueObject instanceof Category) eachIndividualValues.get(observationSetId).set(
					value.getFeature().getName(),
					Integer.parseInt(((Category) value.getValue().get("value")).getValueCode()));
		}

		List<Object> results = new ArrayList<Object>();
		for (MapEntity mapEntity : eachIndividualValues.values())
		{
			if (Iterables.size(mapEntity.getAttributeNames()) != featureNames.size()) continue;
			Object result = ScriptEvaluator.eval(request.getAlgorithmScript(), mapEntity);
			Object untypedResult = new Double(Context.toNumber(result));
			results.add(untypedResult);
		}
		Map<String, Object> jsonResults = new HashMap<String, Object>();
		jsonResults.put("results", results);
		jsonResults.put("totalCounts", Iterables.size(observationSets));
		return jsonResults;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/savescript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, String> saveScript(@RequestBody
	OntologyMatcherRequest request)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		if (request.getSelectedDataSetIds().size() > 0)
		{
			return ontologyMatcher.updateScript(userName, request);
		}
		return new HashMap<String, String>();
	}

	private List<String> extractFeatureName(String algorithmScript)
	{
		List<String> featureNames = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\$\\('([^\\$\\(\\)]*)'\\)");
		Matcher matcher = pattern.matcher(algorithmScript);
		while (matcher.find())
		{
			featureNames.add(matcher.group(1));
		}
		if (featureNames.size() > 0) return featureNames;

		pattern = Pattern.compile("\\$\\(([^\\$\\(\\)]*)\\)");
		matcher = pattern.matcher(algorithmScript);
		while (matcher.find())
		{
			featureNames.add(matcher.group(1));
		}
		return featureNames;
	}

	@Override
	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Arrays.asList("bootstrap-fileupload.min.js", "jquery-ui-1.9.2.custom.min.js", "common-component.js",
				"catalogue-chooser.js", "ontology-annotator.js", "ontology-matcher.js", "mapping-manager.js",
				"algorithm-editor.js", "biobank-connect.js", "jstat.min.js", "d3.min.js", "vega.min.js",
				"biobankconnect-graph.js");
	}

	@Override
	@ModelAttribute("stylesheets")
	public List<String> getStylesheets()
	{
		return Arrays.asList("bootstrap-fileupload.min.css", "jquery-ui-1.9.2.custom.min.css", "biobank-connect.css",
				"catalogue-chooser.css", "ontology-matcher.css", "mapping-manager.css", "ontology-annotator.css",
				"algorithm-editor.css");
	}
}