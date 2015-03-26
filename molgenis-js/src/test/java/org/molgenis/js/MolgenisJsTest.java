package org.molgenis.js;

import static org.testng.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import org.molgenis.data.Entity;
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
		Entity person = new MapEntity();
		person.set("weight", 82);

		Object weight = ScriptEvaluator.eval("$('weight').value()", person);
		assertEquals(weight, 82);
	}

	@Test
	public void map()
	{
		Object result = ScriptEvaluator.eval("$('gender').map({'2':'20','B2':'B'}).value()", new MapEntity("gender",
				'B'));
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void div()
	{
		Object result = ScriptEvaluator.eval("$('height').div(100).value()", new MapEntity("height", 200));
		assertEquals(result, 2d);
	}

	@Test
	public void pow()
	{
		Object result = ScriptEvaluator.eval("$('height').pow(2).value()", new MapEntity("height", 20));
		assertEquals(result, 400d);
	}

	@Test
	public void testBmi()
	{
		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		Object bmi = ScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()", person);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(82.0 / (1.89 * 1.89)));
	}

	@Test
	public void testGlucose()
	{
		Entity glucose = new MapEntity();
		glucose.set("GLUC_1", 4.1);

		Object bmi = ScriptEvaluator.eval("$('GLUC_1').div(100).value()", glucose);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(4.1 / 100));
	}

	@Test
	public void age()
	{
		Object result = ScriptEvaluator.eval("$('birthdate').age().value()", new MapEntity("birthdate", new Date()));
		assertEquals(result, 0d);
	}
}
