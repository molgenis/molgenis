package org.molgenis.data.semanticsearch.utils;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.molgenis.ontology.utils.NGramDistanceAlgorithm.STOPWORDSLIST;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.utils.Stemmer;

import com.google.common.collect.Sets;

public class SemanticSearchServiceUtils
{
	private final static String ILLEGAL_CHARS_REGEX = "[^\\p{IsAlphabetic}0-9]+";

	public static Set<String> findMatchedWords(String string1, String string2)
	{
		Set<String> intersectedWords = new LinkedHashSet<>();
		Set<String> stemmedWordsFromString2 = splitIntoUniqueTerms(string2).stream().map(Stemmer::stem)
				.collect(Collectors.toSet());
		for (String wordFromString1 : splitIntoUniqueTerms(string1))
		{
			String stemmedSourceWord = Stemmer.stem(wordFromString1);
			if (stemmedWordsFromString2.contains(stemmedSourceWord))
			{
				intersectedWords.add(wordFromString1);
			}
		}
		return intersectedWords;
	}

	/**
	 * A helper function to create a list of queryTerms based on the information from the targetAttribute as well as
	 * user defined searchTerms. If the user defined searchTerms exist, the targetAttribute information will not be
	 * used.
	 *
	 * @param targetAttribute
	 * @param userQueries
	 * @return list of queryTerms
	 */
	public static Set<String> getQueryTermsFromAttribute(AttributeMetaData targetAttribute, Set<String> userQueries)
	{
		Set<String> queryTerms = new HashSet<>();
		if (userQueries != null && !userQueries.isEmpty())
		{
			queryTerms.addAll(userQueries);
		}
		else if (targetAttribute != null)
		{
			if (isNotBlank(targetAttribute.getLabel()))
			{
				queryTerms.add(targetAttribute.getLabel());
			}
			if (isNotBlank(targetAttribute.getDescription()))
			{
				queryTerms.add(targetAttribute.getDescription());
			}
		}
		return queryTerms;
	}

	public static Set<String> collectLowerCaseTerms(OntologyTermImpl ontologyTerm)
	{
		Set<String> allTerms = Sets.newLinkedHashSet();
		allTerms.addAll(ontologyTerm.getSynonyms().stream().map(StringUtils::lowerCase).collect(Collectors.toList()));
		allTerms.add(ontologyTerm.getLabel().toLowerCase());
		return allTerms;
	}

	public static Set<String> getLowerCaseTerms(OntologyTermImpl ontologyTerm)
	{
		Set<String> allTerms = Sets.newLinkedHashSet();
		allTerms.addAll(ontologyTerm.getSynonyms().stream().map(StringUtils::lowerCase).collect(Collectors.toList()));
		allTerms.add(ontologyTerm.getLabel().toLowerCase());
		return allTerms;
	}

	public static Set<String> splitIntoUniqueTerms(String description)
	{
		return newLinkedHashSet(stream(description.split(ILLEGAL_CHARS_REGEX)).map(StringUtils::lowerCase)
				.filter(StringUtils::isNotBlank).collect(toList()));
	}

	public static List<String> splitIntoTerms(String description)
	{
		return stream(description.split(ILLEGAL_CHARS_REGEX)).map(StringUtils::lowerCase)
				.filter(StringUtils::isNotBlank).collect(toList());
	}

	public static Set<String> splitRemoveStopWords(String description)
	{
		return newLinkedHashSet(stream(description.split(ILLEGAL_CHARS_REGEX)).map(StringUtils::lowerCase)
				.filter(w -> !STOPWORDSLIST.contains(w) && isNotBlank(w)).collect(toList()));
	}
}