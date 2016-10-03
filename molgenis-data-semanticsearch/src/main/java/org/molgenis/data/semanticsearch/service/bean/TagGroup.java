package org.molgenis.data.semanticsearch.service.bean;

import java.util.Arrays;
import java.util.List;

import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

import com.google.auto.value.AutoValue;

/**
 * {@link OntologyTerm}s that got matched to an attribute.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_TagGroup.class)
public abstract class TagGroup implements Comparable<TagGroup>
{
	public static TagGroup create(OntologyTerm ontologyTerm, String matchedWords, float score)
	{
		return new AutoValue_TagGroup(Arrays.asList(ontologyTerm), matchedWords, Math.round(score * 100000));
	}

	public static TagGroup create(List<OntologyTerm> ontologyTerms, String matchedWords, float score)
	{
		return new AutoValue_TagGroup(ontologyTerms, matchedWords, Math.round(score * 100000));
	}

	/**
	 * The ontology terms that got matched to the attribute, combined into one {@link OntologyTerm}
	 */
	public abstract List<OntologyTerm> getOntologyTerms();

	/**
	 * A long string containing all words in the {@link getJoinedSynonym()} that got matched to the attribute.
	 */
	public abstract String getMatchedWords();

	public abstract int getScoreInt();

	public float getScore()
	{
		return getScoreInt() / 100000.0f;
	}

	public OntologyTerm getCombinedOntologyTerm()
	{
		return OntologyTerm.and(getOntologyTerms().stream().toArray(OntologyTerm[]::new));
	}

	@Override
	public int compareTo(TagGroup o)
	{
		return Float.compare(getScore(), o.getScore());
	}
}
