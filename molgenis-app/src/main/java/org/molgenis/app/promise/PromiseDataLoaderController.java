package org.molgenis.app.promise;

import static org.molgenis.app.promise.PromiseDataLoaderController.URI;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.molgenis.app.promise.client.PromiseDataParser;
import org.molgenis.app.promise.mapper.MappingReport;
import org.molgenis.app.promise.mapper.PromiseMapper;
import org.molgenis.app.promise.mapper.PromiseMapperFactory;
import org.molgenis.app.promise.model.PromiseMappingProjectMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import autovalue.shaded.com.google.common.common.collect.Lists;

@Controller
@EnableScheduling
@RequestMapping(URI)
public class PromiseDataLoaderController extends MolgenisPluginController
{
	public static final String ID = "promiseloader";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final Logger LOG = LoggerFactory.getLogger(PromiseDataLoaderController.class);

	private final DataService dataService;
	private final PromiseMapperFactory promiseMapperFactory;

	@Autowired
	public PromiseDataLoaderController(PromiseDataParser proMiseDataParser, DataService dataService,
			PromiseMapperFactory promiseMapperFactory)
	{
		super(URI);
		this.dataService = Objects.requireNonNull(dataService);
		this.promiseMapperFactory = Objects.requireNonNull(promiseMapperFactory);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-promiseloader";
	}

	/**
	 * Returns a list of the project names so they can be listed in the control panel
	 */
	@RequestMapping(value = "projects", method = RequestMethod.GET)
	@ResponseBody
	public List<String> projects()
	{
		Iterable<Entity> projects = dataService.findAll(PromiseMappingProjectMetaData.FULLY_QUALIFIED_NAME);
		List<String> names = Lists.newArrayList();

		projects.forEach(p -> names.add(p.get("name").toString()));

		return names;
	}

	@RequestMapping(value = "map/{name}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional
	public MappingReport map(@PathVariable("name") String projectName) throws IOException
	{
		Entity project = dataService.findOne(PromiseMappingProjectMetaData.FULLY_QUALIFIED_NAME, projectName);
		PromiseMapper promiseMapper = promiseMapperFactory.getMapper(project.getString("mapper"));
		return promiseMapper.map(project);
	}

	@Scheduled(cron = "0 0 0 * * *")
	@RunAsSystem
	public void executeScheduled()
	{
		Iterable<Entity> projects = dataService.findAll(PromiseMappingProjectMetaData.FULLY_QUALIFIED_NAME);
		for (Entity project : projects)
		{
			LOG.info("Starting scheduled mapping task for ProMISe biobank " + project.getString("name"));
			PromiseMapper promiseMapper = promiseMapperFactory.getMapper(project.getString("mapper"));
			promiseMapper.map(project);
		}
	}
}
