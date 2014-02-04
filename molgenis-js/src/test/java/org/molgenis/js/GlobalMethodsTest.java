package org.molgenis.js;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.mozilla.javascript.Context;
import org.testng.annotations.Test;

public class GlobalMethodsTest extends MolgenisJsTest
{
	@Test
	public void test$()
	{
		Entity person = new MapEntity();
		person.set("weight", 82);

		Object weight = ScriptEvaluator.eval("$('weight')", person);
		assertEquals((int) Context.toNumber(weight), 82);
	}
}
