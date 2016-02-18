package org.molgenis.data.importer;

import org.apache.commons.io.FilenameUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.data.rest.Href;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.system.ImportRun;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.FileExtensionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.molgenis.data.importer.ImportApi.URI;

@Controller
@RequestMapping(URI)
public class ImportApi
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ImportApi.class);

	public static final String ID = "importer";
	public static final String URI = "api/" + ID;

	private ImportServiceFactory importServiceFactory;
	private FileStore fileStore;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private ImportRunService importRunService;
	private ExecutorService asyncImportJobs;
	private DataService dataService;

	@Autowired
	public ImportApi(ImportServiceFactory importServiceFactory, FileStore fileStore,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportRunService importRunService,
			DataService dataService)
	{
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importRunService = importRunService;
		this.dataService = dataService;
		this.asyncImportJobs = Executors.newCachedThreadPool();
	}

	public ImportApi(ImportServiceFactory importServiceFactory, FileStore fileStore,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportRunService importRunService,
			DataService dataService, ExecutorService executorService)
	{
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importRunService = importRunService;
		this.dataService = dataService;
		this.asyncImportJobs = executorService;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/importByUrl")
	@ResponseBody
	public ResponseEntity<String> importFileByUrl(HttpServletRequest request,
			@RequestParam("location") String fileLocation,
			@RequestParam(value = "entityName", required = false) String entityName,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "notify", required = false) Boolean notify) throws IOException
	{
		ImportRun importRun;
		try
		{
			File tmpFile = fileLoctionToStoredRenamedFile(fileLocation, entityName);
			importRun = importFile(request, tmpFile, action, notify);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(
				Href.concatEntityHref("/api/v2", importRun.getEntityMetaData().getName(), importRun.getIdValue()),
				HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/importFile")
	public ResponseEntity<String> importFile(HttpServletRequest request,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "entityName", required = false) String entityName,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "notify", required = false) Boolean notify) throws IOException
	{
		ImportRun importRun;
		String filename;
		try
		{
			filename = getFilename(file.getOriginalFilename(), entityName);
			File tmpFile = null;
			tmpFile = fileStore.store(file.getInputStream(), filename);
			importRun = importFile(request, tmpFile, action, notify);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(
				Href.concatEntityHref("/api/v2", importRun.getEntityMetaData().getName(), importRun.getIdValue()),
				HttpStatus.CREATED);
	}

	private File fileLoctionToStoredRenamedFile(String fileLocation, String entityName) throws IOException
	{
		Path path = Paths.get(fileLocation);
		FileInputStream fileInputStream = new FileInputStream(path.toString());
		String filename = path.getFileName().toString();
		return fileStore.store(fileInputStream, getFilename(filename, entityName));
	}

	private String getFilename(String originalFileName, String entityName)
	{
		String filename;
		String extension = FileExtensionUtils.findExtensionFromPossibilities(originalFileName,
				GenericImporterExtensions.getAll());
		if (entityName == null)
		{
			filename = originalFileName;
		}
		else
		{
			filename = entityName + "." + extension;
			if (!extension.equals("vcf") || (!extension.equals("vcf.gz")))
				LOG.warn("Specifing a filename for a non-VCF file has no effect on entity names.");
		}
		return filename;
	}

	private ImportRun importFile(HttpServletRequest request, File file, String action, Boolean notify)
	{
		// no action specified? default is ADD just like the importerPlugin
		ImportRun importRun;
		DatabaseAction databaseAction = getDatabaseAction(file, action);
		if (dataService.hasRepository(FilenameUtils.getBaseName(file.getName())))
		{
			throw new MolgenisDataException("A repository with name " + file.getName() + " already exists");
		}
		ImportService importService = importServiceFactory.getImportService(file.getName());
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(file);

		synchronized (this)
		{
			importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername(), Boolean.TRUE.equals(notify));
			asyncImportJobs.execute(new ImportJob(importService, SecurityContextHolder.getContext(),
					repositoryCollection, databaseAction, importRun.getId(), importRunService, request.getSession(),
					Package.DEFAULT_PACKAGE_NAME));
		}

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
				throw new IllegalArgumentException("Invalid action:[" + action.toUpperCase() + "] valid values: "
						+ (Arrays.toString(DatabaseAction.values())));
			}
		}

		String extension = FileExtensionUtils.findExtensionFromPossibilities(file.getName(),
				GenericImporterExtensions.getAll());

		if (extension.equals("vcf") || extension.equals("vcf.gz"))
		{
			MetaValidationUtils.validateName(file.getName().replace("." + extension, ""));
			if (!DatabaseAction.ADD.equals(databaseAction))
			{
				throw new IllegalArgumentException(
						"Update mode " + databaseAction + " is not supported, only ADD is supported for VCF");
			}
		}
		return databaseAction;
	}
}
