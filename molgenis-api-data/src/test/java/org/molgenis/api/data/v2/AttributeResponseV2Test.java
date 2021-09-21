package org.molgenis.api.data.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class AttributeResponseV2Test {
  private final UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
  @Mock private EntityType entityType;
  @Mock private Attribute attr;
  @Mock private Tag tag1;
  @Mock private Tag tag2;
  @Mock private Fetch fetch;
  @Mock private UserPermissionEvaluator permissionService;
  @Mock private DataService dataService;

  @Test
  void testTags() {
    when(attr.getTags()).thenReturn(List.of(tag1, tag2));

    when(tag1.getRelationIri()).thenReturn(Relation.type.getIRI());
    when(tag1.getRelationLabel()).thenReturn(Relation.type.getLabel());
    when(tag1.getObjectIri()).thenReturn("http://www.w3.org/2001/XMLSchema#token");
    when(tag1.getLabel()).thenReturn("Token");

    when(tag2.getRelationIri()).thenReturn(Relation.isAssociatedWith.getIRI());
    when(tag2.getRelationLabel()).thenReturn(Relation.isAssociatedWith.getLabel());
    when(tag2.getObjectIri()).thenReturn(null);
    when(tag2.getLabel()).thenReturn("Label");

    var tagresponse1 =
        TagResponseV2.create(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "type",
            "http://www.w3.org/2001/XMLSchema#token",
            "Token");
    var tagresponse2 =
        TagResponseV2.create(
            "http://molgenis.org#isAssociatedWith", "isAssociatedWith", null, "Label");

    AttributeResponseV2 attributeResponseV2 =
        new AttributeResponseV2(
            uriBuilder, "parent", entityType, attr, fetch, permissionService, dataService);
    assertEquals(List.of(tagresponse1, tagresponse2), attributeResponseV2.getTags());
  }
}
