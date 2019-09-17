package org.molgenis.js.magma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.test.AbstractMockitoTest;

class JsMagmaScriptExecutorTest extends AbstractMockitoTest {
  @Mock private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeFactory attributeFactory;
  private JsMagmaScriptExecutor jsMagmaScriptExecutor;

  @BeforeEach
  void setUpBeforeMethod() {
    jsMagmaScriptExecutor =
        new JsMagmaScriptExecutor(jsMagmaScriptEvaluator, entityTypeFactory, attributeFactory);
  }

  @Test
  void testExecuteScript() {
    String entityId = "entity";
    String attributeName = "myAttribute";
    String attributeValue = "value";
    String jsScript = "MyJsScript";

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(STRING);
    when(attributeFactory.create()).thenReturn(attribute);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttribute(attributeName)).thenReturn(attribute);
    when(entityTypeFactory.create(entityId)).thenReturn(entityType);

    Object object = mock(Object.class);
    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    when(jsMagmaScriptEvaluator.eval(eq(jsScript), entityCaptor.capture())).thenReturn(object);
    Map<String, Object> parameters = Collections.singletonMap(attributeName, attributeValue);

    assertEquals(object, jsMagmaScriptExecutor.executeScript(jsScript, parameters));
    verify(attribute).setName(attributeName);
    assertEquals(attributeValue, entityCaptor.getValue().get(attributeName));
  }
}
