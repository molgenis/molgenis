package org.molgenis.compute.db.controller;

import org.molgenis.compute.db.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Scope("request")
@Controller
@RequestMapping("/api/v1")
public class ApiController
{
	private final RunService runService;

	@Autowired
	public ApiController(RunService runService)
	{
		this.runService = runService;
	}

}
