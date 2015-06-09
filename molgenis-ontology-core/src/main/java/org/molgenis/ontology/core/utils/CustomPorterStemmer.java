package org.molgenis.ontology.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.tartarus.snowball.ext.PorterStemmer;

public class CustomPorterStemmer
{
	private final PorterStemmer porterStemer = new PorterStemmer();
	private final static String ILLEGAL_REGEX_PATTERN = "[^a-zA-Z0-9 ]";

	/**
	 * Remove illegal characters from the string and stem each single word
	 * 
	 * @param phrase
	 * @return a string that consists of stemmed words
	 */
	public String cleanStemPhrase(String phrase)
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

	public synchronized String stem(String word)
	{
		porterStemer.setCurrent(word);
		porterStemer.stem();
		return porterStemer.getCurrent();
	}

	public String replaceIllegalCharacter(String string)
	{
		return string.replaceAll(ILLEGAL_REGEX_PATTERN, " ").replaceAll(" +", " ").trim();
	}
}
