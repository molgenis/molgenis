package org.molgenis.data.view.controller;

import static org.molgenis.data.view.controller.ViewConfigurationController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.stream.Collectors;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.ViewMetaData;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class ViewConfigurationController extends MolgenisPluginController
{
	@Autowired
	DataService dataService;

	@Autowired
	IdGenerator idGenerator;

	public static final String NAME = "configureview";
	public static final String URI = PLUGIN_URI_PREFIX + NAME;

	public ViewConfigurationController()
	{
		super(URI);
	}

	/**
	 * Init view
	 * 
	 * @param masterEntityName
	 * @return
	 */
	@RequestMapping
	public String init(Model model)
	{
		return "view-configure-view";
	}

	@RequestMapping(value = "/add-entity-view", method = POST)
	@ResponseBody
	public void addEntityView(@RequestParam(value = "viewName") String viewName,
			@RequestParam(value = "masterEntityName") String masterEntityName)
	{
		Entity newViewEntity = new MapEntity(new ViewMetaData());
		newViewEntity.set(ViewMetaData.NAME, viewName);
		newViewEntity.set(ViewMetaData.MASTER_ENTITY, masterEntityName);
		dataService.add(ViewMetaData.ENTITY_NAME, newViewEntity);
	}

	@RequestMapping(value = "/get-view-rows", method = POST)
	@ResponseBody
	public List<Entity> getViewRows(@RequestParam(value = "viewName") String viewName)
	{
		return dataService
				.findAll(ViewMetaData.ENTITY_NAME, new QueryImpl().eq(ViewMetaData.NAME, viewName))
				.collect(Collectors.toList());
	}

	@RequestMapping(value = "/delete-entity-view", method = POST)
	@ResponseBody
	public void deleteEntityView(@RequestParam(value = "viewName") String viewName)
	{
		dataService.delete(ViewMetaData.ENTITY_NAME, dataService.findAll(ViewMetaData.ENTITY_NAME,
				new QueryImpl().eq(ViewMetaData.NAME, viewName)));
	}
}
