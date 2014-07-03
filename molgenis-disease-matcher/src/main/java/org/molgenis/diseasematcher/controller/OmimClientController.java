package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.OmimClientController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(BASE_URI)
public class OmimClientController
{
	/**
	 * The URI to which this controller is mapped.
	 */
	public static final String BASE_URI = "/omim";

	private OmimService omimService;

	/**
	 * 
	 * @param keysCSV
	 */
	@Autowired
	public OmimClientController(OmimService omimService)
	{
		super();
		this.omimService = omimService;
	}

	/**
	 * 
	 * @param omimId
	 * @param response
	 */
	@RequestMapping(value = "/{omimId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public void getOmimData(@PathVariable("omimId") String omimId, HttpServletResponse response)
	{
		try
		{
			response.setContentType("application/json");
			omimService.getOmimData(omimId, response.getOutputStream());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}