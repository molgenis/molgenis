package org.molgenis.lifelines.controller;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Scope(SCOPE_REQUEST)
@Controller
@RequestMapping("/indexer")
public class IndexerController
{
	@Autowired
	private Database database;

	@Autowired
	private SearchService searchService;

	@RequestMapping("/datasets")
	@ResponseBody
	public String indexDataSets() throws DatabaseException, TableException
	{
		// for (DataSet dataSet : database.find(DataSet.class))
		// {
		// TupleTable tupleTable = new DataSetTable(dataSet, database);
		// searchService.indexTupleTable(dataSet.getName(), tupleTable);
		// }

		return "Done";
	}
}
