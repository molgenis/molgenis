package org.molgenis.semanticmapper.controller;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.Relation;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TagWizardControllerTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private OntologyService ontologyService;
  @Mock private OntologyTagService ontologyTagService;
  @Mock private SemanticSearchService semanticSearchService;
  private TagWizardController tagWizardController;

  @BeforeMethod
  public void setUpBeforeMethod() {
    tagWizardController =
        new TagWizardController(
            dataService, ontologyService, ontologyTagService, semanticSearchService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testTagWizardController() {
    new TagWizardController(null, null, null, null);
  }

  @Test
  public void testViewTagWizardFilterSystemEntityTypes() {
    when(ontologyService.getOntologies()).thenReturn(emptyList());

    String attributeName = "myAttribute";
    Attribute attribute = when(mock(Attribute.class).getName()).thenReturn(attributeName).getMock();

    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    when(entityType.getAttributes()).thenReturn(singletonList(attribute));

    Package systemPackage = when(mock(Package.class).getId()).thenReturn("sys").getMock();
    EntityType systemEntityType = mock(EntityType.class);
    when(systemEntityType.getPackage()).thenReturn(systemPackage);
    when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(Stream.of(entityType, systemEntityType));
    when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);

    LinkedHashMultimap<Relation, OntologyTerm> attributeTags = LinkedHashMultimap.create();
    when(ontologyTagService.getTagsForAttribute(entityType, attribute)).thenReturn(attributeTags);
    Model model = mock(Model.class);
    tagWizardController.viewTagWizard(null, model);

    Multimap<Object, Object> expectedTaggedAttributes = LinkedHashMultimap.create();
    expectedTaggedAttributes.put(attributeName, emptyList());
    verify(model).addAttribute("entity", entityType);
    verify(model).addAttribute("entityTypeIds", singletonList(entityTypeId));
    verify(model).addAttribute("attributes", singletonList(attribute));
    verify(model).addAttribute("ontologies", emptyList());
    verify(model).addAttribute("taggedAttributes", singletonMap(attributeName, attributeTags));
    verify(model)
        .addAttribute(
            "relations",
            new Relation[] {
              Relation.instanceOf,
              Relation.link,
              Relation.homepage,
              Relation.isDefinedBy,
              Relation.seeAlso,
              Relation.hasLowerValue,
              Relation.hasUpperValue,
              Relation.isRealizationOf,
              Relation.isGeneralizationOf,
              Relation.hasSourceId,
              Relation.hasSourceName,
              Relation.isAssociatedWith
            });
  }
}
