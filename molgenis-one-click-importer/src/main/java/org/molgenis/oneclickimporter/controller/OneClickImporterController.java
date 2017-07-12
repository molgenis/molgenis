package org.molgenis.oneclickimporter.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.ErrorMessageResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.URI;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

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

	public OneClickImporterController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings, ExcelService excelService, OneClickImporterService oneClickImporterService)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
		this.excelService = requireNonNull(excelService);
		this.oneClickImporterService = requireNonNull(oneClickImporterService);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("baseUrl", getBaseUrl());
		return "view-one-click-importer";
	}

	@PostMapping("/upload")
	public void importFile(@RequestParam("file") MultipartFile multipartFile)
			throws UnknownFileTypeException, IOException, InvalidFormatException
	{
		File file = new File(multipartFile.getOriginalFilename());
		multipartFile.transferTo(file);

		String fileName = file.getName();
		String fileTypePart = fileName.substring(fileName.lastIndexOf('.') + 1);

		if (fileTypePart.equals("xls") || fileTypePart.equals("xlsx"))
		{
			Sheet sheet = excelService.buildExcelSheetFromFile(file);
			DataCollection dataCollection = oneClickImporterService.buildDataCollection(sheet);
		}
		else
		{
			throw new UnknownFileTypeException(
					"File with extention: " + fileTypePart + " is not a valid one-click importer file");
		}
	}

	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ UnknownFileTypeException.class, IOException.class, InvalidFormatException.class })
	public ErrorMessageResponse handleUnknownEntityException(Exception e)
	{
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
	{
		File convFile = new File(multipart.getOriginalFilename());
		multipart.transferTo(convFile);
		return convFile;
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER);
	}
}
