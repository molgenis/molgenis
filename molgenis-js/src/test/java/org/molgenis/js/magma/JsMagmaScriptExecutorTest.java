package org.molgenis.js.magma;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JsMagmaScriptExecutorTest extends AbstractMockitoTest {
  @Mock private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeFactory attributeFactory;
  private JsMagmaScriptExecutor jsMagmaScriptExecutor;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jsMagmaScriptExecutor =
        new JsMagmaScriptExecutor(jsMagmaScriptEvaluator, entityTypeFactory, attributeFactory);
  }

  @Test
  public void testExecuteScript() {
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

    assertEquals(jsMagmaScriptExecutor.executeScript(jsScript, parameters), object);
    verify(attribute).setName(attributeName);
    assertEquals(entityCaptor.getValue().get(attributeName), attributeValue);
  }
}
