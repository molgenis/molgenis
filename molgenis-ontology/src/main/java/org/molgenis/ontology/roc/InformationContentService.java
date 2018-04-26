package org.molgenis.ontology.roc;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.semanticsearch.string.Stemmer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;

public class InformationContentService
{
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final String SINGLE_WHITESPACE = " ";

	private final LoadingCache<String, Long> CACHED_TOTAL_WORD_COUNT = CacheBuilder.newBuilder()
																				   .maximumSize(Integer.MAX_VALUE)
																				   .expireAfterWrite(1, TimeUnit.DAYS)
																				   .build(new CacheLoader<String, Long>()
																				   {
																					   @Override
																					   public Long load(
																							   String ontologyIri)
																					   {
																						   Entity ontologyEntity = dataService
																								   .findOne(ONTOLOGY,
																										   new QueryImpl<>()
																												   .eq(OntologyMetaData.ONTOLOGY_IRI,
																														   ontologyIri));
																						   if (ontologyEntity != null)
																						   {
																							   return dataService.count(
																									   ONTOLOGY_TERM,
																									   new QueryImpl<>()
																											   .eq(OntologyTermMetaData.ONTOLOGY,
																													   ontologyEntity));
																						   }
																						   return (long) 0;
																					   }
																				   });
	private final LoadingCache<OntologyWord, Double> CACHED_INVERSE_DOCUMENT_FREQ = CacheBuilder.newBuilder()
																								.maximumSize(
																										Integer.MAX_VALUE)
																								.expireAfterWrite(1,
																										TimeUnit.DAYS)
																								.build(new CacheLoader<OntologyWord, Double>()
																								{
																									public Double load(
																											OntologyWord key)
																											throws
																											ExecutionException
																									{
																										String ontologyIri = key
																												.getOntologyIri();
																										Entity ontologyEntity = dataService
																												.findOne(
																														ONTOLOGY,
																														new QueryImpl<>()
																																.eq(OntologyMetaData.ONTOLOGY_IRI,
																																		ontologyIri));
																										if (ontologyEntity
																												!= null)
																										{
																											QueryRule queryRule = new QueryRule(
																													Arrays.asList(
																															new QueryRule(
																																	OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
																																	Operator.FUZZY_MATCH,
																																	key.getWord())));
																											queryRule.setOperator(
																													Operator.DIS_MAX);
																											QueryRule finalQuery = new QueryRule(
																													Arrays.asList(
																															new QueryRule(
																																	OntologyTermMetaData.ONTOLOGY,
																																	Operator.EQUALS,
																																	ontologyEntity),
																															new QueryRule(
																																	Operator.AND),
																															queryRule));
																											long wordCount = dataService
																													.count(ONTOLOGY_TERM,
																															new QueryImpl<>(
																																	finalQuery));
																											Long total = CACHED_TOTAL_WORD_COUNT
																													.get(ontologyIri);
																											BigDecimal idfValue = new BigDecimal(
																													total
																															== null ? 0 : (
																															1
																																	+ Math
																																	.log((double) total
																																			/ (
																																			wordCount
																																					+ 1))));
																											return idfValue
																													.doubleValue();
																										}
																										return (double) 0;
																									}
																								});

	private final DataService dataService;

	public InformationContentService(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public Map<String, Double> redistributedNGramScore(String queryString, String ontologyIri)
	{
		Map<String, Double> wordIDFMap = createWordIDF(queryString, ontologyIri);
		Map<String, Double> wordWeightedSimilarity = new HashMap<>();

		if (wordIDFMap.size() > 0)
		{
			double averageIDFValue = wordIDFMap.values()
											   .stream()
											   .mapToDouble(Double::doubleValue)
											   .average()
											   .getAsDouble();
			double queryStringLength = StringUtils.join(createStemmedWordSet(queryString), SINGLE_WHITESPACE)
												  .trim()
												  .length();
			double totalContribution = 0;
			double totalDenominator = 0;

			for (Entry<String, Double> entry : wordIDFMap.entrySet())
			{
				double diff = entry.getValue() - averageIDFValue;
				if (diff < 0)
				{
					Double contributedSimilarity =
							(entry.getKey().length() / queryStringLength * 100) * (diff / averageIDFValue);
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
		}
		return wordWeightedSimilarity;
	}

	Map<String, Double> createWordIDF(String queryString, String ontologyIri)
	{
		Map<String, Double> wordFreqMap = new HashMap<>();
		Set<String> wordsInQueryString = createStemmedWordSet(queryString);
		wordsInQueryString.stream().forEach(word ->
		{
			Double wordIDF = null;
			try
			{
				wordIDF = CACHED_INVERSE_DOCUMENT_FREQ.get(new OntologyWord(ontologyIri, word));
			}
			catch (ExecutionException e)
			{
				throw new UncheckedExecutionException(e);
			}

			if (wordIDF != null && wordIDF != 0)
			{
				wordFreqMap.put(word, wordIDF);
			}
		});
		return wordFreqMap;
	}

	public Set<String> createStemmedWordSet(String queryString)
	{
		Set<String> uniqueTerms = Sets.newHashSet(queryString.toLowerCase().trim().split(NON_WORD_SEPARATOR))
									  .stream()
									  .filter(term -> !NGramDistanceAlgorithm.STOPWORDSLIST.contains(term))
									  .map(Stemmer::stem)
									  .filter(StringUtils::isNotBlank)
									  .collect(Collectors.toSet());
		return Sets.newHashSet(uniqueTerms);
	}
}