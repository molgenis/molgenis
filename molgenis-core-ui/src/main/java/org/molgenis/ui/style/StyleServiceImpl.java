package org.molgenis.ui.style;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.file.model.FileMetaMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ui.style.BootstrapVersion.BOOTSTRAP_VERSION_3;
import static org.molgenis.ui.style.StyleMetadata.STYLE_SHEET;

@Component
public class StyleServiceImpl implements StyleService
{
	private static final String LOCAL_CSS_BOOTSTRAP_THEME_LOCATION = "classpath*:css/bootstrap-*.min.css";

	private final AppSettings appSettings;
	private final IdGenerator idGenerator;
	private final FileStore fileStore;
	private final FileMetaFactory fileMetaFactory;
	private final StyleSheetFactory styleSheetFactory;
	private final DataService dataService;

	@Autowired
	public StyleServiceImpl(AppSettings appSettings, IdGenerator idGenerator,FileStore fileStore,
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
		Stream<StyleSheet> styleEntities = dataService.findAll(StyleMetadata.STYLE_SHEET, StyleSheet.class);

		return styleEntities.map(s -> Style.createLocal(s.getName())).collect(Collectors.toSet());
	}

	@Override
	public void addStyles(String styleId, String bootstrap3FileName, InputStream bootstrap3StyleData,
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
		if(bootstrap4FileName != null && bootstrap4StyleData != null) {
			FileMeta bootstrap4ThemeFileMeta = createStyleSheetFileMeta(bootstrap4FileName, bootstrap4StyleData);
			styleSheet.setBootstrap4Theme(bootstrap4ThemeFileMeta);
		}

		dataService.add(STYLE_SHEET, styleSheet);
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
		ServletUriComponentsBuilder currentRequest = ServletUriComponentsBuilder.fromCurrentRequest();
		UriComponents downloadUri = currentRequest.replacePath(FileDownloadController.URI + '/' + fileId)
				.replaceQuery(null)
				.build();
		fileMeta.setUrl(downloadUri.toUriString());
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
			throws MolgenisStyleException, IOException
	{
		Query<StyleSheet> findByName = new QueryImpl<StyleSheet>().eq(StyleMetadata.NAME, styleName);
		StyleSheet styleSheet = dataService.findOne(StyleMetadata.STYLE_SHEET, findByName, StyleSheet.class);

		if (styleSheet == null)
		{
			throw new MolgenisStyleException("No theme found for with name: " + styleName);
		}

		// Fetch the theme file from the store.
		File file;
		if (bootstrapVersion.equals(BOOTSTRAP_VERSION_3))
		{
			FileMeta fileMeta = styleSheet.getBootstrap3Theme();
			file = fileStore.getFile(fileMeta.getId());
		}
		else
		{
			FileMeta fileMeta = styleSheet.getBootstrap4Theme();
			// If no bootstrap 4 theme was set fetch the default theme from the resources folder
			if (fileMeta == null)
			{
				file = getFallBackTheme();
			}
			else
			{
				file = fileStore.getFile(fileMeta.getId());
			}
		}

		return new FileSystemResource(file);
	}

	private File getFallBackTheme() throws IOException
	{
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		Resource resource = resolver.getResource("classpath*:css/bootstrap-4/bootstrap.min.css");

		return resource.getFile();
	}



}
