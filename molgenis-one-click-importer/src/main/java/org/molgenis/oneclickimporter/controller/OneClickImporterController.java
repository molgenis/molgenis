package org.molgenis.oneclickimporter.controller;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.jobs.JobExecutor;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.file.FileStore;
import org.molgenis.navigator.NavigatorController;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecutionFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.controller.VuePluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.Href.concatEntityHref;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@Controller
@RequestMapping(URI)
public class OneClickImporterController extends VuePluginController
{
	public static final String ONE_CLICK_IMPORTER = "one-click-importer";
	public static final String URI = PLUGIN_URI_PREFIX + ONE_CLICK_IMPORTER;

	private FileStore fileStore;
	private OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory;
	private JobExecutor jobExecutor;

	public OneClickImporterController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings, UserAccountService userAccountService, FileStore fileStore,
			OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory, JobExecutor jobExecutor)
	{
		super(URI, menuReaderService, languageService, appSettings, userAccountService);
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
}
