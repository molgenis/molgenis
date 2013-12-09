/**
 * 
 */
package org.molgenis.variome;

import static org.molgenis.variome.VariomeController.URI;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller wrapper for the VariomeService.
 * 
 * @author Mark-de-Haan
 * 
 */
@Controller
@RequestMapping(URI)
public class VariomeController extends MolgenisPluginController{
	
	private static final Logger logger = Logger.getLogger(VariomeController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "variome";

	private final VariomeService pluginVariomeService;
	
	@Autowired
	public VariomeController(VariomeService pluginVariomeService)
	{
		super(URI);
		if (pluginVariomeService == null) throw new IllegalArgumentException(
				"PluginVariomeService is null");
		this.pluginVariomeService = pluginVariomeService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model){
		return "view-variome";
	}
	
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
