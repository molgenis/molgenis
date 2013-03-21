package org.molgenis.dataexplorer.controller;

import java.util.List;

import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.observ.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	@RequestMapping("/index")
	public String indexDataSets(Model model) throws DatabaseException, TableException
	{
		dataSetsIndexer.index();

		model.addAttribute("message", "Indexing done");
		List<DataSet> dataSets = database.find(DataSet.class);
		if (!dataSets.isEmpty())
		{
			model.addAttribute("dataset", dataSets.get(0));
		}

		return "explorer";
	}

}
