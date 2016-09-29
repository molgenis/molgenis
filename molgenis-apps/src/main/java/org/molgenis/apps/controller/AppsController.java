package org.molgenis.apps.controller;

import org.molgenis.apps.model.AppMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.apps.controller.AppsController.URI;
import static org.molgenis.apps.model.AppMetaData.APP;
import static org.molgenis.data.system.core.FreemarkerTemplateMetaData.*;

@Controller
@RequestMapping(URI)
public class AppsController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(AppsController.class);

	public static final String ID = "apps";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-apps";

	private DataService dataService;
	private FileStore fileStore;

	@Autowired
	public AppsController(DataService dataService, FileStore fileStore)
	{
		super(URI);

		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
	}

	@RequestMapping
	public String init(Model model)
	{
		List<Entity> apps = dataService.findAll(APP).collect(toList());
		model.addAttribute("apps", apps);
		return VIEW_NAME;
	}

	@RequestMapping(value = { "/{appName}" })
	public String viewApp(@PathVariable String appName, Model model) throws IOException
	{
		Entity appEntity = dataService.findOneById(APP, appName);
		if (appEntity == null)
		{
			model.addAttribute("appNotAvailableMessage", appName);
			return VIEW_NAME;
		}
		else
		{
			Entity freemarkerEntity = dataService
					.findOne(FREEMARKER_TEMPLATE, new QueryImpl<>().eq(NAME, "view-" + appName + ".ftl"));
			if (freemarkerEntity == null)
			{
				LOG.debug("No FreemarkerTemplate available yet for this app, creating one...");
				freemarkerEntity = new DynamicEntity(dataService.getEntityMetaData(FREEMARKER_TEMPLATE));
				freemarkerEntity.set(NAME, "view-" + appName + ".ftl");

				FileMeta file = (FileMeta) appEntity.get(AppMetaData.RESOURCE_FILES);
				File myFile = fileStore.getFile(file.getFilename());

				StringBuilder freemarkerBody = new StringBuilder();
				FileReader reader = new FileReader(myFile);
				BufferedReader br = new BufferedReader(reader);

				br.lines().forEach(freemarkerBody::append);

				freemarkerEntity.set(VALUE, freemarkerBody.toString());
				dataService.add(FREEMARKER_TEMPLATE, freemarkerEntity);
			}
			return "view-" + appName;
		}
	}
}
