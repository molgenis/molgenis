package org.molgenis.vortext;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.servlet.http.HttpSession;

import org.molgenis.file.FileMeta;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping(SpaPluginController.URI)
public class SpaPluginController extends MolgenisPluginController
{
	public static final String ID = "textmining";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VORTEX_URL = "https://www.dropbox.com/s/f4q0jpmiidgpq4u/Arlier%20Z%20-%20J%20Child%20Neurol%20-%202010.json?dl=1";// "https://www.dropbox.com/s/gb6e206azdbf9dm/vortext.json?dl=1";
	private static final String SESSION_ATTR_FILE_META_ID = "publicationPdfFileMetaId";
	private static final Charset CHARSET = Charset.forName("UTF-8");

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
	public String view(Model model, HttpSession session)
	{
		return "view-spa";
	}

	@RequestMapping("/annotate")
	public void annotate(InputStream pdf, @RequestParam("filename") String filename, @RequestParam("size") long size,
			OutputStream out, HttpSession session) throws IOException
	{
		// Save pdf to filestore
		String baseUri = ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(null).build().toUriString();
		FileMeta fileMeta = publicationService.savePdf(baseUri, filename, size, pdf);

		// Save file id to session for later
		session.setAttribute(SESSION_ATTR_FILE_META_ID, fileMeta.getId());

		// Call vertext server to annotate pdf
		byte[] json = annotateWithVortext(fileMeta);

		// Save the annotations
		try (Reader r = new InputStreamReader(new ByteArrayInputStream(json), CHARSET))
		{
			Marginalia marginalia = vortexResponseParser.parse(r);
			publicationService.saveMarginalia(fileMeta.getId(), marginalia);
		}

		// Write json to client
		out.write(json);
	}

	@RequestMapping(value = "/{fileMetaId}/annotations", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Marginalia getMarginalia(@PathVariable("fileMetaId") String fileMetaId, HttpSession session)
	{
		// Save file id to session for later
		session.setAttribute(SESSION_ATTR_FILE_META_ID, fileMetaId);

		return publicationService.getMarginalia(fileMetaId);
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseStatus(OK)
	public void saveMarginalia(@RequestBody Marginalia marginalia, HttpSession session)
	{
		String fileMetaId = (String) session.getAttribute(SESSION_ATTR_FILE_META_ID);
		if (fileMetaId != null)
		{
			publicationService.saveMarginalia(fileMetaId, marginalia);
		}
	}

	private byte[] annotateWithVortext(FileMeta fileMeta) throws MalformedURLException, IOException
	{
		URLConnection connection = new URL(VORTEX_URL).openConnection();
		connection.setDoOutput(true);

		// TODO uncomment and test if vortex server is up and running
		// Write pdf
		// File pdf = fileStore.getFile(fileMeta.getId());
		// FileCopyUtils.copy(new BufferedInputStream(new FileInputStream(pdf)), connection.getOutputStream());

		return FileCopyUtils.copyToByteArray(new BufferedInputStream(connection.getInputStream()));
	}
}
