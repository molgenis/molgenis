package org.molgenis.core.ui.data.importer.wizard;

import org.molgenis.core.ui.data.support.Href;
import org.molgenis.core.ui.wizard.AbstractWizardController;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.util.FileExtensionUtils;
import org.molgenis.data.importer.*;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.molgenis.core.ui.data.importer.wizard.ImportWizardController.URI;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Controller
@RequestMapping(URI)
public class ImportWizardController extends AbstractWizardController
{
	public static final String ID = "importwizard";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final UploadWizardPage uploadWizardPage;
	private final OptionsWizardPage optionsWizardPage;
	private final ValidationResultWizardPage validationResultWizardPage;
	private final ImportResultsWizardPage importResultsWizardPage;
	private final PackageWizardPage packageWizardPage;

	private ImportServiceFactory importServiceFactory;
	private FileStore fileStore;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private ImportRunService importRunService;
	private ExecutorService asyncImportJobs;
	private DataService dataService;
	private static final Logger LOG = LoggerFactory.getLogger(ImportWizardController.class);

	@Autowired
	public ImportWizardController(UploadWizardPage uploadWizardPage, OptionsWizardPage optionsWizardPage,
			PackageWizardPage packageWizardPage, ValidationResultWizardPage validationResultWizardPage,
			ImportResultsWizardPage importResultsWizardPage, DataService dataService,
			ImportServiceFactory importServiceFactory, FileStore fileStore,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportRunService importRunService)
	{
		super(URI, "importWizard");
		if (uploadWizardPage == null) throw new IllegalArgumentException("UploadWizardPage is null");
		if (optionsWizardPage == null) throw new IllegalArgumentException("OptionsWizardPage is null");
		if (validationResultWizardPage == null)
		{
			throw new IllegalArgumentException("ValidationResultWizardPage is null");
		}
		if (importResultsWizardPage == null) throw new IllegalArgumentException("ImportResultsWizardPage is null");
		this.uploadWizardPage = uploadWizardPage;
		this.optionsWizardPage = optionsWizardPage;
		this.validationResultWizardPage = validationResultWizardPage;
		this.importResultsWizardPage = importResultsWizardPage;
		this.packageWizardPage = packageWizardPage;
		this.dataService = dataService;
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importRunService = importRunService;
		this.dataService = dataService;
		this.asyncImportJobs = Executors.newSingleThreadExecutor();
	}

	public ImportWizardController(UploadWizardPage uploadWizardPage, OptionsWizardPage optionsWizardPage,
			PackageWizardPage packageWizardPage, ValidationResultWizardPage validationResultWizardPage,
			ImportResultsWizardPage importResultsWizardPage, DataService dataService,
			ImportServiceFactory importServiceFactory, FileStore fileStore,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportRunService importRunService,
			ExecutorService executorService)
	{
		super(URI, "importWizard");
		if (uploadWizardPage == null) throw new IllegalArgumentException("UploadWizardPage is null");
		if (optionsWizardPage == null) throw new IllegalArgumentException("OptionsWizardPage is null");
		if (validationResultWizardPage == null)
			throw new IllegalArgumentException("ValidationResultWizardPage is null");
		if (importResultsWizardPage == null) throw new IllegalArgumentException("ImportResultsWizardPage is null");
		this.uploadWizardPage = uploadWizardPage;
		this.optionsWizardPage = optionsWizardPage;
		this.validationResultWizardPage = validationResultWizardPage;
		this.importResultsWizardPage = importResultsWizardPage;
		this.packageWizardPage = packageWizardPage;
		this.dataService = dataService;
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importRunService = importRunService;
		this.dataService = dataService;
		this.asyncImportJobs = executorService;
	}

	@Override
	protected Wizard createWizard()
	{
		Wizard wizard = new ImportWizard();
		wizard.addPage(uploadWizardPage);
		wizard.addPage(optionsWizardPage);
		wizard.addPage(packageWizardPage);
		wizard.addPage(validationResultWizardPage);
		wizard.addPage(importResultsWizardPage);

		return wizard;
	}

	/**
	 * Imports entities present in the submitted file
	 *
	 * @param url URL from which a file is downloaded
	 */
	@PostMapping("/importByUrl")
	@ResponseBody
	public ResponseEntity<String> importFileByUrl(HttpServletRequest request, @RequestParam("url") String url,
			@RequestParam(value = "entityTypeId", required = false) String entityTypeId,
			@RequestParam(value = "packageId", required = false) String packageId,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "notify", required = false) Boolean notify) throws IOException, URISyntaxException
	{
		ImportRun importRun;
		try
		{
			File tmpFile = fileLocationToStoredRenamedFile(url, entityTypeId);
			if (packageId != null && dataService.getMeta().getPackage(packageId) == null)
			{
				return ResponseEntity.badRequest()
									 .contentType(TEXT_PLAIN)
									 .body(MessageFormat.format("Package [{0}] does not exist.", packageId));
			}
			if (packageId == null) packageId = PACKAGE_DEFAULT;
			importRun = importFile(request, tmpFile, action, notify, packageId);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
			return ResponseEntity.badRequest().contentType(TEXT_PLAIN).body(e.getMessage());
		}
		return createCreatedResponseEntity(importRun);
	}

	/**
	 * Imports entities present in the submitted file
	 *
	 * @param file         File containing entities. Can be VCF, VCF.gz, or EMX
	 * @param entityTypeId Only for VCF and VCF.gz. If set, uses this ID for the table name. Is ignored when uploading EMX
	 * @param packageId    Only for VCF and VCF.gz. If set, places the VCF under the provided package. Is ignored when uploading EMX. If not set, uses the default package 'base'. Throws an error when the supplied package does not exist
	 * @param action       Specifies the import method. Supported: ADD, ADD_UPDATE
	 * @param notify       Should admin be notified when the import fails?
	 * @return ResponseEntity containing the API URL with the current import status
	 */
	@PostMapping("/importFile")
	public ResponseEntity<String> importFile(HttpServletRequest request,
			@RequestParam(value = "file") MultipartFile file,
			@RequestParam(value = "entityTypeId", required = false) String entityTypeId,
			@RequestParam(value = "packageId", required = false) String packageId,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "notify", required = false) Boolean notify) throws IOException, URISyntaxException
	{
		ImportRun importRun;
		String filename;
		try
		{
			filename = getFilename(file.getOriginalFilename(), entityTypeId);
			File tmpFile = fileStore.store(file.getInputStream(), filename);

			if (packageId != null && dataService.getMeta().getPackage(packageId) == null)
			{
				return ResponseEntity.badRequest()
									 .contentType(TEXT_PLAIN)
									 .body(MessageFormat.format("Package [{0}] does not exist.", packageId));
			}
			if (packageId == null) packageId = PACKAGE_DEFAULT;

			importRun = importFile(request, tmpFile, action, notify, packageId);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
			return ResponseEntity.badRequest().contentType(TEXT_PLAIN).body(e.getMessage());
		}
		return createCreatedResponseEntity(importRun);
	}

	private ResponseEntity<String> createCreatedResponseEntity(ImportRun importRun) throws URISyntaxException
	{
		String href = Href.concatEntityHref("/api/v2", importRun.getEntityType().getId(), importRun.getIdValue());
		return ResponseEntity.created(new java.net.URI(href)).contentType(TEXT_PLAIN).body(href);
	}

	private File fileLocationToStoredRenamedFile(String fileLocation, String entityTypeId) throws IOException
	{
		Path path = Paths.get(fileLocation);
		String filename = path.getFileName().toString();
		URL url = new URL(fileLocation);

		return fileStore.store(url.openStream(), getFilename(filename, entityTypeId));
	}

	private String getFilename(String originalFileName, String entityTypeId)
	{
		String filename;
		String extension = FileExtensionUtils.findExtensionFromPossibilities(originalFileName,
				importServiceFactory.getSupportedFileExtensions());
		if (entityTypeId == null)
		{
			filename = originalFileName;
		}
		else
		{
			filename = entityTypeId + "." + extension;
		}
		return filename;
	}

	private ImportRun importFile(HttpServletRequest request, File file, String action, Boolean notify, String packageId)
	{
		// no action specified? default is ADD just like the importerPlugin
		ImportRun importRun;
		String fileExtension = getExtension(file.getName());
		DatabaseAction databaseAction = getDatabaseAction(file, action);
		if (fileExtension.contains("vcf") && dataService.hasRepository(getBaseName(file.getName())))
		{
			throw new MolgenisDataException(
					"A repository with name " + getBaseName(file.getName()) + " already exists");
		}
		ImportService importService = importServiceFactory.getImportService(file.getName());
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
				file);

		importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername(), Boolean.TRUE.equals(notify));
		asyncImportJobs.execute(
				new ImportJob(importService, SecurityContextHolder.getContext(), repositoryCollection, databaseAction,
						importRun.getId(), importRunService, request.getSession(), packageId));

		return importRun;
	}

	private DatabaseAction getDatabaseAction(File file, String action)
	{
		DatabaseAction databaseAction = DatabaseAction.ADD;
		if (action != null)
		{
			try
			{
				databaseAction = DatabaseAction.valueOf(action.toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
						"Invalid action:[" + action.toUpperCase() + "] valid values: " + (Arrays.toString(
								DatabaseAction.values())));
			}
		}
		return databaseAction;
	}

	/**
	 * Added for testability
	 */
	void setExecutorService(ExecutorService executorService)
	{
		this.asyncImportJobs = executorService;
	}
}