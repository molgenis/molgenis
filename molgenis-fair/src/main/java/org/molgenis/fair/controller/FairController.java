package org.molgenis.fair.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.converter.RDFMediaType;
import org.molgenis.ui.model.SubjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(BASE_URI)
public class FairController
{
	static final String BASE_URI = "/fdp";

	private final DataService dataService;

	@Autowired
	public FairController(DataService dataService)
	{
		this.dataService = dataService;
	}
	}

	@RequestMapping(method = GET, produces = RDFMediaType.TEXT_TURTLE_VALUE)
	@ResponseBody
	@RunAsSystem
	public SubjectEntity getMetadata()
	{
		return new SubjectEntity("http://molgenis01.gcc.rug.nl/fdp", dataService.findOne("fdp_Metadata", new QueryImpl<>()));
	}
}
