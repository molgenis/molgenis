package org.molgenis.r;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.molgenis.security.token.TokenExtractor;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisRControllerTest extends AbstractMockitoTest {

  private MockMvc mockMvc;

  @BeforeMethod
  public void beforeTest() {
    MolgenisRController controller = new MolgenisRController();
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            // use a json view to easily test the model values that get set
            .setSingleView(new MappingJackson2JsonView())
            .setCustomArgumentResolvers(new TokenExtractor())
            .build();
  }

  @Test
  public void testShowMolgenisRApiClientWithTokenParam() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/molgenis.R?molgenis-token=abcde"))
        .andExpect(jsonPath("$.api_url").value("http://localhost/api/"))
        .andExpect(jsonPath("$.token").value("abcde"));
  }

  @Test
  public void testShowMolgenisRApiClientWithTokenHeader() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/molgenis.R").header("X-Molgenis-Token", "abcde"))
        .andExpect(jsonPath("$.api_url").value("http://localhost/api/"))
        .andExpect(jsonPath("$.token").value("abcde"));
  }

  @Test
  public void testShowMolgenisRApiClientWithoutToken() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/molgenis.R"))
        .andExpect(jsonPath("$.api_url").value("http://localhost/api/"))
        .andExpect(jsonPath("$.token").doesNotExist());
  }
}
