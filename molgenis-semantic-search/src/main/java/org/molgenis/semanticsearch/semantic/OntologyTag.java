package org.molgenis.semanticsearch.semantic;

import com.google.auto.value.AutoValue;
import org.molgenis.data.semantic.Relation;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTag.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class OntologyTag {
  public abstract OntologyTerm getOntologyTerm();

  public abstract String getRelationIRI();

  public static OntologyTag create(OntologyTerm term, Relation relation) {
    return new AutoValue_OntologyTag(term, relation.getIRI());
  }
}
