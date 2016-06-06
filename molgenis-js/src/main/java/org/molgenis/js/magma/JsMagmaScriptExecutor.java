package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.molgenis.data.meta.AttributeMetaDataFactory;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataFactory;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.ScriptEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Executes a JavaScript using the Magma API
 */
@Service
public class JsMagmaScriptExecutor
{
	private final EntityMetaDataFactory entityMetaFactory;
	private final AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	public JsMagmaScriptExecutor(EntityMetaDataFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory)
	{
		this.entityMetaFactory = requireNonNull(entityMetaFactory);
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
		EntityMetaData entityMeta = entityMetaFactory.create().setSimpleName("entity");
		parameters.keySet().stream().forEach(key -> {
			entityMeta.addAttribute(attrMetaFactory.create().setName(key));
		});
		MapEntity entity = new MapEntity(parameters);
		return ScriptEvaluator.eval(jsScript, entity, entityMeta);
	}
}
