package org.molgenis.omx.biobankconnect.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class BiobankConnectLogoutHandler implements LogoutHandler
{
	@Autowired
	private CurrentUserStatus currentUserStatus;

	@Autowired
	private UserAccountService userAccountService;

	private final Logger logger = Logger.getLogger(BiobankConnectLogoutHandler.class);

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
	{
		try
		{
			currentUserStatus.removeCurrentUser(userAccountService.getCurrentUser().getUsername(),
					request.getRequestedSessionId());
		}
		catch (Exception e)
		{
			logger.error("Failed to remove user", e);
		}
	}
}