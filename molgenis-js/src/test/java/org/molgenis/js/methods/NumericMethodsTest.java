package org.molgenis.js.methods;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.MolgenisJsTest;
import org.molgenis.js.ScriptEvaluator;
import org.mozilla.javascript.Context;
import org.testng.annotations.Test;

public class NumericMethodsTest extends MolgenisJsTest
{
	@Test
	public void div()
	{
		Object result = ScriptEvaluator.eval("$('height').div(100)", new MapEntity("height", 200));
		assertEquals(Context.toNumber(result), 2d);
	}

	@Test
	public void pow()
	{
		Object result = ScriptEvaluator.eval("$('height').pow(2)", new MapEntity("height", 20));
		assertEquals((int) Context.toNumber(result), 400);
	}

	@Test
	public void testBmi()
	{
		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		Object bmi = ScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2))", person);
		assertEquals(Context.toNumber(bmi), 82.0 / (1.89 * 1.89));
		System.out.println(Context.toNumber(bmi));
	}

	@Test
	public void testGlucose()
	{
		Entity glucose = new MapEntity();
		glucose.set("GLUC_1", 4.1);

		Object bmi = ScriptEvaluator.eval("$('GLUC_1').div(100)", glucose);
		assertEquals(Context.toNumber(bmi), 4.1 / 100);
		System.out.println(Context.toNumber(bmi));
	}
}
