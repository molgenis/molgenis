package org.molgenis.dataexplorer.controller;

import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller class for the data explorer
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Controller
@RequestMapping("/plugin/dataexplorer")
public class DataExplorerController
{
	@Autowired
	private DataSetsIndexer dataSetsIndexer;

	@Autowired
	private Database database;

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		return "dataexplorer";
	}

	/**
	 * When someone directly accesses /dataexplorer and is not logged in an
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
