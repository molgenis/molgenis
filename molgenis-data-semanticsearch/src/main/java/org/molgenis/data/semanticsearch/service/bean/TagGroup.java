package org.molgenis.data.semanticsearch.service.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.CombinedOntologyTerm;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * A List of {@link OntologyTerm}s that got matched to an attribute, plus the score of the match.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_TagGroup.class)
public abstract class TagGroup implements Comparable<TagGroup>
{
	public static TagGroup create(OntologyTerm ontologyTerm, String matchedWords, float score)
	{
		return new AutoValue_TagGroup(singletonList(ontologyTerm), matchedWords, score);
	}

	public static TagGroup create(List<OntologyTerm> ontologyTerms, String matchedWords, float score)
	{
		return new AutoValue_TagGroup(ontologyTerms, matchedWords, score);
	}

	/**
	 * The ontology terms that got matched to the attribute, combined into one {@link OntologyTerm}
	 */
	public abstract List<OntologyTerm> getOntologyTerms();

	/**
	 * A long string containing all words that got matched to the attribute.
	 */
	public abstract String getMatchedWords();

	public abstract float getScore();

	public CombinedOntologyTerm getCombinedOntologyTerm()
	{
		return CombinedOntologyTerm.and(getOntologyTerms().stream().toArray(OntologyTerm[]::new));
	}

	@Override
	public int compareTo(TagGroup o)
	{
		return Float.compare(getScore(), o.getScore());
	}
}
