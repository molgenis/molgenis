package org.molgenis.api.data.v3;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.molgenis.api.model.Selection;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FetchMapperTest extends AbstractMockitoTest {
  private FetchMapper fetchMapper;

  @BeforeMethod
  public void setUpBeforeMethod() {
    fetchMapper = new FetchMapper();
  }

  @Test
  public void testToFetch() {
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
    assertEquals(fetchMapper.toFetch(entityType, filter, expand), expected);
  }

  @Test
  public void testToFetchEmpty() {
    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("test");
    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));
    Selection expand = Selection.EMPTY_SELECTION;
    Selection filter = Selection.FULL_SELECTION;
    Fetch expected = new Fetch().field("test");
    assertEquals(fetchMapper.toFetch(entityType, filter, expand), expected);
  }
}
