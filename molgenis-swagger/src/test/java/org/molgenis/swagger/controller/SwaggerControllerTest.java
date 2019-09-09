package org.molgenis.swagger.controller;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.i18n.LanguageService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SecurityTestExecutionListeners
class SwaggerControllerTest extends AbstractMockitoSpringContextTests {
  @Mock private MetaDataService metaDataService;
  @Mock private TokenService tokenService;
  @Mock private HttpServletResponse response;
  @Mock private EntityType type1;
  @Mock private EntityType type2;
  @Mock private Model model;

  private SwaggerController swaggerController;

  @BeforeEach
  void beforeMethod() {
    reset(metaDataService, type1, type2, tokenService);
    swaggerController = new SwaggerController(metaDataService, tokenService);
  }

  @Test
  @WithMockUser
  void testInit() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    when(tokenService.generateAndStoreToken("user", "For Swagger UI")).thenReturn("ABCDEFG");
    assertEquals("view-swagger-ui", swaggerController.init(model));
    verify(model).addAttribute("molgenisUrl", "http://localhost/plugin/swagger/swagger.yml");
    verify(model).addAttribute("baseUrl", "http://localhost");
    verify(model).addAttribute("token", "ABCDEFG");
    verifyNoMoreInteractions(model);
  }

  @Test
  void testInitWithoutUser() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertEquals("view-swagger-ui", swaggerController.init(model));
    verify(model).addAttribute("molgenisUrl", "http://localhost/plugin/swagger/swagger.yml");
    verify(model).addAttribute("baseUrl", "http://localhost");
    verifyNoMoreInteractions(model);
  }

  @Test
  void testSwagger() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    when(type1.getId()).thenReturn("abc_EntityType2ëæ");
    when(type2.getId()).thenReturn("abc_EntityType1ëæ");
    when(metaDataService.getEntityTypes()).thenReturn(Stream.of(type1, type2));

    assertEquals("view-swagger", swaggerController.swagger(model, response));

    verify(model).addAttribute("scheme", "http");
    verify(model).addAttribute("host", "localhost");
    verify(model)
        .addAttribute("entityTypes", newArrayList("abc_EntityType1ëæ", "abc_EntityType2ëæ"));
    verify(model).addAttribute("attributeTypes", AttributeType.getOptionsLowercase());
    verify(model)
        .addAttribute("languageCodes", LanguageService.getLanguageCodes().collect(toList()));
    verifyNoMoreInteractions(model);
  }

  @Test
  void testSwaggerHostAddsPort() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setMethod("GET");
    request.setServletPath("/plugin/swagger/");
    request.setServerPort(8080);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    when(metaDataService.getEntityTypes()).thenReturn(Stream.empty());

    assertEquals("view-swagger", swaggerController.swagger(model, response));

    verify(model).addAttribute("scheme", "http");
    verify(model).addAttribute("host", "localhost:8080");
  }
}
