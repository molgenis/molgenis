package org.molgenis.ontology.utils;

import static org.molgenis.ontology.repository.AbstractOntologyRepository.ONTOLOGY_IRI;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SINGLE_WHITESPACE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SYNONYMS;
import static org.molgenis.ontology.service.OntologyServiceImpl.SCORE;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.tartarus.snowball.ext.PorterStemmer;

public class PostProcessOntologyTermIDFAlgorithm
{
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final String DEFAULT_FIELD = "Name";

	public static void process(List<ComparableEntity> comparableEntities, Map<String, Object> inputData,
			OntologyServiceImpl ontologyService)
	{
		if (inputData.size() == 1 && inputData.containsKey(DEFAULT_FIELD))
		{
			String queryString = inputData.get(DEFAULT_FIELD).toString();

			// Collect the frequencies for all of the unique words from query string
			Map<String, Integer> wordFreqMap = new HashMap<String, Integer>();
			Set<String> wordsInQueryString = medicalStemProxy(queryString);
			for (String word : wordsInQueryString)
			{
				wordFreqMap.put(word, 0);
			}

			int totalDocs = comparableEntities.size() > 10 ? 10 : comparableEntities.size();
			for (ComparableEntity comparableEntity : comparableEntities.subList(0, totalDocs))
			{
				Set<String> listOfWordsInSynonymGroup = medicalStemProxy(StringUtils.join(ontologyService
						.getOntologyTermSynonyms(comparableEntity.getString(ONTOLOGY_TERM_IRI),
								comparableEntity.getString(ONTOLOGY_IRI)), SINGLE_WHITESPACE));

				for (String word : wordFreqMap.keySet())
				{
					if (listOfWordsInSynonymGroup.contains(word))
					{
						wordFreqMap.put(word, (wordFreqMap.get(word) + 1));
					}
				}
			}

			for (ComparableEntity entity : comparableEntities.subList(0, totalDocs))
			{
				String combineSynonyms = combineSynonyms(entity, wordFreqMap, queryString, ontologyService);
				Set<String> wordsInOntologyTerm = medicalStemProxy(combineSynonyms);
				int termLength = StringUtils.join(wordsInOntologyTerm, SINGLE_WHITESPACE).length();
				int queryStringLength = StringUtils.join(wordsInQueryString, SINGLE_WHITESPACE).length();
				if (queryStringLength > termLength) termLength = queryStringLength;

				BigDecimal unmatchedPart = new BigDecimal(100).subtract(entity.getDecimal());
				// Weight the matched key word based on inverse document frequency (IDF)
				wordsInOntologyTerm.retainAll(wordFreqMap.keySet());
				for (String word : wordsInOntologyTerm)
				{
					if (wordFreqMap.containsKey(word))
					{
						BigDecimal score = entity.getDecimal();
						BigDecimal idfValue = new BigDecimal(1 + Math.log((double) totalDocs
								/ (wordFreqMap.get(word) + 1)));
						BigDecimal partialScore = score.multiply(new BigDecimal((double) word.length() / termLength));
						double doubleValue = score.subtract(partialScore).add(partialScore.multiply(idfValue))
								.doubleValue();
						entity.set(SCORE, doubleValue);
					}
				}

				// Weight unmatched key word based on inverse doucment frequency
				wordsInOntologyTerm = medicalStemProxy(combineSynonyms);
				for (String word : wordFreqMap.keySet())
				{
					if (!wordsInOntologyTerm.contains(word))
					{
						BigDecimal idfValue = new BigDecimal(1 + Math.log((double) totalDocs
								/ (wordFreqMap.get(word) + 1)));
						BigDecimal partialScore = unmatchedPart.multiply(new BigDecimal((double) word.length()
								/ termLength));

						BigDecimal incrementalValue = partialScore.multiply(idfValue).subtract(partialScore);
						if (incrementalValue.doubleValue() > 0)
						{
							entity.set(SCORE, entity.getDecimal().subtract(incrementalValue).doubleValue());
						}
					}
				}
			}
		}
	}

	private static String combineSynonyms(ComparableEntity comparableEntity, Map<String, Integer> wordFreqMap,
			String queryString, OntologyServiceImpl ontologyServiceImpl)
	{
		StringBuilder combinedSynonym = new StringBuilder();
		combinedSynonym.append(comparableEntity.getString(SYNONYMS));
		Set<String> matchedOntologyTermSynWords = medicalStemProxy(comparableEntity.getString(SYNONYMS));
		Set<String> keyWords = new HashSet<String>(wordFreqMap.keySet());
		keyWords.removeAll(matchedOntologyTermSynWords);

		if (keyWords.size() != 0)
		{
			for (String word : keyWords)
			{
				double bestScore = 0;
				StringBuilder bestSynonym = new StringBuilder();
				for (String synonym : ontologyServiceImpl.getOntologyTermSynonyms(
						comparableEntity.getString(ONTOLOGY_TERM_IRI), comparableEntity.getString(ONTOLOGY_IRI)))
				{
					if (!synonym.equalsIgnoreCase(comparableEntity.getString(SYNONYMS)))
					{
						Set<String> synonymAllWords = medicalStemProxy(synonym);
						if (synonymAllWords.contains(word))
						{
							synonymAllWords.addAll(matchedOntologyTermSynWords);
							double score = NGramMatchingModel.stringMatching(ontologyServiceImpl
									.removeIllegalCharWithSingleWhiteSpace(queryString), ontologyServiceImpl
									.removeIllegalCharWithSingleWhiteSpace(StringUtils.join(synonymAllWords,
											SINGLE_WHITESPACE)));
							if (score > bestScore)
							{
								bestScore = score;
								bestSynonym.delete(0, bestSynonym.length()).append(synonym);
							}
						}
					}
				}
				combinedSynonym.append(SINGLE_WHITESPACE).append(bestSynonym.toString());
			}
		}

		return combinedSynonym.toString();
	}

	private static Set<String> medicalStemProxy(String queryString)
	{
		PorterStemmer stemmer = new PorterStemmer();
		Set<String> uniqueTerms = new HashSet<String>();
		for (String term : new HashSet<String>(
				Arrays.asList(queryString.toLowerCase().trim().split(NON_WORD_SEPARATOR))))
		{
			if (!NGramMatchingModel.STOPWORDSLIST.contains(term))
			{
				stemmer.setCurrent(term);
				stemmer.stem();
				String afterStem = stemmer.getCurrent();
				if (!StringUtils.isEmpty(afterStem))
				{
					uniqueTerms.add(afterStem);
				}
			}
		}
		return uniqueTerms;
	}
}