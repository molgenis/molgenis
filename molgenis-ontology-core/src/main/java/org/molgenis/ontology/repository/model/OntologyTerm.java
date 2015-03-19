package org.molgenis.ontology.repository.model;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.join;

import java.util.List;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTerm.class)
public abstract class OntologyTerm
{
	public abstract String getIRI();

	public abstract String getName();

	public abstract List<String> getSynonyms();

	public static OntologyTerm create(String iri, String name, List<String> synonyms)
	{
		return new AutoValue_OntologyTerm(iri, name, copyOf(synonyms));
	}

	/**
	 * Creates a new {@link OntologyTerm} that is the combined ontology term of a couple ontology terms. Its IRI will be
	 * a comma separated list of all original ontology term IRIs. Its name will be for instance
	 * <code>(term1 and term2 and term3)</code>. It won't have any synonyms.
	 * 
	 * This can be used to tag an attribute with (hypertension and medication).
	 * 
	 * @param terms
	 *            the {@link OntologyTerm}s to combine
	 * @return the combined OntologyTerm
	 */
	public static OntologyTerm add(OntologyTerm... terms)
	{
		return new AutoValue_OntologyTerm(join(stream(terms).map(OntologyTerm::getIRI).toArray(), ','), "("
				+ join(stream(terms).map(OntologyTerm::getName).toArray(), " and ") + ")", emptyList());
	}
}
