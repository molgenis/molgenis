package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.OmimClientController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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

	private static final Logger logger = Logger.getLogger(OmimClientController.class);

	@Value("${omim_key:@null}")
	private String apiKey;

	@RequestMapping(value = "/{omimId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public void getOmimData(@PathVariable("omimId") String omimId, HttpServletResponse response)
	{
		if (apiKey == null)
		{
			return;
		}

		try
		{
			String uri = "http://api.europe.omim.org/api/entry?mimNumber=" + omimId
					+ "&include=text&include=clinicalSynopsis&format=json&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");

			// String uri = "http://api.europe.omim.org/api/entry?mimNumber=" + omimId
			// + "&include=all&format=json&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");

			URL omimRequest = new URL(uri);
			response.setContentType("application/json");
			FileCopyUtils.copy(omimRequest.openStream(), response.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}