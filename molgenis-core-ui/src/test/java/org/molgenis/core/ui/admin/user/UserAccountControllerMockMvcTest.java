package org.molgenis.core.ui.admin.user;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.web.converter.GsonWebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

@WebAppConfiguration
@ContextConfiguration(classes = {UserAccountControllerTestConfig.class, GsonWebConfig.class})
class UserAccountControllerMockMvcTest extends AbstractMolgenisSpringTest {
  @Autowired private UserAccountControllerTestConfig config;
  @Autowired private UserAccountController userAccountController;
  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;
  @Autowired private LocaleResolver localeResolver;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    config.resetMocks();
    MockitoAnnotations.initMocks(this);
    mockMvc =
        MockMvcBuilders.standaloneSetup(userAccountController)
            .setLocaleResolver(localeResolver)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();
  }

  @Test
  void changeLanguageOk() throws Exception {
    mockMvc
        .perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
        .andExpect(status().isNoContent());
    verify(localeResolver).setLocale(any(), any(), eq(new Locale("nl")));
  }

  @Test
  void changeLanguageForbidden() throws Exception {
    doThrow(new AccessDeniedException("Access denied."))
        .when(localeResolver)
        .setLocale(any(), any(), eq(new Locale("nl")));
    // FIXME: update expected status after specific exceptions are implemented
    mockMvc
        .perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
        .andExpect(status().isForbidden());
  }

  @Test
  void changeLanguageUnknownLanguage() throws Exception {
    mockMvc
        .perform(post("/plugin/useraccount/language/update").param("languageCode", "swahili"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void changeLanguageNPE() throws Exception {
    doThrow(new NullPointerException()).when(localeResolver).setLocale(any(), any(), any());
    mockMvc
        .perform(post("/plugin/useraccount/language/update").param("languageCode", "nl"))
        .andExpect(status().isInternalServerError());
  }
}
