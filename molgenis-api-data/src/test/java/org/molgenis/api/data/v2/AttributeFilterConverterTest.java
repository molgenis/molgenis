package org.molgenis.api.data.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttributeFilterConverterTest {

  private AttributeFilterConverter attributeFilterConverter;

  @BeforeEach
  void setUpBeforeMethod() {
    attributeFilterConverter = new AttributeFilterConverter();
  }

  @Test
  void convertSingleAttribute() {
    AttributeFilter attributeFilter = new AttributeFilter().add("attr0");
    assertEquals(attributeFilterConverter.convert("attr0"), attributeFilter);
  }

  @Test
  void convertSingleAttributeEscapeComma() {
    AttributeFilter attributeFilter = new AttributeFilter().add("attr,0");
    assertEquals(attributeFilterConverter.convert("attr\\,0"), attributeFilter);
  }

  // @Test
  // void convertSingleAttributeEscapeSlash()
  // {
  // AttributeFilter attributeFilter = new AttributeFilter().add("attr/0");
  // assertEquals(attributeFilterConverter.convert("attr//0"),
  // attributeFilter);
  // }

  @Test
  void convertSingleAttributeEscapeParenthesisLeft() {
    AttributeFilter attributeFilter = new AttributeFilter().add("attr(0");
    assertEquals(attributeFilterConverter.convert("attr\\(0"), attributeFilter);
  }

  @Test
  void convertSingleAttributeEscapeParenthesisRight() {
    AttributeFilter attributeFilter = new AttributeFilter().add("attr)0");
    assertEquals(attributeFilterConverter.convert("attr\\)0"), attributeFilter);
  }

  // @Test
  // void convertSingleAttributeSubSelectionSingleAttributeSlash()
  // {
  // AttributeFilter attributeFilter = new AttributeFilter().add("attr0", new
  // AttributeFilter().add("subattr0"));
  // assertEquals(attributeFilterConverter.convert("attr0/subattr0"), attributeFilter);
  // }

  @Test
  void convertSingleAttributeSubSelectionSingleAttributeParenthesis() {
    AttributeFilter attributeFilter =
        new AttributeFilter().add("attr0", new AttributeFilter().add("subattr0"));
    assertEquals(attributeFilterConverter.convert("attr0(subattr0)"), attributeFilter);
  }

  @Test
  void convertSingleAttributeSubSelectionSingleAttributeParenthesisWildcard() {
    AttributeFilter attributeFilter =
        new AttributeFilter().add("attr0", new AttributeFilter().setIncludeAllAttrs(true));
    assertEquals(attributeFilterConverter.convert("attr0(*)"), attributeFilter);
  }

  @Test
  void convertSingleAttributeSubSelectionMultipleAttributes() {
    AttributeFilter attributeFilter =
        new AttributeFilter().add("attr0", new AttributeFilter().add("subattr0").add("subattr1"));
    assertEquals(attributeFilterConverter.convert("attr0(subattr0,subattr1)"), attributeFilter);
  }

  @Test
  void convertSingleAttributeSubSelectionMultipleAttributesWildcard1() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add("attr0", new AttributeFilter().add("subattr0").setIncludeAllAttrs(true));
    assertEquals(attributeFilterConverter.convert("attr0(subattr0,*)"), attributeFilter);
  }

  @Test
  void convertSingleAttributeSubSelectionMultipleAttributesWildcard2() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add("attr0", new AttributeFilter().add("subattr1").setIncludeAllAttrs(true));
    assertEquals(attributeFilterConverter.convert("attr0(*,subattr1)"), attributeFilter);
  }

  @Test
  void convertSingleAttributeSubSubSelection() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add(
                "attr0",
                new AttributeFilter().add("subattr0", new AttributeFilter().add("subsubattr0")));
    assertEquals(attributeFilterConverter.convert("attr0(subattr0(subsubattr0)"), attributeFilter);
  }

  @Test
  void convertMultipleAttributes() {
    AttributeFilter attributeFilter = new AttributeFilter().add("attr0").add("attr1");
    assertEquals(attributeFilterConverter.convert("attr0,attr1"), attributeFilter);
  }

  // @Test
  // void convertMultipleAttributesSubSelectionSingleAttributeSlash()
  // {
  // AttributeFilter attributeFilter = new AttributeFilter().add("attr0").add("attr1",
  // new AttributeFilter().add("subattr1"));
  // assertEquals(attributeFilterConverter.convert("attr0,attr1/subattr1"), attributeFilter);
  // }

  @Test
  void convertMultipleAttributesSubSelectionSingleAttributeParenthesis() {
    AttributeFilter attributeFilter =
        new AttributeFilter().add("attr0").add("attr1", new AttributeFilter().add("subattr1"));
    assertEquals(attributeFilterConverter.convert("attr0,attr1(subattr1)"), attributeFilter);
  }

  @Test
  void convertMultipleAttributesSubSelectionSingleAttributeParenthesisWildcard() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add("attr0")
            .add("attr1", new AttributeFilter().setIncludeAllAttrs(true));
    assertEquals(attributeFilterConverter.convert("attr0,attr1(*)"), attributeFilter);
  }

  @Test
  void convertMultipleAttributesSubSelectionMultipleAttributes() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add("attr0")
            .add("attr1", new AttributeFilter().add("subattr1").add("subattr2"));
    assertEquals(
        attributeFilterConverter.convert("attr0,attr1(subattr1,subattr2)"), attributeFilter);
  }

  @Test
  void convertMultipleAttributesSubSelectionMultipleAttributesWildCard1() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add("attr0")
            .add("attr1", new AttributeFilter().add("subattr1").setIncludeAllAttrs(true));
    assertEquals(attributeFilterConverter.convert("attr0,attr1(subattr1,*)"), attributeFilter);
  }

  @Test
  void convertMultipleAttributesSubSelectionMultipleAttributesWildCard2() {
    AttributeFilter attributeFilter =
        new AttributeFilter()
            .add("attr0")
            .add("attr1", new AttributeFilter().add("subattr2").setIncludeAllAttrs(true));
    assertEquals(attributeFilterConverter.convert("attr0,attr1(*,subattr2)"), attributeFilter);
  }
}
