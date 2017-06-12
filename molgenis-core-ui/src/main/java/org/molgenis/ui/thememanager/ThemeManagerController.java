package org.molgenis.ui.thememanager;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.style.StyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

import static org.molgenis.ui.thememanager.ThemeManagerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class ThemeManagerController extends MolgenisPluginController
{
	public static final String ID = "thememanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final StyleService styleService;

	@Autowired
	public ThemeManagerController(StyleService styleService)
	{
		super(URI);
		if (styleService == null) throw new IllegalArgumentException("styleService is null");
		this.styleService = styleService;
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		if (styleService.getSelectedStyle() != null)
			model.addAttribute("selectedStyle", styleService.getSelectedStyle().getName());
		model.addAttribute("availableStyles", styleService.getAvailableStyles());

		return "view-thememanager";
	}

	/**
	 * Set a new bootstrap theme
	 *
	 * @param styleName
	 */
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@RequestMapping(value = "/set-bootstrap-theme", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public @ResponseBody
	void setBootstrapTheme(@Valid @RequestBody String styleName)
	{
		styleService.setSelectedStyle(styleName);
	}
}
