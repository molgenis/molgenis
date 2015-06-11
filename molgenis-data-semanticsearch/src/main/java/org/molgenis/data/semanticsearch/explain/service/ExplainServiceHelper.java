package org.molgenis.data.semanticsearch.explain.service;

import static org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm.stringMatching;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.semanticsearch.string.Stemmer;

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

public class ExplainServiceHelper
{
	public final static Pattern REGEXR_PATTERN = Pattern.compile("^weight\\(\\w*:(\\w*)(.*|)\\s.*");
	private final Splitter termSplitter = Splitter.onPattern("[^\\p{IsAlphabetic}]+");
	private final Stemmer stemmer = new Stemmer("en");

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

	public String discoverMatchedQueries(Explanation explanation)
	{
		StringBuilder stringBuilder = new StringBuilder();

		String description = explanation.getDescription();
		if (description.startsWith(Options.SUM_OF.toString()) || description.startsWith(Options.PRODUCT_OF.toString()))
		{
			List<String> matchedTermsFromElasticExplanation = Lists.newArrayList(explanation.getDetails()).stream()
					.map(this::discoverMatchedQueries).filter(term -> StringUtils.isNotEmpty(term))
					.collect(Collectors.toList());

			stringBuilder.append(joinTerms(StringUtils.join(matchedTermsFromElasticExplanation, ' ')));
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

			stringBuilder.append('|').append(discoverMatchedQueries(maxExplanation)).append('|');
		}
		else if (description.startsWith(Options.WEIGHT.toString()))
		{
			stringBuilder.append(getMatchedWord(description));
		}

		return stringBuilder.toString();
	}

	public String joinTerms(String description)
	{
		if (StringUtils.isNotEmpty(description))
		{
			if (description.charAt(0) == '|')
			{
				description = description.substring(1);
			}

			if (description.charAt(description.length() - 1) == '|')
			{
				description = description.substring(0, description.length() - 1);
			}

			description = description.replaceAll("(\\|\\s*\\||\\s*\\|\\s*)", "|");
		}
		return description;
	}

	public Map<String, Double> recursivelyFindQuery(String queryPart, List<QueryRule> rules)
	{
		Map<String, Double> qualifiedTerms = new HashMap<String, Double>();
		for (QueryRule queryRule : rules)
		{
			if (queryRule.getNestedRules().size() > 0)
			{
				qualifiedTerms.putAll(recursivelyFindQuery(queryPart, queryRule.getNestedRules()));
			}
			else
			{
				String removeBoostFromQuery = removeBoostFromQuery(queryRule.getValue().toString());
				String cleanStemPhrase = stemmer.cleanStemPhrase(removeBoostFromQuery);
				if (splitIntoTerms(cleanStemPhrase).containsAll(splitIntoTerms(queryPart)))
				{
					qualifiedTerms.put(removeBoostFromQuery, stringMatching(queryPart, cleanStemPhrase));
				}
			}
		}
		return qualifiedTerms;
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