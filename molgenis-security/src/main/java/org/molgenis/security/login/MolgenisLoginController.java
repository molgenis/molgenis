package org.molgenis.security.login;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import static org.molgenis.security.login.MolgenisLoginController.URI;

@Controller
@RequestMapping(URI)
public class MolgenisLoginController
{
	public static final String PARAM_SESSION_EXPIRED = "expired";

	public static final String URI = "/login";
	public static final String ERROR_MESSAGE_ATTRIBUTE = "errorMessage";

	private static final String ERROR_MESSAGE_BAD_CREDENTIALS = "The username or password you entered is incorrect.";
	public static final String ERROR_MESSAGE_DISABLED = "Your account is not yet activated.";
	private static final String ERROR_MESSAGE_SESSION_AUTHENTICATION = "Your login session has expired.";
	private static final String ERROR_MESSAGE_UNKNOWN = "Sign in failed.";

	private static final String VIEW_LOGIN = "view-login";

	@GetMapping
	public String getLoginPage()
	{
		return VIEW_LOGIN;
	}

	@GetMapping(params = PARAM_SESSION_EXPIRED)
	public String getLoginErrorPage(Model model)
	{
		model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, ERROR_MESSAGE_SESSION_AUTHENTICATION);
		return VIEW_LOGIN;
	}

	@GetMapping(params = "error")
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
			else if (attribute instanceof SessionAuthenticationException)
			{
				errorMessage = ERROR_MESSAGE_SESSION_AUTHENTICATION;
			}
			else
			{
				if (!determineErrorMessagesFromInternalAuthenticationExceptions(attribute).isEmpty())
				{
					errorMessage = determineErrorMessagesFromInternalAuthenticationExceptions(attribute);
				}
				else
				{
					errorMessage = ERROR_MESSAGE_UNKNOWN;
				}
			}
		}
		else
		{
			errorMessage = ERROR_MESSAGE_UNKNOWN;
		}

		model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, errorMessage);
		return VIEW_LOGIN;
	}

	private String determineErrorMessagesFromInternalAuthenticationExceptions(Object attribute)
	{
		String errorMessage = "";
		if (attribute instanceof InternalAuthenticationServiceException)
		{
			Throwable throwable = ((InternalAuthenticationServiceException) attribute).getCause();
			if (throwable.getCause() instanceof UsernameNotFoundException)
			{
				errorMessage = ERROR_MESSAGE_BAD_CREDENTIALS;
			}
		}
		return errorMessage;
	}

}
