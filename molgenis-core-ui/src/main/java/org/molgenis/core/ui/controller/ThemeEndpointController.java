package org.molgenis.core.ui.controller;

import static java.util.Objects.requireNonNull;

import io.swagger.annotations.Api;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Dynamically serves currently active theme on a fixed endpoint */
@Api("Theme")
@Controller
@RequestMapping(ThemeEndpointController.ID)
public class ThemeEndpointController {
  public static final String ID = "theme";

  private final AppSettings appSettings;

  public ThemeEndpointController(AppSettings appSettings) {
    this.appSettings = requireNonNull(appSettings);
  }

  @GetMapping(value = "/style.css")
  public void getTheme(HttpServletResponse httpServletResponse) {
    httpServletResponse.setHeader("Location", appSettings.getThemeURL());
    httpServletResponse.setStatus(302);
  }
}
