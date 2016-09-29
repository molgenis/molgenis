package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.js.ScriptEvaluator;
import org.mozilla.javascript.EcmaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ValidationUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ValidationUtils.class);

	/**
	 * Resolves a boolean expression (validation or visible expression)
	 *
	 * @param expression
	 * @param entity
	 * @param meta
	 * @return true or false
	 * @throws MolgenisDataException if the script resolves to null or to a non boolean
	 */
	public static boolean resolveBooleanExpression(String expression, Entity entity, EntityMetaData meta)
	{
		return resolveBooleanExpressions(Collections.singletonList(expression), entity, meta).get(0);
	}

	@SuppressWarnings("unchecked")
	public static List<Boolean> resolveBooleanExpressions(List<String> expressions, Entity entity, EntityMetaData meta)
	{
		Object result = null;
		try
		{
			result = ScriptEvaluator.eval(expressions, entity, meta);
		}
		catch (EcmaError e)
		{
			LOG.warn("Error evaluation validationExpression", e);
		}

		return (List<Boolean>) result;
	}
}
