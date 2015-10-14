package org.molgenis.ui.menumanager;

import static org.molgenis.ui.menumanager.MenuManagerController.URI;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.Part;
import javax.validation.Valid;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;
import org.molgenis.ui.menu.Menu;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

/**
 * Plugin to view and modify the app UI menu
 */
@Controller
@RequestMapping(URI)
public class MenuManagerController extends MolgenisPluginController
{
	public static final String ID = "menumanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final MenuManagerService menuManagerService;
	private final FileStore fileStore;
	private final MolgenisUi molgenisUi;
	private final AppSettings appSettings;

	private static final String ERRORMESSAGE_LOGO = "The logo needs to be an image file like png or jpg.";

	@Autowired
	public MenuManagerController(MenuManagerService menuManagerService, FileStore fileStore, MolgenisUi molgenisUi,
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

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		List<MolgenisUiMenuItem> menus = new TreeTraverser<MolgenisUiMenuItem>()
		{
			@Override
			public Iterable<MolgenisUiMenuItem> children(MolgenisUiMenuItem root)
			{
				if (root.getType() == MolgenisUiMenuItemType.MENU)
				{
					MolgenisUiMenu menu = (MolgenisUiMenu) root;
					return Iterables.filter(menu.getItems(), new Predicate<MolgenisUiMenuItem>()
					{
						@Override
						public boolean apply(MolgenisUiMenuItem molgenisUiMenuItem)
						{
							return molgenisUiMenuItem.getType() == MolgenisUiMenuItemType.MENU;
						}
					});
				}
				else return Collections.emptyList();
			}
		}.preOrderTraversal(molgenisUi.getMenu()).toList();

		List<MolgenisPlugin> plugins = Lists.newArrayList(menuManagerService.getPlugins());
		Collections.sort(plugins, new Comparator<MolgenisPlugin>()
		{
			@Override
			public int compare(MolgenisPlugin molgenisPlugin1, MolgenisPlugin molgenisPlugin2)
			{
				return molgenisPlugin1.getId().compareTo(molgenisPlugin2.getId());
			}
		});

		model.addAttribute("menus", menus);
		model.addAttribute("plugins", plugins);
		model.addAttribute("molgenis_ui", molgenisUi);
		return "view-menumanager";
	}

	@RequestMapping(value = "/save", method = POST)
	@ResponseStatus(OK)
	public void save(@Valid @RequestBody Menu molgenisMenu)
	{
		menuManagerService.saveMenu(molgenisMenu);
	}

	@RequestMapping(value = "logo", method = POST)
	public void uploadLogo(@Valid @RequestBody File newLogo)
	{
		System.out.println(newLogo.getName());
	}

	/**
	 * Upload a new molgenis logo
	 * 
	 * @param part
	 * @param model
	 * @return model
	 * @throws IOException
	 */
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@RequestMapping(value = "/upload-logo", method = RequestMethod.POST)
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
