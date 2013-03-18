package org.molgenis.search;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.util.Entity;

/**
 * Interface that a concrete SearchService must implement.
 * 
 * @author erwin
 * 
 */
public interface SearchService
{
	void indexDatabase(Database db) throws DatabaseException;

	void updateIndex(String documentName, Iterable<? extends Entity> entities);

	void indexTupleTable(String documentName, TupleTable tupleTable);

	SearchResult search(SearchRequest request);

	long count(String documentName, List<QueryRule> queryRules);
}
