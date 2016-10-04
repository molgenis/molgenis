package org.molgenis.ontology.core.model;

import static java.util.Collections.emptyList;

import java.util.List;

import javax.annotation.Nullable;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTermImpl.class)
public abstract class OntologyTermImpl implements OntologyTerm
{
	public abstract String getId();

	@Nullable
	public abstract String getDescription();

	public abstract List<String> getSynonyms();

	public abstract List<String> getNodePaths();

	@Nullable
	public abstract List<OntologyTermAnnotation> getAnnotations();

	@Nullable
	public abstract List<SemanticType> getSemanticTypes();

	public static OntologyTermImpl create(String id, String iri, String label)
	{
		return new AutoValue_OntologyTermImpl(iri, label, id, null, emptyList(), emptyList(), emptyList(), emptyList());
	}

	public static OntologyTermImpl create(String id, String iri, String label, List<String> synonyms)
	{
		return new AutoValue_OntologyTermImpl(iri, label, id, null, synonyms, emptyList(), emptyList(), emptyList());
	}

	public static OntologyTermImpl create(String id, String iri, String label, String description,
			List<String> synonyms, List<String> nodePaths, List<OntologyTermAnnotation> annotations,
			List<SemanticType> semanticTypes)
	{
		return new AutoValue_OntologyTermImpl(iri, label, id, description, synonyms, nodePaths, annotations,
				semanticTypes);
	}
}
