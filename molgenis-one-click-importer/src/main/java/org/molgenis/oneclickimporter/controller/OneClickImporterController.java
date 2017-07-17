package org.molgenis.oneclickimporter.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.EntityService;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.ErrorMessageResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.URI;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class OneClickImporterController extends MolgenisPluginController
{
	public static final String ONE_CLICK_IMPORTER = "one-click-importer";
	public static final String URI = PLUGIN_URI_PREFIX + ONE_CLICK_IMPORTER;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;
	private OneClickImporterService oneClickImporterService;
	private ExcelService excelService;
	private EntityService entityService;
	private FileStore fileStore;

	public OneClickImporterController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings, ExcelService excelService, OneClickImporterService oneClickImporterService,
			EntityService entityService, FileStore fileStore)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
		this.excelService = requireNonNull(excelService);
		this.oneClickImporterService = requireNonNull(oneClickImporterService);
		this.entityService = requireNonNull(entityService);
		this.fileStore = requireNonNull(fileStore);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("baseUrl", getBaseUrl());

		return "view-one-click-importer";
	}

	@ResponseBody
	@RequestMapping(value = "/upload", method = POST)
	public void importFile(HttpServletResponse response, @RequestParam(value = "file") MultipartFile multipartFile)
			throws UnknownFileTypeException, IOException, InvalidFormatException
	{
		String filename = multipartFile.getOriginalFilename();
		File file = fileStore.store(multipartFile.getInputStream(), filename);

		String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
		String dataCollectionName = filename.substring(0, filename.lastIndexOf('.'));

		DataCollection dataCollection;
		if (fileExtension.equals("xls") || fileExtension.equals("xlsx"))
		{
			Sheet sheet = excelService.buildExcelSheetFromFile(file);
			dataCollection = oneClickImporterService.buildDataCollection(dataCollectionName, sheet);
		}
		else
		{
			throw new UnknownFileTypeException(
					String.format("File with extension: %s is not a valid one-click importer file", fileExtension));
		}

		EntityType dataTable = entityService.createEntity(dataCollection);

		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader("Location", builder.build().toUriString() + dataTable.getId());
	}

	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ UnknownFileTypeException.class, IOException.class, InvalidFormatException.class,
			MolgenisDataException.class })
	public ErrorMessageResponse handleUnknownEntityException(Exception e)
	{
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER);
	}
}
