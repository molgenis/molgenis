package org.molgenis.omx.biobankconnect.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

public class AlgorithmGenerator
{
	@Autowired
	private OntologyMatcher ontologyMatcher;

	@Autowired
	private AlgorithmScriptLibrary algorithmScriptLibrary;

	@Autowired
	private AlgorithmUnitConverter algorithmUnitConverter;

	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	private final static String NODE_PATH = "nodePath";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";

	@RunAsSystem
	public String generateAlgorithm(String userName, OntologyMatcherRequest request)
	{
		StringBuilder suggestedScript = new StringBuilder();

		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		if (selectedDataSetIds.size() > 0)
		{
			SearchResult searchResult = ontologyMatcher.generateMapping(userName, request.getFeatureId(),
					request.getTargetDataSetId(), selectedDataSetIds.get(0));
			ObservableFeature standardFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
					request.getFeatureId(), ObservableFeature.class);
			String scriptTemplate = algorithmScriptLibrary.findScriptTemplate(standardFeature);
			if (searchResult.getTotalHitCount() > 0)
			{
				if (StringUtils.isEmpty(scriptTemplate))
				{
					suggestedScript.append(convertToJavascript(standardFeature, searchResult));
				}
				else
				{
					suggestedScript.append(convertToJavascriptByFormula(scriptTemplate, standardFeature, searchResult));
				}
			}
		}

		return suggestedScript.toString();
	}

	/**
	 * Convert to Javascript based on the variable matching only
	 * 
	 * @param standardFeature
	 * @param searchResult
	 * @return
	 */
	private String convertToJavascript(ObservableFeature standardFeature, SearchResult searchResult)
	{
		Hit hit = searchResult.getSearchHits().get(0);
		ObservableFeature customFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				Integer.parseInt(hit.getColumnValueMap().get(ObservableFeature.ID.toLowerCase()).toString()),
				ObservableFeature.class);
		String conversionScript = algorithmUnitConverter.convert(standardFeature.getUnit(), customFeature.getUnit());
		StringBuilder javaScript = new StringBuilder();
		javaScript.append(createJavascriptName(customFeature.getName(), conversionScript, false));

		// If two variables are categorical, map the value codes onto each other
		if (standardFeature.getDataType().equalsIgnoreCase(MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL.toString())
				&& customFeature.getDataType()
						.equalsIgnoreCase(MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL.toString()))
		{
			Iterable<Category> categoriesForStandardFeature = dataService.findAll(Category.ENTITY_NAME,
					new QueryImpl().eq(Category.OBSERVABLEFEATURE, standardFeature), Category.class);

			Iterable<Category> categoriesForCustomFeature = dataService.findAll(Category.ENTITY_NAME,
					new QueryImpl().eq(Category.OBSERVABLEFEATURE, customFeature), Category.class);

			Map<String, String> valueCodeMapping = new HashMap<String, String>();
			for (Category customCategory : categoriesForCustomFeature)
			{
				double similarityScore = 0;
				String mappedValueCode = null;
				for (Category standardCategory : categoriesForStandardFeature)
				{
					double score = NGramMatchingModel.stringMatching(customCategory.getName(),
							standardCategory.getName(), false);
					if (score > similarityScore)
					{
						similarityScore = score;
						mappedValueCode = standardCategory.getValueCode();
					}
				}
				if (mappedValueCode != null) valueCodeMapping.put(customCategory.getValueCode(), mappedValueCode);
			}
			if (valueCodeMapping.size() > 0)
			{
				javaScript.append(".map({");
				for (Entry<String, String> entry : valueCodeMapping.entrySet())
				{
					javaScript.append("'").append(entry.getKey()).append("'").append(" : ").append("'")
							.append(entry.getValue()).append("',");
				}
				javaScript.delete(javaScript.length() - 1, javaScript.length());
				javaScript.append("})");
			}
		}
		return javaScript.toString();
	}

	/**
	 * Convert to Javascript based on the formula pre-defined. E.g. BMI,
	 * Hypertension
	 * 
	 * @param scriptTemplate
	 * @param standardFeature
	 * @param searchResult
	 * @return
	 */
	private String convertToJavascriptByFormula(String scriptTemplate, ObservableFeature standardFeature,
			SearchResult searchResult)
	{
		for (String standardFeatureName : ApplyAlgorithms.extractFeatureName(scriptTemplate))
		{
			SearchResult result = algorithmScriptLibrary.findOntologyTerm(Arrays.asList(standardFeatureName));
			if (result.getTotalHitCount() > 0)
			{
				Hit bestMatchedFeature = null;
				int miniDistance = 1000000;
				for (Hit candidateFeature : searchResult.getSearchHits())
				{
					int distance = compareOntologyTermDistance(result.getSearchHits().get(0),
							findOntologyTerms(candidateFeature));
					if (distance >= 0 && distance < miniDistance)
					{
						miniDistance = distance;
						bestMatchedFeature = candidateFeature;
					}
				}
				if (bestMatchedFeature != null)
				{
					ObservableFeature mappedFeature = dataService.findOne(
							ObservableFeature.ENTITY_NAME,
							Integer.parseInt(bestMatchedFeature.getColumnValueMap()
									.get(ObservableFeature.ID.toLowerCase()).toString()), ObservableFeature.class);
					String conversionScript = algorithmUnitConverter.convert(standardFeature.getUnit(),
							mappedFeature.getUnit());
					String mappedFeatureJavaScriptName = createJavascriptName(bestMatchedFeature.getColumnValueMap()
							.get(ObservableFeature.NAME.toLowerCase()).toString(), conversionScript, true);
					String standardJavaScriptName = createJavascriptName(standardFeatureName, null, true);
					scriptTemplate = scriptTemplate.replaceAll(standardJavaScriptName, mappedFeatureJavaScriptName);
				}
			}
		}
		return scriptTemplate;
	}

	private String createJavascriptName(String mappedFeatureName, String suffix, boolean escaped)
	{
		StringBuilder javaScriptName = new StringBuilder();
		javaScriptName.append(escaped ? "\\$\\('" : "$('").append(mappedFeatureName).append(escaped ? "'\\)" : "')");
		if (!StringUtils.isEmpty(suffix)) javaScriptName.append(suffix);
		return javaScriptName.toString();
	}

	private List<Hit> findOntologyTerms(Hit candidateFeature)
	{
		Integer featureId = Integer.parseInt(candidateFeature.getColumnValueMap()
				.get(ObservableFeature.ID.toLowerCase()).toString());
		ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId,
				ObservableFeature.class);

		QueryImpl query = new QueryImpl();
		for (OntologyTerm ot : feature.getDefinitions())
		{
			if (query.getRules().size() > 0) query.addRule(new QueryRule(Operator.OR));
			query.addRule(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, ot.getTermAccession()));
		}
		return searchService.search(new SearchRequest(null, query, null)).getSearchHits();
	}

	private int compareOntologyTermDistance(Hit targetOntologyTerm, List<Hit> sourceOntologyTerms)
	{
		int miniDistance = 1000000;
		List<String> totTermPathParts = Arrays.asList(targetOntologyTerm.getColumnValueMap().get(NODE_PATH).toString()
				.split("\\."));
		Set<String> uniquePaths = new HashSet<String>();
		for (Hit sourceOntologyTerm : sourceOntologyTerms)
		{
			if (sourceOntologyTerm.getColumnValueMap().containsKey(NODE_PATH))
			{
				uniquePaths.add(sourceOntologyTerm.getColumnValueMap().get(NODE_PATH).toString());
			}
		}
		for (String uniquePath : uniquePaths)
		{
			List<String> sosPathParts = new ArrayList<String>(Arrays.asList(uniquePath.split("\\.")));
			int beforeRemove = sosPathParts.size();
			sosPathParts.removeAll(totTermPathParts);
			int afterRemove = sosPathParts.size();
			int distance = (beforeRemove + totTermPathParts.size()) - 2 * (beforeRemove - afterRemove);
			if (distance == 0) return distance;
			if (distance > 0 && distance < miniDistance) miniDistance = distance;
		}
		return miniDistance;
	}
}
