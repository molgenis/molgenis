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

public class ExplainServiceHelper
{
	public final static Pattern REGEXR_PATTERN = Pattern.compile("^weight\\(\\w*:(\\w*)(.*|)\\s.*");

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
			Set<String> matchedTermsFromElasticExplanation = Lists.newArrayList(explanation.getDetails()).stream()
					.map(this::discoverMatchedQueries).filter(term -> StringUtils.isNotEmpty(term))
					.collect(Collectors.toSet());

			if (termsConsistOfSingleWord(matchedTermsFromElasticExplanation))
			{
				stringBuilder.append(StringUtils.join(matchedTermsFromElasticExplanation, ' '));
			}
			else
			{
				stringBuilder.append(StringUtils.join(matchedTermsFromElasticExplanation, '|'));
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

			stringBuilder.append(discoverMatchedQueries(maxExplanation));
		}
		else if (description.startsWith(Options.WEIGHT.toString()))
		{
			stringBuilder.append(getMatchedWord(description));
		}

		return stringBuilder.toString();
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
			else if (queryRule.getValue().toString().contains(queryPart))
			{
				qualifiedTerms.put(queryRule.getValue().toString(),
						stringMatching(queryPart, queryRule.getValue().toString()));
			}
		}
		return qualifiedTerms;
	}

	public boolean termsConsistOfSingleWord(Set<String> terms)
	{
		return terms.stream().allMatch(word -> word.split("\\s+").length == 1);
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
}