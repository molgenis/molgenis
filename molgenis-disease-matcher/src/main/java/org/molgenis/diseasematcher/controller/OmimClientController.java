package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.OmimClientController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
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

	// TODO hide api key
	private static final String API_KEY = "CF5DAEF52E82034FEB69333BB4A1B1FCB27B8F96";

	@RequestMapping(value = "/{omimId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public void getOmimData(@PathVariable("omimId") String omimId, HttpServletResponse response) throws Exception
	{
		String uri = "http://api.europe.omim.org/api/entry?mimNumber=" + omimId + "&include=all&format=json&apiKey="
				+ URLEncoder.encode(API_KEY, "UTF-8");

		URL omimRequest = new URL(uri);
		response.setContentType("application/json");
		FileCopyUtils.copy(omimRequest.openStream(), response.getOutputStream());
	}

}