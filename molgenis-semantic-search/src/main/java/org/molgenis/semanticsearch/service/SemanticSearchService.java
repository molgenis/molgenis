package org.molgenis.semanticsearch.service;

import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Set;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.semanticsearch.explain.bean.AttributeSearchResults;
import org.molgenis.semanticsearch.explain.bean.EntityTypeSearchResults;
import org.molgenis.semanticsearch.semantic.Hits;

public interface SemanticSearchService {
  /**
   * Find {@link Attribute attributes} in a {@link EntityType entity type} that match the given
   * target attribute in the target entity type.
   */
  default AttributeSearchResults findAttributes(
      EntityType sourceEntityType, EntityType targetEntityType, Attribute targetAttribute) {
    return findAttributes(sourceEntityType, targetEntityType, targetAttribute, emptySet());
  }

  /**
   * Find {@link Attribute attributes} in a source {@link EntityType entity type} that match
   * attributes in a target entity type.
   */
  default EntityTypeSearchResults findAttributes(
      EntityType sourceEntityType, EntityType targetEntityType) {
    return findAttributes(sourceEntityType, targetEntityType, emptySet());
  }

  /**
   * Find {@link Attribute attributes} in an {@link EntityType entity type} that match the given
   * target attribute in the target entity type. Optionally constrain the search using one or more
   * search terms.
   */
  AttributeSearchResults findAttributes(
      EntityType sourceEntityType,
      EntityType targetEntityType,
      Attribute targetAttribute,
      Set<String> searchTerms);

  /**
   * Find {@link Attribute attributes} in a source {@link EntityType entity type} that match
   * attributes in a target entity type. Optionally constrain the search using one or more search
   * terms.
   */
  EntityTypeSearchResults findAttributes(
      EntityType sourceEntityType, EntityType targetEntityType, Set<String> searchTerms);

  /**
   * Finds {@link OntologyTerm ontology terms} in the given ontologies that can be used to tag an
   * attribute.
   */
  Hits<OntologyTerm> findOntologyTerms(Attribute attribute, Collection<Ontology> ontologies);
}
