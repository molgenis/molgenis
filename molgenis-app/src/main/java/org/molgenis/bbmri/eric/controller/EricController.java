package org.molgenis.bbmri.eric.controller;

import static org.molgenis.bbmri.eric.controller.EricController.BASE_URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.bbmri.eric.service.NlToEricConverter;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(BASE_URI)
public class EricController
{
	public static final String BASE_URI = "/bbmri";
	private NlToEricConverter nlToEricConverter;

	@Autowired
	public EricController(DataService dataService, NlToEricConverter nlToEricConverter)
	{
		if (nlToEricConverter == null) throw new IllegalArgumentException("nlToEricConverter is null");

		this.nlToEricConverter = nlToEricConverter;
	}

	@RequestMapping(value = "/convert", method = GET)
	@ResponseBody
	public void convert()
	{
		nlToEricConverter.convertNlToEric();
	}
}
