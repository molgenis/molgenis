package org.molgenis.app.promise;

import autovalue.shaded.com.google.common.common.collect.Lists;
import org.molgenis.app.promise.client.PromiseDataParser;
import org.molgenis.app.promise.mapper.*;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.app.promise.PromiseDataLoaderController.URI;

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
	public PromiseDataLoaderController(PromiseDataParser promiseDataParser, DataService dataService,
			PromiseMapperFactory promiseMapperFactory)
	{
		super(URI);
		this.dataService = requireNonNull(dataService);
		this.promiseMapperFactory = requireNonNull(promiseMapperFactory);
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
		Stream<Entity> projects = dataService.findAll(PromiseMappingProjectMetaData.FULLY_QUALIFIED_NAME);
		List<String> names = Lists.newArrayList();

		projects.forEach(p -> names.add(p.getString("name")));

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
		// TODO make configurable via MOLGENIS 'scheduler'

		Stream<Entity> projects = dataService.findAll(PromiseMappingProjectMetaData.FULLY_QUALIFIED_NAME);
		projects.forEach(project -> {
			LOG.info("Starting scheduled mapping task for ProMISe biobank " + project.getString("name"));
			PromiseMapper promiseMapper = promiseMapperFactory.getMapper(project.getString("mapper"));
			promiseMapper.map(project);
		});
	}
}
