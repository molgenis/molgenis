package org.molgenis.data.semanticsearch.explain.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

public class ExplainServiceHelper
{
	public final static Pattern REGEXR_PATTERN = Pattern.compile("^weight\\(\\w*:(\\w*)(.*|)\\s.*");
	private final Splitter termSplitter = Splitter.onPattern("[^\\p{IsAlphabetic}]+");

	public enum Options
	{
		/**
		 * elasticsearch description that is product of:
		 */
		PRODUCT_OF("product of:"),
		/**
		 * elasticsearch description that is max of:
		 */
		MAX_OF("max of:"),

		/**
		 * elasticsearch description that is sum of:
		 */
		SUM_OF("sum of:"),

		/**
		 * elasticsearch description that starts with weight:
		 */
		WEIGHT("weight");

		private String label;

		Options(String label)
		{
			this.label = label;
		}

		public String toString()
		{
			return label;
		}
	}

	/**
	 * This method is able to recursively collect all the matched words from ElastisSearch Explanation document
	 * 
	 * @param explanation
	 * @return a set of matched words that are matched to different ontology terms
	 */
	public Set<String> findMatchedWords(Explanation explanation)
	{
		Set<String> words = new HashSet<String>();
		String description = explanation.getDescription();
		if (description.startsWith(Options.SUM_OF.toString()) || description.startsWith(Options.PRODUCT_OF.toString()))
		{
			if (Lists.newArrayList(explanation.getDetails()).stream().allMatch(this::reachLastLevel))
			{
				words.add(extractMatchedWords(explanation.getDetails()));
			}
			else
			{
				for (Explanation subExplanation : explanation.getDetails())
				{
					words.addAll(findMatchedWords(subExplanation));
				}
			}
		}
		else if (description.startsWith(Options.MAX_OF.toString()))
		{
			Explanation maxExplanation = Lists.newArrayList(explanation.getDetails()).stream()
					.max(new Comparator<Explanation>()
					{
						public int compare(Explanation explanation1, Explanation explanation2)
						{
							return Float.compare(explanation1.getValue(), explanation2.getValue());
						}
					}).get();

			words.addAll(findMatchedWords(maxExplanation));
		}
		else if (description.startsWith(Options.WEIGHT.toString()))
		{
			words.add(getMatchedWord(description));
		}
		return words;
	}

	public String extractMatchedWords(Explanation[] explanations)
	{
		List<String> collect = Lists.newArrayList(explanations).stream()
				.map(explanation -> getMatchedWord(explanation.getDescription())).collect(Collectors.toList());
		return StringUtils.join(collect, ' ');
	}

	public boolean reachLastLevel(Explanation explanation)
	{
		return explanation.getDescription().startsWith(Options.WEIGHT.toString());
	}

	/**
	 * This method is able to find the queries that are used in the matching. Only queries that contain all matched
	 * words are potential queries.
	 * 
	 * @param matchedWords
	 * @param collectExpandedQueryMap
	 * @return a map of potentail queries and their matching scores
	 */
	public Map<String, Double> findMatchQueries(String matchedWords, Map<String, String> collectExpandedQueryMap)
	{
		Map<String, Double> qualifiedQueries = new HashMap<String, Double>();

		for (Entry<String, String> entry : collectExpandedQueryMap.entrySet())
		{
			if (splitIntoTerms(entry.getKey()).containsAll(splitIntoTerms(matchedWords)))
			{
				qualifiedQueries.put(entry.getKey(),
						NGramDistanceAlgorithm.stringMatching(matchedWords, entry.getKey()));
			}
		}
		return qualifiedQueries;
	}

	public String removeBoostFromQuery(String description)
	{
		return description.replaceAll("\\^\\d*\\.{0,1}\\d+", "");
	}

	public String getMatchedWord(String description)
	{
		Matcher matcher = REGEXR_PATTERN.matcher(description);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		throw new MolgenisDataAccessException("Failed to find matched word in : " + description);
	}

	private Set<String> splitIntoTerms(String description)
	{
		return FluentIterable.from(termSplitter.split(description)).transform(String::toLowerCase)
				.filter(w -> !StringUtils.isEmpty(w)).toSet();
	}
}