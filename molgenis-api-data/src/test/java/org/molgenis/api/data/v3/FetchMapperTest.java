package org.molgenis.api.data.v3;

import static freemarker.template.utility.Collections12.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import org.molgenis.api.model.Selection;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.Test;

public class FetchMapperTest {

  @Test
  public void testToFetch() {
    FetchMapper fetchMapper = new FetchMapper();

    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("test");
    Attribute attributeXref = mock(Attribute.class);
    when(attributeXref.getName()).thenReturn("xref");
    when(attributeXref.getDataType()).thenReturn(AttributeType.XREF);
    Attribute refAttribute = mock(Attribute.class);
    when(refAttribute.getName()).thenReturn("refId");
    EntityType refEntityType = mock(EntityType.class);
    when(refAttribute.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAtomicAttributes()).thenReturn(Arrays.asList(attribute, attributeXref));
    when(refEntityType.getAtomicAttributes()).thenReturn(singletonList(refAttribute));

    Selection expand = new Selection(Collections.singletonMap("xref", Selection.FULL_SELECTION));
    Selection filter = Selection.FULL_SELECTION;
    Fetch expected = new Fetch().field("xref").field("test");
    assertEquals(fetchMapper.toFetch(entityType, filter, expand), expected);
  }

  @Test
  public void testToFetchEmpty() {
    FetchMapper fetchMapper = new FetchMapper();
    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("test");
    when(entityType.getAtomicAttributes()).thenReturn(Collections.singletonList(attribute));
    Selection expand = Selection.EMPTY_SELECTION;
    Selection filter = Selection.FULL_SELECTION;
    Fetch expected = new Fetch().field("test");
    assertEquals(fetchMapper.toFetch(entityType, filter, expand), expected);
  }
}
