package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.MultiSearchRequest;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;

public interface SearchService
{

	/**
	 * Returns all type names for this index
	 * 
	 * @return
	 */
	Iterable<String> getTypes();

	/**
	 * @deprecated see search(Query, EntityMetaData) or aggregate(AggregateQuery, EntityMetaData)
	 */
	@Deprecated
	SearchResult search(SearchRequest request);

	// TODO this method is only used by BiobankConnect and should be removed in the future
	SearchResult multiSearch(MultiSearchRequest request);

	/**
	 * @deprecated see count(Query, EntityMetaData)
	 */
	@Deprecated
	long count(String documentType, Query q);

	// TODO this method is only used by BiobankConnect and should be removed in
	// the future
	SearchResult multiSearch(SearchType searchType, MultiSearchRequest request);

	/**
	 * @deprecated see index(Iterable<Entity>, EntityMetaData)
	 */
	@Deprecated
	void indexRepository(Repository repository);

	boolean documentTypeExists(String documentType);

	/**
	 * @deprecated see delete(EntityMetaData)
	 */
	@Deprecated
	void deleteDocumentsByType(String documentType);

	/**
	 * @deprecated see delete(Iterable<Entity>, EntityMetaData)
	 */
	@Deprecated
	void deleteDocumentByIds(String documentType, List<String> documentIds);

	void updateRepositoryIndex(Repository repository);

	void updateDocumentById(String documentType, String documentId, String updateScript);

	boolean hasMapping(Repository repository);

	boolean hasMapping(EntityMetaData entityMetaData);

	/**
	 * @deprecated see createMappings(EntityMetaData)
	 */
	@Deprecated
	void createMappings(Repository repository, boolean storeSource, boolean enableNorms, boolean createAllIndex)
			throws IOException;

	void createMappings(EntityMetaData entityMetaData) throws IOException;

	void createMappings(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms, boolean createAllIndex)
			throws IOException;

	/**
	 * Refresh index, making all operations performed since the last refresh available for search
	 */
	void refresh();

	long count(EntityMetaData entityMetaData);

	long count(Query q, EntityMetaData entityMetaData);

	void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode);

	void index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode);

	void delete(Entity entity, EntityMetaData entityMetaData);

	void deleteById(String id, EntityMetaData entityMetaData);

	void deleteById(Iterable<String> ids, EntityMetaData entityMetaData);

	void delete(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);

	void delete(String entityName);

	/**
	 * Returns entity with given id or null if entity does not exist
	 * 
	 * @param entityId
	 * @param entityMetaData
	 * @return
	 */
	Entity get(Object entityId, EntityMetaData entityMetaData);

	Iterable<Entity> get(Iterable<Object> entityIds, EntityMetaData entityMetaData);

	// TODO replace Iterable<Entity> with EntityCollection and add EntityCollection.getTotal()
	Iterable<Entity> search(Query q, EntityMetaData entityMetaData);

	AggregateResult aggregate(AggregateQuery aggregateQuery, EntityMetaData entityMetaData);

	/**
	 * Frees memory from the index by flushing data to the index storage and clearing the internal transaction log
	 */
	void flush();

	void rebuildIndex(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);

	Hit searchById(String documentType, String id);

}