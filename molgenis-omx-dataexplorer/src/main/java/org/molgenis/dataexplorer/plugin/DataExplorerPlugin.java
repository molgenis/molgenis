package org.molgenis.dataexplorer.plugin;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

public class DataExplorerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	private static final String PARAM_DATASET = "dataset";

	public DataExplorerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		// get query params from current request URL
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String dataSetIdentifier = request.getParameter(PARAM_DATASET);

		// construct iframe URL using query params
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(DataExplorerController.URI);
		if (dataSetIdentifier != null) uriBuilder.queryParam(PARAM_DATASET, dataSetIdentifier);
		return uriBuilder.build().toUriString();
	}
}
