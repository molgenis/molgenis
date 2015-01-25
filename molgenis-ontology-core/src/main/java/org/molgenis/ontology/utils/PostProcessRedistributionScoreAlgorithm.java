package org.molgenis.ontology.utils;

import static org.molgenis.ontology.beans.ComparableEntity.COMBINED_SCORE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SINGLE_WHITESPACE;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.service.OntologyServiceImpl;

public class PostProcessRedistributionScoreAlgorithm
{
	private static final String DEFAULT_FIELD = "Name";

	public static void process(List<ComparableEntity> entities, Map<String, Object> inputData,
			OntologyServiceImpl ontologyService)
	{
		if (inputData.size() == 1 && inputData.containsKey(DEFAULT_FIELD))
		{
			String queryString = inputData.get(DEFAULT_FIELD).toString();
			Set<String> wordsInQueryString = AlgorithmHelper.medicalStemProxy(queryString);
			int totalDocs = entities.size() > 10 ? 10 : entities.size();

			Map<String, Double> wordAdjustedScore = redistributedScore(queryString, entities, totalDocs,
					ontologyService);

			for (ComparableEntity entity : entities.subList(0, totalDocs))
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
			int totalDocs, OntologyServiceImpl ontologyService)
	{
		// Collect the frequencies for all of the unique words from query string
		Set<String> wordsInQueryString = AlgorithmHelper.medicalStemProxy(queryString);
		double queryStringLength = StringUtils.join(wordsInQueryString, SINGLE_WHITESPACE).trim().length();
		Map<String, Integer> wordFreqMap = AlgorithmHelper.createWordFreq(queryString, entities, totalDocs,
				ontologyService);

		Map<String, Double> wordWeightedSimilarity = new HashMap<String, Double>();
		double averageIDFValue = 0;
		for (Entry<String, Integer> entry : wordFreqMap.entrySet())
		{
			BigDecimal idfValue = new BigDecimal(1 + Math.log10((double) totalDocs / (entry.getValue() + 1)));
			averageIDFValue += idfValue.doubleValue();
			wordWeightedSimilarity.put(entry.getKey(), entry.getKey().length() / queryStringLength * 100);
		}
		averageIDFValue = averageIDFValue / wordFreqMap.size();

		double totalContribution = 0;
		double totalDenominator = 0;
		for (Entry<String, Integer> entry : wordFreqMap.entrySet())
		{
			BigDecimal idfValue = new BigDecimal(1 + Math.log10((double) totalDocs / (entry.getValue() + 1)));
			double diff = idfValue.doubleValue() - averageIDFValue;
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

		for (Entry<String, Integer> entry : wordFreqMap.entrySet())
		{
			BigDecimal idfValue = new BigDecimal(1 + Math.log10((double) totalDocs / (entry.getValue() + 1)));
			double diff = idfValue.doubleValue() - averageIDFValue;
			if (diff > 0)
			{
				wordWeightedSimilarity.put(entry.getKey(), ((diff / totalDenominator) * totalContribution));
			}
		}
		return wordWeightedSimilarity;
	}
}