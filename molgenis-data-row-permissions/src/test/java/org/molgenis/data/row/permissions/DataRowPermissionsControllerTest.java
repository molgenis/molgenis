package org.molgenis.data.row.permissions;

import static org.mockito.Mockito.when;
import static org.molgenis.data.row.permissions.DataRowPermissionsController.VIEW_TEMPLATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ExtendWith(MockitoExtension.class)
class DataRowPermissionsControllerTest {
  private MockMvc mockMvc;
  @Mock private MenuReaderService menuReaderService;

  @BeforeEach
  void before() {
    DataRowPermissionsController permissionsController =
        new DataRowPermissionsController(menuReaderService);
    mockMvc = MockMvcBuilders.standaloneSetup(permissionsController).build();
  }

  @Test
  void testInit() throws Exception {
    when(menuReaderService.findMenuItemPath("data-row-permissions"))
        .thenReturn("/menu/main/data-row-permissions");

    mockMvc
        .perform(get(DataRowPermissionsController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_TEMPLATE))
        .andExpect(model().attribute("baseUrl", "/menu/main/data-row-permissions"));
  }
}
