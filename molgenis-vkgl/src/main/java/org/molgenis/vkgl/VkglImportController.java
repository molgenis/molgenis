package org.molgenis.vkgl;

import java.io.File;
import java.io.IOException;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(VkglImportController.URI)
public class VkglImportController extends MolgenisPluginController
{
	public static final String ID = "vkgl";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final VkglImportService vkglImportService;

	@Autowired
	public VkglImportController(VkglImportService vkglImportService)
	{
		super(URI);
		this.vkglImportService = vkglImportService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-vkgl-import";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String importVcfFile(@RequestParam(value = "filename", required = true) String filename) throws IOException
	{
		vkglImportService.importVcf(new File(filename));
		return init();
	}
}
