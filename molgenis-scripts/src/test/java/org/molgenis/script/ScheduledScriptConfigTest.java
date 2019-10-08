package org.molgenis.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.test.AbstractMockitoTest;

class ScheduledScriptConfigTest extends AbstractMockitoTest {

  @Mock SavedScriptRunner savedScriptRunner;
  @Mock ScheduledJobTypeFactory scheduledJobTypeFactory;
  @Mock ScriptJobExecutionMetadata scriptJobExecutionMetadata;

  private ScheduledScriptConfig scheduledJobConfig;

  @BeforeEach
  void setUp() {
    this.scheduledJobConfig =
        new ScheduledScriptConfig(
            savedScriptRunner, scheduledJobTypeFactory, scriptJobExecutionMetadata, new Gson());
  }

  @Test
  void testGetParameterMap() {
    ScriptJobExecution scriptJobExecution = mock(ScriptJobExecution.class);
    when(scriptJobExecution.getParameters()).thenReturn("{param1:test,param2:value}");
    when(scriptJobExecution.getIdValue()).thenReturn("scriptJobExecutionIdentifier");

    Map<String, Object> actual = scheduledJobConfig.getParameterMap(scriptJobExecution);
    Map<String, Object> expected = new HashMap<>();
    expected.put("param1", "test");
    expected.put("param2", "value");
    expected.put("scriptJobExecutionId", "scriptJobExecutionIdentifier");

    assertEquals(expected, actual);
  }

  @Test
  void testGetParameterMapEmptyString() {
    ScriptJobExecution scriptJobExecution = mock(ScriptJobExecution.class);
    when(scriptJobExecution.getParameters()).thenReturn(null);
    when(scriptJobExecution.getIdValue()).thenReturn("scriptJobExecutionIdentifier");

    Map<String, Object> actual = scheduledJobConfig.getParameterMap(scriptJobExecution);
    Map<String, Object> expected = new HashMap<>();
    expected.put("scriptJobExecutionId", "scriptJobExecutionIdentifier");

    assertEquals(expected, actual);
  }
}
