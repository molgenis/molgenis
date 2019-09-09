package org.molgenis.script;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class ScriptRunnerControllerTest extends AbstractMockitoTest {

  @Mock private ScriptJobExecutionFactory scriptJobExecutionFactory;
  @Mock private JobExecutor jobExecutor;
  @Mock private SavedScriptRunner savedScriptRunner;
  @Mock private JobsController jobsController;
  @Mock private ScriptJobExecution scriptJobExecution;
  private ScriptRunnerController controller;
  private Gson gson; // no mock since this is a final class

  @BeforeEach
  void setUp() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    gson = new Gson();
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("sys_job_test");
    when(scriptJobExecution.getIdValue()).thenReturn("ID");
    when(scriptJobExecution.getEntityType()).thenReturn(entityType);
    when(scriptJobExecutionFactory.create()).thenReturn(scriptJobExecution);
    controller =
        new ScriptRunnerController(
            scriptJobExecutionFactory, jobExecutor, savedScriptRunner, gson, jobsController);
  }

  @Test
  void testSubmitScript() {
    Map<String, Object> parameters = newHashMap();
    parameters.put("arg1", "value1");
    ResponseEntity responseEntity = controller.submitScript("test", parameters);
    assertEquals(responseEntity.getStatusCodeValue(), 200);
    assertEquals(responseEntity.getBody(), "/api/v2/sys_job_test/ID");
    verify(jobExecutor).submit(scriptJobExecution);
    verify(scriptJobExecution).setName("test");
    verify(scriptJobExecution).setParameters(gson.toJson(parameters));
  }

  @Test
  void testStartScript() throws IOException {
    Map<String, Object> parameters = newHashMap();
    parameters.put("arg1", "value1");
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(jobsController.createJobExecutionViewHref("/api/v2/sys_job_test/ID", 1000))
        .thenReturn("redirect:url");

    controller.startScript("test", parameters, response);
    verify(jobExecutor).submit(scriptJobExecution);
    verify(scriptJobExecution).setName("test");
    verify(scriptJobExecution).setParameters(gson.toJson(parameters));
    verify(response).sendRedirect("redirect:url");
  }
}
