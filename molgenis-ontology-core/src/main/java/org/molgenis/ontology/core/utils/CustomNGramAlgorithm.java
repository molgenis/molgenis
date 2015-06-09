package org.molgenis.ontology.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Sets;

/**
 * This class has implemented Levenshtein distance algorithm so a similarity score could be calculated between two
 * sequences. The two input strings would be tokenized depending on what nGrams we have specified. The default ngram is
 * 2 which can be changed in the constructor. The two groups of tokens will be further used to work out the similarity
 * score. In addition, by default a list of stop words has been defined, in the method stringMatching(), one of the
 * parameters "removeStopWords" indicates whether the stop words will be used to remove the useless or meaningless words
 * from the String. This the stop words could be customized by setStopWords(List<String> stopWords) or
 * setStopWords(String[] stopWords).
 * 
 * How to use? LevenShteinDistanceModel model = new LevenShteinDistanceModel(2); double similarityScore =
 * model.stringMatching("Smoking", "Smoker", false); System.out.println(similarityScore);
 * 
 * The other way List<String> tokens_1 = model.createNGrams("Smoking", false); List<String> tokens_2 =
 * model.createNGrams("Have you smoked last year?", true); //remove stop words! double similarityScore =
 * model.calculateScore(tokens_1, tokens_2);
 * 
 * 
 * @author Chao Pang
 * 
 */

public class CustomNGramAlgorithm
{

	private static int nGrams = 2;
	public static final Set<String> STOPWORDSLIST;
	static
	{
		STOPWORDSLIST = Sets.newHashSet("a", "you", "about", "above", "after", "again", "against", "all", "am", "an",
				"and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below",
				"between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does",
				"doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had",
				"hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
				"here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm",
				"i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more",
				"most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
				"other", "ought", "our", "ours ", " ourselves", "out", "over", "own", "same", "shan't", "she", "she'd",
				"she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the",
				"their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd",
				"they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up",
				"very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's",
				"when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with",
				"won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
				"yourself", "yourselves", "many", ")", "(");
	}
	private static CustomPorterStemmer CUSTOM_PORTER_STEMMER = new CustomPorterStemmer();

	public static double stringMatching(String queryOne, String queryTwo)
	{
		double similarityScore = calculateScore(createNGrams(queryOne.toLowerCase().trim(), true),
				createNGrams(queryTwo.toLowerCase().trim(), true));
		return similarityScore;
	}

	public static double stringMatching(String queryOne, String queryTwo, boolean removeStopWords)
	{
		double similarityScore = calculateScore(createNGrams(queryOne.toLowerCase().trim(), removeStopWords),
				createNGrams(queryTwo.toLowerCase().trim(), removeStopWords));
		return similarityScore;
	}

	/**
	 * //create n-grams tokens of the string.
	 * 
	 * @param inputString
	 * @param nGrams
	 * @return
	 */
	public static Map<String, Integer> createNGrams(String inputQuery, boolean removeStopWords)
	{
		List<String> wordsInString = Lists.newArrayList(stemPhrase(inputQuery).split(" "));
		Map<String, Integer> tokens = new HashMap<String, Integer>();
		if (removeStopWords) wordsInString.removeAll(STOPWORDSLIST);
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
					String token = null;
					if (i + nGrams < length)
					{
						token = singleString.substring(i, i + nGrams);

					}
					else
					{
						token = singleString.substring(length - 2);
					}

					if (!tokens.containsKey(token))
					{
						tokens.put(token, 1);
					}
					else
					{
						tokens.put(token, (tokens.get(token) + 1));
					}
				}
			}
		}

		return tokens;
	}

	/**
	 * Calculate the ngram distance
	 * 
	 * @param inputStringTokens
	 * @param ontologyTermTokens
	 * @return
	 */
	private static double calculateScore(Map<String, Integer> inputStringTokens, Map<String, Integer> ontologyTermTokens)
	{
		if (inputStringTokens.size() == 0 || ontologyTermTokens.size() == 0) return (double) 0;
		double totalToken = getTotalNumTokens(inputStringTokens) + getTotalNumTokens(ontologyTermTokens);

		int numMatchedToken = 0;

		for (String token : inputStringTokens.keySet())
		{
			if (ontologyTermTokens.containsKey(token))
			{
				numMatchedToken += Math.min(inputStringTokens.get(token), ontologyTermTokens.get(token));
			}
		}

		DecimalFormat df = new DecimalFormat("##.###", new DecimalFormatSymbols(Locale.ENGLISH));
		return Double.parseDouble(df.format(2 * numMatchedToken / totalToken * 100));
	}

	private static int getTotalNumTokens(Map<String, Integer> inputStringTokens)
	{
		int totalNum = 0;

		for (Integer frequency : inputStringTokens.values())
		{
			totalNum += frequency;
		}
		return totalNum;
	}

	private static String stemPhrase(String originalString)
	{
		return CUSTOM_PORTER_STEMMER.cleanStemPhrase(originalString.toLowerCase().trim());
	}
}