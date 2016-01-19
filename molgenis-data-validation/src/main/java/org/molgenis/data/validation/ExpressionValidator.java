package org.molgenis.data.validation;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.springframework.stereotype.Component;

/**
 * Spring bean for {@link ValidationUtils}
 */
@Component
public class ExpressionValidator
{
	/**
	 * see {@link ValidationUtils#resolveBooleanExpression}
	 * 
	 * @param expression
	 * @param entity
	 * @param meta
	 * @return
	 */
	public boolean resolveBooleanExpression(String expression, Entity entity, EntityMetaData meta)
	{
		return ValidationUtils.resolveBooleanExpression(expression, entity, meta);
	}

	/**
	 * see {@link ValidationUtils#resolveBooleanExpressions}
	 * 
	 * @param expressions
	 * @param entity
	 * @param meta
	 * @return
	 */
	public List<Boolean> resolveBooleanExpressions(List<String> expressions, Entity entity, EntityMetaData meta)
	{
		return ValidationUtils.resolveBooleanExpressions(expressions, entity, meta);
	}
}
