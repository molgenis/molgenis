package org.molgenis.data.importer;

import org.apache.commons.io.FilenameUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.data.rest.Href;
import org.molgenis.data.system.ImportRun;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.security.user.UserAccountService;
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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.molgenis.data.importer.ImportApi.URI;

@Controller
@RequestMapping(URI)
public class ImportApi
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ImportApi.class);
	private final ExecutorService asyncImportJobs = Executors.newCachedThreadPool();

	public static final String ID = "importer";
	public static final String URI = "api/" + ID;

	@Autowired
	private ImportServiceFactory importServiceFactory;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	private ImportRunService importRunService;

	@Autowired
	UserAccountService userAccountService;

	@Autowired
	MolgenisUserService userService;

	@Autowired
	DataService dataService;

	@RequestMapping(method = RequestMethod.POST, value = "/importByUrl")
	@ResponseBody
	public ResponseEntity<String> importFileByUrl(HttpServletRequest request,
			@RequestParam("location") String fileLocation,
			@RequestParam(value = "action", required = true) String action)
	{
		ImportRun importRun;
		try
		{
			File file = new File(fileLocation);
			importRun = importFile(request, file, action);
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
			@RequestParam(value = "action", required = false) String action) throws IOException
	{
		ImportRun importRun;
		try
		{
			if (entityName == null)
			{
				entityName = file.getOriginalFilename();
			}
			File tmpFile = null;
			tmpFile = fileStore.store(file.getInputStream(), entityName);
			importRun = importFile(request, tmpFile, action);
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

	private ImportRun importFile(HttpServletRequest request, File file, String action)
	{
		// no action specified? default is ADD just like the importerPlugin
		DatabaseAction databaseAction = DatabaseAction.ADD;
		ImportRun importRun = null;
		if (action != null)
		{
			try
			{
				databaseAction = DatabaseAction.valueOf(action);
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
						"Invalid action:[" + action + "] valid values: " + DatabaseAction.values());
			}
		}
		if (FilenameUtils.getExtension(file.getName()).equals("vcf")
				|| FilenameUtils.getExtension(file.getName()).equals("vcf.gz"))
			MetaValidationUtils.validateName(file.getName());
		// convert input to database action
		DatabaseAction entityDbAction = databaseAction;
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file.getName());

		synchronized (this)
		{
			importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername());
			asyncImportJobs.execute(new ImportJob(importService, SecurityContextHolder.getContext(),
					repositoryCollection, entityDbAction, importRun.getId(), importRunService, request.getSession(),
					Package.DEFAULT_PACKAGE_NAME));
		}

		return importRun;
	}
}
