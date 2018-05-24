package org.molgenis.data.validation;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.script.core.ScriptException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ExpressionValidatorTest extends AbstractMockitoTest
{
	@Mock
	private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
	@Mock
	private Entity entity;
	private ExpressionValidator expressionValidator;

	@BeforeMethod
	public void beforeMethod()
	{
		expressionValidator = new ExpressionValidator(jsMagmaScriptEvaluator);
	}

	@Test
	public void testResolveBooleanExpressions()
	{
		List<String> expressions = Arrays.asList("a", "b");
		when(jsMagmaScriptEvaluator.eval(expressions, entity)).thenReturn(Arrays.asList(TRUE, FALSE));
		assertEquals(expressionValidator.resolveBooleanExpressions(expressions, entity), Arrays.asList(true, false));
	}

	@DataProvider(name = "resultProvider")
	public Object[][] resultProvider()
	{
		// @formatter:off
		return new Object[][] {
				new Object[] { FALSE, false },
				new Object[] { "true", true },
				new Object[] { "TRUE", true },
				new Object[] { 0, false },
				new Object[] { 1, false },
				new Object[] { new ScriptException("Evaluation failed on line 0, column 10: Undefined is not an object"), false } };
		// @formatter:on
	}

	@Test(dataProvider = "resultProvider")
	public void testResolveBooleanExpression(Object result, boolean expected)
	{
		when(jsMagmaScriptEvaluator.eval(singletonList("expression"), entity)).thenReturn(singletonList(result));
		assertEquals(expressionValidator.resolveBooleanExpression("expression", entity), expected);
	}
}