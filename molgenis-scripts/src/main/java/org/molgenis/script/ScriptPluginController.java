package org.molgenis.script;

import static org.molgenis.script.ScriptPluginController.URI;

import org.molgenis.data.DataService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class ScriptPluginController extends MolgenisPluginController
{
	public static final String ID = "scripts";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final DataService dataService;

	@Autowired
	public ScriptPluginController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String listScripts(Model model)
	{

		model.addAttribute("scripts", Lists.newArrayList(dataService.findAll(Script.ENTITY_NAME, Script.class)));
		model.addAttribute("parameters",
				Lists.newArrayList(dataService.findAll(ScriptParameter.ENTITY_NAME, ScriptParameter.class)));
		return "view-scripts";
	}
}
