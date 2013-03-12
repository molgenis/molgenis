package org.molgenis.search;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Scope(SCOPE_REQUEST)
@Controller
@RequestMapping("/search")
public class SearchController
{
	@Autowired
	private SearchService searchService;

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
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

}
