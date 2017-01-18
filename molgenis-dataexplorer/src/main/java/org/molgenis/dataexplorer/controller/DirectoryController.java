package org.molgenis.dataexplorer.controller;

import com.google.api.client.util.Maps;
import org.molgenis.dataexplorer.directory.DirectorySettings;
import org.molgenis.dataexplorer.directory.NegotiatorQuery;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.dataexplorer.controller.DirectoryController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class DirectoryController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);
	public static final String URI = "/directory";

	private DirectorySettings settings;

	@Autowired
	public DirectoryController(DirectorySettings settings)
	{
		this.settings = requireNonNull(settings);
	}

	@RequestMapping(value = "/export", method = POST)
	@ResponseBody
	public String exportToNegotiator(@RequestBody NegotiatorQuery query) throws Exception
	{
		LOG.info("NegotiatorQuery\n\n" + query + "\n\nreceived, sending request");

		Map<String, Object> map = Maps.newHashMap();
		map.put("URL", query.getURL());
		map.put("collections", query.getCollections());
		map.put("humanReadable", query.getHumanReadable());
		map.put("nToken", query.getnToken());

		String username = settings.getString(DirectorySettings.USERNAME);
		String password = settings.getString(DirectorySettings.PASSWORD);

		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", generateBase64Authentication(username, password));

		HttpEntity entity = new HttpEntity(map, headers);

		try
		{
			LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", settings.getString(DirectorySettings.NEGOTIATOR_URL));
			String redirectURL = template.postForLocation(settings.getString(DirectorySettings.NEGOTIATOR_URL), entity)
					.toASCIIString();
			LOG.trace("Redirecting to " + redirectURL);
			return redirectURL;
		}
		catch (Exception e)
		{
			LOG.error("Posting to the directory went wrong: ", e);
			throw e;
		}
	}

	/**
	 * Generate base64 authentication based on settings
	 *
	 * @return String
	 */
	private static String generateBase64Authentication(String username, String password)
	{
		requireNonNull(username, password);
		String userPass = username + ":" + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
		return "Basic " + userPassBase64;
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}
