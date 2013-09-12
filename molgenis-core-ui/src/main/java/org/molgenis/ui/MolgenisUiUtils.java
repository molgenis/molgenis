package org.molgenis.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class MolgenisUiUtils
{
	/**
	 * Gets the uri which is currently visible in the browser.
	 * 
	 * Must be used in a Spring environment
	 * 
	 */
	public static String getCurrentUri()
	{
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		StringBuilder uri = new StringBuilder();
		uri.append(request.getAttribute("javax.servlet.forward.request_uri"));

		if (StringUtils.isNotBlank(request.getQueryString()))
		{
			uri.append("?").append(request.getQueryString());
		}

		return uri.toString();
	}

}
