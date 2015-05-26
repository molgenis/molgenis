package org.molgenis.app.controller;

import static org.molgenis.app.controller.QueryTestController.URI;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that handles home page requests
 */
@RestController
@RequestMapping(URI)
public class QueryTestController
{
	public static final String URI = "/querytest";

	@Autowired
	DataService dataService;

	@RequestMapping
	public String testQuery()
	{
		// filter on root level:

		Query q = new QueryImpl().search("house.address", "Berendsen");

		System.out.println("###### Query written in code   : " + q);

		Iterable<Entity> t = dataService.findAll("persons", q);
		System.out.println(t.iterator().next());

		return null;
	}
}
