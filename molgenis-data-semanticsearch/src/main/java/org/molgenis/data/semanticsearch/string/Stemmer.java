package org.molgenis.data.semanticsearch.string;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer
{
	private final static String ILLEGAL_REGEX_PATTERN = "[^a-zA-Z0-9 ]";
	private final SnowballProgram porterStemmer;

	public Stemmer()
	{
		porterStemmer = new PorterStemmer();
	}

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
		porterStemmer.setCurrent(word);
		porterStemmer.stem();
		return porterStemmer.getCurrent();
	}

	public String stemAndJoin(Set<String> terms)
	{
		return terms.stream().map(this::stem).collect(Collectors.joining(" "));
	}

	public String replaceIllegalCharacter(String string)
	{
		return string.replaceAll(ILLEGAL_REGEX_PATTERN, " ").replaceAll(" +", " ").trim().toLowerCase();
	}
}
