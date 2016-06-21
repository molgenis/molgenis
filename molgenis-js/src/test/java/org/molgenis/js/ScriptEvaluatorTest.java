package org.molgenis.js;

import static freemarker.template.utility.Collections12.singletonList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

public class ScriptEvaluatorTest
{

	private static EntityMetaData personWeightEntityMeta;
	private static EntityMetaData personHeightEntityMeta;
	private static EntityMetaData personWeightAndHeightEntityMeta;
	private static EntityMetaData personBirthDateMeta;
	private static EntityMetaData personAgeEntityMeta;
	private static EntityMetaData genderEntityMeta;
	private static EntityMetaData personGenderEntityMeta;

	@BeforeClass
	private static void beforeClass()
	{
		AttributeMetaData weightAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("weight").getMock();
		when(weightAttr.getDataType()).thenReturn(INT);
		personWeightEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personWeightEntityMeta.getAttribute("weight")).thenReturn(weightAttr);
		when(personWeightEntityMeta.getAtomicAttributes()).thenReturn(singletonList(weightAttr));

		AttributeMetaData heightAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("height").getMock();
		when(heightAttr.getDataType()).thenReturn(INT);
		personHeightEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personHeightEntityMeta.getAttribute("height")).thenReturn(heightAttr);
		when(personHeightEntityMeta.getAtomicAttributes()).thenReturn(singletonList(heightAttr));

		personWeightAndHeightEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personWeightAndHeightEntityMeta.getAttribute("weight")).thenReturn(weightAttr);
		when(personWeightAndHeightEntityMeta.getAttribute("height")).thenReturn(heightAttr);
		when(personWeightAndHeightEntityMeta.getAtomicAttributes()).thenReturn(asList(weightAttr, heightAttr));

		AttributeMetaData birtDateAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("birthdate")
				.getMock();
		when(birtDateAttr.getDataType()).thenReturn(DATE);
		personBirthDateMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personBirthDateMeta.getAttribute("birthdate")).thenReturn(birtDateAttr);
		when(personBirthDateMeta.getAtomicAttributes()).thenReturn(singletonList(birtDateAttr));

		AttributeMetaData ageAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("age").getMock();
		when(ageAttr.getDataType()).thenReturn(INT);
		personAgeEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personAgeEntityMeta.getAttribute("age")).thenReturn(ageAttr);
		when(personAgeEntityMeta.getAtomicAttributes()).thenReturn(singletonList(ageAttr));

		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		genderEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("gender").getMock();
		when(genderEntityMeta.getIdAttribute()).thenReturn(idAttr);
		when(genderEntityMeta.getAttribute("id")).thenReturn(idAttr);
		when(genderEntityMeta.getAtomicAttributes()).thenReturn(singletonList(idAttr));

		AttributeMetaData genderAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("gender").getMock();
		when(genderAttr.getDataType()).thenReturn(CATEGORICAL);
		personGenderEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(personGenderEntityMeta.getAttribute("gender")).thenReturn(genderAttr);
		when(personGenderEntityMeta.getAtomicAttributes()).thenReturn(singletonList(genderAttr));
	}

	@BeforeMethod
	public void beforeMethod()
	{
		new RhinoConfig().init();
	}

	@Test
	public void test$()
	{
		Entity person = new DynamicEntity(personWeightEntityMeta);
		person.set("weight", 82);

		Object weight = ScriptEvaluator.eval("$('weight').value()", person, personWeightEntityMeta);
		assertEquals(weight, 82);
	}

	@Test
	public void testUnitConversion()
	{
		Entity person = new DynamicEntity(personWeightEntityMeta);
		person.set("weight", 82);

		Object weight = ScriptEvaluator
				.eval("$('weight').unit('kg').toUnit('poundmass').value()", person, personWeightEntityMeta);
		assertEquals(weight, 180.7790549915996);
	}

	@Test
	public void mapSimple()
	{
		Entity gender = new DynamicEntity(genderEntityMeta);
		gender.set("id", "B");

		Entity person = new DynamicEntity(personGenderEntityMeta);
		person.set("gender", gender);

		Object result = ScriptEvaluator
				.eval("$('gender').map({'20':'2','B':'B2'}).value()", person, personGenderEntityMeta);
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void mapDefault()
	{
		Entity gender = new DynamicEntity(genderEntityMeta);
		gender.set("id", "B");

		Entity person = new DynamicEntity(personGenderEntityMeta);
		person.set("gender", gender);

		Object result = ScriptEvaluator
				.eval("$('gender').map({'20':'2'}, 'B2').value()", person, personGenderEntityMeta);
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void mapNull()
	{
		Object result = ScriptEvaluator
				.eval("$('gender').map({'20':'2'}, 'B2', 'B3').value()", new DynamicEntity(personGenderEntityMeta),
						personGenderEntityMeta);
		assertEquals(result.toString(), "B3");
	}

	@Test
	public void testAverageValueOfMultipleNumericAttributes()
	{
		AttributeMetaData sbp1Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("SBP_1").getMock();
		when(sbp1Attr.getDataType()).thenReturn(DECIMAL);
		AttributeMetaData sbp2Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("SBP_2").getMock();
		when(sbp2Attr.getDataType()).thenReturn(DECIMAL);
		EntityMetaData sbpPersonEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(sbpPersonEntityMeta.getAttribute("SBP_1")).thenReturn(sbp1Attr);
		when(sbpPersonEntityMeta.getAttribute("SBP_2")).thenReturn(sbp2Attr);
		when(sbpPersonEntityMeta.getAtomicAttributes()).thenReturn(asList(sbp1Attr, sbp2Attr));

		Entity entity0 = new DynamicEntity(sbpPersonEntityMeta);
		entity0.set("SBP_1", 120d);
		entity0.set("SBP_2", 124d);

		Entity entity1 = new DynamicEntity(sbpPersonEntityMeta);
		entity1.set("SBP_1", 120d);

		Entity entity2 = new DynamicEntity(sbpPersonEntityMeta);

		String script = "var counter = 0;\nvar SUM=newValue(0);\nif(!$('SBP_1').isNull().value()){\n\tSUM.plus($('SBP_1').value());\n\tcounter++;\n}\nif(!$('SBP_2').isNull().value()){\n\tSUM.plus($('SBP_2').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\nSUM.value();\n}\nelse{\n\tnull;\n}";
		Object result1 = ScriptEvaluator.eval(script, entity0, sbpPersonEntityMeta);
		assertEquals(result1.toString(), "122.0");

		Object result2 = ScriptEvaluator.eval(script, entity1, sbpPersonEntityMeta);
		assertEquals(result2.toString(), "120.0");

		Object result3 = ScriptEvaluator.eval(script, entity2, sbpPersonEntityMeta);
		assertEquals(result3, null);
	}

	@Test
	public void testGroup()
	{
		Entity entity1 = new DynamicEntity(personAgeEntityMeta);
		entity1.set("age", 29);

		Object result1 = ScriptEvaluator.eval("$('age').group([18, 35, 56]).value();", entity1, personAgeEntityMeta);
		assertEquals(result1.toString(), "18-35");

		Entity entity2 = new DynamicEntity(personAgeEntityMeta);
		entity2.set("age", 999);

		Object result2 = ScriptEvaluator
				.eval("$('age').group([18, 35, 56], [888, 999]).value();", entity2, personAgeEntityMeta);
		assertEquals(result2.toString(), "999");

		Entity entity3 = new DynamicEntity(personAgeEntityMeta);
		entity3.set("age", 47);

		Object result3 = ScriptEvaluator.eval("$('age').group([18, 35, 56]).value();", entity3, personAgeEntityMeta);
		assertEquals(result3.toString(), "35-56");
	}

	@Test
	public void testGroupNull()
	{
		Entity entity4 = new DynamicEntity(personAgeEntityMeta);
		entity4.set("age", 47);

		Object result4 = ScriptEvaluator.eval("$('age').group().value();", entity4, personAgeEntityMeta);
		assertEquals(result4, null);

		Object result5 = ScriptEvaluator.eval("$('age').group([56, 18, 35]).value();", entity4, personAgeEntityMeta);
		assertEquals(result5, null);

		Object result6 = ScriptEvaluator
				.eval("$('age').group([56, 18, 35], null,'123456').value();", entity4, personAgeEntityMeta);
		assertEquals(result6.toString(), "123456");
	}

	@Test
	public void testGroupConstantValue()
	{
		Entity entity4 = new DynamicEntity(personAgeEntityMeta);
		entity4.set("age", 47);

		Object result4 = ScriptEvaluator
				.eval("var age_variable=new newValue(45);age_variable.group([18, 35, 56]).value();", entity4,
						personAgeEntityMeta);
		assertEquals(result4.toString(), "35-56");
	}

	@Test
	public void combineGroupMapFunctions()
	{
		Entity entity1 = new DynamicEntity(personAgeEntityMeta);
		entity1.set("age", 29);

		Object result1 = ScriptEvaluator
				.eval("$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
						entity1, personAgeEntityMeta);
		assertEquals(result1.toString(), "1");

		Entity entity2 = new DynamicEntity(personAgeEntityMeta);
		entity2.set("age", 17);

		Object result2 = ScriptEvaluator
				.eval("$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
						entity2, personAgeEntityMeta);
		assertEquals(result2.toString(), "0");

		Entity entity3 = new DynamicEntity(personAgeEntityMeta);
		entity3.set("age", 40);

		Object result3 = ScriptEvaluator
				.eval("$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
						entity3, personAgeEntityMeta);
		assertEquals(result3.toString(), "2");

		Entity entity4 = new DynamicEntity(personAgeEntityMeta);
		entity4.set("age", 70);

		Object result4 = ScriptEvaluator
				.eval("$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
						entity4, personAgeEntityMeta);
		assertEquals(result4.toString(), "3");

		Entity entity5 = new DynamicEntity(personAgeEntityMeta);
		entity5.set("age", 999);

		Object result5 = ScriptEvaluator
				.eval("$('age').group([18, 35, 56], [999]).map({'-18':0,'18-35':1,'35-56':2,'56+':3,'999':'9'}).value();",
						entity5, personAgeEntityMeta);
		assertEquals(result5.toString(), "9");
	}

	@Test
	public void combinePlusGroupMapFunctions()
	{
		AttributeMetaData food59Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("FOOD59A1").getMock();
		when(food59Attr.getDataType()).thenReturn(INT);
		AttributeMetaData food60Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("FOOD60A1").getMock();
		when(food60Attr.getDataType()).thenReturn(INT);
		EntityMetaData foodPersonEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("person").getMock();
		when(foodPersonEntityMeta.getAttribute("FOOD59A1")).thenReturn(food59Attr);
		when(foodPersonEntityMeta.getAttribute("FOOD60A1")).thenReturn(food60Attr);
		when(foodPersonEntityMeta.getAtomicAttributes()).thenReturn(asList(food59Attr, food60Attr));

		Entity entity0 = new DynamicEntity(foodPersonEntityMeta);
		entity0.set("FOOD59A1", 7);
		entity0.set("FOOD60A1", 6);

		Object result1 = ScriptEvaluator
				.eval("var SUM_WEIGHT = new newValue(0);SUM_WEIGHT.plus($('FOOD59A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.plus($('FOOD60A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.group([0,1,2,6,7]).map({\"0-1\":\"4\",\"1-2\":\"3\",\"2-6\":\"2\",\"6-7\":\"1\", \"7+\" : \"1\"},null,null).value();",
						entity0, foodPersonEntityMeta);

		assertEquals(result1.toString(), "1");
	}

	@Test
	public void testPlusValue()
	{
		Entity entity0 = new DynamicEntity(personHeightEntityMeta);
		entity0.set("height", 180);
		Object result = ScriptEvaluator.eval("$('height').plus(100).value()", entity0, personHeightEntityMeta);
		assertEquals(result, (double) 280);
	}

	@Test
	public void testPlusObject()
	{
		Entity entity0 = new DynamicEntity(personHeightEntityMeta);
		entity0.set("height", 180);
		Object result1 = ScriptEvaluator
				.eval("$('height').plus(new newValue(100)).value()", entity0, personHeightEntityMeta);
		assertEquals(result1, (double) 280);
	}

	@Test
	public void testPlusNullValue()
	{
		Entity entity0 = new DynamicEntity(personHeightEntityMeta);
		entity0.set("height", 180);
		Object result1 = ScriptEvaluator.eval("$('height').plus(null).value()", entity0, personHeightEntityMeta);
		assertEquals(result1, 180);
	}

	@Test
	public void testTimes()
	{
		Entity entity0 = new DynamicEntity(personHeightEntityMeta);
		entity0.set("height", 2);
		Object result = ScriptEvaluator.eval("$('height').times(100).value()", entity0, personHeightEntityMeta);
		assertEquals(result, (double) 200);
	}

	@Test
	public void div()
	{
		Entity entity0 = new DynamicEntity(personHeightEntityMeta);
		entity0.set("height", 200);
		Object result = ScriptEvaluator.eval("$('height').div(100).value()", entity0, personHeightEntityMeta);
		assertEquals(result, 2d);
	}

	@Test
	public void pow()
	{
		Entity entity0 = new DynamicEntity(personHeightEntityMeta);
		entity0.set("height", 20);
		Object result = ScriptEvaluator.eval("$('height').pow(2).value()", entity0, personHeightEntityMeta);
		assertEquals(result, 400d);
	}

	@Test
	public void testBmi()
	{
		Entity person = new DynamicEntity(personWeightAndHeightEntityMeta);
		person.set("weight", 82);
		person.set("height", 189);

		Object bmi = ScriptEvaluator
				.eval("$('weight').div($('height').div(100).pow(2)).value()", person, personWeightAndHeightEntityMeta);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(82.0 / (1.89 * 1.89)));
	}

	@Test
	public void testGlucose()
	{
		AttributeMetaData gluc1Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("GLUC_1").getMock();
		when(gluc1Attr.getDataType()).thenReturn(DECIMAL);
		EntityMetaData personGlucoseMeta = when(mock(EntityMetaData.class).getName()).thenReturn("glucose").getMock();
		when(personGlucoseMeta.getAttribute("GLUC_1")).thenReturn(gluc1Attr);
		when(personGlucoseMeta.getAtomicAttributes()).thenReturn(singletonList(gluc1Attr));

		Entity glucose = new DynamicEntity(personGlucoseMeta);
		glucose.set("GLUC_1", 4.1);

		Object bmi = ScriptEvaluator.eval("$('GLUC_1').div(100).value()", glucose, personGlucoseMeta);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(4.1 / 100));
	}

	@Test
	public void age()
	{
		Entity person = new DynamicEntity(personBirthDateMeta);
		person.set("birthdate", new Date());

		Object result = ScriptEvaluator.eval("$('birthdate').age().value()", person, personBirthDateMeta);
		assertEquals(result, 0d);
	}

	@Test
	public void testNull()
	{
		Entity person0 = new DynamicEntity(personBirthDateMeta);
		person0.set("birthdate", new Date());

		String script = "$('birthdate').age().value() < 18  || $('birthdate').value() != null";

		Object result = ScriptEvaluator.eval(script, person0, personBirthDateMeta);
		assertEquals(result, true);

		Entity person1 = new DynamicEntity(personBirthDateMeta);
		person1.set("birthdate", null);
		result = ScriptEvaluator.eval(script, person1, personBirthDateMeta);
		assertEquals(result, false);
	}

	@Test
	public void testEq()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", 100);
		String script = "$('weight').eq(100).value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, false);
	}

	@Test
	public void testIsNull()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').isNull().value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, false);
	}

	@Test
	public void testNot()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').isNull().not().value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, true);
	}

	@Test
	public void testOr()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').eq(99).or($('weight').eq(100)).value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person2 = new DynamicEntity(personWeightEntityMeta);
		person2.set("weight", 100);
		result = ScriptEvaluator.eval(script, person2, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person3 = new DynamicEntity(personWeightEntityMeta);
		person3.set("weight", 99);
		result = ScriptEvaluator.eval(script, person3, personWeightEntityMeta);
		assertEquals(result, true);
	}

	@Test
	public void testGt()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').gt(100).value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person2 = new DynamicEntity(personWeightEntityMeta);
		person2.set("weight", 100);
		result = ScriptEvaluator.eval(script, person2, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person3 = new DynamicEntity(personWeightEntityMeta);
		person3.set("weight", 101);
		result = ScriptEvaluator.eval(script, person3, personWeightEntityMeta);
		assertEquals(result, true);
	}

	@Test
	public void testLt()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').lt(100).value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person2 = new DynamicEntity(personWeightEntityMeta);
		person2.set("weight", 100);
		result = ScriptEvaluator.eval(script, person2, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person3 = new DynamicEntity(personWeightEntityMeta);
		person3.set("weight", 101);
		result = ScriptEvaluator.eval(script, person3, personWeightEntityMeta);
		assertEquals(result, false);
	}

	@Test
	public void testGe()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').ge(100).value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person2 = new DynamicEntity(personWeightEntityMeta);
		person2.set("weight", 100);
		result = ScriptEvaluator.eval(script, person2, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person3 = new DynamicEntity(personWeightEntityMeta);
		person3.set("weight", 101);
		result = ScriptEvaluator.eval(script, person3, personWeightEntityMeta);
		assertEquals(result, true);
	}

	@Test
	public void testLe()
	{
		Entity person0 = new DynamicEntity(personWeightEntityMeta);
		person0.set("weight", null);
		String script = "$('weight').le(100).value()";

		Object result = ScriptEvaluator.eval(script, person0, personWeightEntityMeta);
		assertEquals(result, false);

		Entity person1 = new DynamicEntity(personWeightEntityMeta);
		person1.set("weight", 99);
		result = ScriptEvaluator.eval(script, person1, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person2 = new DynamicEntity(personWeightEntityMeta);
		person2.set("weight", 100);
		result = ScriptEvaluator.eval(script, person2, personWeightEntityMeta);
		assertEquals(result, true);

		Entity person3 = new DynamicEntity(personWeightEntityMeta);
		person3.set("weight", 101);
		result = ScriptEvaluator.eval(script, person3, personWeightEntityMeta);
		assertEquals(result, false);
	}

	@Test(enabled = false)
	public void testBatchPerformance()
	{
		Entity person = new DynamicEntity(personWeightAndHeightEntityMeta);
		person.set("weight", 82);
		person.set("height", 189);

		Stopwatch sw = Stopwatch.createStarted();

		Object bmi = ScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()",
				FluentIterable.from(Iterables.cycle(person)).limit(1000), personWeightAndHeightEntityMeta);
		sw.stop();
		Assert.assertTrue(sw.elapsed(TimeUnit.MILLISECONDS) < 1200);
		assertEquals(bmi, Collections.nCopies(1000, 82.0 / (1.89 * 1.89)));
	}

	@Test
	public void testBatchErrors()
	{
		Entity person = new DynamicEntity(personWeightAndHeightEntityMeta);
		person.set("weight", 82);
		person.set("height", 189);

		List<Object> bmis = ScriptEvaluator
				.eval("$('weight').div($('height').div(100).pow(2)).value()", Arrays.asList(person, null, person),
						personWeightAndHeightEntityMeta);
		assertEquals(bmis.get(0), 82.0 / (1.89 * 1.89));
		assertEquals(NullPointerException.class, bmis.get(1).getClass());
		assertEquals(bmis.get(2), 82.0 / (1.89 * 1.89));
	}

	@Test
	public void testBatchSyntaxError()
	{
		Entity person = new DynamicEntity(personWeightAndHeightEntityMeta);
		person.set("weight", 82);
		person.set("height", 189);

		try
		{
			ScriptEvaluator.eval("$('weight'))", Arrays.asList(person, person), personWeightAndHeightEntityMeta);
			Assert.fail("Syntax errors should throw exception");
		}
		catch (EcmaError expected)
		{
			assertEquals(expected.getName(), "SyntaxError");
			assertEquals(expected.getErrorMessage(), "missing ; before statement");
		}
	}
}
