package org.molgenis.js;

import static org.testng.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.mozilla.javascript.EcmaError;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class MolgenisJsTest
{

	@BeforeMethod
	public void beforeMethod()
	{
		new RhinoConfig().init();
	}

	@Test
	public void test$()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);

		Object weight = ScriptEvaluator.eval("$('weight').value()", person, emd);
		assertEquals(weight, 82);
	}

	@Test
	public void testUnitConversion()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);

		Object weight = ScriptEvaluator.eval("$('weight').unit('kg').toUnit('poundmass').value()", person, emd);
		assertEquals(weight, 180.7790549915996);
	}

	@Test
	public void mapSimple()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = ScriptEvaluator.eval("$('gender').map({'20':'2','B':'B2'}).value()",
				new MapEntity("gender", 'B'), emd);
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void mapDefault()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = ScriptEvaluator.eval("$('gender').map({'20':'2'}, 'B2').value()", new MapEntity("gender", 'B'),
				emd);
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void mapNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = ScriptEvaluator.eval("$('gender').map({'20':'2'}, 'B2', 'B3').value()", new MapEntity(), emd);
		assertEquals(result.toString(), "B3");
	}

	@Test
	public void testAverageValueOfMultipleNumericAttributes()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("SBP_1").setDataType(MolgenisFieldTypes.DECIMAL);
		emd.addAttribute("SBP_2").setDataType(MolgenisFieldTypes.DECIMAL);
		String script = "var counter = 0;\nvar SUM=newValue(0);\nif(!$('SBP_1').isNull().value()){\n\tSUM.plus($('SBP_1').value());\n\tcounter++;\n}\nif(!$('SBP_2').isNull().value()){\n\tSUM.plus($('SBP_2').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\nSUM.value();\n}\nelse{\n\tnull;\n}";
		Object result1 = ScriptEvaluator.eval(script, new MapEntity(ImmutableMap.of("SBP_1", 120, "SBP_2", 124)), emd);
		assertEquals(result1.toString(), "122.0");

		Object result2 = ScriptEvaluator.eval(script, new MapEntity(ImmutableMap.of("SBP_1", 120)), emd);
		assertEquals(result2.toString(), "120.0");

		Object result3 = ScriptEvaluator.eval(script, new MapEntity(), emd);
		assertEquals(result3, null);
	}

	@Test
	public void testGroup()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("age").setDataType(MolgenisFieldTypes.INT);

		Object result1 = ScriptEvaluator.eval("$('age').group([18, 35, 56]).value();", new MapEntity("age", 29), emd);
		assertEquals(result1.toString(), "18-35");

		Object result2 = ScriptEvaluator.eval("$('age').group([18, 35, 56], [888, 999]).value();",
				new MapEntity("age", 999), emd);
		assertEquals(result2.toString(), "999");

		Object result3 = ScriptEvaluator.eval("$('age').group([18, 35, 56]).value();", new MapEntity("age", 47), emd);
		assertEquals(result3.toString(), "35-56");
	}

	@Test
	public void testGroupNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("age").setDataType(MolgenisFieldTypes.INT);

		Object result4 = ScriptEvaluator.eval("$('age').group().value();", new MapEntity("age", 47), emd);
		assertEquals(result4, null);

		Object result5 = ScriptEvaluator.eval("$('age').group([56, 18, 35]).value();", new MapEntity("age", 47), emd);
		assertEquals(result5, null);

		Object result6 = ScriptEvaluator.eval("$('age').group([56, 18, 35], null,'123456').value();",
				new MapEntity("age", 47), emd);
		assertEquals(result6.toString(), "123456");
	}

	@Test
	public void testGroupConstantValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("age").setDataType(MolgenisFieldTypes.INT);
		Object result4 = ScriptEvaluator.eval(
				"var age_variable=new newValue(45);age_variable.group([18, 35, 56]).value();", new MapEntity("age", 47),
				emd);
		assertEquals(result4.toString(), "35-56");
	}

	@Test
	public void combineGroupMapFunctions()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("age").setDataType(MolgenisFieldTypes.INT);

		Object result1 = ScriptEvaluator.eval(
				"$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
				new MapEntity("age", 29), emd);
		assertEquals(result1.toString(), "1");

		Object result2 = ScriptEvaluator.eval(
				"$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
				new MapEntity("age", 17), emd);
		assertEquals(result2.toString(), "0");

		Object result3 = ScriptEvaluator.eval(
				"$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
				new MapEntity("age", 40), emd);
		assertEquals(result3.toString(), "2");

		Object result4 = ScriptEvaluator.eval(
				"$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();",
				new MapEntity("age", 70), emd);
		assertEquals(result4.toString(), "3");

		Object result5 = ScriptEvaluator.eval(
				"$('age').group([18, 35, 56], [999]).map({'-18':0,'18-35':1,'35-56':2,'56+':3,'999':'9'}).value();",
				new MapEntity("age", 999), emd);
		assertEquals(result5.toString(), "9");
	}

	@Test
	public void combinePlusGroupMapFunctions()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("FOOD59A1").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("FOOD60A1").setDataType(MolgenisFieldTypes.INT);
		Object result1 = ScriptEvaluator.eval(
				"var SUM_WEIGHT = new newValue(0);SUM_WEIGHT.plus($('FOOD59A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.plus($('FOOD60A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.group([0,1,2,6,7]).map({\"0-1\":\"4\",\"1-2\":\"3\",\"2-6\":\"2\",\"6-7\":\"1\", \"7+\" : \"1\"},null,null).value();",
				new MapEntity(ImmutableMap.of("FOOD59A1", 7, "FOOD60A1", 6)), emd);

		assertEquals(result1.toString(), "1");
	}

	@Test
	public void testPlusValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		Object result = ScriptEvaluator.eval("$('height').plus(100).value()", new MapEntity("height", 180), emd);
		assertEquals(result, (double) 280);
	}

	@Test
	public void testPlusObject()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		Object result1 = ScriptEvaluator.eval("$('height').plus(new newValue(100)).value()",
				new MapEntity("height", 180), emd);
		assertEquals(result1, (double) 280);
	}

	@Test
	public void testPlusNullValue()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		Object result1 = ScriptEvaluator.eval("$('height').plus(null).value()", new MapEntity("height", 180), emd);
		assertEquals(result1, 180);
	}

	@Test
	public void testTimes()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		Object result = ScriptEvaluator.eval("$('height').times(100).value()", new MapEntity("height", 1.8), emd);
		assertEquals(result, (double) 180);
	}

	@Test
	public void div()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Object result = ScriptEvaluator.eval("$('height').div(100).value()", new MapEntity("height", 200), emd);
		assertEquals(result, 2d);
	}

	@Test
	public void pow()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Object result = ScriptEvaluator.eval("$('height').pow(2).value()", new MapEntity("height", 20), emd);
		assertEquals(result, 400d);
	}

	@Test
	public void testBmi()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		Object bmi = ScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()", person, emd);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(82.0 / (1.89 * 1.89)));
	}

	@Test
	public void testGlucose()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("glucose");
		emd.addAttribute("GLUC_1").setDataType(MolgenisFieldTypes.INT);

		Entity glucose = new MapEntity();
		glucose.set("GLUC_1", 4.1);

		Object bmi = ScriptEvaluator.eval("$('GLUC_1').div(100).value()", glucose, emd);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(4.1 / 100));
	}

	@Test
	public void age()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("birthdate").setDataType(MolgenisFieldTypes.DATE);

		Object result = ScriptEvaluator.eval("$('birthdate').age().value()", new MapEntity("birthdate", new Date()),
				emd);
		assertEquals(result, 0d);
	}

	@Test
	public void testNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("birthdate").setDataType(MolgenisFieldTypes.DATE);

		String script = "$('birthdate').age().value() < 18  || $('birthdate').value() != null";

		Object result = ScriptEvaluator.eval(script, new MapEntity("birthdate", new Date()), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("birthdate", null), emd);
		assertEquals(result, false);
	}

	@Test
	public void testEq()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').eq(100).value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);
	}

	@Test
	public void testIsNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').isNull().value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);
	}

	@Test
	public void testNot()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').isNull().not().value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);
	}

	@Test
	public void testOr()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').eq(99).or($('weight').eq(100)).value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);
	}

	@Test
	public void testGt()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').gt(100).value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, true);
	}

	@Test
	public void testLt()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').lt(100).value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, false);
	}

	@Test
	public void testGe()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').ge(100).value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, true);
	}

	@Test
	public void testLe()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').le(100).value()";

		Object result = ScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = ScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, false);
	}

	@Test(enabled = false)
	public void testBatchPerformance()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		Stopwatch sw = Stopwatch.createStarted();

		Object bmi = ScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()",
				FluentIterable.from(Iterables.cycle(person)).limit(1000), emd);
		sw.stop();
		Assert.assertTrue(sw.elapsed(TimeUnit.MILLISECONDS) < 1200);
		assertEquals(bmi, Collections.nCopies(1000, 82.0 / (1.89 * 1.89)));
	}

	@Test
	public void testBatchErrors()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		List<Object> bmis = ScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()",
				Arrays.asList(person, null, person), emd);
		assertEquals(bmis.get(0), 82.0 / (1.89 * 1.89));
		assertEquals(NullPointerException.class, bmis.get(1).getClass());
		assertEquals(bmis.get(2), 82.0 / (1.89 * 1.89));
	}

	@Test
	public void testBatchSyntaxError()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		try
		{
			ScriptEvaluator.eval("$('weight'))", Arrays.asList(person, person), emd);
			Assert.fail("Syntax errors should throw exception");
		}
		catch (EcmaError expected)
		{
			assertEquals(expected.getName(), "SyntaxError");
			assertEquals(expected.getErrorMessage(), "missing ; before statement");
		}
	}
}
