package org.molgenis.core.ui.menumanager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.util.FileUploadUtils;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Part;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.molgenis.core.ui.menumanager.MenuManagerController.URI;
import static org.springframework.http.HttpStatus.OK;

/**
 * Plugin to view and modify the app UI menu
 */
@Controller
@RequestMapping(URI)
public class MenuManagerController extends PluginController
{
	public static final String ID = "menumanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final MenuManagerService menuManagerService;
	private final FileStore fileStore;
	private final Ui molgenisUi;
	private final AppSettings appSettings;

	private static final String ERRORMESSAGE_LOGO = "The logo needs to be an image file like png or jpg.";

	public MenuManagerController(MenuManagerService menuManagerService, FileStore fileStore, Ui molgenisUi,
			AppSettings appSettings)
	{
		super(URI);
		if (menuManagerService == null) throw new IllegalArgumentException("menuManagerService is null");
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		if (fileStore == null) throw new IllegalArgumentException("fileStore is null");
		if (appSettings == null) throw new IllegalArgumentException("appSettings is null");
		this.menuManagerService = menuManagerService;
		this.molgenisUi = molgenisUi;
		this.fileStore = fileStore;
		this.appSettings = appSettings;
	}

	@GetMapping
	public String init(Model model)
	{
		List<UiMenuItem> menus = new TreeTraverser<UiMenuItem>()
		{
			@Override
			public Iterable<UiMenuItem> children(UiMenuItem root)
			{
				if (root.getType() == UiMenuItemType.MENU)
				{
					UiMenu menu = (UiMenu) root;
					return Iterables.filter(menu.getItems(),
							molgenisUiMenuItem -> molgenisUiMenuItem.getType() == UiMenuItemType.MENU);
				}
				else return Collections.emptyList();
			}
		}.preOrderTraversal(molgenisUi.getMenu()).toList();

		List<Plugin> plugins = Lists.newArrayList(menuManagerService.getPlugins());
		plugins.sort(Comparator.comparing(Plugin::getId));

		model.addAttribute("menus", menus);
		model.addAttribute("plugins", plugins);
		model.addAttribute("molgenis_ui", molgenisUi);
		return "view-menumanager";
	}

	@PostMapping("/save")
	@ResponseStatus(OK)
	public void save(@Valid @RequestBody Menu molgenisMenu)
	{
		menuManagerService.saveMenu(molgenisMenu);
	}

	@PostMapping("logo")
	public void uploadLogo(@Valid @RequestBody File newLogo)
	{
		System.out.println(newLogo.getName());
	}

	/**
	 * Upload a new molgenis logo
	 */
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@PostMapping("/upload-logo")
	public String uploadLogo(@RequestParam("logo") Part part, Model model) throws IOException
	{
		String contentType = part.getContentType();
		if ((contentType == null) || !contentType.startsWith("image"))
		{
			model.addAttribute("errorMessage", ERRORMESSAGE_LOGO);
		}
		else
		{
			// Create the logo subdir in the filestore if it doesn't exist
			File logoDir = new File(fileStore.getStorageDir() + "/logo");
			if (!logoDir.exists())
			{
				if (!logoDir.mkdir())
				{
					throw new IOException("Unable to create directory [" + logoDir.getAbsolutePath() + "]");
				}
			}

			// Store the logo in the logo dir of the filestore
			String file = "/logo/" + FileUploadUtils.getOriginalFileName(part);
			fileStore.store(part.getInputStream(), file);

			// Set logo
			appSettings.setLogoNavBarHref(file);
		}

		return init(model);
	}
}
