package org.molgenis.metadata.manager.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.molgenis.data.populate.Sequences;
import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.molgenis.metadata.manager.service.MetadataManagerService;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(MetadataManagerController.URI)
public class MetadataManagerController extends PluginController {

  public static final String METADATA_MANAGER = "metadata-manager";
  public static final String URI = PLUGIN_URI_PREFIX + METADATA_MANAGER;
  private static final String KEY_BASE_URL = "baseUrl";

  private MetadataManagerService metadataManagerService;
  private final MenuReaderService menuReaderService;
  private final Sequences sequences;

  public MetadataManagerController(
      MenuReaderService menuReaderService,
      MetadataManagerService metadataManagerService,
      Sequences sequences) {
    super(URI);
    this.metadataManagerService = requireNonNull(metadataManagerService);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.sequences = requireNonNull(sequences);
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute(KEY_BASE_URL, menuReaderService.findMenuItemPath(METADATA_MANAGER));
    return "view-metadata-manager";
  }

  @GetMapping(value = "/sequences", produces = "application/json")
  @ResponseBody
  public List<String> getSequences() {
    return sequences.getSequences();
  }

  @PostMapping("/sequences/{sequenceName}")
  @ResponseStatus(NO_CONTENT)
  public void setValue(@PathVariable String sequenceName, @RequestParam long value) {
    sequences.setValue(sequenceName, value);
  }

  @ResponseBody
  @GetMapping(value = "/editorPackages", produces = "application/json")
  public List<EditorPackageIdentifier> getEditorPackages() {
    return metadataManagerService.getEditorPackages();
  }

  @ResponseBody
  @GetMapping(value = "/entityType/{id:.*}", produces = "application/json")
  public EditorEntityTypeResponse getEditorEntityType(@PathVariable("id") String id) {
    return metadataManagerService.getEditorEntityType(id);
  }

  @ResponseBody
  @GetMapping(value = "/create/entityType", produces = "application/json")
  public EditorEntityTypeResponse createEditorEntityType() {
    return metadataManagerService.createEditorEntityType();
  }

  @ResponseStatus(OK)
  @PostMapping(value = "/entityType", consumes = "application/json")
  public void upsertEntityType(@RequestBody EditorEntityType editorEntityType) {
    metadataManagerService.upsertEntityType(editorEntityType);
  }

  @ResponseBody
  @GetMapping(value = "/create/attribute", produces = "application/json")
  public EditorAttributeResponse createEditorAttribute() {
    return metadataManagerService.createEditorAttribute();
  }
}
