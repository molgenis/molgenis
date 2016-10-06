package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.emptyList;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTerm.class)
public abstract class OntologyTerm implements OntologyTagObject
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

	public static OntologyTerm create(String id, String iri, String label)
	{
		return new AutoValue_OntologyTerm(iri, label, id, null, emptyList(), emptyList(), emptyList(), emptyList());
	}

	public static OntologyTerm create(String id, String iri, String label, List<String> synonyms)
	{
		return new AutoValue_OntologyTerm(iri, label, id, null, synonyms, emptyList(), emptyList(), emptyList());
	}

	public static OntologyTerm create(String id, String iri, String label, String description,
			List<String> synonyms, List<String> nodePaths, List<OntologyTermAnnotation> annotations,
			List<SemanticType> semanticTypes)
	{
		return new AutoValue_OntologyTerm(iri, label, id, description, synonyms, nodePaths, annotations,
				semanticTypes);
	}
}
