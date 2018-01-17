package org.molgenis.data.validation.data;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.script.core.exception.ScriptExecutionException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class ExpressionValidatorTest extends AbstractMockitoTest
{
	@Mock
	private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	private ExpressionValidator expressionValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		expressionValidator = new ExpressionValidator(jsMagmaScriptEvaluator);
	}

	@Test
	public void testResolveBooleanExpressions()
	{
		List<String> expressions = asList("expressionTrue", "expressionFalse");
		Entity entity = mock(Entity.class);
		doReturn(true).when(jsMagmaScriptEvaluator).eval("expressionTrue", entity);
		doReturn(false).when(jsMagmaScriptEvaluator).eval("expressionFalse", entity);
		List<Boolean> resolvedExpressions = expressionValidator.resolveBooleanExpressions(expressions, entity);
		assertEquals(resolvedExpressions, asList(true, false));
	}

	@Test(expectedExceptions = ScriptExecutionException.class)
	public void testResolveBooleanExpressionsException()
	{
		List<String> expressions = asList("expressionTrue", "expressionFalse");
		Entity entity = mock(Entity.class);
		doThrow(new ScriptExecutionException("expressionTrue")).when(jsMagmaScriptEvaluator)
															   .eval("expressionTrue", entity);
		expressionValidator.resolveBooleanExpressions(expressions, entity);
	}
}