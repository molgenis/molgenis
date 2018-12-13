package org.molgenis.core.ui.menumanager;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.menumanager.MenuManagerController.URI;
import static org.springframework.http.HttpStatus.OK;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.Part;
import javax.validation.Valid;
import org.molgenis.core.util.FileUploadUtils;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Plugin to view and modify the app UI menu */
@Controller
@RequestMapping(URI)
public class MenuManagerController extends PluginController {
  public static final String ID = "menumanager";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  private final MenuManagerService menuManagerService;
  private final MenuReaderService menuReaderService;
  private final FileStore fileStore;
  private final AppSettings appSettings;

  private static final String ERRORMESSAGE_LOGO =
      "The logo needs to be an image file like png or jpg.";

  public MenuManagerController(
      MenuManagerService menuManagerService,
      MenuReaderService menuReaderService,
      FileStore fileStore,
      AppSettings appSettings) {
    super(URI);
    this.menuManagerService = requireNonNull(menuManagerService);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.fileStore = requireNonNull(fileStore);
    this.appSettings = requireNonNull(appSettings);
  }

  @GetMapping
  public String init(Model model) {
    List<Plugin> plugins = Lists.newArrayList(menuManagerService.getPlugins());
    plugins.sort(Comparator.comparing(Plugin::getId));
    model.addAttribute("plugins", plugins);
    model.addAttribute("menu", menuReaderService.getMenu());
    return "view-menumanager";
  }

  @PostMapping("/save")
  @ResponseStatus(OK)
  public void save(@Valid @RequestBody Menu molgenisMenu) {
    menuManagerService.saveMenu(molgenisMenu);
  }

  /** Upload a new molgenis logo */
  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @PostMapping("/upload-logo")
  public String uploadLogo(@RequestParam("logo") Part part, Model model) throws IOException {
    String contentType = part.getContentType();
    if ((contentType == null) || !contentType.startsWith("image")) {
      model.addAttribute("errorMessage", ERRORMESSAGE_LOGO);
    } else {
      // Create the logo subdir in the filestore if it doesn't exist
      File logoDir = new File(fileStore.getStorageDir() + "/logo");
      if (!logoDir.exists()) {
        if (!logoDir.mkdir()) {
          throw new IOException("Unable to create directory [" + logoDir.getAbsolutePath() + "]");
        }
      }

      // Store the logo in the logo dir of the filestore
      String file = "/logo/" + FileUploadUtils.getOriginalFileName(part);
      try (InputStream inputStream = part.getInputStream()) {
        fileStore.store(inputStream, file);
      }

      // Set logo
      appSettings.setLogoNavBarHref(file);
    }

    return init(model);
  }
}
