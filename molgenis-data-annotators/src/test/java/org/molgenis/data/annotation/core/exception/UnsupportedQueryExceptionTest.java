package org.molgenis.data.annotation.core.exception;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.QueryRule;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class UnsupportedQueryExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("annotators");
	}

	@Test
	public void testGetLocalizedMessageArguments()
	{
		QueryRule rule1 = new QueryRule();
		rule1.setOperator(QueryRule.Operator.EQUALS);
		rule1.setField("gene");
		rule1.setValue(10);
		QueryRule rule2 = new QueryRule();
		rule2.setOperator(QueryRule.Operator.RANGE);
		rule2.setField("score");
		rule2.setValue(Arrays.asList(10, 20));
		assertEquals(new UnsupportedQueryException(ImmutableList.of(rule1, rule2)).getLocalizedMessage(),
				"The query 'gene' = '10', 'score' RANGE '[10, 20]' is not supported on this repository.");
	}
}