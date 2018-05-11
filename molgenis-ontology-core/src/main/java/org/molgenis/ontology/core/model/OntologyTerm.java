package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.join;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTerm.class)
public abstract class OntologyTerm
{
	public abstract String getIRI();

	public abstract String getLabel();

	@Nullable
	public abstract String getDescription();

	public abstract List<String> getSynonyms();

	public static OntologyTerm create(String iri, String label)
	{
		return new AutoValue_OntologyTerm(iri, label, null, Collections.singletonList(label));
	}

	public static OntologyTerm create(String iri, String label, List<String> synonyms)
	{
		return new AutoValue_OntologyTerm(iri, label, null, copyOf(synonyms));
	}

	public static OntologyTerm create(String iri, String label, String description)
	{
		return new AutoValue_OntologyTerm(iri, label, description, Collections.singletonList((description)));
	}

	public static OntologyTerm create(String iri, String label, String description, List<String> synonyms)
	{
		return new AutoValue_OntologyTerm(iri, label, description, copyOf(synonyms));
	}

	/**
	 * Creates a new {@link OntologyTerm} that is the combined ontology term of a couple ontology terms. Its IRI will be
	 * a comma separated list of all original ontology term IRIs. Its label will be for instance
	 * <code>(term1 and term2 and term3)</code>. It won't have any synonyms.
	 * <p>
	 * This can be used to tag an attribute with (hypertension and medication).
	 *
	 * @param terms the {@link OntologyTerm}s to combine
	 * @return the combined OntologyTerm
	 */
	public static OntologyTerm and(OntologyTerm... terms)
	{
		if (terms == null || terms.length == 0)
		{
			return null;
		}
		if (terms.length == 1)
		{
			return terms[0];
		}
		return create(join(stream(terms).map(OntologyTerm::getIRI).toArray(), ','),
				"(" + join(stream(terms).map(OntologyTerm::getLabel).toArray(), " and ") + ")");
	}
}
