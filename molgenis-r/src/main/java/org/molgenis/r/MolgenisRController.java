package org.molgenis.r;

import org.molgenis.security.token.TokenParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Returns the molgenis R api client script */
@Controller
public class MolgenisRController {
  private static final String URI = "/molgenis.R";
  private static final String API_URI = "/api/";

  @GetMapping(URI)
  public String showMolgenisRApiClient(@TokenParam String token, Model model) {
    // If the request contains a molgenis security token, use it
    if (token != null) {
      model.addAttribute("token", token);
    }

    model.addAttribute(
        "api_url",
        ServletUriComponentsBuilder.fromCurrentContextPath().path(API_URI).toUriString());

    return "molgenis.R";
  }
}
