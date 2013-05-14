package org.molgenis.entityexplorer.plugin;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.entityexplorer.controller.EntityExplorerController;
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

public class EntityExplorerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ENTITY = "entity";
	private static final String PARAM_IDENTIFIER = "identifier";
	private static final String PARAM_QUERY = "query";

	public EntityExplorerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		// get query params from current request URL
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String entity = request.getParameter(PARAM_ENTITY);
		String identifier = request.getParameter(PARAM_IDENTIFIER);
		String query = request.getParameter(PARAM_QUERY);

		// construct iframe URL using query params
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(EntityExplorerController.URI);
		if (entity != null) uriBuilder.queryParam(PARAM_ENTITY, entity);
		if (identifier != null) uriBuilder.queryParam(PARAM_IDENTIFIER, identifier);
		if (query != null) uriBuilder.queryParam(PARAM_QUERY, query);
		return uriBuilder.build().toUriString();
	}
}
