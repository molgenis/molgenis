package org.molgenis.api.fair.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.core.ui.converter.RdfConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonWebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.ForwardedHeaderFilter;

@WebAppConfiguration
@ContextConfiguration(classes = {GsonWebConfig.class})
class FairControllerTest extends AbstractMockitoSpringContextTests {

  @Mock private DataService dataService;
  @Mock private MetaDataService metaDataService;
  @Mock private EntityModelWriter entityModelWriter;
  @Mock private EntityType catalogMeta;

  private MockMvc mockMvc;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @BeforeEach
  void beforeTest() {
    FairController controller = new FairController(dataService, metaDataService, entityModelWriter);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(
                new FormHttpMessageConverter(), gsonHttpMessageConverter, new RdfConverter())
            .addFilter(new ForwardedHeaderFilter())
            .build();
  }

  @Test
  void getCatalogTest() throws Exception {
    Entity answer = mock(Entity.class);

    when(metaDataService.getEntityType("fdp_Catalog")).thenReturn(Optional.of(catalogMeta));
    when(entityModelWriter.isADcatResource(catalogMeta)).thenReturn(true);
    when(dataService.findOneById("fdp_Catalog", "catalogID")).thenReturn(answer);

    this.mockMvc
        .perform(
            get(URI.create(
                    "http://molgenis01.gcc.rug.nl:8080/api/fdp/fdp_Catalog/catalogID?blah=value"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk());

    Mockito.verify(entityModelWriter).createRdfModel(answer);
  }

  @Test
  void getCatalogTestUnknownCatalog() throws Exception {
    when(metaDataService.getEntityType("fdp_Catalog")).thenReturn(Optional.of(catalogMeta));
    when(entityModelWriter.isADcatResource(catalogMeta)).thenReturn(true);

    this.mockMvc
        .perform(
            get(URI.create(
                    "http://molgenis01.gcc.rug.nl:8080/api/fdp/fdp_Catalog/catalogID?blah=value"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest());
  }
}
