package org.molgenis.omx.mobile.catalogue;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/mobile/catalogue")
public class CatalogueController
{
	/**
	 * Show the multi page template
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "molgenis-mobile-catalogue";
	}

}
