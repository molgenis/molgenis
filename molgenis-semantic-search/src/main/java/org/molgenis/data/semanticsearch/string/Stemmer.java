package org.molgenis.data.semanticsearch.string;

import org.apache.commons.lang3.StringUtils;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.Set;
import java.util.stream.Collectors;

public class Stemmer
{
	private final static String ILLEGAL_REGEX_PATTERN = "[^a-zA-Z0-9 ]";

	/**
	 * Remove illegal characters from the string and stem each single word
	 *
	 * @param phrase
	 * @return a string that consists of stemmed words
	 */
	public static String cleanStemPhrase(String phrase)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (String word : replaceIllegalCharacter(phrase).split(" "))
		{
			String stemmedWord = stem(word);
			if (StringUtils.isNotEmpty(stemmedWord))
			{
				if (stringBuilder.length() > 0)
				{
					stringBuilder.append(' ');
				}

				stringBuilder.append(stemmedWord);
			}
		}
		return stringBuilder.toString();
	}

	public static String stem(String word)
	{
		PorterStemmer porterStemmer = new PorterStemmer();
		porterStemmer.setCurrent(word);
		porterStemmer.stem();
		return porterStemmer.getCurrent();
	}

	public static String stemAndJoin(Set<String> terms)
	{
		return terms.stream().map(Stemmer::stem).collect(Collectors.joining(" "));
	}

	public static String replaceIllegalCharacter(String string)
	{
		return string.replaceAll(ILLEGAL_REGEX_PATTERN, " ").replaceAll(" +", " ").trim().toLowerCase();
	}
}
