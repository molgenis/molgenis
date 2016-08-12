package org.molgenis.ui;

import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;

@Controller
public class LogoController
{
	private final FileStore fileStore;

	@Autowired
	public LogoController(FileStore fileStore)
	{
		this.fileStore = fileStore;
	}

	/**
	 * Get a file from the logo subdirectory of the filestore
	 *
	 * @param out
	 * @param name
	 * @param extension
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/logo/{name}.{extension}")
	public void getLogo(OutputStream out, @PathVariable("name") String name,
			@PathVariable("extension") String extension, HttpServletResponse response) throws IOException
	{
		File f = fileStore.getFile("/logo/" + name + "." + extension);
		if (!f.exists())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Try to get contenttype from file
		String contentType = URLConnection.guessContentTypeFromName(f.getName());
		if (contentType != null)
		{
			response.setContentType(contentType);
		}

		FileCopyUtils.copy(new FileInputStream(f), out);
	}
}
