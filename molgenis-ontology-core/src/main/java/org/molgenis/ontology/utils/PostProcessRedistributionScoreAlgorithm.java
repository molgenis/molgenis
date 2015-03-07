package org.molgenis.ontology.utils;

import static org.molgenis.ontology.beans.ComparableEntity.COMBINED_SCORE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SINGLE_WHITESPACE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.service.OntologyServiceImpl;

public class PostProcessRedistributionScoreAlgorithm
{
	private static final int MAX_NUM = 100;

	public static void process(List<ComparableEntity> entities, Map<String, Object> inputData,
			OntologyServiceImpl ontologyService)
	{
		if (inputData.size() == 1 && inputData.containsKey(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD)
				&& entities.size() > 0)
		{
			String queryString = inputData.get(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD).toString();
			Set<String> wordsInQueryString = AlgorithmHelper.medicalStemProxy(queryString);

			Map<String, Double> wordAdjustedScore = redistributedScore(queryString, entities, ontologyService);

			for (ComparableEntity entity : entities.subList(0, entities.size() > MAX_NUM ? MAX_NUM : entities.size()))
			{
				String combineSynonyms = AlgorithmHelper.combineSynonyms(entity, queryString, ontologyService);
				Set<String> wordsInOntologyTerm = AlgorithmHelper.medicalStemProxy(combineSynonyms);

				for (String word : wordsInQueryString)
				{
					if (wordsInOntologyTerm.contains(word) && wordAdjustedScore.containsKey(word))
					{
						entity.set(COMBINED_SCORE,
								(entity.getCombinedScore().doubleValue() + wordAdjustedScore.get(word)));
					}
				}
			}
		}
	}

	private static Map<String, Double> redistributedScore(String queryString, List<ComparableEntity> entities,
			OntologyServiceImpl ontologyService)
	{
		// Collect the frequencies for all of the unique words from query string
		String ontologyIri = entities.get(0).getString(OntologyTermQueryRepository.ONTOLOGY_IRI);
		Set<String> wordsInQueryString = AlgorithmHelper.medicalStemProxy(queryString);
		double queryStringLength = StringUtils.join(wordsInQueryString, SINGLE_WHITESPACE).trim().length();
		Map<String, Double> wordIDFMap = AlgorithmHelper.createWordIDF(queryString, ontologyIri, ontologyService);

		Map<String, Double> wordWeightedSimilarity = new HashMap<String, Double>();
		double averageIDFValue = 0;
		for (Entry<String, Double> entry : wordIDFMap.entrySet())
		{
			averageIDFValue += entry.getValue();
			wordWeightedSimilarity.put(entry.getKey(), entry.getKey().length() / queryStringLength * 100);
		}
		averageIDFValue = averageIDFValue / wordIDFMap.size();

		double totalContribution = 0;
		double totalDenominator = 0;
		for (Entry<String, Double> entry : wordIDFMap.entrySet())
		{
			// BigDecimal idfValue = new BigDecimal(1 + Math.log((double) totalDocs / (entry.getValue() + 1)));
			double diff = entry.getValue() - averageIDFValue;
			if (diff < 0)
			{
				Double contributedSimilarity = wordWeightedSimilarity.get(entry.getKey()) * (diff / averageIDFValue);
				totalContribution += Math.abs(contributedSimilarity);
				wordWeightedSimilarity.put(entry.getKey(), contributedSimilarity);
			}
			else
			{
				totalDenominator += diff;
			}
		}

		for (Entry<String, Double> entry : wordIDFMap.entrySet())
		{
			double diff = entry.getValue() - averageIDFValue;
			if (diff > 0)
			{
				wordWeightedSimilarity.put(entry.getKey(), ((diff / totalDenominator) * totalContribution));
			}
		}
		return wordWeightedSimilarity;
	}
}