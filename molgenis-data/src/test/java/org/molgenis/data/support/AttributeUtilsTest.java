package org.molgenis.data.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.util.AttributeUtils;

class AttributeUtilsTest {
  static Iterator<Object[]> isIdAttributeTypeAllowedProvider() {
    return Arrays.asList(
            new Object[] {BOOL, false},
            new Object[] {CATEGORICAL, false},
            new Object[] {CATEGORICAL_MREF, false},
            new Object[] {COMPOUND, false},
            new Object[] {DATE, false},
            new Object[] {DATE_TIME, false},
            new Object[] {DECIMAL, false},
            new Object[] {EMAIL, true},
            new Object[] {ENUM, false},
            new Object[] {FILE, false},
            new Object[] {HTML, false},
            new Object[] {HYPERLINK, true},
            new Object[] {INT, true},
            new Object[] {LONG, true},
            new Object[] {MREF, false},
            new Object[] {SCRIPT, false},
            new Object[] {STRING, true},
            new Object[] {TEXT, false},
            new Object[] {XREF, false})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("isIdAttributeTypeAllowedProvider")
  void isIdAttributeTypeAllowed(AttributeType attrType, boolean validIdAttrType) {
    Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(attrType).getMock();
    assertEquals(validIdAttrType, AttributeUtils.isIdAttributeTypeAllowed(attr));
  }

  static Iterator<Object[]> getI18nAttributeNameProvider() {
    List<Object[]> dataList = new ArrayList<>();
    dataList.add(new Object[] {"lang", "en", "langEn"});
    dataList.add(new Object[] {"lang", "En", "langEn"});
    dataList.add(new Object[] {"lang", "EN", "langEn"});
    return dataList.iterator();
  }

  @ParameterizedTest
  @MethodSource("getI18nAttributeNameProvider")
  void testGetI18nAttributeName(String attrName, String languageCode, String i18nAttrName) {
    assertEquals(i18nAttrName, getI18nAttributeName(attrName, languageCode));
  }

  // AttributeUtils.getDefaultTypedValue tested through DefaultValuePopulator
}
