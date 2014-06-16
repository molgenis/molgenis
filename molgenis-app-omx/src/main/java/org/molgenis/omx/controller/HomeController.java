package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.HomeController.URI;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.DatabaseAction;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.importer.EntityImporterController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends AbstractStaticContentController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	private EntityImporterController importer;

	public HomeController()
	{
		super(ID, URI);
	}

	@RequestMapping(value = "/vcf", method = RequestMethod.GET)
	public void test()
	{
		File file = new File(
				"C:\\Users\\Dennis\\Documents\\projects\\variantbrowser\\CardioDataCollection_31mar2014\\UMCG_Diagnostics_Cardio_Batch1_106Samples.vcf");
		try
		{
			importer.handleImportRequest(file, DatabaseAction.ADD);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
