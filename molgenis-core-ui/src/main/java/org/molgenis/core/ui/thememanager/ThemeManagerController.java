package org.molgenis.core.ui.thememanager;

import org.molgenis.core.ui.style.MolgenisStyleException;
import org.molgenis.core.ui.style.Style;
import org.molgenis.core.ui.style.StyleService;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.PluginController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Collections.singletonList;
import static org.molgenis.core.ui.thememanager.ThemeManagerController.URI;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
@RequestMapping(URI)
public class ThemeManagerController extends PluginController
{
	public static final String ID = "thememanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final StyleService styleService;

	public ThemeManagerController(StyleService styleService)
	{
		super(URI);
		if (styleService == null) throw new IllegalArgumentException("styleService is null");
		this.styleService = styleService;
	}

	@GetMapping
	public String init(Model model)
	{
		if (styleService.getSelectedStyle() != null)
			model.addAttribute("selectedStyle", styleService.getSelectedStyle().getName());
		model.addAttribute("availableStyles", styleService.getAvailableStyles());

		return "view-thememanager";
	}

	/**
	 * Set a new bootstrap theme
	 */
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@PostMapping(value = "/set-bootstrap-theme", produces = "application/json", consumes = "application/json")
	public @ResponseBody
	void setBootstrapTheme(@Valid @RequestBody String styleName)
	{
		styleService.setSelectedStyle(styleName);
	}

	/**
	 * Add a new bootstrap theme, theme is passed as a bootstrap css file.
	 * It is mandatory to pass a bootstrap3 style file but optional to pass a bootstrap 4 style file
	 */
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@PostMapping(value = "/add-bootstrap-theme")
	public @ResponseBody
	Style addBootstrapTheme(@RequestParam(value = "bootstrap3-style") MultipartFile bootstrap3Style,
			@RequestParam(value = "bootstrap4-style", required = false) MultipartFile bootstrap4Style)
			throws MolgenisStyleException
	{
		String styleIdentifier = bootstrap3Style.getOriginalFilename();
		try
		{
			String bs4FileName = null;
			InputStream bs4InputStream = null;
			if (bootstrap4Style != null)
			{
				bs4FileName = bootstrap4Style.getOriginalFilename();
				bs4InputStream = bootstrap4Style.getInputStream();
			}
			return styleService.addStyle(styleIdentifier, bootstrap3Style.getOriginalFilename(),
					bootstrap3Style.getInputStream(), bs4FileName, bs4InputStream);
		}
		catch (IOException e)
		{
			throw new MolgenisStyleException("unable to add style: " + styleIdentifier, e);
		}
	}

	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ MolgenisStyleException.class })
	public ErrorMessageResponse handleStyleException(Exception e)
	{
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}
}
