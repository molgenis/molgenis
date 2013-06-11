package org.molgenis.omx.harmonizationIndexer.controller;

import static org.molgenis.omx.harmonizationIndexer.controller.HarmonizationIndexerController.URI;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public IndexResponse getAvailableIndexer()
	{
		return null;
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

	class IndexResponse
	{
		private String name;

	}
}
