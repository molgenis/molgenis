package org.molgenis.core.ui.style;

import org.molgenis.core.ui.file.FileDownloadController;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.settings.AppSettings;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.style.BootstrapVersion.BOOTSTRAP_VERSION_3;
import static org.molgenis.core.ui.style.StyleSheetMetadata.STYLE_SHEET;

@Component
public class StyleServiceImpl implements StyleService
{
	public static final String BOOTSTRAP_FALL_BACK_THEME = "bootstrap-basic.min.css";
	private final AppSettings appSettings;
	private final IdGenerator idGenerator;
	private final FileStore fileStore;
	private final FileMetaFactory fileMetaFactory;
	private final StyleSheetFactory styleSheetFactory;
	private final DataService dataService;

	public StyleServiceImpl(AppSettings appSettings, IdGenerator idGenerator, FileStore fileStore,
			FileMetaFactory fileMetaFactory, StyleSheetFactory styleSheetFactory, DataService dataService)
	{
		this.appSettings = requireNonNull(appSettings);
		this.idGenerator = requireNonNull(idGenerator);
		this.fileStore = requireNonNull(fileStore);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.styleSheetFactory = requireNonNull(styleSheetFactory);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Set<Style> getAvailableStyles()
	{
		Stream<StyleSheet> styleEntities = dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class);

		return styleEntities.map(s -> Style.createLocal(s.getName())).collect(Collectors.toSet());
	}

	@Override
	public Style addStyle(String styleId, String bootstrap3FileName, InputStream bootstrap3StyleData,
			String bootstrap4FileName, InputStream bootstrap4StyleData) throws MolgenisStyleException
	{
		if (dataService.getRepository(STYLE_SHEET).findOneById(styleId) != null)
		{
			throw new MolgenisStyleException(
					String.format("A style with the same identifier (%s) already exists", styleId));
		}

		StyleSheet styleSheet = styleSheetFactory.create(styleId);
		styleSheet.setName(styleId);

		FileMeta bootstrap3ThemeFileMeta = createStyleSheetFileMeta(bootstrap3FileName, bootstrap3StyleData);
		styleSheet.setBootstrap3Theme(bootstrap3ThemeFileMeta);

		// Setting the bootstrap 4 style is optional
		if (bootstrap4FileName != null && bootstrap4StyleData != null)
		{
			FileMeta bootstrap4ThemeFileMeta = createStyleSheetFileMeta(bootstrap4FileName, bootstrap4StyleData);
			styleSheet.setBootstrap4Theme(bootstrap4ThemeFileMeta);
		}

		dataService.add(STYLE_SHEET, styleSheet);

		return Style.createLocal(styleSheet.getName());
	}

	private FileMeta createStyleSheetFileMeta(String fileName, InputStream data) throws MolgenisStyleException
	{
		String fileId = idGenerator.generateId();
		try
		{
			fileStore.store(data, fileId);
		}
		catch (IOException e)
		{
			throw new MolgenisStyleException("Unable to save style file with name : " + fileName, e);
		}

		FileMeta fileMeta = fileMetaFactory.create(fileId);
		fileMeta.setContentType("css");
		fileMeta.setFilename(fileName);
		fileMeta.setSize(fileStore.getFile(fileId).length());
		fileMeta.setUrl(buildFileUrl(fileId));
		dataService.add(FileMetaMetaData.FILE_META, fileMeta);
		return fileMeta;
	}

	@Override
	public void setSelectedStyle(String styleName)
	{
		// Pressing save in the UI without doing a selection returns undefined
		if (!styleName.equals("undefined"))
		{
			String bootstrapTheme = getStyle(styleName).getLocation();
			appSettings.setBootstrapTheme(bootstrapTheme);
		}
	}

	@Override
	public Style getSelectedStyle()
	{
		for (Style style : getAvailableStyles())
		{
			String bootstrapTheme = appSettings.getBootstrapTheme();
			if (style.getLocation().equals(bootstrapTheme))
			{
				return style;
			}
		}
		return null;
	}

	@Override
	public Style getStyle(String styleName)
	{
		try
		{
			for (Style style : getAvailableStyles())
			{
				if (style.getName().equals(styleName))
				{
					return style;
				}
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e + " Selected style was not found");
		}

		return null;
	}

	@Override
	@RunAsSystem
	public FileSystemResource getThemeData(String styleName, BootstrapVersion bootstrapVersion)
			throws MolgenisStyleException
	{
		StyleSheet styleSheet = findThemeByName(styleName);

		if (styleSheet == null)
		{
			throw new MolgenisStyleException("No theme found for with name: " + styleName);
		}

		// Fetch the theme file from the store.
		FileMeta fileMeta;
		if (bootstrapVersion.equals(BOOTSTRAP_VERSION_3))
		{
			fileMeta = styleSheet.getBootstrap3Theme();
		}
		else
		{
			fileMeta = styleSheet.getBootstrap4Theme();
			// If no bootstrap 4 theme was set fetch the default theme from the resources folder
			if (fileMeta == null)
			{
				StyleSheet fallBackTheme = findThemeByName(BOOTSTRAP_FALL_BACK_THEME);
				fileMeta = fallBackTheme.getBootstrap4Theme();
			}
		}

		File file = fileStore.getFile(fileMeta.getId());
		return new FileSystemResource(file);
	}

	@Override
	public StyleSheet findThemeByName(String themeName)
	{
		Query<StyleSheet> findByName = new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, themeName);
		return dataService.findOne(StyleSheetMetadata.STYLE_SHEET, findByName, StyleSheet.class);
	}

	/**
	 * Build a downLoadUrl for the file identified by the given fileId. If this method is called outside of a
	 * request context ( system initialization for example) the server context is used for generating a relative path
	 */
	private String buildFileUrl(String fileId)
	{

		if (RequestContextHolder.getRequestAttributes() != null)
		{
			ServletUriComponentsBuilder currentRequest = ServletUriComponentsBuilder.fromCurrentRequest();
			UriComponents downloadUri = currentRequest.replacePath(FileDownloadController.URI + '/' + fileId)
													  .replaceQuery(null)
													  .build();
			return downloadUri.toUriString();
		}
		else
		{
			// TODO this is a workaround for the situation where the File url needs to be created without a request
			// context, in order to fix the properly we would need to add a deploy time property
			// defining the file download server location
			return FileDownloadController.URI + '/' + fileId;
		}

	}

}

