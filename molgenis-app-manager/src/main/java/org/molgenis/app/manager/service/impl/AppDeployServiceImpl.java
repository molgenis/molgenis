package org.molgenis.app.manager.service.impl;

import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Service
public class AppDeployServiceImpl implements AppDeployService
{
	private static final String JS_FOLDER = "js";

	private DataService dataService;
	private FileStore fileStore;

	public AppDeployServiceImpl(DataService dataService, FileStore fileStore)
	{
		this.dataService = Objects.requireNonNull(dataService);
		this.fileStore = Objects.requireNonNull(fileStore);
	}

	@Override
	public void loadJavascriptResources(String uri, String fileName, HttpServletResponse response) throws IOException
	{
		App app = findAppByUri(uri);
		File requestedJsFile = new File(
				app.getResourceFolder() + File.separator + JS_FOLDER + File.separator + fileName);

		System.out.println("requestedJsFile = " + requestedJsFile.getAbsolutePath());

		response.setContentType("application/javascript;charset=UTF-8");
		response.setContentLength((int) requestedJsFile.length());
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName.replace(" ", "_"));

		try (InputStream is = new FileInputStream(requestedJsFile))
		{
			FileCopyUtils.copy(is, response.getOutputStream());
		}
	}

	@Override
	public void loadCSSResources(String uri, HttpServletResponse response) throws IOException
	{

	}

	private App findAppByUri(String uri)
	{
		Query<App> query = QueryImpl.EQ(AppMetadata.URI, uri);
		App app = dataService.findOne(AppMetadata.APP, query, App.class);
		if (app == null)
		{
			throw new AppManagerException("App with uri [" + uri + "] does not exist.");
		}
		return app;
	}
}
