package org.molgenis.data.validation;

import org.molgenis.data.AttributeMetaData;
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
	public static boolean resolveBooleanExpression(String expression, Entity entity, EntityMetaData meta,
			AttributeMetaData attribute)
	{
		Object result = null;
		try
		{
			result = ScriptEvaluator.eval(expression, entity, meta);
		}
		catch (EcmaError e)
		{
			LOG.warn("Error evaluation validationExpression", e);
		}

		if ((result == null) || !(result instanceof Boolean))
		{
			throw new MolgenisDataException(String.format(
					"Invalid boolean expression '%s' for attribute '%s' of entity '%s'",
					attribute.getValidationExpression(), attribute.getName(), meta.getName()));
		}

		return (Boolean) result;
	}
}
