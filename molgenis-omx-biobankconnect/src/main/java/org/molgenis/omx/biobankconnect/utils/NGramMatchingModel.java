package org.molgenis.omx.biobankconnect.utils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.tartarus.snowball.ext.PorterStemmer;

/**
 * This class has implemented Levenshtein distance algorithm so a similarity
 * score could be calculated between two sequences. The two input strings would
 * be tokenized depending on what nGrams we have specified. The default ngram is
 * 2 which can be changed in the constructor. The two groups of tokens will be
 * further used to work out the similarity score. In addition, by default a list
 * of stop words has been defined, in the method stringMatching(), one of the
 * parameters "removeStopWords" indicates whether the stop words will be used to
 * remove the useless or meaningless words from the String. This the stop words
 * could be customized by setStopWords(List<String> stopWords) or
 * setStopWords(String[] stopWords).
 * 
 * How to use? LevenShteinDistanceModel model = new LevenShteinDistanceModel(2);
 * double similarityScore = model.stringMatching("Smoking", "Smoker", false);
 * System.out.println(similarityScore);
 * 
 * The other way List<String> tokens_1 = model.createNGrams("Smoking", false);
 * List<String> tokens_2 = model.createNGrams("Have you smoked last year?",
 * true); //remove stop words! double similarityScore =
 * model.calculateScore(tokens_1, tokens_2);
 * 
 * 
 * @author Chao Pang
 * 
 */

public class NGramMatchingModel
{

	private static int nGrams = 2;
	private static final Set<String> STOPWORDSLIST;
	private static PorterStemmer stemmer = new PorterStemmer();
	static
	{
		STOPWORDSLIST = new HashSet<String>(Arrays.asList("a", "you", "about", "above", "after", "again", "against",
				"all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
				"being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did",
				"didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
				"further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
				"he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
				"i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
				"let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on",
				"once", "only", "or", "other", "ought", "our", "ours ", " ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
				"that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
				"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under",
				"until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't",
				"what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
				"why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves", "many", ")", "("));
	}

	public static double stringMatching(String queryOne, String queryTwo, boolean whetherRemoveStopWords)
	{
		double similarityScore = calculateScore(createNGrams(queryOne.toLowerCase().trim(), whetherRemoveStopWords),
				createNGrams(queryTwo.toLowerCase().trim(), whetherRemoveStopWords));
		return similarityScore;
	}

	/**
	 * //create n-grams tokens of the string.
	 * 
	 * @param inputString
	 * @param nGrams
	 * @return
	 */
	private static Set<String> createNGrams(String inputQuery, boolean removeStopWords)
	{
		Set<String> wordsInString = new HashSet<String>(Arrays.asList(inputQuery.trim().split(" ")));
		Set<String> tokens = new HashSet<String>();
		wordsInString.removeAll(STOPWORDSLIST);
		// Padding the string
		for (String singleWord : wordsInString)
		{
			singleWord = stemmerString(singleWord);
			// The s$ will be the produced from two words.
			StringBuilder singleString = new StringBuilder(singleWord.length() + 2);
			singleString.append('^').append(singleWord.toLowerCase()).append('$');
			int length = singleString.length();
			for (int i = 0; i < length; i++)
			{
				if (i + nGrams < length) tokens.add(singleString.substring(i, i + nGrams));
				else tokens.add(singleString.substring(length - 2));
			}
		}
		return tokens;
	}

	/**
	 * Calculate the levenshtein distance
	 * 
	 * @param inputStringTokens
	 * @param ontologyTermTokens
	 * @return
	 */
	private static double calculateScore(Set<String> inputStringTokens, Set<String> ontologyTermTokens)
	{
		int matchedTokens = 0;
		double totalToken = Math.max(inputStringTokens.size(), ontologyTermTokens.size());
		inputStringTokens.retainAll(ontologyTermTokens);
		matchedTokens = inputStringTokens.size();
		DecimalFormat df = new DecimalFormat("#0.000");
		return Double.parseDouble(df.format(matchedTokens / totalToken * 100));
	}

	private static String stemmerString(String originalString)
	{
		stemmer.setCurrent(originalString.trim());
		stemmer.stem();
		return stemmer.getCurrent();
	}

	public static void main(String args[])
	{
		System.out.println(NGramMatchingModel.stringMatching("diastolic blood pressure",
				"diastolic blood pressure 1st measurement", true));
	}
}