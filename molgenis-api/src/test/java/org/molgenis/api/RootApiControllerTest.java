package org.molgenis.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.gson.GsonConfig;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {RootApiController.class, GsonConfig.class})
class RootApiControllerTest extends AbstractMockitoSpringContextTests {

  private MockMvc mockMvc;
  @Autowired private Gson gson;

  @BeforeEach
  void beforeMethod() {
    var rootApiController = new RootApiController("versionString", "2019-05-21 10:32 UTC");
    mockMvc =
        MockMvcBuilders.standaloneSetup(rootApiController)
            .setMessageConverters(new GsonHttpMessageConverter(gson))
            .build();
  }

  @Test
  void testGetVersion() throws Exception {
    MvcResult result =
        mockMvc
            .perform(options("/api").contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    String actual = result.getResponse().getContentAsString();
    String expected =
        "{\"app\":{\"version\":\"versionString\",\"buildDate\":\"2019-05-21T10:32:00Z\"}}";

    assertEquals(expected, actual);
  }
}
