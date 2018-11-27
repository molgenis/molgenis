package org.molgenis.security.captcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/recaptcha")
public class ReCaptchaController {

  @Autowired private ReCaptchaService reCaptchaService;

  @PostMapping
  @ResponseBody
  public Boolean validateCaptcha(@RequestBody ReCaptchaValidationRequest reCaptchaRequest) {
    return reCaptchaService.validate(reCaptchaRequest.getToken());
  }
}
