package org.molgenis.js.magma;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.ScriptEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Executes a JavaScript using the Magma API
 */
@Service
public class JsMagmaScriptExecutor
{
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	public JsMagmaScriptExecutor(EntityTypeFactory entityTypeFactory, AttributeMetaDataFactory attrMetaFactory)
	{
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
	}

	/**
	 * Execute a JavaScript using the Magma API
	 *
	 * @param jsScript
	 * @param parameters
	 * @return
	 */
	public Object executeScript(String jsScript, Map<String, Object> parameters)
	{
		EntityType entityType = entityTypeFactory.create().setSimpleName("entity");
		parameters.keySet().stream().forEach(key ->
		{
			entityType.addAttribute(attrMetaFactory.create().setName(key));
		});
		Entity entity = new DynamicEntity(entityType);
		parameters.entrySet().forEach(parameter ->
		{
			entity.set(parameter.getKey(), parameter.getValue());
		});
		return ScriptEvaluator.eval(jsScript, entity, entityType);
	}
}
