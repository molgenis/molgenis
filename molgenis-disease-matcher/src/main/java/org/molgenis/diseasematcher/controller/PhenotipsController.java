package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.PhenotipsController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.diseasematcher.service.PhenotipsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the OMIM API service.
 * 
 * @author tommydeboer
 * 
 */
@Controller
@RequestMapping(BASE_URI)
public class PhenotipsController
{
	/**
	 * The URI to which this controller is mapped.
	 */
	public static final String BASE_URI = "/phenotips";

	private static final Logger logger = Logger.getLogger(OmimClientController.class);

	private PhenotipsService phenotipsService;

	private static final String CONTENT_TYPE_HTML = "text/html";
	private static final String CONTENT_TYPE_JSON = "application/json";

	@Autowired
	public PhenotipsController(PhenotipsService phenotipsService)
	{
		super();
		this.phenotipsService = phenotipsService;
	}

	@RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
	public void getPhenotipsRanking(@RequestParam("symptom") List<String> hpoTerms, HttpServletResponse response)
	{
		try
		{
			response.setContentType(CONTENT_TYPE_HTML);
			phenotipsService.getRanking(hpoTerms, response.getOutputStream());
		}
		catch (Exception e)
		{
			logger.error("Phenotips API query unsuccessfull", e);
			response.setContentType(CONTENT_TYPE_JSON);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}