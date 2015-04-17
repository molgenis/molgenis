package org.molgenis.js;

import static org.testng.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	public void map()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = ScriptEvaluator.eval("$('gender').map({'2':'20','B2':'B'}).value()", new MapEntity("gender",
				'B'), emd);
		assertEquals(result.toString(), "B2");
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
}
