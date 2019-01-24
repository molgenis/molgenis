package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.stereotype.Service;

/** Executes a JavaScript using the Magma API */
@Service
class JsMagmaScriptExecutor {
  private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
  private final EntityTypeFactory entityTypeFactory;
  private final AttributeFactory attributeFactory;

  JsMagmaScriptExecutor(
      JsMagmaScriptEvaluator jsMagmaScriptEvaluator,
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attributeFactory) {
    this.jsMagmaScriptEvaluator = jsMagmaScriptEvaluator;
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeFactory = requireNonNull(attributeFactory);
  }

  /** Execute a JavaScript using the Magma API */
  Object executeScript(String jsScript, Map<String, Object> parameters) {
    EntityType entityType = entityTypeFactory.create("entity");
    Set<String> attributeNames = parameters.keySet();
    attributeNames.forEach(key -> entityType.addAttribute(attributeFactory.create().setName(key)));
    if (attributeNames.iterator().hasNext()) {
      entityType.getAttribute(attributeNames.iterator().next()).setIdAttribute(true);
    }
    Entity entity = new DynamicEntity(entityType);
    parameters.forEach(entity::set);
    return jsMagmaScriptEvaluator.eval(jsScript, entity);
  }
}
