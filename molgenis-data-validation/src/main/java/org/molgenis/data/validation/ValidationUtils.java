package org.molgenis.data.validation;

import java.util.Collections;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.js.ScriptEvaluator;
import org.mozilla.javascript.EcmaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ValidationUtils.class);

	/**
	 * Resolves a boolean expression (validation or visible expression)
	 * 
	 * @throws MolgenisDataException
	 *             if the script resolves to null or to a non boolean
	 * @param expression
	 * @param entity
	 * @param meta
	 * @param attribute
	 * @return true or false
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
