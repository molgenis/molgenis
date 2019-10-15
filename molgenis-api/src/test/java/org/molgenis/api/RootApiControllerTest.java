package org.molgenis.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@TestPropertySource(properties = {"molgenis.version = 10.3.8"})
@ContextConfiguration(classes = {GsonHttpMessageConverter.class})
class RootApiControllerTest extends AbstractMockitoSpringContextTests {

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  private MockMvc mockMvc;

  @BeforeEach
  void beforeMethod() {
    RootApiController rootApiController =
        new RootApiController("versionString", "2019-05-21 10:32 UTC");
    mockMvc =
        MockMvcBuilders.standaloneSetup(rootApiController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();
  }

  @Test
  void testGetVersion() throws Exception {
    MvcResult result =
        mockMvc
            .perform(options("/api").contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andReturn();
    String actual = result.getResponse().getContentAsString();
    // note: seconds/nanos instead of '2019-05-21T10:32:00Z' because of default spring converter
    String expected =
        "{\"app\":{\"version\":\"versionString\",\"buildDate\":{\"seconds\":1558434720,\"nanos\":0}}}";

    assertEquals(expected, actual);
  }
}
