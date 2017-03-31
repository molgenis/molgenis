package org.molgenis.dataexplorer.controller;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.dataexplorer.directory.DirectorySettings;
import org.molgenis.dataexplorer.directory.NegotiatorQuery;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.MolgenisPluginController;
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

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.molgenis.dataexplorer.controller.DirectoryController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class DirectoryController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);
	public static final String ID = "directory";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DirectorySettings settings;
	private final RestTemplate restTemplate;
	private final MolgenisPermissionService permissions;

	@Autowired
	public DirectoryController(DirectorySettings settings, RestTemplate restTemplate,
			MolgenisPermissionService permissions)
	{
		super(URI);
		this.settings = requireNonNull(settings);
		this.restTemplate = requireNonNull(restTemplate);
		this.permissions = requireNonNull(permissions);
	}

	@RunAsSystem
	public boolean showDirectoryButton(String selectedEntityName)
	{
		if (!permissions.hasPermissionOnPlugin(ID, Permission.READ))
		{
			return false;
		}
		final EntityType collectionEntityType = settings.getCollectionEntityType();
		return collectionEntityType != null && collectionEntityType.getId().equals(selectedEntityName);
	}

	@RequestMapping(value = "/export", method = POST)
	@ResponseBody
	public String exportToNegotiator(@RequestBody NegotiatorQuery query) throws Exception
	{
		LOG.info("NegotiatorQuery\n\n" + query + "\n\nreceived, sending request");

		String username = settings.getUsername();
		String password = settings.getPassword();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", generateBase64Authentication(username, password));

		HttpEntity<NegotiatorQuery> entity = new HttpEntity<>(query, headers);

		try
		{
			LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", settings.getNegotiatorURL());
			String redirectURL = restTemplate.postForLocation(settings.getNegotiatorURL(), entity).toASCIIString();
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
	 * Generate base64 authentication based on username and password.
	 *
	 * @return Authentication header value.
	 */
	private static String generateBase64Authentication(String username, String password)
	{
		requireNonNull(username, password);
		String userPass = username + ":" + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(UTF_8));
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
