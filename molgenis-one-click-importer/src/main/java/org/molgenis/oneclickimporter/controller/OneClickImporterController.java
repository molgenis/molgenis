package org.molgenis.oneclickimporter.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.FileStore;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.navigator.NavigatorController;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecutionFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeParseException;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.rest.util.Href.concatEntityHref;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Controller
@RequestMapping(URI)
public class OneClickImporterController extends VuePluginController
{
	public static final String ONE_CLICK_IMPORTER = "one-click-importer";
	public static final String URI = PLUGIN_URI_PREFIX + ONE_CLICK_IMPORTER;

	private FileStore fileStore;
	private OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory;
	private JobExecutor jobExecutor;

	public OneClickImporterController(MenuReaderService menuReaderService, AppSettings appSettings,
			UserAccountService userAccountService, FileStore fileStore,
			OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory, JobExecutor jobExecutor)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
		this.fileStore = requireNonNull(fileStore);
		this.oneClickImportJobExecutionFactory = requireNonNull(oneClickImportJobExecutionFactory);
		this.jobExecutor = requireNonNull(jobExecutor);
	}

	@GetMapping
	public String init(Model model)
	{
		super.init(model, ONE_CLICK_IMPORTER);
		model.addAttribute("navigatorBaseUrl", getBaseUrl(NavigatorController.ID));
		model.addAttribute("dataExplorerBaseUrl", getBaseUrl(DataExplorerController.ID));

		return "view-one-click-importer";
	}

	@ResponseBody
	@PostMapping(value = "/upload", produces = MediaType.TEXT_HTML_VALUE)
	public String importFile(@RequestParam(value = "file") MultipartFile multipartFile) throws IOException
	{
		String filename = multipartFile.getOriginalFilename();
		fileStore.store(multipartFile.getInputStream(), filename);

		OneClickImportJobExecution jobExecution = oneClickImportJobExecutionFactory.create();
		jobExecution.setUser(getCurrentUsername());
		jobExecution.setFile(filename);
		jobExecutor.submit(jobExecution);

		return concatEntityHref(jobExecution);
	}

	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ UnknownFileTypeException.class, IOException.class, InvalidFormatException.class,
			MolgenisDataException.class })
	public ErrorMessageResponse handleUnknownEntityException(Exception e)
	{
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	@ResponseBody
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler({ DateTimeParseException.class })
	public ErrorMessageResponse handleInternalServerError(Exception e)
	{
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

}
