package org.molgenis.core.ui.style;

import org.apache.commons.io.IOUtils;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.style.BootstrapVersion.BOOTSTRAP_VERSION_3;
import static org.molgenis.core.ui.style.BootstrapVersion.BOOTSTRAP_VERSION_4;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class StyleController
{
	private final StyleService styleService;

	public StyleController(StyleService styleService)
	{
		this.styleService = requireNonNull(styleService);
	}

	@GetMapping("/css/bootstrap-{bootstrap-version}/{theme}")
	public ResponseEntity getThemeCss(@PathVariable("bootstrap-version") String bootstrapVersion,
			@PathVariable("theme") String theme, HttpServletResponse response) throws MolgenisStyleException
	{
		response.setHeader("Content-Type", "text/css");
		response.setHeader("Cache-Control", "max-age=31536000");

		final String themeName = theme.endsWith(".css") ? theme : theme + ".css";

		BootstrapVersion version = bootstrapVersion.equals("4") ? BOOTSTRAP_VERSION_4 : BOOTSTRAP_VERSION_3;
		Resource styleSheetResource = styleService.getThemeData(themeName, version);

		try
		{
			InputStream inputStream = styleSheetResource.getInputStream();
			IOUtils.copy(inputStream, response.getOutputStream());
			IOUtils.closeQuietly(inputStream);
			response.flushBuffer();
		}
		catch (IOException e)
		{
			throw new MolgenisStyleException("Unable to return theme data", e);
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	@ResponseBody
	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler({ MolgenisStyleException.class, IOException.class })
	public ErrorMessageResponse handleStyleException(Exception e)
	{
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

}
