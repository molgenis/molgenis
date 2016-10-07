package org.molgenis.data.semanticsearch.explain.criteria;

import org.molgenis.ontology.core.meta.OntologyTermEntity;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.Set;

public interface MatchingCriterion
{
	/**
	 * apply the matching criteria to determine whether or not the {@link OntologyTermEntity} is a good candidate
	 *
	 * @param words
	 * @param ontologyTerm
	 * @return
	 */
	public abstract boolean apply(Set<String> words, OntologyTerm ontologyTerm);
}
