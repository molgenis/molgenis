package org.molgenis.script;

import org.molgenis.data.DataService;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static java.util.stream.Collectors.toList;
import static org.molgenis.script.ScriptMetaData.SCRIPT;
import static org.molgenis.script.ScriptParameterMetaData.SCRIPT_PARAMETER;
import static org.molgenis.script.ScriptPluginController.URI;

@Controller
@RequestMapping(URI)
public class ScriptPluginController extends PluginController
{
	public static final String ID = "scripts";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
	private final DataService dataService;

	public ScriptPluginController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String listScripts(Model model)
	{

		model.addAttribute("scripts", dataService.findAll(SCRIPT, Script.class).collect(toList()));
		model.addAttribute("parameters",
				dataService.findAll(SCRIPT_PARAMETER, ScriptParameter.class).collect(toList()));
		return "view-scripts";
	}
}
