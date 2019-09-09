package org.molgenis.web.rsql;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class RSQLValueParserTest {
  private RSQLValueParser rSqlValueParser;

  @BeforeEach
  void setUpBeforeMethod() {
    rSqlValueParser = new RSQLValueParser();
  }

  static Iterator<Object[]> parseProvider() {
    return newArrayList(
            new Object[] {ONE_TO_MANY, INT, 1},
            new Object[] {ONE_TO_MANY, STRING, "1"},
            new Object[] {XREF, INT, 1},
            new Object[] {XREF, STRING, "1"})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("parseProvider")
  void parse(AttributeType attrType, AttributeType refIdAttrType, Object parsedValue) {
    Attribute oneToManyAttr = mock(Attribute.class);
    when(oneToManyAttr.getDataType()).thenReturn(attrType);
    EntityType refEntity = mock(EntityType.class);
    Attribute refIdAttr = mock(Attribute.class);
    when(refIdAttr.getDataType()).thenReturn(refIdAttrType);
    when(refEntity.getIdAttribute()).thenReturn(refIdAttr);
    when(oneToManyAttr.getRefEntity()).thenReturn(refEntity);
    assertEquals(parsedValue, rSqlValueParser.parse("1", oneToManyAttr));
  }
}
