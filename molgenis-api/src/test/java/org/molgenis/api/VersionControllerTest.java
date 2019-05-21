package org.molgenis.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;

import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
@TestPropertySource(properties = {"molgenis.version = 10.3.8"})
@ContextConfiguration(classes = {GsonConfig.class})
public class VersionControllerTest extends AbstractTestNGSpringContextTests {

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  private MockMvc mockMvc;

  @BeforeMethod
  public void beforeMethod() {
    RootApiController rootApiController =
        new RootApiController("versionString", "2019-05-21 10:32 UTC");
    mockMvc =
        MockMvcBuilders.standaloneSetup(rootApiController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();
  }

  @Test
  public void testGetVersion() throws Exception {
    MvcResult result =
        mockMvc
            .perform(options("/api").contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andReturn();
    String actual = result.getResponse().getContentAsString();
    String expected =
        "{\"app\":{\"version\":\"versionString\",\"buildDate\":\"2019-05-21T10:32:00Z\"}}";

    assertEquals(actual, expected);
  }
}
