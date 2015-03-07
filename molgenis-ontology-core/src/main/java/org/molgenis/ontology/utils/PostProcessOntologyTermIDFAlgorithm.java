package org.molgenis.ontology.utils;

import static org.molgenis.ontology.beans.ComparableEntity.COMBINED_SCORE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SINGLE_WHITESPACE;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.service.OntologyServiceImpl;

public class PostProcessOntologyTermIDFAlgorithm
{
	private static final int MAX_NUM = 100;
	private static final String DEFAULT_FIELD = "Name";

	public static void process(List<ComparableEntity> entities, Map<String, Object> inputData,
			OntologyServiceImpl ontologyService)
	{
		if (inputData.size() == 1 && inputData.containsKey(DEFAULT_FIELD) && entities.size() > 0)
		{
			String queryString = inputData.get(DEFAULT_FIELD).toString();

			int totalDocs = entities.size() > MAX_NUM ? MAX_NUM : entities.size();

			Set<String> wordsInQueryString = AlgorithmHelper.medicalStemProxy(queryString);
			// Collect the frequencies for all of the unique words from query string
			Map<String, Double> wordIDFMap = AlgorithmHelper.createWordIDF(queryString,
					entities.get(0).getString(OntologyTermQueryRepository.ONTOLOGY_IRI), ontologyService);

			for (ComparableEntity entity : entities.subList(0, totalDocs))
			{
				String combineSynonyms = AlgorithmHelper.combineSynonyms(entity, queryString, ontologyService);

				Set<String> wordsInOntologyTerm = AlgorithmHelper.medicalStemProxy(combineSynonyms);
				int termLength = StringUtils.join(wordsInOntologyTerm, SINGLE_WHITESPACE).length();
				int queryStringLength = StringUtils.join(wordsInQueryString, SINGLE_WHITESPACE).length();
				if (queryStringLength > termLength) termLength = queryStringLength;

				BigDecimal unmatchedPart = new BigDecimal(100).subtract(entity.getCombinedScore());
				// Weight the matched key word based on inverse document frequency (IDF)
				wordsInOntologyTerm.retainAll(wordIDFMap.keySet());
				for (String word : wordsInOntologyTerm)
				{
					if (wordIDFMap.containsKey(word) && wordIDFMap.get(word) != 0)
					{
						BigDecimal score = entity.getCombinedScore();
						BigDecimal idfValue = new BigDecimal(wordIDFMap.get(word));
						BigDecimal partialScore = score.multiply(new BigDecimal((double) word.length() / termLength));
						entity.set(COMBINED_SCORE, score.subtract(partialScore).add(partialScore.multiply(idfValue))
								.doubleValue());
					}
				}

				// Weight unmatched key word based on inverse doucment frequency
				wordsInOntologyTerm = AlgorithmHelper.medicalStemProxy(combineSynonyms);
				for (String word : wordIDFMap.keySet())
				{
					if (!wordsInOntologyTerm.contains(word) && wordIDFMap.get(word) != 0)
					{
						BigDecimal idfValue = new BigDecimal(wordIDFMap.get(word));
						BigDecimal partialScore = unmatchedPart.multiply(new BigDecimal((double) word.length()
								/ termLength));

						BigDecimal incrementalValue = partialScore.multiply(idfValue).subtract(partialScore);
						if (incrementalValue.doubleValue() > 0)
						{
							entity.set(COMBINED_SCORE, entity.getCombinedScore().subtract(incrementalValue)
									.doubleValue());
						}
					}
				}
			}
		}
	}
}