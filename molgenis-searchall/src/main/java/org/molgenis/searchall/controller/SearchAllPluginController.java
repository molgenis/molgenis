package org.molgenis.searchall.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.molgenis.searchall.model.Result;
import org.molgenis.searchall.service.SearchAllService;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(SearchAllPluginController.URI)
public class SearchAllPluginController extends PluginController {
  public static final String ID = "searchAll";
  public static final String URI = PLUGIN_URI_PREFIX + ID;

  public static final String NAVIGATOR = "navigator";
  public static final String DATAEXPLORER = "dataexplorer";

  private final SearchAllService searchAllService;
  private final MenuReaderService menuReaderService;

  public SearchAllPluginController(
      MenuReaderService menuReaderService, SearchAllService searchAllService) {
    super(URI);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.searchAllService = requireNonNull(searchAllService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute(KEY_BASE_URL, menuReaderService.findMenuItemPath(ID));
    model.addAttribute("navigatorBaseUrl", menuReaderService.findMenuItemPath(NAVIGATOR));
    model.addAttribute("dataExplorerBaseUrl", menuReaderService.findMenuItemPath(DATAEXPLORER));
    return "view-search-all";
  }

  @GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public Result searchAll(@RequestParam(value = "term") String searchterm) {
    return searchAllService.searchAll(searchterm);
  }
}
