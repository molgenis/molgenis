package org.molgenis.search;

import java.util.List;

import org.molgenis.data.Query;
import org.molgenis.data.Repository;

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
	 * Indexes all entities in a repository
	 * 
	 * @param repository
	 */
	void indexRepository(Repository repository);

	void updateRepositoryIndex(Repository repository);

	/**
	 * delete documents by Ids
	 * 
	 * @param documentType
	 * @param documentId
	 */
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	/**
	 * update document by Id
	 * 
	 * @param documentType
	 * @param documentId
	 * @param updateScript
	 */
	void updateDocumentById(String documentType, String documentId, String updateScript);

	/**
	 * Search the index
	 * 
	 * @param request
	 * @return
	 */
	SearchResult search(SearchRequest request);

	SearchResult multiSearch(MultiSearchRequest request);

	/**
	 * Get the total hit count
	 * 
	 * @param documentType
	 * @param queryRules
	 * @return
	 */
	long count(String documentType, Query q);

	/**
	 * delete documentType from index
	 * 
	 * @param indexname
	 * @return boolean succeeded
	 */
	void deleteDocumentsByType(String documentType);

}
