package org.molgenis.data.semanticsearch.string;

import java.util.Set;
import java.util.stream.Collectors;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.GermanStemmer;

public class Stemmer
{
	private final static String DEFAULT_LANG = "EN";
	private final SnowballProgram porterStemmer;

	public Stemmer()
	{
		this(DEFAULT_LANG);
	}

	public Stemmer(String lang)
	{
		switch (lang.toLowerCase())
		{
			case "da":
				porterStemmer = new DanishStemmer();
				break;
			case "nl":
				porterStemmer = new DutchStemmer();
				break;
			case "en":
				porterStemmer = new EnglishStemmer();
				break;
			case "fi":
				porterStemmer = new FinnishStemmer();
				break;
			case "fr":
				porterStemmer = new FrenchStemmer();
				break;
			case "de":
				porterStemmer = new GermanStemmer();
				break;
			default:
				porterStemmer = new EnglishStemmer();
		}
	}

	public synchronized String stem(String term)
	{
		porterStemmer.setCurrent(term);
		porterStemmer.stem();
		return porterStemmer.getCurrent();
	}

	public String stemAndJoin(Set<String> terms)
	{
		return terms.stream().map(this::stem).collect(Collectors.joining(" "));
	}
}