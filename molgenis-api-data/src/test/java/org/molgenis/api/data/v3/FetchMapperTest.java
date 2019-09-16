package org.molgenis.api.data.v3;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Selection;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class FetchMapperTest extends AbstractMockitoTest {
  private FetchMapper fetchMapper;

  @BeforeEach
  void setUpBeforeMethod() {
    fetchMapper = new FetchMapper();
  }

  @Test
  void testToFetch() {
    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("test");
    Attribute attributeXref = mock(Attribute.class);
    when(attributeXref.getName()).thenReturn("xref");
    when(attributeXref.getDataType()).thenReturn(AttributeType.XREF);
    when(entityType.getAtomicAttributes()).thenReturn(Arrays.asList(attribute, attributeXref));

    Selection expand = new Selection(Collections.singletonMap("xref", Selection.FULL_SELECTION));
    Selection filter = Selection.FULL_SELECTION;
    Fetch expected = new Fetch().field("xref").field("test");
    assertEquals(expected, fetchMapper.toFetch(entityType, filter, expand));
  }

  @Test
  void testToFetchEmpty() {
    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("test");
    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));
    Selection expand = Selection.EMPTY_SELECTION;
    Selection filter = Selection.FULL_SELECTION;
    Fetch expected = new Fetch().field("test");
    assertEquals(expected, fetchMapper.toFetch(entityType, filter, expand));
  }
}
