package org.molgenis.oneclickimporter.controller;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.FileStore;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.jobs.JobExecutionUriUtils;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.navigator.NavigatorController;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecutionFactory;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(OneClickImporterController.URI)
public class OneClickImporterController extends PluginController {
  public static final String ONE_CLICK_IMPORTER = "one-click-importer";
  public static final String URI = PLUGIN_URI_PREFIX + ONE_CLICK_IMPORTER;
  private static final String KEY_BASE_URL = "baseUrl";

  private FileStore fileStore;
  private OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory;
  private JobExecutor jobExecutor;
  private MenuReaderService menuReaderService;

  public OneClickImporterController(
      MenuReaderService menuReaderService,
      FileStore fileStore,
      OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory,
      JobExecutor jobExecutor) {
    super(URI);
    this.fileStore = requireNonNull(fileStore);
    this.oneClickImportJobExecutionFactory = requireNonNull(oneClickImportJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.menuReaderService = requireNonNull(menuReaderService);
  }

  @GetMapping
  public String init(Model model) {
    model.addAttribute(KEY_BASE_URL, menuReaderService.findMenuItemPath(ONE_CLICK_IMPORTER));
    model.addAttribute(
        "navigatorBaseUrl", menuReaderService.findMenuItemPath(NavigatorController.ID));
    model.addAttribute(
        "dataExplorerBaseUrl", menuReaderService.findMenuItemPath(DataExplorerController.ID));

    return "view-one-click-importer";
  }

  @ResponseBody
  @PostMapping(value = "/upload", produces = MediaType.TEXT_HTML_VALUE)
  public String importFile(@RequestParam(value = "file") MultipartFile multipartFile)
      throws IOException {
    String filename = multipartFile.getOriginalFilename();
    try (InputStream inputStream = multipartFile.getInputStream()) {
      fileStore.store(inputStream, filename);
    }

    OneClickImportJobExecution jobExecution = oneClickImportJobExecutionFactory.create();
    jobExecution.setFile(filename);
    jobExecutor.submit(jobExecution);

    return JobExecutionUriUtils.getUriPath(jobExecution);
  }

  @ResponseBody
  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({
    UnknownFileTypeException.class,
    IOException.class,
    InvalidFormatException.class,
    MolgenisDataException.class
  })
  public ErrorMessageResponse handleUnknownEntityException(Exception e) {
    return new ErrorMessageResponse(
        singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
  }

  @ResponseBody
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler({DateTimeParseException.class})
  public ErrorMessageResponse handleInternalServerError(Exception e) {
    return new ErrorMessageResponse(
        singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
  }
}
