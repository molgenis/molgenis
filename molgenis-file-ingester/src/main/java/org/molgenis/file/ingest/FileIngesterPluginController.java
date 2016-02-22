package org.molgenis.file.ingest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.data.DataService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(FileIngesterPluginController.URI)
public class FileIngesterPluginController extends MolgenisPluginController
{
	public static final String ID = "fileingest";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;

	@Autowired
	public FileIngesterPluginController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-file-ingest";
	}

	public void runNow()
	{

	}
}
