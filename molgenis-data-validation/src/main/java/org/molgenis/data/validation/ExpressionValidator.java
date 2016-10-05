package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.List;

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
	public boolean resolveBooleanExpression(String expression, Entity entity, EntityType meta)
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
	public List<Boolean> resolveBooleanExpressions(List<String> expressions, Entity entity, EntityType meta)
	{
		return ValidationUtils.resolveBooleanExpressions(expressions, entity, meta);
	}
}
