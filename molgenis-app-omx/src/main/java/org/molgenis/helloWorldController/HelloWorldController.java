package org.molgenis.helloWorldController;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.molgenis.helloWorldController.HelloWorldController.URI;

@Controller
@RequestMapping(URI)
public class HelloWorldController extends MolgenisPluginController
{
	public static final String ID = "helloworld";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
public HelloWorldController()
	{
		super(URI);
		// TODO Auto-generated constructor stub
	}

@RequestMapping(method = GET)
public String init(Model model){
	model.addAttribute("naam", "Marieke");
	return "view-HelloWorld";
}

@RequestMapping("/2")
public String init(@RequestParam("voornaam") String voornaam, Model model){
	model.addAttribute("naam", voornaam);
	return "view-HelloWorld";
}
}
