package org.molgenis.genomebrowser.controller;

import static org.molgenis.genomebrowser.controller.GenomebrowserController.URI;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.genomebrowser.services.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class GenomebrowserController extends MolgenisPluginController
{
	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM   = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS   = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
	
	public static final String URI = "/plugin/genomebrowser";
	private final MolgenisSettings molgenisSettings;
	
	@Autowired
	public GenomebrowserController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{	
		model.addAttribute(INITLOCATION,molgenisSettings.getProperty(INITLOCATION));
		model.addAttribute(COORDSYSTEM,molgenisSettings.getProperty(COORDSYSTEM));  
		model.addAttribute(CHAINS,molgenisSettings.getProperty(CHAINS));
		model.addAttribute(SOURCES,molgenisSettings.getProperty(SOURCES));
		model.addAttribute(BROWSERLINKS,molgenisSettings.getProperty(BROWSERLINKS));  
		model.addAttribute(SEARCHENDPOINT,molgenisSettings.getProperty(SEARCHENDPOINT));
		model.addAttribute(KARYOTYPEENDPOINT,molgenisSettings.getProperty(KARYOTYPEENDPOINT));

		return "view-genomebrowser";
	}
}