package org.molgenis.oneclickimporter.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

	@PostMapping("/upload")
	public void importFile(@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		File file = multipartToFile(multipartFile);

		String fileName = file.getName();
		String fileTypePart = fileName.substring(fileName.lastIndexOf('.') + 1);

		if (fileTypePart.equals("xls") || fileTypePart.equals("xlsx"))
		{
			Sheet sheet = buildExcelSheetFromFile(file);
			writeSheetToConsole(sheet);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
	{
		File convFile = new File(multipart.getOriginalFilename());
		multipart.transferTo(convFile);
		return convFile;
	}

	private Sheet buildExcelSheetFromFile(File file) throws Exception
	{
		Workbook workbook = WorkbookFactory.create(file);
		return workbook.getSheetAt(0);
	}

	private void writeSheetToConsole(Sheet sheet)
	{
		Row headerRow = sheet.getRow(0);
		headerRow.cellIterator().forEachRemaining(cell -> System.out.println("cell = " + cell));

		//		int rows; // No of rows
		//		rows = sheet.getPhysicalNumberOfRows();
		//
		//		int cols = 0; // No of columns
		//		int tmp = 0;
		//
		//		// This trick ensures that we get the data properly even if it doesn't start from first few rows
		//		for (int i = 0; i < 10 || i < rows; i++)
		//		{
		//			row = sheet.getRow(i);
		//			if (row != null)
		//			{
		//				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
		//				if (tmp > cols) cols = tmp;
		//			}
		//		}
		//
		//		for (int r = 0; r < rows; r++)
		//		{
		//			row = sheet.getRow(r);
		//			System.out.println("row = " + row);
		//			if (row != null)
		//			{
		//				for (int c = 0; c < cols; c++)
		//				{
		//					cell = row.getCell((short) c);
		//					if (cell != null)
		//					{
		//
		//					}
		//				}
		//			}
		//		}
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER);
	}
}
