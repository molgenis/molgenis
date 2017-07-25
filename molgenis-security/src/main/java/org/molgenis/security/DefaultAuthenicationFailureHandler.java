package org.molgenis.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by sido on 25/07/2017.
 */
public class DefaultAuthenicationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException
	{
		setDefaultFailureUrl("/login?error");

		if(exception instanceof Secret2FAKeyNotAvailable) {
			response.sendRedirect("/login/configure-2fa");
		}

		super.onAuthenticationFailure(request, response, exception);
		String errorMessage = exception.getMessage();
		request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorMessage);
	}
}