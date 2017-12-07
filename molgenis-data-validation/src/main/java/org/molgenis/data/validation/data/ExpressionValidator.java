package org.molgenis.data.validation.data;

import org.molgenis.data.Entity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Validates entities using JavaScript expressions. Expressions can use the Magma API.
 */
@Component
public class ExpressionValidator
{
	private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	public ExpressionValidator(JsMagmaScriptEvaluator jsMagmaScriptEvaluator)
	{
		this.jsMagmaScriptEvaluator = requireNonNull(jsMagmaScriptEvaluator);
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
