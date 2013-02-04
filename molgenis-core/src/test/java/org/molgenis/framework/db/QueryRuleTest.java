package org.molgenis.framework.db;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class QueryRuleTest
{

	@Test
	public void equals()
	{

		QueryRule q1 = new QueryRule();
		QueryRule q2 = new QueryRule();

		assertTrue("The equals function is not working!", q1.equals(q2));
	}

}
