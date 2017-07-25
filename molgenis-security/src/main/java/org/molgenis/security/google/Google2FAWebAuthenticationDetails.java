package org.molgenis.security.google;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * @author sido
 */
public class Google2FAWebAuthenticationDetails extends WebAuthenticationDetails
{

	private static final long serialVersionUID = 1L;

	private final String verificationCode;

	public Google2FAWebAuthenticationDetails(HttpServletRequest request) {
		super(request);
		verificationCode = request.getParameter("code");
	}

	public String getVerificationCode() {
		return verificationCode;
	}
}
