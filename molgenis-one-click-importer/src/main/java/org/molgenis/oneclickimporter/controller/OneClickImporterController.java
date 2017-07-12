package org.molgenis.oneclickimporter.controller;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.URI;
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

	public OneClickImporterController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("baseUrl", getBaseUrl());
		return "view-one-click-importer";
	}

	private static enum exelTypeExtentions {

	}

	@PostMapping("/upload")
	public void importFile(@RequestParam("file") MultipartFile multipartFile)
	{
		String fileName = multipartFile.getOriginalFilename();
		String fileTypePart = fileName.substring(fileName.lastIndexOf('.') + 1);

		//Every xls/xlsx/csv/csv-zip file with 1 sheet is importable with the upload plugin



	}

	private HSSFSheet buildExelSheetFromFile(MultipartFile multipartFile) {
		POIFSFileSystem fs = null;
		try
		{
			fs = new POIFSFileSystem(multipartFile.getInputStream());
			HSSFWorkbook wb = null;

			wb = new HSSFWorkbook(fs);

			// at the moment only single sheet is supported
			return wb.getSheetAt(0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		HSSFCell cell = new HSSFCell();

		return null;
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER);
	}
}
