package org.molgenis.omx.mobile.login;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mobile")
public class LoginController
{
	@RequestMapping(value = "/authenticated", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Boolean> isUserAuthenticated()
	{
		boolean loggedIn = SecurityUtils.currentUserIsAuthenticated();
		return new ResponseEntity<Boolean>(loggedIn, HttpStatus.OK);
	}
}
