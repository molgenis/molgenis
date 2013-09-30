package org.molgenis.genomebrowser.controller;

import static org.molgenis.genomebrowser.controller.MutationController.URI;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.genomebrowser.services.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class MutationController extends MolgenisPluginController
{
	public static final String URI = "/plugin/mutation";
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public MutationService service;

	@Autowired
	public MutationController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/data")
	public @ResponseBody
	void getAll(HttpServletResponse response) throws ParseException, DatabaseException, IOException
	{
		PrintWriter p = response.getWriter();
		response.setContentType("text/plain");
		p.print(service.getData(""));
		p.close();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/data/{someID}")
	public @ResponseBody
	void getAttr(@PathVariable(value = "someID")
	String id, HttpServletResponse response) throws ParseException, DatabaseException, IOException
	{
		PrintWriter p = response.getWriter();
		response.setContentType("text/plain");
		p.print(service.getData(id));
		p.close();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		return "view-genomebrowser";
	}
}