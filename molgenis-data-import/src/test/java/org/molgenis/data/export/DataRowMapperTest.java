package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.export.mapper.DataRowMapper;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class DataRowMapperTest {

  @Test
  void testMapDataRow() {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);
    Entity refEntity1 = mock(Entity.class);
    Entity refEntity2 = mock(Entity.class);

    when(refEntity1.getIdValue()).thenReturn("ref1");
    when(refEntity2.getIdValue()).thenReturn("ref2");

    Attribute string = mock(Attribute.class);
    when(string.getName()).thenReturn("string");
    when(string.getDataType()).thenReturn(STRING);

    Attribute mref = mock(Attribute.class);
    when(mref.getName()).thenReturn("mref");
    when(mref.getDataType()).thenReturn(MREF);

    Attribute xref = mock(Attribute.class);
    when(xref.getName()).thenReturn("xref");
    when(xref.getDataType()).thenReturn(XREF);

    when(entityType.getAtomicAttributes()).thenReturn(newArrayList(string, xref, mref));

    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.get("string")).thenReturn("stringValue");
    when(entity.getEntity("xref")).thenReturn(refEntity1);
    when(entity.getEntities("mref")).thenReturn(newArrayList(refEntity1, refEntity2));

    List<Object> actual = DataRowMapper.mapDataRow(entity);
    List<Object> expected = newArrayList("stringValue", "ref1", "ref1,ref2");
    assertEquals(expected, actual);
  }
}
