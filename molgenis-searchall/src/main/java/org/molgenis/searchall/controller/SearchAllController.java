package org.molgenis.searchall.controller;

import org.molgenis.searchall.model.Result;
import org.molgenis.searchall.service.SearchAllService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Objects.requireNonNull;
import static org.molgenis.searchall.controller.SearchAllController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(BASE_URI)
public class SearchAllController
{
	public static final String BASE_URI = "/api/searchall/";
	private final SearchAllService searchAllService;

	public SearchAllController(SearchAllService searchAllService)
	{
		this.searchAllService = requireNonNull(searchAllService);

	}

	@GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Result searchAll(@RequestParam(value = "term") String searchterm)
	{
		return searchAllService.searchAll(searchterm);
	}
}
