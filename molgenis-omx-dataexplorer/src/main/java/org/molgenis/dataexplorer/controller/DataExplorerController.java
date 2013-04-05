package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;

import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller class for the data explorer.
 * 
 * The implementation javascript file for the resultstable is defined in a
 * MolgenisSettings property named 'dataexplorer.resultstable.js' posible values
 * are '/js/SingleObservationSetTable.js' or '/js/MultiObservationSetTable.js'
 * with '/js/MultiObservationSetTable.js' as the default
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Controller
@RequestMapping(URI)
public class DataExplorerController
{
	public static final String URI = "/plugin/dataexplorer";

	@Autowired
	private DataSetsIndexer dataSetsIndexer;

	@Autowired
	private Database database;

	@Autowired
	private MolgenisSettings molgenisSettings;

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{

		String resultsTableJavascriptFile = molgenisSettings.getProperty("dataexplorer.resultstable.js",
				"/js/MultiObservationSetTable.js");

		model.addAttribute("resultsTableJavascriptFile", resultsTableJavascriptFile);

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
