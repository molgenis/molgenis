package org.molgenis.vortext;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

//TODO url permissions
@Controller
@RequestMapping(SpaPluginController.URI)
public class SpaPluginController extends MolgenisPluginController
{
	public static final String ID = "textmining";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VORTEX_URL = "https://www.dropbox.com/s/f4q0jpmiidgpq4u/Arlier%20Z%20-%20J%20Child%20Neurol%20-%202010.json?dl=1";// "https://www.dropbox.com/s/gb6e206azdbf9dm/vortext.json?dl=1";
	private final PublicationService publicationService;
	private final VortexResponseParser vortexResponseParser;

	@Autowired
	public SpaPluginController(PublicationService publicationService, VortexResponseParser vortexResponseParser)
	{
		super(URI);
		this.publicationService = publicationService;
		this.vortexResponseParser = vortexResponseParser;
	}

	@RequestMapping
	public String view()
	{
		return "view-spa";
	}

	@RequestMapping("/upload")
	public void upload(OutputStream out) throws MalformedURLException, IOException
	{
		FileCopyUtils.copy(new URL(VORTEX_URL).openStream(), out);
	}

	// @ResponseStatus(OK)
	@RequestMapping(value = "/updatePublication/{publicationId}", method = POST)
	public void updatePublication(@PathVariable("publicationId") String publicationId, HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException, IOException
	{
		if (!publicationService.exists(publicationId))
		{
			response.setStatus(404);
			return;
		}

		try (Reader in = new InputStreamReader(request.getInputStream(), "UTF-8"))
		{
			List<AnnotationGroup> annotationGroups = vortexResponseParser.parse(in);
			publicationService.addAnnotationGroups(publicationId, annotationGroups);
		}
	}
}
