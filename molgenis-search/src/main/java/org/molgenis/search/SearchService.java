package org.molgenis.search;

import java.util.List;

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
	/**
	 * Check if a type exists in the index
	 * 
	 * @param documentType
	 * @return
	 */
	boolean documentTypeExists(String documentType);

	/**
	 * Insert or update entities in the index of a documentType
	 * 
	 * @param documentType
	 * @param entities
	 */
	void updateIndex(String documentType, Iterable<? extends Entity> entities);

	/**
	 * Index a TupleTable
	 * 
	 * @param documentType
	 *            , teh documentType name
	 * @param tupleTable
	 */
	void indexTupleTable(String documentType, TupleTable tupleTable);

	/**
	 * Search the index
	 * 
	 * @param request
	 * @return
	 */
	SearchResult search(SearchRequest request);

	/**
	 * Get the total hit count
	 * 
	 * @param documentType
	 * @param queryRules
	 * @return
	 */
	long count(String documentType, List<QueryRule> queryRules);
	
	/**
	 * delete documentType from index
	 * 
	 * @param indexname
	 * @return boolean succeeded
	 */
	void deleteDocumentsByType(String documentType);
}
