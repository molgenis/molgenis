package org.molgenis.ontology.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.tartarus.snowball.ext.PorterStemmer;

public class NgramGenerator
{
	public final static String LETTER_PATTERN = "[^a-zA-Z ]";

	private static final double T_TEST_ALPHA = 0.05;
	private final Map<String, Double> nGramTokenFreq;
	private final Map<String, Double> startNGramTokenFreq;
	private final Map<String, List<String>> nGramRelations;
	private final int nGrams = 2;

	public NgramGenerator(OntologyLoader ontologyLoader)
	{
		startNGramTokenFreq = new LinkedHashMap<String, Double>();
		nGramTokenFreq = new LinkedHashMap<String, Double>();
		nGramRelations = new HashMap<String, List<String>>();

		int totalNumTokens = 0;
		int totalNumStartTokens = 0;

		for (OWLClass cls : ontologyLoader.getAllclasses())
		{
			for (String synonym : ontologyLoader.getSynonyms(cls))
			{
				synonym = synonym.replaceAll(LETTER_PATTERN, OntologyTermQueryRepository.SINGLE_WHITESPACE)
						.toLowerCase();
				if (!StringUtils.isEmpty(synonym))
				{
					String medicalStemProxy = medicalStemProxy(synonym);
					for (String word : medicalStemProxy.split(" +"))
					{
						String lastNgramToken = null;
						for (String nGramToken : createLinkedNGrams(word))
						{
							if (lastNgramToken == null)
							{
								lastNgramToken = nGramToken;
							}
							else
							{
								if (!nGramRelations.containsKey(lastNgramToken))
								{
									nGramRelations.put(lastNgramToken, new ArrayList<String>());
								}

								if (!nGramRelations.get(lastNgramToken).contains(nGramToken))
								{
									nGramRelations.get(lastNgramToken).add(nGramToken);
								}

								lastNgramToken = nGramToken;
							}

							if (!nGramTokenFreq.containsKey(nGramToken))
							{
								nGramTokenFreq.put(nGramToken, (double) 1);
							}
							else
							{
								nGramTokenFreq
										.put(nGramToken, (double) (nGramTokenFreq.get(nGramToken).intValue() + 1));
							}

							if (nGramToken.startsWith("^"))
							{
								if (!startNGramTokenFreq.containsKey(nGramToken))
								{
									startNGramTokenFreq.put(nGramToken, (double) 1);
								}
								else
								{
									startNGramTokenFreq.put(nGramToken, (double) (startNGramTokenFreq.get(nGramToken)
											.intValue() + 1));
								}

								totalNumStartTokens++;
							}

							totalNumTokens++;
						}
					}
				}
			}
		}

		for (String startToken : startNGramTokenFreq.keySet())
		{
			startNGramTokenFreq.put(startToken, startNGramTokenFreq.get(startToken) / totalNumStartTokens);
		}

		for (String startToken : nGramTokenFreq.keySet())
		{
			nGramTokenFreq.put(startToken, nGramTokenFreq.get(startToken) / totalNumTokens);
		}
	}

	private String medicalStemProxy(String queryString)
	{
		PorterStemmer stemmer = new PorterStemmer();
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(queryString.toLowerCase().trim().split(" +")));
		uniqueTerms.removeAll(NGramMatchingModel.STOPWORDSLIST);
		for (String term : uniqueTerms)
		{
			stemmer.setCurrent(term);
			stemmer.stem();
			String afterStem = stemmer.getCurrent();
			if (!StringUtils.isEmpty(afterStem))
			{
				stringBuilder.append(afterStem).append(OntologyTermQueryRepository.SINGLE_WHITESPACE);
			}
		}
		return stringBuilder.toString().trim();
	}

	private List<String> createLinkedNGrams(String inputQuery)
	{
		Set<String> wordsInString = new HashSet<String>(Arrays.asList(inputQuery.trim().split(" ")));
		List<String> tokens = new ArrayList<String>();
		// Padding the string
		for (String singleWord : wordsInString)
		{
			if (!StringUtils.isEmpty(singleWord))
			{
				// The s$ will be the produced from two words.
				StringBuilder singleString = new StringBuilder(singleWord.length() + 2);
				singleString.append('^').append(singleWord.toLowerCase()).append('$');
				int length = singleString.length();
				for (int i = 0; i < length - 1; i++)
				{
					if (i + nGrams < length) tokens.add(singleString.substring(i, i + nGrams));
					else tokens.add(singleString.substring(length - 2));
				}
			}
		}
		return tokens;
	}

	private String generateRandomString(int length)
	{
		StringBuilder stringBuilder = new StringBuilder();
		String currentToken = drawTokenOnPossibility(startNGramTokenFreq, 1);
		if (StringUtils.isEmpty(currentToken)) return StringUtils.EMPTY;
		stringBuilder.append(currentToken);
		stringBuilder.delete(1, stringBuilder.length());

		for (int i = 0; i < length - 1; i++)
		{
			if (currentToken.endsWith("$")) break;

			double factor = 0;
			Map<String, Double> tempMap = new HashMap<String, Double>();
			for (String possibleToken : nGramRelations.get(currentToken))
			{
				factor += nGramTokenFreq.get(possibleToken);
				tempMap.put(possibleToken, nGramTokenFreq.get(possibleToken));
			}
			currentToken = drawTokenOnPossibility(tempMap, factor);
			if (StringUtils.isEmpty(currentToken)) return StringUtils.EMPTY;
			stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length()).append(currentToken);
		}

		if (stringBuilder.toString().endsWith("$")) stringBuilder.delete(stringBuilder.length() - 1,
				stringBuilder.length());

		while (stringBuilder.length() != length)
		{
			stringBuilder.delete(0, stringBuilder.length()).append(generateRandomString(length));
		}

		return stringBuilder.toString();
	}

	private String drawTokenOnPossibility(Map<String, Double> nGramTokenFreq, double factor)
	{
		double p = Math.random() * factor;
		double cumulativeProbability = 0.0;
		for (Entry<String, Double> entry : nGramTokenFreq.entrySet())
		{
			cumulativeProbability += entry.getValue();
			if (p <= cumulativeProbability)
			{
				return entry.getKey();
			}
		}
		return null;
	}

	public static boolean isSignificantVal(List<Double> listOfValues, double expectedValue)
	{
		double[] sample = new double[listOfValues.size()];
		for (int i = 0; i < listOfValues.size(); i++)
		{
			sample[i] = listOfValues.get(i);
		}

		double tTest = TestUtils.tTest(expectedValue, sample);
		return tTest < T_TEST_ALPHA;
	}

	public static void main(String args[]) throws OWLOntologyCreationException
	{
		String string_one = "ABCDEFG";

		String string_two = "ABCREFGT";

		System.out.println(NGramMatchingModel.stringMatching(string_one, string_two));

		// OntologyLoader ontologyLoader = new OntologyLoader("HPO", new File(
		// "/Users/chaopang/Desktop/Ontologies/HPO/hp.owl"));
		// NgramGenerator ngramGenerator = new NgramGenerator(ontologyLoader);
		//
		// String inputTerm = "Complex partial seizures MF038";
		// String firstBestMatchedTerm = "Complex partial seizures";
		// String secondBestMatchedTerm = "Simple partial occipital seizures";
		//
		// double firstBestScore = NGramMatchingModel.stringMatching(ngramGenerator.medicalStemProxy(inputTerm),
		// ngramGenerator.medicalStemProxy(firstBestMatchedTerm));
		//
		// double secondBestScore = NGramMatchingModel.stringMatching(ngramGenerator.medicalStemProxy(inputTerm),
		// ngramGenerator.medicalStemProxy(secondBestMatchedTerm));
		//
		// System.out.println("First score : " + firstBestScore + " ; Second score : " + secondBestScore);
		//
		// StringBuilder simulatedString = new StringBuilder();
		// List<Double> scores = new ArrayList<Double>();
		// for (int i = 0; i < 10000; i++)
		// {
		// simulatedString.delete(0, simulatedString.length());
		// simulatedString.append("Complex").append(' ').append("partial").append(' ').append("seizures").append(' ')
		// .append(ngramGenerator.generateRandomString(5));
		// double stringMatching = NGramMatchingModel.stringMatching(ngramGenerator.medicalStemProxy(inputTerm),
		// ngramGenerator.medicalStemProxy(simulatedString.toString()));
		// scores.add(stringMatching);
		// }
		//
		// Collections.sort(scores);
		//
		// System.out.println(scores);
		//
		// System.out.println("Is best score (" + firstBestScore + ") different from simulated set? "
		// + isSignificantVal(scores, firstBestScore));
		//
		// System.out.println("Is second best score (" + secondBestScore + ") different from simulated set? "
		// + isSignificantVal(scores, secondBestScore));
		//
		// System.out.println(NGramMatchingModel.stringMatching(ngramGenerator.medicalStemProxy("brachydactyly toe"),
		// ngramGenerator.medicalStemProxy("short toe")));
		//
		// System.out.println(NGramMatchingModel.stringMatching(ngramGenerator.medicalStemProxy("brachydactyly toe"),
		// ngramGenerator.medicalStemProxy("brachydactyly foot")));
		//
		// System.out.println(NGramMatchingModel.stringMatching(ngramGenerator.medicalStemProxy("brachydactyly toe"),
		// ngramGenerator.medicalStemProxy("short toe brachydactyly foot")));
	}
}
