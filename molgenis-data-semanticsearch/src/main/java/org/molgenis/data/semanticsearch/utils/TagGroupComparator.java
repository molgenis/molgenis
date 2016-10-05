package org.molgenis.data.semanticsearch.utils;

import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.utils.Stemmer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TagGroupComparator implements Comparator<TagGroup>
{
	@Override
	public int compare(TagGroup o2, TagGroup o1)
	{
		String synonym1 = o1.getMatchedWords();
		String synonym2 = o2.getMatchedWords();

		float score1 = o1.getScore();
		float score2 = o2.getScore();

		int compare = Float.compare(score1, score2);

		// two elements have the same score, they need to be sorted according to other standards
		if (compare == 0)
		{
			if (synonymEquals(synonym1, synonym2))
			{
				// if the current ontology term doesn't have semantic types and next ontology term does, we are in favor
				// of the ontology terms that have semantic types
				if (isSemanticTypesEmpty(o1) && !isSemanticTypesEmpty(o2))
				{
					return -1;
				}

				if (!isSemanticTypesEmpty(o1) && isSemanticTypesEmpty(o2))
				{
					return 1;
				}

				// if the next ontologyterm is matched based on its label rather than any of the synonyms, the
				// order of the next ontologyterm should be higher than the previous one
				if (isOntologyTermNameMatched(o1) && !isOntologyTermNameMatched(o2))
				{
					return 1;
				}

				if (!isOntologyTermNameMatched(o1) && isOntologyTermNameMatched(o2))
				{
					return -1;
				}

				// if both of the ontology terms are matched based on one of the synonyms rather than the name,
				// the order of these two elements need to be re-sorted based on the synonym information content
				if (!isOntologyTermNameMatched(o1) && !isOntologyTermNameMatched(o2))
				{
					List<String> synonyms1 = o1.getOntologyTerms().stream().flatMap(ot -> ot.getSynonyms().stream())
							.distinct().collect(Collectors.toList());
					List<String> synonyms2 = o2.getOntologyTerms().stream().flatMap(ot -> ot.getSynonyms().stream())
							.distinct().collect(Collectors.toList());

					int informationContent1 = calculateInformationContent(synonym1, synonyms1);
					int informationContent2 = calculateInformationContent(synonym2, synonyms2);

					return Integer.compare(informationContent1, informationContent2);
				}
			}
		}
		return compare;
	}

	int calculateInformationContent(String bestMatchingSynonym, List<String> synonyms)
	{
		final String bestMatchingSynonymLowerCase = bestMatchingSynonym.toLowerCase();
		long count = synonyms.stream().filter(s -> s.toLowerCase().contains(bestMatchingSynonymLowerCase)).count();
		return (int) count;
	}

	boolean synonymEquals(String synonym1, String synonym2)
	{
		return synonym1.equals(synonym2) || Stemmer.cleanStemPhrase(synonym1)
				.equalsIgnoreCase(Stemmer.cleanStemPhrase(synonym2));
	}

	boolean isOntologyTermNameMatched(TagGroup hit)
	{
		return hit.getCombinedOntologyTerm().getLabel().equalsIgnoreCase(hit.getMatchedWords());
	}

	boolean isSemanticTypesEmpty(TagGroup hit)
	{
		return hit.getOntologyTerms().stream().flatMap(ot -> ot.getSemanticTypes().stream()).distinct()
				.collect(Collectors.toList()).isEmpty();
	}
}