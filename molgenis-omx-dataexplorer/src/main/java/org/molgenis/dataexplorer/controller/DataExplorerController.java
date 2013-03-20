package org.molgenis.dataexplorer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller class for the data explorer
 * 
 * @author erwin
 * 
 */
@Controller
@RequestMapping("/explorer")
public class DataExplorerController
{
	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("message", "Hey dude");
		return "explorer";
	}
}
