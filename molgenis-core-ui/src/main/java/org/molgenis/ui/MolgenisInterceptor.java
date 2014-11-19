package org.molgenis.ui;

import static org.molgenis.ui.MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;

	@Autowired
	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry)
	{
		if (resourceFingerprintRegistry == null)
		{
			throw new IllegalArgumentException("resourceFingerprintRegistry ui is null");
		}
		this.resourceFingerprintRegistry = resourceFingerprintRegistry;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
		}
	}
}