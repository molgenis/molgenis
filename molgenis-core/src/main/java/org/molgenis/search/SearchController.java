package org.molgenis.search;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller wrapper for the SearchService.
 * 
 * 
 * @author erwin
 * 
 */
@Scope(SCOPE_REQUEST)
@Controller
@RequestMapping("/search")
public class SearchController
{
	@Autowired
	private SearchService searchService;

	@Autowired
	private Database database;

	@RequestMapping(method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<SearchResult> search(@RequestBody
	SearchRequest request)
	{
		SearchResult result;
		try
		{
			result = searchService.search(request);
		}
		catch (Exception e)
		{
			result = new SearchResult(e.getMessage());
		}

		return new ResponseEntity<SearchResult>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/index", method = GET)
	@ResponseBody
	public String indexDatabase() throws DatabaseException
	{
		searchService.indexDatabase(database);
		return "Indexing done";
	}

}
