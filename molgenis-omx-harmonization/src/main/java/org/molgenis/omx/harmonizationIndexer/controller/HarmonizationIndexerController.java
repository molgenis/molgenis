package org.molgenis.omx.harmonizationIndexer.controller;

import static org.molgenis.omx.harmonizationIndexer.controller.HarmonizationIndexerController.URI;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(URI)
public class HarmonizationIndexerController
{
	public static final String URI = "/plugin/ontologyindexer";

	@Autowired
	private Database database;

	/**
	 * Show the harmonization page
	 * 
	 * @param model
	 * @return the view name
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		return "Hamronization-indexer";
	}

	/**
	 * When someone directly accesses /harmonization and is not logged in an
	 * DataAccessException is thrown, redirect him to the home page
	 * 
	 * @return
	 */
	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated()
	{
		return "redirect:/";
	}
}
