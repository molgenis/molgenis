package org.molgenis.data.semanticsearch.string;

import java.util.Set;
import java.util.stream.Collectors;

import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer
{
	private PorterStemmer porterStemmer = new PorterStemmer();

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