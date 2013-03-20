package org.molgenis.dataexplorer.search;

import javax.annotation.Resource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.dataset.DataSetTable;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class that indexes all datasets for use in the dataexplorer
 * 
 * @author erwin
 * 
 */
public class DataSetsIndexer
{
	@Autowired
	private SearchService searchService;

	@Resource(name = "unauthorizedDatabase")
	private Database unauthorizedDatabase;

	public void index() throws DatabaseException, TableException
	{
		for (DataSet dataSet : unauthorizedDatabase.find(DataSet.class))
		{
			index(dataSet);
		}
	}

	private void index(DataSet dataSet) throws TableException
	{
		searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, unauthorizedDatabase));
	}
}
