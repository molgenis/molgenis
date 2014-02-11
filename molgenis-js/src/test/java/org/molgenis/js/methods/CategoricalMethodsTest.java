package org.molgenis.js.methods;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.support.MapEntity;
import org.molgenis.js.MolgenisJsTest;
import org.molgenis.js.ScriptEvaluator;
import org.testng.annotations.Test;

public class CategoricalMethodsTest extends MolgenisJsTest
{
	@Test
	public void map()
	{
		Object result = ScriptEvaluator.eval("$('gender').map({'10':'1','20':'2'})", new MapEntity("gender", 2));
		assertEquals(Integer.parseInt(result.toString()), 20);
	}
}
