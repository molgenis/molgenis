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
		Object result = ScriptEvaluator.eval("$('gender').map({'2':'20','B2':'B'})", new MapEntity("gender", 'B'));
		assertEquals(result.toString(), "B2");
	}
}
