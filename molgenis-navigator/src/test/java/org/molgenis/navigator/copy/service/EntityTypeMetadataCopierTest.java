package org.molgenis.navigator.copy.service;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.INDEXING_DEPTH;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.meta.model.EntityTypeMetadata.TAGS;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.Progress;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityTypeMetadataCopierTest extends AbstractMockitoTest {

  @Mock private AttributeFactory attributeFactory;
  private EntityTypeMetadataCopier copier;

  @BeforeMethod
  public void beforeMethod() {
    copier = new EntityTypeMetadataCopier(attributeFactory);
  }

  @Test
  public void testCopy() {
    EntityType entityType = mock(EntityType.class);
    EntityType entityTypeMeta = mockEntityTypeMetadata();
    when(entityType.getEntityType()).thenReturn(entityTypeMeta);
    Attribute idAttr = mock(Attribute.class);
    when(idAttr.getIdentifier()).thenReturn("id");
    when(idAttr.getName()).thenReturn("id");
    Attribute idAttrCopy = mock(Attribute.class);
    when(idAttrCopy.getName()).thenReturn("id");
    when(idAttrCopy.setEntity(any())).thenReturn(idAttrCopy);
    when(entityType.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
    when(attributeFactory.create()).thenReturn(idAttrCopy);
    CopyState state = CopyState.create(null, mock(Progress.class));

    copier.copy(entityType, state);

    assertEquals(state.copiedAttributes(), ImmutableMap.of("id", idAttrCopy));
  }

  private static EntityType mockEntityTypeMetadata() {
    EntityType entityTypeMeta = mock(EntityType.class);
    Attribute attributesAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    doReturn(attributesAttr).when(entityTypeMeta).getAttribute(ATTRIBUTES);
    Attribute abstractAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    doReturn(abstractAttr).when(entityTypeMeta).getAttribute(IS_ABSTRACT);
    Attribute tagsAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    doReturn(tagsAttr).when(entityTypeMeta).getAttribute(TAGS);
    Attribute depthAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(depthAttr).when(entityTypeMeta).getAttribute(INDEXING_DEPTH);
    return entityTypeMeta;
  }
}
