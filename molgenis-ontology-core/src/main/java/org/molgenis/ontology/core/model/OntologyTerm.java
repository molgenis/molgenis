package org.molgenis.ontology.core.model;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.List;

import javax.annotation.Nullable;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTerm.class)
public abstract class OntologyTerm
{
	public abstract String getIRI();

	public abstract String getLabel();

	@Nullable
	public abstract String getDescription();

	public abstract List<String> getSynonyms();

	public abstract List<String> getNodePaths();

	@Nullable
	public abstract List<OntologyTermAnnotation> getAnnotations();

	@Nullable
	public abstract List<SemanticType> getSemanticTypes();

	public static OntologyTerm create(String iri, String label)
	{
		return new AutoValue_OntologyTerm(iri, label, null, emptyList(), emptyList(), emptyList(), emptyList());
	}

	public static OntologyTerm create(String iri, String label, List<String> synonyms)
	{
		return new AutoValue_OntologyTerm(iri, label, null, synonyms, emptyList(), emptyList(), emptyList());
	}

	public static OntologyTerm create(String iri, String label, String description, List<String> synonyms,
			List<String> nodePaths, List<OntologyTermAnnotation> annotations, List<SemanticType> semanticTypes)
	{
		return new AutoValue_OntologyTerm(iri, label, description, synonyms, nodePaths, annotations, semanticTypes);
	}

	/**
	 * Creates a new {@link OntologyTerm} that is the combined ontology term of a couple ontology terms. Its IRI will be
	 * a comma separated list of all original ontology term IRIs. Its label will be for instance
	 * <code>(term1 and term2 and term3)</code>. It won't have any synonyms.
	 * 
	 * This can be used to tag an attribute with (hypertension and medication).
	 * 
	 * @param terms
	 *            the {@link OntologyTerm}s to combine
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
