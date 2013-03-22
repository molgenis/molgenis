package org.molgenis.dataexplorer.search;

import javax.annotation.Resource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.dataset.DataSetTable;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class that indexes all datasets for use in the dataexplorer
 * 
 * @author erwin
 * 
 */
public class DataSetsIndexer implements InitializingBean
{
	private SearchService searchService;
	private Database unauthorizedDatabase;

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Resource(name = "unauthorizedDatabase")
	public void setUnauthorizedDatabase(Database unauthorizedDatabase)
	{
		this.unauthorizedDatabase = unauthorizedDatabase;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
		if (unauthorizedDatabase == null) throw new IllegalArgumentException(
				"Missing bean of type Database with name 'unauthorizedDatabase'");
	}

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
