package org.molgenis.data.view.controller;

import static org.molgenis.data.view.controller.ViewConfigurationController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.ViewMetaData;
import org.molgenis.data.view.service.ViewService;
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
	ViewService viewService;

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

	@RequestMapping(value = "/delete-view", method = POST)
	@ResponseBody
	public void deleteEntityView(@RequestParam(value = "viewName") String viewName)
	{
		// TODO Delete SlaveEntity and JoinedAttributes belonging to this view as well (cascading)
		dataService.delete(ViewMetaData.ENTITY_NAME,
				dataService.findAll(ViewMetaData.ENTITY_NAME, new QueryImpl().eq(ViewMetaData.NAME, viewName)));
	}

	@RequestMapping(value = "/save-new-view", method = POST)
	@ResponseBody
	public void saveNewView(@RequestParam(value = "viewName") String viewName,
			@RequestParam(value = "masterEntity") String masterEntityName,
			@RequestParam(value = "slaveEntity") String slaveEntityName,
			@RequestParam(value = "masterAttribute") String masterAttributeId,
			@RequestParam(value = "slaveAttribute") String slaveAttributeId) throws InterruptedException
	{
		Entity viewEntity = viewService.getViewEntity(viewName, masterEntityName);
		if (viewEntity == null)
		{
			viewService.createNewView(viewName, masterEntityName, slaveEntityName, masterAttributeId, slaveAttributeId);
		}
		else
		{
			Entity slaveEntity = viewService.getSlaveEntity(slaveEntityName);
			if (slaveEntity == null)
			{
				viewService.addNewSlaveEntityToExistingView(viewEntity, slaveEntityName, masterAttributeId,
						slaveAttributeId);
			}
			else
			{
				viewService.addNewAttributeMappingToExistingSlave(slaveEntityName, masterAttributeId, slaveAttributeId);
			}
		}
	}

}
