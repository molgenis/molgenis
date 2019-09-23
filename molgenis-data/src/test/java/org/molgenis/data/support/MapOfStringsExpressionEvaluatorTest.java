package org.molgenis.data.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = MapOfStringsExpressionEvaluatorTest.Config.class)
class MapOfStringsExpressionEvaluatorTest extends AbstractMolgenisSpringTest {
  private Entity entity;
  private EntityType emd;
  private EntityType refEmd;

  @Autowired private EntityTypeFactory entityTypeFactory;
  @Autowired private AttributeFactory attributeFactory;

  MapOfStringsExpressionEvaluatorTest() {
    super(Strictness.WARN);
  }

  private EntityType createDynamicLocationMetaData() {
    return entityTypeFactory
        .create("Location")
        .addAttribute(attributeFactory.create().setName("Identifier").setDataType(STRING), ROLE_ID)
        .addAttribute(attributeFactory.create().setName("Chromosome").setDataType(STRING))
        .addAttribute(attributeFactory.create().setName("Position").setDataType(STRING));
  }

  private EntityType createDynamicSourceMetaData() {
    return entityTypeFactory
        .create("Source")
        .addAttribute(attributeFactory.create().setName("Identifier").setDataType(STRING), ROLE_ID)
        .addAttribute(attributeFactory.create().setName("Int").setDataType(INT))
        .addAttribute(attributeFactory.create().setName("String").setDataType(STRING))
        .addAttribute(attributeFactory.create().setName("NonNumericString").setDataType(STRING))
        .addAttribute(attributeFactory.create().setName("Long").setDataType(LONG));
  }

  @BeforeEach
  void createEntity() {
    emd = createDynamicSourceMetaData();
    refEmd = createDynamicLocationMetaData();

    entity = new DynamicEntity(emd);
    entity.set("Int", 1);
    entity.set("String", "12");
    entity.set("Long", 10L);
    entity.set("NonNumericString", "Hello World!");
  }

  @Test
  void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasExpression() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
    when(amd.getDataType()).thenReturn(STRING);
    try {
      new MapOfStringsExpressionEvaluator(amd, emd);
      fail("Expected NPE");
    } catch (NullPointerException expected) {
      assertEquals("Attribute has no expression.", expected.getMessage());
    }
  }

  @Test
  void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasRefEntity() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("location").getMock();
    when(amd.getDataType()).thenReturn(XREF);
    when(amd.getExpression()).thenReturn("{'a':b}");
    try {
      new MapOfStringsExpressionEvaluator(amd, emd);
      fail("Expected IllegalArgumentException.");
    } catch (NullPointerException expected) {
      assertEquals("refEntity not specified.", expected.getMessage());
    }
  }

  @Test
  void testMapOfStringsEvaluatorConstructorChecksIfExpressionIsMap() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("location").getMock();
    when(amd.getDataType()).thenReturn(XREF);
    when(amd.hasRefEntity()).thenReturn(true);
    when(amd.getRefEntity()).thenReturn(refEmd);
    when(amd.getExpression()).thenReturn("hallo");
    try {
      new MapOfStringsExpressionEvaluator(amd, emd);
      fail("Expected JSON exception");
    } catch (JsonSyntaxException expected) {

    }
  }

  @Test
  void testMapOfStringsEvaluatorConstructorChecksThatExpressionIsMapOfStrings() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
    when(amd.getDataType()).thenReturn(XREF);
    when(amd.hasRefEntity()).thenReturn(true);
    when(amd.getRefEntity()).thenReturn(refEmd);
    when(amd.getExpression()).thenReturn("{'Chromosome':{'hallo1':'bla'}}");
    try {
      new MapOfStringsExpressionEvaluator(amd, emd);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertEquals(
          "Nested expressions not supported, expression must be Map<String,String>.",
          expected.getMessage());
    }
  }

  @Test
  void testMapOfStringsEvaluatorConstructorChecksIfCalculatedAttributesAllExist() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
    when(amd.getDataType()).thenReturn(STRING);
    when(amd.hasRefEntity()).thenReturn(true);
    when(amd.getRefEntity()).thenReturn(refEmd);
    when(amd.getExpression()).thenReturn("{'hallo':String}");
    try {
      new MapOfStringsExpressionEvaluator(amd, emd);
      fail("Expected illegal argument exception");
    } catch (IllegalArgumentException expected) {
      assertEquals("Unknown target attribute: hallo.", expected.getMessage());
    }
  }

  @Test
  void testMapOfStringsEvaluatorConstructorChecksIfMentionedAttributesAllExist() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
    when(amd.getDataType()).thenReturn(STRING);
    when(amd.hasRefEntity()).thenReturn(true);
    when(amd.getRefEntity()).thenReturn(refEmd);
    when(amd.getExpression()).thenReturn("{'Chromosome':hallo}");
    try {
      new MapOfStringsExpressionEvaluator(amd, emd);
      fail("Expected illegal argument exception");
    } catch (IllegalArgumentException expected) {
      assertEquals(
          "Expression for attribute 'Chromosome' references non-existant attribute 'hallo'.",
          expected.getMessage());
    }
  }

  @Test
  void testEvaluate() {
    Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
    when(amd.getDataType()).thenReturn(XREF);
    when(amd.hasRefEntity()).thenReturn(true);
    when(amd.getRefEntity()).thenReturn(refEmd);
    when(amd.getExpression()).thenReturn("{'Chromosome':String, 'Position':Int}");
    when(amd.getEntityType()).thenReturn(mock(EntityType.class));
    when(amd.getDataType()).thenReturn(XREF);
    ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(amd, emd);
    Entity expected = new DynamicEntity(refEmd);
    expected.set("Chromosome", "12");
    expected.set("Position", "1");
    Entity actual = (Entity) evaluator.evaluate(entity);
    assertTrue(EntityUtils.equals(actual, expected));
  }
}
