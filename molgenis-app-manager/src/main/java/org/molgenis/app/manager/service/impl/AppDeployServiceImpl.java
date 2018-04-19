package org.molgenis.app.manager.service.impl;

import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.probeContentType;
import static java.util.Objects.requireNonNull;

@Service
public class AppDeployServiceImpl implements AppDeployService
{
	private static final String JS_FOLDER = "js";
	private static final String CSS_FOLDER = "css";
	private static final String IMG_FOLDER = "img";

	private final DataService dataService;

	public AppDeployServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public String configureTemplateResourceReferencing(String template, String baseUrl)
	{
		template = template.replaceAll("src=js/", "src=" + baseUrl + "js/");
		template = template.replaceAll("href=css/", "href=" + baseUrl + "css/");
		template = template.replaceAll("src=img/", "src=" + baseUrl + "img/");
		return template;
	}

	@Override
	public void loadJavascriptResources(String uri, String fileName, HttpServletResponse response) throws IOException
	{
		App app = findAppByUri(uri);
		File requestedJsFile = new File(
				app.getResourceFolder() + File.separator + JS_FOLDER + File.separator + fileName);

		response.setContentType("application/javascript; charset=UTF-8");
		response.setContentLength((int) requestedJsFile.length());
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName.replace(" ", "_"));

		try (InputStream is = new FileInputStream(requestedJsFile))
		{
			FileCopyUtils.copy(is, response.getOutputStream());
		}
	}

	@Override
	public void loadCSSResources(String uri, String fileName, HttpServletResponse response) throws IOException
	{
		App app = findAppByUri(uri);
		File requestedCSSFile = new File(
				app.getResourceFolder() + File.separator + CSS_FOLDER + File.separator + fileName);

		response.setContentType("text/css; charset=utf-8");
		response.setContentLength((int) requestedCSSFile.length());
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName.replace(" ", "_"));

		try (InputStream is = new FileInputStream(requestedCSSFile))
		{
			FileCopyUtils.copy(is, response.getOutputStream());
		}
	}

	@Override
	public void loadImageResources(String uri, String fileName, HttpServletResponse response) throws IOException
	{
		App app = findAppByUri(uri);
		File requestedImageFile = new File(
				app.getResourceFolder() + File.separator + IMG_FOLDER + File.separator + fileName);

		Path source = Paths.get(requestedImageFile.getPath());
		String contentType = probeContentType(source);
		response.setContentType(contentType);
		response.setContentLength((int) requestedImageFile.length());
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName.replace(" ", "_"));

		try (InputStream is = new FileInputStream(requestedImageFile))
		{
			FileCopyUtils.copy(is, response.getOutputStream());
		}
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
