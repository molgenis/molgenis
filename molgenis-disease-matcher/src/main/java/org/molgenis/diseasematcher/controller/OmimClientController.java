package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.OmimClientController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.diseasematcher.service.OmimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the OMIM API service.
 * 
 * @author tommydeboer
 * 
 */
@Controller
@RequestMapping(BASE_URI)
public class OmimClientController
{
	/**
	 * The URI to which this controller is mapped.
	 */
	public static final String BASE_URI = "/omim";

	private static final Logger logger = Logger.getLogger(OmimClientController.class);

	private OmimService omimService;

	private static final String CONTENT_TYPE_JSON = "application/json";

	/**
	 * Constructor for OmimClientController.
	 * 
	 * @param omimService
	 *            an instance of OmimService
	 */
	@Autowired
	public OmimClientController(OmimService omimService)
	{
		super();
		this.omimService = omimService;
	}

	/**
	 * Retrieves an OMIM JSON object using the OMIM API.
	 * 
	 * @param omimId
	 *            the OMIM identifier to search
	 * @param response
	 *            the OMIM JSON object
	 */
	@RequestMapping(value = "/{omimId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public void getOmimData(@PathVariable("omimId") String omimId, HttpServletResponse response)
	{
		try
		{
			response.setContentType(CONTENT_TYPE_JSON);
			omimService.getOmimData(omimId, response.getOutputStream());
		}
		catch (Exception e)
		{
			logger.error("OMIM API query unsuccessfull", e);
			response.setContentType(CONTENT_TYPE_JSON);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}