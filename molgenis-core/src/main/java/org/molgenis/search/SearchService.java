package org.molgenis.search;

import java.util.List;

import org.molgenis.framework.db.QueryRule;
import org.molgenis.util.Entity;

/**
 * Interface that a concrete SearchService must implement.
 * 
 * @author erwin
 * 
 */
public interface SearchService
{
	void updateIndex(String documentName, Iterable<Entity> entities);

	SearchResult search(SearchRequest request);

	long count(String documentName, List<QueryRule> queryRules);
}
