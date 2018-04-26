package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Validates entities using JavaScript expressions. Expressions can use the Magma API.
 */
@Component
public class ExpressionValidator
{
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionValidator.class);

	private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	public ExpressionValidator(JsMagmaScriptEvaluator jsMagmaScriptEvaluator)
	{
		this.jsMagmaScriptEvaluator = requireNonNull(jsMagmaScriptEvaluator);
	}

	/**
	 * Resolves a boolean expression (validation or visible expression)
	 *
	 * @param expression JavaScript expression
	 * @param entity     entity used during expression evaluation
	 * @return <code>true</code> or <code>false</code>
	 * @throws MolgenisDataException if the script resolves to null or to a non boolean
	 */
	boolean resolveBooleanExpression(String expression, Entity entity)
	{
		return resolveBooleanExpressions(singletonList(expression), entity).get(0);
	}

	/**
	 * Resolved boolean expressions
	 *
	 * @param expressions JavaScript expressions
	 * @param entity      entity used during expression evaluations
	 * @return for each expression: <code>true</code> or <code>false</code>
	 */
	List<Boolean> resolveBooleanExpressions(List<String> expressions, Entity entity)
	{
		if (expressions.isEmpty())
		{
			return Collections.emptyList();
		}

		List<Boolean> validationResults = new ArrayList<>(expressions.size());
		for (String expression : expressions)
		{
			Object value = jsMagmaScriptEvaluator.eval(expression, entity);
			validationResults.add(value != null ? Boolean.valueOf(value.toString()) : null);
		}
		return validationResults;
	}
}
