package org.molgenis.security.login;

import static java.util.Objects.requireNonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login")
public class MolgenisLoginController
{
	public static final String SESSION_EXPIRED_SESSION_ATTR = "sessionExpired";
	private static final String ERROR_MESSAGE_BAD_CREDENTIALS = "The username or password you entered is incorrect.";
	private static final String ERROR_MESSAGE_DISABLED = "Your account is not yet activated.";
	private static final String ERROR_MESSAGE_SESSION_AUTHENTICATION = "Your login session has expired.";
	private static final String ERROR_MESSAGE_UNKNOWN = "Sign in failed.";

	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final AppSettings appSettings;

	@Autowired
	public MolgenisLoginController(ResourceFingerprintRegistry resourceFingerprintRegistry, AppSettings appSettings)
	{
		this.resourceFingerprintRegistry = requireNonNull(resourceFingerprintRegistry);
		this.appSettings = requireNonNull(appSettings);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getLoginPage(Model model, HttpSession session)
	{
		model.addAttribute("resource_fingerprint_registry", resourceFingerprintRegistry);
		model.addAttribute("enable_self_registration", appSettings.getSignUp());

		if (session.getAttribute(SESSION_EXPIRED_SESSION_ATTR) != null)
		{
			model.addAttribute("errorMessage", ERROR_MESSAGE_SESSION_AUTHENTICATION);
			session.removeAttribute("sessionExpired");
		}

		return "view-login";
	}

	@RequestMapping(method = RequestMethod.GET, params = "error")
	public String getLoginErrorPage(Model model, HttpServletRequest request)
	{
		String errorMessage;
		Object attribute = request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		if (attribute != null)
		{
			if (attribute instanceof BadCredentialsException)
			{
				errorMessage = ERROR_MESSAGE_BAD_CREDENTIALS;
			}
			else if (attribute instanceof DisabledException)
			{
				errorMessage = ERROR_MESSAGE_DISABLED;
			}
			else if (attribute instanceof SessionAuthenticationException)
			{
				errorMessage = ERROR_MESSAGE_SESSION_AUTHENTICATION;
			}
			else
			{
				errorMessage = ERROR_MESSAGE_UNKNOWN;
			}
		}
		else
		{
			errorMessage = ERROR_MESSAGE_UNKNOWN;
		}

		model.addAttribute("resource_fingerprint_registry", resourceFingerprintRegistry);
		model.addAttribute("errorMessage", errorMessage);
		model.addAttribute("enable_self_registration", appSettings.getSignUp());
		return "view-login";
	}
}
