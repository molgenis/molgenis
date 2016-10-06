package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.join;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CombinedOntologyTerm.class)
public abstract class CombinedOntologyTerm implements OntologyTagObject
{
	public static CombinedOntologyTerm create(String iri, String label)
	{
		return new AutoValue_CombinedOntologyTerm(iri, label);
	}

	/**
	 * Creates a new {@link OntologyTerm} that is the combined ontology term of a couple ontology terms. Its IRI
	 * will be a comma separated list of all original ontology term IRIs. Its label will be for instance
	 * <code>(term1 and term2 and term3)</code>. It won't have any synonyms.
	 * <p>
	 * This can be used to tag an attribute with (hypertension and medication).
	 *
	 * @param terms the {@link OntologyTerm}s to combine
	 * @return the combined OntologyTerm
	 */
	public static CombinedOntologyTerm and(OntologyTerm... terms)
	{
		return create(join(stream(terms).map(OntologyTerm::getIRI).toArray(), ','),
				"(" + join(stream(terms).map(OntologyTerm::getLabel).toArray(), " and ") + ")");
	}
}
