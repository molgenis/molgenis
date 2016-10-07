package org.molgenis.data.semanticsearch.explain.criteria.impl;

import org.molgenis.data.semanticsearch.explain.criteria.MatchingCriterion;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.utils.Stemmer;

import java.util.Set;

import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.collectLowerCaseTerms;

public class StrictMatchingCriterion implements MatchingCriterion
{
	@Override
	public boolean apply(Set<String> words, OntologyTerm ontologyTerm)
	{
		Set<String> ontologyTermSynonyms = collectLowerCaseTerms(ontologyTerm);
		for (String synonym : ontologyTermSynonyms)
		{
			Set<String> wordsInSynonym = Stemmer.splitAndStem(synonym);
			if (wordsInSynonym.size() != 0 && words.containsAll(wordsInSynonym)) return true;
		}
		return false;
	}
}
