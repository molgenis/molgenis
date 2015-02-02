package org.molgenis.ontology.utils;

import static org.molgenis.ontology.repository.AbstractOntologyRepository.ONTOLOGY_IRI;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SINGLE_WHITESPACE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SYNONYMS;

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

public class AlgorithmHelper

{
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";

	public static Map<String, Integer> createWordFreq(String queryString, List<ComparableEntity> comparableEntities,
			int totalDocs, OntologyServiceImpl ontologyService)
	{
		Map<String, Integer> wordFreqMap = new HashMap<String, Integer>();
		Set<String> wordsInQueryString = medicalStemProxy(queryString);
		for (String word : wordsInQueryString)
		{
			wordFreqMap.put(word, 0);
		}

		for (ComparableEntity entity : comparableEntities.subList(0, totalDocs))
		{
			Set<String> listOfWordsInSynonymGroup = medicalStemProxy(StringUtils.join(
					ontologyService.getOntologyTermSynonyms(entity.getString(ONTOLOGY_TERM_IRI),
							entity.getString(ONTOLOGY_IRI)), SINGLE_WHITESPACE));
			for (String word : wordFreqMap.keySet())
			{
				if (listOfWordsInSynonymGroup.contains(word))
				{
					wordFreqMap.put(word, (wordFreqMap.get(word) + 1));
				}
			}
		}
		for (String word : wordsInQueryString)
		{
			if (wordFreqMap.get(word) == 0) wordFreqMap.remove(word);
		}
		return wordFreqMap;
	}

	public static String combineSynonyms(ComparableEntity comparableEntity, String queryString,
			OntologyServiceImpl ontologyServiceImpl)
	{
		StringBuilder combinedSynonym = new StringBuilder();
		combinedSynonym.append(comparableEntity.getString(SYNONYMS));
		Set<String> matchedOntologyTermSynWords = medicalStemProxy(comparableEntity.getString(SYNONYMS));
		Set<String> keyWords = new HashSet<String>(medicalStemProxy(queryString));
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

	public static Set<String> medicalStemProxy(String queryString)
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
