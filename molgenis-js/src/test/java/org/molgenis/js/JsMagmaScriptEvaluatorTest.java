package org.molgenis.js;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Double.valueOf;
import static java.lang.Long.MAX_VALUE;
import static java.lang.String.format;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.SystemEntityType.UNIFIED_IDENTIFIER_REGEX_JS;
import static org.molgenis.js.magma.JsMagmaScriptContext.ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH;

import com.google.common.base.Stopwatch;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.graal.GraalScriptEngine;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;

class JsMagmaScriptEvaluatorTest {
  private static EntityType personWeightEntityType;
  private static EntityType personHeightEntityType;
  private static EntityType personWeightAndHeightEntityType;
  private static EntityType personBirthDateMeta;
  private static EntityType personAgeEntityType;
  private static EntityType personGenderEntityType;
  private static EntityType personTraitEntityType;
  private static EntityType personSmokingEntityType;
  private static EntityType personLastUpdatedEntityType;
  private static EntityType personLongEntityType;

  // Reference tables
  private static EntityType genderEntityType;
  private static EntityType traitEntityType;

  private static JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

  @BeforeAll
  static void beforeClass() {
    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute weightAttr = when(mock(Attribute.class).getName()).thenReturn("weight").getMock();
    when(weightAttr.getDataType()).thenReturn(INT);
    personWeightEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personWeightEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personWeightEntityType.getAttribute("weight")).thenReturn(weightAttr);
    when(personWeightEntityType.getAtomicAttributes()).thenReturn(singletonList(weightAttr));

    Attribute heightAttr = when(mock(Attribute.class).getName()).thenReturn("height").getMock();
    when(heightAttr.getDataType()).thenReturn(INT);
    personHeightEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personHeightEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personHeightEntityType.getAttribute("height")).thenReturn(heightAttr);
    when(personHeightEntityType.getAtomicAttributes()).thenReturn(singletonList(heightAttr));

    personWeightAndHeightEntityType =
        when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personWeightAndHeightEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personWeightAndHeightEntityType.getAttribute("weight")).thenReturn(weightAttr);
    when(personWeightAndHeightEntityType.getAttribute("height")).thenReturn(heightAttr);
    when(personWeightAndHeightEntityType.getAtomicAttributes())
        .thenReturn(asList(weightAttr, heightAttr));

    Attribute birthDateAttr =
        when(mock(Attribute.class).getName()).thenReturn("birthdate").getMock();
    when(birthDateAttr.getDataType()).thenReturn(DATE);
    personBirthDateMeta = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personBirthDateMeta.getIdAttribute()).thenReturn(idAttribute);
    when(personBirthDateMeta.getAttribute("birthdate")).thenReturn(birthDateAttr);
    when(personBirthDateMeta.getAtomicAttributes()).thenReturn(singletonList(birthDateAttr));

    Attribute ageAttr = when(mock(Attribute.class).getName()).thenReturn("age").getMock();
    when(ageAttr.getDataType()).thenReturn(INT);
    personAgeEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personAgeEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personAgeEntityType.getAttribute("age")).thenReturn(ageAttr);
    when(personAgeEntityType.getAtomicAttributes()).thenReturn(singletonList(ageAttr));

    Attribute genderAttr = when(mock(Attribute.class).getName()).thenReturn("gender").getMock();
    when(genderAttr.getDataType()).thenReturn(CATEGORICAL);
    personGenderEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personGenderEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personGenderEntityType.getAttribute("gender")).thenReturn(genderAttr);
    when(personGenderEntityType.getAtomicAttributes()).thenReturn(singletonList(genderAttr));

    Attribute labelAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
    when(labelAttr.getDataType()).thenReturn(STRING);
    genderEntityType = when(mock(EntityType.class).getId()).thenReturn("gender").getMock();
    when(genderEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(genderEntityType.getAttribute("id")).thenReturn(idAttribute);
    when(genderEntityType.getAttribute("label")).thenReturn(labelAttr);
    when(genderEntityType.getAtomicAttributes()).thenReturn(newArrayList(idAttribute, labelAttr));

    Attribute traitAttr = when(mock(Attribute.class).getName()).thenReturn("trait").getMock();
    when(traitAttr.getDataType()).thenReturn(MREF);
    personTraitEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personTraitEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personTraitEntityType.getAttribute("id")).thenReturn(idAttribute);
    when(personTraitEntityType.getAttribute("trait")).thenReturn(traitAttr);
    when(personTraitEntityType.getAtomicAttributes())
        .thenReturn(newArrayList(idAttribute, traitAttr));

    Attribute nameAttr = when(mock(Attribute.class).getName()).thenReturn("name").getMock();
    when(nameAttr.getDataType()).thenReturn(STRING);
    traitEntityType = when(mock(EntityType.class).getId()).thenReturn("trait").getMock();
    when(traitEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(traitEntityType.getAttribute("id")).thenReturn(idAttribute);
    when(traitEntityType.getAttribute("name")).thenReturn(nameAttr);
    when(traitEntityType.getAtomicAttributes()).thenReturn(newArrayList(idAttribute, nameAttr));

    Attribute smokingAttr = when(mock(Attribute.class).getName()).thenReturn("smoking").getMock();
    when(smokingAttr.getDataType()).thenReturn(BOOL);
    personSmokingEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personSmokingEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personSmokingEntityType.getAttribute("id")).thenReturn(idAttribute);
    when(personSmokingEntityType.getAttribute("smoking")).thenReturn(smokingAttr);
    when(personSmokingEntityType.getAtomicAttributes())
        .thenReturn(newArrayList(idAttribute, smokingAttr));

    Attribute lastUpdateAttr =
        when(mock(Attribute.class).getName()).thenReturn("lastUpdate").getMock();
    when(lastUpdateAttr.getDataType()).thenReturn(DATE_TIME);
    personLastUpdatedEntityType =
        when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personLastUpdatedEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personLastUpdatedEntityType.getAttribute("id")).thenReturn(idAttribute);
    when(personLastUpdatedEntityType.getAttribute("lastUpdate")).thenReturn(lastUpdateAttr);
    when(personLastUpdatedEntityType.getAtomicAttributes())
        .thenReturn(newArrayList(idAttribute, lastUpdateAttr));

    Attribute longAttr = when(mock(Attribute.class).getName()).thenReturn("long").getMock();
    when(longAttr.getDataType()).thenReturn(LONG);
    personLongEntityType = when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(personLongEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(personLongEntityType.getAttribute("id")).thenReturn(idAttribute);
    when(personLongEntityType.getAttribute("long")).thenReturn(longAttr);
    when(personLongEntityType.getAtomicAttributes())
        .thenReturn(newArrayList(idAttribute, longAttr));

    jsMagmaScriptEvaluator = new JsMagmaScriptEvaluator(new GraalScriptEngine());
  }

  @SuppressWarnings("UnnecessaryBoxing")
  @Test
  void testValueForDateTime() {
    Entity person = new DynamicEntity(personLastUpdatedEntityType);
    Instant lastUpdate = Instant.now();
    person.set("lastUpdate", lastUpdate);

    Object result = jsMagmaScriptEvaluator.eval("$('lastUpdate').value()", person, 1);
    assertEquals(valueOf(lastUpdate.toEpochMilli()), result);
  }

  @Test
  void testValueForBool() {
    Entity person = new DynamicEntity(personSmokingEntityType);
    person.set("smoking", true);

    Object result = jsMagmaScriptEvaluator.eval("$('smoking').value()", person, 1);
    assertEquals(true, result);
  }

  @Test
  void testIdentifierRegex() {
    Entity person = new DynamicEntity(personSmokingEntityType);
    person.set("id", "0-bbmri-EriC999");
    String expression = format("$('id').matches(%s).value()", UNIFIED_IDENTIFIER_REGEX_JS);

    assertEquals(true, jsMagmaScriptEvaluator.eval(expression, person, 1));
  }

  @SuppressWarnings("UnnecessaryBoxing")
  @Test
  void testValueForLong() {
    Entity person = new DynamicEntity(personLongEntityType);
    person.set("long", Long.MAX_VALUE);

    Object result = jsMagmaScriptEvaluator.eval("$('long').value()", person, 1);
    assertEquals(valueOf(MAX_VALUE), result);
  }

  @Test
  void testValueForXrefDefaultDepth() {
    Entity gender = new DynamicEntity(genderEntityType);
    gender.set("id", "1");
    gender.set("label", "male");

    Entity person = new DynamicEntity(personGenderEntityType);
    person.set("gender", gender);

    Object result = jsMagmaScriptEvaluator.eval("$('gender').attr('label').value()", person);
    assertNull(result);
  }

  @Test
  void testValueForNestedXrefDefaultDepth() {
    Entity gender = new DynamicEntity(genderEntityType);
    gender.set("id", "1");
    gender.set("label", "male");

    Entity person = new DynamicEntity(personGenderEntityType);
    person.set("gender", gender);

    Object scriptExceptionObj =
        jsMagmaScriptEvaluator.eval("$('gender').attr('xref').attr('label').value()", person);
    assertEquals(
        "org.molgenis.script.core.ScriptException: TypeError: Cannot read property \"label\" from undefined",
        scriptExceptionObj.toString());
  }

  @Test
  void testValueForMref() {
    Entity trait = new DynamicEntity(traitEntityType);
    trait.set("id", "1");
    trait.set("name", "Hello");

    Entity person = new DynamicEntity(personTraitEntityType);
    person.set("id", "1");
    person.set("trait", singletonList(trait));

    Object result = jsMagmaScriptEvaluator.eval("$('trait').value()", person, 3);
    assertEquals(singletonList("1"), result);
  }

  @Test
  void testValueForMrefWithComplexAlgorithm() {
    Entity trait = new DynamicEntity(traitEntityType);
    trait.set("id", "1");
    trait.set("name", "Hello");

    Entity person = new DynamicEntity(personTraitEntityType);
    person.set("id", "1");
    person.set("trait", singletonList(trait));

    Object result =
        jsMagmaScriptEvaluator.eval(
            "var result = [];$('trait').map(function (entity) {result.push(entity.attr('name').value())});result",
            person,
            3);
    assertEquals(singletonList("Hello"), result);
  }

  @Test
  void testValueForMrefWithSimplifiedAlgorithm() {
    Entity trait = new DynamicEntity(traitEntityType);
    trait.set("id", "1");
    trait.set("name", "Hello");

    Entity person = new DynamicEntity(personTraitEntityType);
    person.set("id", "1");
    person.set("trait", singletonList(trait));

    Object result =
        jsMagmaScriptEvaluator.eval(
            "$('trait').map(function (entity) {return entity.attr('name').value()}).value()",
            person,
            3);
    assertEquals(singletonList("Hello"), result);
  }

  @Test
  void test$() {
    Entity person = new DynamicEntity(personWeightEntityType);
    person.set("weight", 82);

    Object weight = jsMagmaScriptEvaluator.eval("$('weight').value()", person, 3);
    assertEquals(82, weight);
  }

  @Test
  void testEvalDefaultDepth$() {
    Entity person = new DynamicEntity(personWeightEntityType);
    person.set("weight", 82);

    Object weight = jsMagmaScriptEvaluator.eval("$('weight').value()", person);
    assertEquals(82, weight);
  }

  @Test
  void testUnitConversion() {
    Entity person = new DynamicEntity(personWeightEntityType);
    person.set("weight", 82);

    Object weight =
        jsMagmaScriptEvaluator.eval(
            "$('weight').unit('kg').toUnit('poundmass').value()", person, 3);
    assertEquals(180.7790549915996, weight);
  }

  @Test
  void testAttrValue() {
    Entity gender = new DynamicEntity(genderEntityType);
    gender.set("id", "1");
    gender.set("label", "male");

    Entity person = new DynamicEntity(personGenderEntityType);
    person.set("gender", gender);

    Object weight = jsMagmaScriptEvaluator.eval("$('gender').attr('label').value()", person, 3);
    assertEquals("male", weight);
  }

  @Test
  void testCompareIdValue() {
    Entity gender = new DynamicEntity(genderEntityType);
    gender.set("id", "1");
    gender.set("label", "male");

    Entity person = new DynamicEntity(personGenderEntityType);
    person.set("gender", gender);

    Object weight = jsMagmaScriptEvaluator.eval("$('gender').value() === '1'", person, 3);
    assertEquals(true, weight);
  }

  @Test
  void mapSimple() {
    Entity gender = new DynamicEntity(genderEntityType);
    gender.set("id", "m");
    gender.set("label", "Male");

    Entity person = new DynamicEntity(personGenderEntityType);
    person.set("gender", gender);

    Object result = jsMagmaScriptEvaluator.eval("$('gender').map({'m':'Male'}).value()", person, 3);
    assertEquals("Male", result);
  }

  @Test
  void mapDefault() {
    Entity gender = new DynamicEntity(genderEntityType);
    gender.set("id", "f");
    gender.set("label", "Female");

    Entity person = new DynamicEntity(personGenderEntityType);
    person.set("gender", gender);

    Object result =
        jsMagmaScriptEvaluator.eval("$('gender').map({'m':'Male'}, 'Female').value()", person, 3);
    assertEquals("Female", result);
  }

  @Test
  void mapNull() {
    Object result =
        jsMagmaScriptEvaluator.eval(
            "$('gender').map({'20':'2'}, 'B2', 'B3').value()",
            new DynamicEntity(personGenderEntityType),
            3);
    assertEquals("B3", result);
  }

  @Test
  void testAverageValueOfMultipleNumericAttributes() {
    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute sbp1Attr = when(mock(Attribute.class).getName()).thenReturn("SBP_1").getMock();
    when(sbp1Attr.getDataType()).thenReturn(DECIMAL);
    Attribute sbp2Attr = when(mock(Attribute.class).getName()).thenReturn("SBP_2").getMock();
    when(sbp2Attr.getDataType()).thenReturn(DECIMAL);

    EntityType sbpPersonEntityType =
        when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(sbpPersonEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(sbpPersonEntityType.getAttribute("SBP_1")).thenReturn(sbp1Attr);
    when(sbpPersonEntityType.getAttribute("SBP_2")).thenReturn(sbp2Attr);
    when(sbpPersonEntityType.getAtomicAttributes()).thenReturn(asList(sbp1Attr, sbp2Attr));

    Entity entity0 = new DynamicEntity(sbpPersonEntityType);
    entity0.set("SBP_1", 120d);
    entity0.set("SBP_2", 124d);

    Entity entity1 = new DynamicEntity(sbpPersonEntityType);
    entity1.set("SBP_1", 120d);

    Entity entity2 = new DynamicEntity(sbpPersonEntityType);

    String script =
        "var counter = 0;\nvar SUM=newValue(0);\nif(!$('SBP_1').isNull().value()){\n\tSUM.plus($('SBP_1').value());\n\tcounter++;\n}\nif(!$('SBP_2').isNull().value()){\n\tSUM.plus($('SBP_2').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\nSUM.value();\n}\nelse{\n\tnull;\n}";
    Object result1 = jsMagmaScriptEvaluator.eval(script, entity0, 3);
    assertEquals(122, result1);

    Object result2 = jsMagmaScriptEvaluator.eval(script, entity1, 3);
    assertEquals(120, result2);

    Object result3 = jsMagmaScriptEvaluator.eval(script, entity2, 3);
    assertNull(result3);
  }

  @Test
  void testGroup() {
    Entity entity1 = new DynamicEntity(personAgeEntityType);
    entity1.set("age", 29);

    Object result1 =
        jsMagmaScriptEvaluator.eval("$('age').group([18, 35, 56]).value();", entity1, 3);
    assertEquals("18-35", result1);

    Entity entity2 = new DynamicEntity(personAgeEntityType);
    entity2.set("age", 999);

    Object result2 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([18, 35, 56], [888, 999]).value();", entity2, 3);
    assertEquals(999, result2);

    Entity entity3 = new DynamicEntity(personAgeEntityType);
    entity3.set("age", 47);

    Object result3 =
        jsMagmaScriptEvaluator.eval("$('age').group([18, 35, 56]).value();", entity3, 3);
    assertEquals("35-56", result3);
  }

  @Test
  void testGroupNull() {
    Entity entity4 = new DynamicEntity(personAgeEntityType);
    entity4.set("age", 47);

    Object result4 = jsMagmaScriptEvaluator.eval("$('age').group().value();", entity4, 3);
    assertNull(result4);

    Object result5 =
        jsMagmaScriptEvaluator.eval("$('age').group([56, 18, 35]).value();", entity4, 3);
    assertNull(result5);

    Object result6 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([56, 18, 35], null,'123456').value();", entity4, 3);
    assertEquals("123456", result6);
  }

  @Test
  void testGroupConstantValue() {
    Entity entity4 = new DynamicEntity(personAgeEntityType);
    entity4.set("age", 47);

    Object result4 =
        jsMagmaScriptEvaluator.eval(
            "var age_variable=new newValue(45);age_variable.group([18, 35, 56]).value();",
            entity4,
            3);
    assertEquals("35-56", result4);
  }

  @Test
  void combineGroupMapFunctions() {
    Entity entity1 = new DynamicEntity(personAgeEntityType);
    entity1.set("age", 29);

    Object result1 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
            entity1,
            3);
    assertEquals("1", result1);

    Entity entity2 = new DynamicEntity(personAgeEntityType);
    entity2.set("age", 17);

    Object result2 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
            entity2,
            3);
    assertEquals("0", result2);

    Entity entity3 = new DynamicEntity(personAgeEntityType);
    entity3.set("age", 40);

    Object result3 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
            entity3,
            3);
    assertEquals("2", result3);

    Entity entity4 = new DynamicEntity(personAgeEntityType);
    entity4.set("age", 70);

    Object result4 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
            entity4,
            3);
    assertEquals("3", result4);

    Entity entity5 = new DynamicEntity(personAgeEntityType);
    entity5.set("age", 999);

    Object result5 =
        jsMagmaScriptEvaluator.eval(
            "$('age').group([18, 35, 56], [999]).map({'-18':0,'18-35':1,'35-56':2,'56+':3,'999':'9'}).value();",
            entity5,
            3);
    assertEquals("9", result5);
  }

  @Test
  void combinePlusGroupMapFunctions() {
    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute food59Attr = when(mock(Attribute.class).getName()).thenReturn("FOOD59A1").getMock();
    when(food59Attr.getDataType()).thenReturn(INT);
    Attribute food60Attr = when(mock(Attribute.class).getName()).thenReturn("FOOD60A1").getMock();
    when(food60Attr.getDataType()).thenReturn(INT);
    EntityType foodPersonEntityType =
        when(mock(EntityType.class).getId()).thenReturn("person").getMock();
    when(foodPersonEntityType.getIdAttribute()).thenReturn(idAttribute);
    when(foodPersonEntityType.getAttribute("FOOD59A1")).thenReturn(food59Attr);
    when(foodPersonEntityType.getAttribute("FOOD60A1")).thenReturn(food60Attr);
    when(foodPersonEntityType.getAtomicAttributes()).thenReturn(asList(food59Attr, food60Attr));

    Entity entity0 = new DynamicEntity(foodPersonEntityType);
    entity0.set("FOOD59A1", 7);
    entity0.set("FOOD60A1", 6);

    Object result1 =
        jsMagmaScriptEvaluator.eval(
            "var SUM_WEIGHT = new newValue(0);SUM_WEIGHT.plus($('FOOD59A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.plus($('FOOD60A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.group([0,1,2,6,7]).map({\"0-1\":\"4\",\"1-2\":\"3\",\"2-6\":\"2\",\"6-7\":\"1\", \"7+\" : \"1\"},null,null).value();",
            entity0,
            3);

    assertEquals("1", result1);
  }

  @Test
  void testPlusValue() {
    Entity entity0 = new DynamicEntity(personHeightEntityType);
    entity0.set("height", 180);
    Object result = jsMagmaScriptEvaluator.eval("$('height').plus(100).value()", entity0, 3);
    assertEquals(280, result);
  }

  @Test
  void testPlusObject() {
    Entity entity0 = new DynamicEntity(personHeightEntityType);
    entity0.set("height", 180);
    Object result1 =
        jsMagmaScriptEvaluator.eval("$('height').plus(new newValue(100)).value()", entity0, 3);
    assertEquals(280, result1);
  }

  @Test
  void testPlusNullValue() {
    Entity entity0 = new DynamicEntity(personHeightEntityType);
    entity0.set("height", 180);
    Object result1 = jsMagmaScriptEvaluator.eval("$('height').plus(null).value()", entity0, 3);
    assertEquals(180, result1);
  }

  @Test
  void testTimes() {
    Entity entity0 = new DynamicEntity(personHeightEntityType);
    entity0.set("height", 2);
    Object result = jsMagmaScriptEvaluator.eval("$('height').times(100).value()", entity0, 3);
    assertEquals(200, result);
  }

  @Test
  void div() {
    Entity entity0 = new DynamicEntity(personHeightEntityType);
    entity0.set("height", 200);
    Object result = jsMagmaScriptEvaluator.eval("$('height').div(100).value()", entity0, 3);
    assertEquals(2, result);
  }

  @Test
  void pow() {
    Entity entity0 = new DynamicEntity(personHeightEntityType);
    entity0.set("height", 20);
    Object result = jsMagmaScriptEvaluator.eval("$('height').pow(2).value()", entity0, 3);
    assertEquals(400, result);
  }

  @Test
  void testBmi() {
    Entity person = new DynamicEntity(personWeightAndHeightEntityType);
    person.set("weight", 82);
    person.set("height", 189);

    Object bmi =
        jsMagmaScriptEvaluator.eval(
            "$('weight').div($('height').div(100).pow(2)).value()", person, 3);
    DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
    assertEquals(df.format(82.0 / (1.89 * 1.89)), df.format(bmi));
  }

  @Test
  void testGlucose() {
    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute gluc1Attr = when(mock(Attribute.class).getName()).thenReturn("GLUC_1").getMock();
    when(gluc1Attr.getDataType()).thenReturn(DECIMAL);
    EntityType personGlucoseMeta =
        when(mock(EntityType.class).getId()).thenReturn("glucose").getMock();
    when(personGlucoseMeta.getIdAttribute()).thenReturn(idAttribute);
    when(personGlucoseMeta.getAttribute("GLUC_1")).thenReturn(gluc1Attr);
    when(personGlucoseMeta.getAtomicAttributes()).thenReturn(singletonList(gluc1Attr));

    Entity glucose = new DynamicEntity(personGlucoseMeta);
    glucose.set("GLUC_1", 4.1);

    Object bmi = jsMagmaScriptEvaluator.eval("$('GLUC_1').div(100).value()", glucose, 3);
    DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
    assertEquals(df.format(4.1 / 100), df.format(bmi));
  }

  @Test
  void age() {
    Entity person = new DynamicEntity(personBirthDateMeta);
    person.set("birthdate", now().atOffset(UTC).toLocalDate());
    Object result = jsMagmaScriptEvaluator.eval("$('birthdate').age().value()", person);
    assertEquals(0, result);
  }

  @Test
  void evalList() {
    Entity person = new DynamicEntity(personWeightAndHeightEntityType);
    person.set("weight", 80);
    person.set("height", 20);

    Collection<Object> result =
        jsMagmaScriptEvaluator.eval(
            Arrays.asList("$('weight').value()", "$('height').pow(2).value()"), person);
    assertEquals(asList(80, 400), result);
  }

  @Disabled
  @Test
  void testPerformance() {
    Entity person = new DynamicEntity(personBirthDateMeta);
    person.set("birthdate", now().atOffset(UTC).toLocalDate());

    jsMagmaScriptEvaluator.eval("$('birthdate').age().value()", person);

    Stopwatch sw = Stopwatch.createStarted();
    jsMagmaScriptEvaluator.eval(Collections.nCopies(10000, "$('birthdate').age().value()"), person);
    System.out.println(sw.elapsed(TimeUnit.MILLISECONDS) + " millis passed evalList");

    sw.reset().start();

    jsMagmaScriptEvaluator.withinMagmaScriptContext(
        context -> {
          for (int i = 0; i < 10000; i++) {
            context.eval(
                List.of("$('birthdate').age().value()"),
                person,
                ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH);
          }
          System.out.println(
              sw.elapsed(TimeUnit.MILLISECONDS)
                  + " millis passed recreating bindings for each evaluation");
          return (Void) null;
        });
  }

  @Test
  void testNull() {
    Entity person0 = new DynamicEntity(personBirthDateMeta);
    person0.set("birthdate", LocalDate.now());

    String script = "$('birthdate').age().value() < 18  || $('birthdate').value() != null";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(true, result);

    Entity person1 = new DynamicEntity(personBirthDateMeta);
    person1.set("birthdate", null);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(false, result);
  }

  @Test
  void testEq() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", 100);
    String script = "$('weight').eq(100).value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(true, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(false, result);
  }

  @Test
  void testIsValidJson() {
    Entity person = new DynamicEntity(personWeightEntityType);
    Collection<Object> result =
        jsMagmaScriptEvaluator.eval(
            Arrays.asList(
                "newValue('{\"foo\":3}').isValidJson().value()",
                "newValue('{foo:3}').isValidJson().value()"),
            person);
    assertEquals(asList(true, false), result);
  }

  @Test
  void testIsNull() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').isNull().value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(true, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(false, result);
  }

  @Test
  void testNot() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').isNull().not().value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(false, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(true, result);
  }

  @Test
  void testOr() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').eq(99).or($('weight').eq(100)).value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(false, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(true, result);

    Entity person2 = new DynamicEntity(personWeightEntityType);
    person2.set("weight", 100);
    result = jsMagmaScriptEvaluator.eval(script, person2, 3);
    assertEquals(true, result);

    Entity person3 = new DynamicEntity(personWeightEntityType);
    person3.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person3, 3);
    assertEquals(true, result);
  }

  @Test
  void testGt() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').gt(100).value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(false, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(false, result);

    Entity person2 = new DynamicEntity(personWeightEntityType);
    person2.set("weight", 100);
    result = jsMagmaScriptEvaluator.eval(script, person2, 3);
    assertEquals(false, result);

    Entity person3 = new DynamicEntity(personWeightEntityType);
    person3.set("weight", 101);
    result = jsMagmaScriptEvaluator.eval(script, person3, 3);
    assertEquals(true, result);
  }

  @Test
  void testLt() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').lt(100).value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(false, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(true, result);

    Entity person2 = new DynamicEntity(personWeightEntityType);
    person2.set("weight", 100);
    result = jsMagmaScriptEvaluator.eval(script, person2, 3);
    assertEquals(false, result);

    Entity person3 = new DynamicEntity(personWeightEntityType);
    person3.set("weight", 101);
    result = jsMagmaScriptEvaluator.eval(script, person3, 3);
    assertEquals(false, result);
  }

  @Test
  void testGe() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').ge(100).value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(false, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(false, result);

    Entity person2 = new DynamicEntity(personWeightEntityType);
    person2.set("weight", 100);
    result = jsMagmaScriptEvaluator.eval(script, person2, 3);
    assertEquals(true, result);

    Entity person3 = new DynamicEntity(personWeightEntityType);
    person3.set("weight", 101);
    result = jsMagmaScriptEvaluator.eval(script, person3, 3);
    assertEquals(true, result);
  }

  @Test
  void testLe() {
    Entity person0 = new DynamicEntity(personWeightEntityType);
    person0.set("weight", null);
    String script = "$('weight').le(100).value()";

    Object result = jsMagmaScriptEvaluator.eval(script, person0, 3);
    assertEquals(false, result);

    Entity person1 = new DynamicEntity(personWeightEntityType);
    person1.set("weight", 99);
    result = jsMagmaScriptEvaluator.eval(script, person1, 3);
    assertEquals(true, result);

    Entity person2 = new DynamicEntity(personWeightEntityType);
    person2.set("weight", 100);
    result = jsMagmaScriptEvaluator.eval(script, person2, 3);
    assertEquals(true, result);

    Entity person3 = new DynamicEntity(personWeightEntityType);
    person3.set("weight", 101);
    result = jsMagmaScriptEvaluator.eval(script, person3, 3);
    assertEquals(false, result);
  }
}
