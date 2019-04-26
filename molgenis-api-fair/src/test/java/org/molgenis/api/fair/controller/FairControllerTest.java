package org.molgenis.api.fair.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import org.mockito.Mockito;
import org.molgenis.core.ui.converter.RdfConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {GsonConfig.class})
public class FairControllerTest extends AbstractTestNGSpringContextTests {

  private DataService dataService;
  private EntityModelWriter entityModelWriter;

  private MockMvc mockMvc;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @BeforeMethod
  public void beforeTest() {
    dataService = mock(DataService.class);
    entityModelWriter = mock(EntityModelWriter.class);
    FairController controller = new FairController(dataService, entityModelWriter);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(
                new FormHttpMessageConverter(), gsonHttpMessageConverter, new RdfConverter())
            .addFilter(new ForwardedHeaderFilter())
            .build();
  }

  @Test
  public void getCatalogTest() throws Exception {
    reset(dataService);

    Entity answer = mock(Entity.class);

    when(dataService.findOneById("fdp_Catalog", "catalogID")).thenReturn(answer);

    this.mockMvc
        .perform(
            get(URI.create("http://molgenis01.gcc.rug.nl:8080/api/fdp/catalogID?blah=value"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk());

    Mockito.verify(entityModelWriter)
        .createRdfModel("http://molgenis01.gcc.rug.nl:8080/api/fdp/catalogID", answer);
  }

  @Test
  public void getCatalogTestUnknownCatalog() throws Exception {
    reset(dataService);

    this.mockMvc
        .perform(
            get(URI.create("http://molgenis01.gcc.rug.nl:8080/api/fdp/catalogID?blah=value"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest());
  }
}
