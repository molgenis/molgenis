package org.molgenis.genomebrowser.controller;

import static org.molgenis.genomebrowser.controller.GenomebrowserController.URI;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.genomebrowser.services.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class GenomebrowserController extends MolgenisPluginController
{
	public static final String ID = "genomebrowser";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
    public static final String GENOMEBROWSERTABLE = "genomeBrowserTable";

	private final MolgenisSettings molgenisSettings;
	public MutationService mutationService;

	@Autowired
	public GenomebrowserController(MolgenisSettings molgenisSettings, MutationService service)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
		this.mutationService = service;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute(INITLOCATION, molgenisSettings.getProperty(INITLOCATION));
		model.addAttribute(COORDSYSTEM, molgenisSettings.getProperty(COORDSYSTEM));
		model.addAttribute(CHAINS, molgenisSettings.getProperty(CHAINS));
		model.addAttribute(SOURCES, molgenisSettings.getProperty(SOURCES));
		model.addAttribute(BROWSERLINKS, molgenisSettings.getProperty(BROWSERLINKS));
		model.addAttribute(SEARCHENDPOINT, molgenisSettings.getProperty(SEARCHENDPOINT));
		model.addAttribute(KARYOTYPEENDPOINT, molgenisSettings.getProperty(KARYOTYPEENDPOINT));
        model.addAttribute(GENOMEBROWSERTABLE, molgenisSettings.getProperty(GENOMEBROWSERTABLE));

		return "view-genomebrowser";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/data", produces =
	{ MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	List<Map<String, String>> getAll(HttpServletResponse response, @RequestParam(value = "mutation", required = false)
	String mutationId, @RequestParam(value = "segment", required = true)
	String segmentId) throws ParseException, IOException
	{
		if (mutationId == null)
		{
			mutationId = "";
		}
		List<Map<String, String>> result = mutationService.getPatientMutationData(segmentId, mutationId);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/mutationdata", produces =
		{ MediaType.APPLICATION_JSON_VALUE })
		public @ResponseBody
		List<Map<String, String>> MutationData(HttpServletResponse response) throws ParseException, IOException
		{
			List<Map<String, String>> result = mutationService.getMutationData();
			return result;
		}
}