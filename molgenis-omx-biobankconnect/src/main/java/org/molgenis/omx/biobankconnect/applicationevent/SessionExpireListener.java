package org.molgenis.omx.biobankconnect.applicationevent;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SessionExpireListener implements HttpSessionListener
{
	ApplicationContext getContext(ServletContext servletContext)
	{
		return WebApplicationContextUtils.getWebApplicationContext(servletContext);
	}

	@Override
	public void sessionCreated(HttpSessionEvent httpSessionEvent)
	{

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent)
	{
		ApplicationContext applicationContext = getContext(httpSessionEvent.getSession().getServletContext());
		CurrentUserStatus currentUserStatus = applicationContext.getBean("currentUserStatus", CurrentUserStatus.class);
		currentUserStatus.removeCurrentUserBySessionId(httpSessionEvent.getSession().getId());
	}
}