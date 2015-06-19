package org.molgenis.hpofilter;

import static org.molgenis.hpofilter.HpoFilterController.URI;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class HpoFilterController extends MolgenisPluginController
{
	public static final String ID = "hpofilter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-hpofilter";
	private final DataService dataService;

	@Autowired
	public HpoFilterController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}
	
	@RequestMapping(URI)
	public String init(Model model)
	{
		return VIEW_NAME;
	}
}