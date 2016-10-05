package org.molgenis.data.semanticsearch.service.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.CombinedOntologyTermImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.OntologyTermImpl;

import java.util.Arrays;
import java.util.List;

/**
 * {@link OntologyTermImpl}s that got matched to an attribute.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_TagGroup.class)
public abstract class TagGroup implements Comparable<TagGroup>
{
	public static TagGroup create(OntologyTermImpl ontologyTerm, String matchedWords, float score)
	{
		return new AutoValue_TagGroup(Arrays.asList(ontologyTerm), matchedWords, Math.round(score * 100000));
	}

	public static TagGroup create(List<OntologyTermImpl> ontologyTerms, String matchedWords, float score)
	{
		return new AutoValue_TagGroup(ontologyTerms, matchedWords, Math.round(score * 100000));
	}

	/**
	 * The ontology terms that got matched to the attribute, combined into one {@link OntologyTermImpl}
	 */
	public abstract List<OntologyTermImpl> getOntologyTerms();

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
		return CombinedOntologyTermImpl.and(getOntologyTerms().stream().toArray(OntologyTermImpl[]::new));
	}

	@Override
	public int compareTo(TagGroup o)
	{
		return Float.compare(getScore(), o.getScore());
	}
}
