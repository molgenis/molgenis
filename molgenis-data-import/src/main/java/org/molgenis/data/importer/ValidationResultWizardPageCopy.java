package org.molgenis.data.importer;

import static org.molgenis.data.importer.ValidationResultWizardPageCopy.URI;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping(URI)
public class ValidationResultWizardPageCopy extends MolgenisPluginController
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ValidationResultWizardPageCopy.class);
	private final ExecutorService asyncImportJobs = Executors.newCachedThreadPool();

	public static final String ID = "importer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public ValidationResultWizardPageCopy()
	{
		super(URI);
	}

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

	@RequestMapping(method = RequestMethod.POST, value = "/importByUrl")
	@ResponseBody
	public String importFileByUrl(HttpServletRequest request, @RequestParam("filename") String filename)
	{
		String importRunId = null;
		File file = new File(filename);
		importRunId = importFile(request, importRunId, file);

		return importRunId;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/importFile")
	@ResponseBody
	public String importFile(HttpServletRequest request, @RequestParam("file") MultipartFile file)
	{
		String importRunId = null;
		String fileName = file.getOriginalFilename();
		File tmpFile = null;
		try {
			tmpFile = fileStore.store(file.getInputStream(), fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		importRunId = importFile(request, importRunId, tmpFile);
		return importRunId;
	}

	private String importFile(HttpServletRequest request, String importRunId, File file) {
		try
        {
            // convert input to database action
            DatabaseAction entityDbAction = DatabaseAction.ADD;
            RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
                    .createFileRepositoryCollection(file);
            ImportService importService = importServiceFactory.getImportService(file.getName());

            synchronized (this)
            {
                ImportRun importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername());
                importRunId = importRun.getId();
                asyncImportJobs.execute(new ImportJob(importService, SecurityContextHolder.getContext(),
                        repositoryCollection, entityDbAction, importRun.getId(), importRunService, request
                        .getSession(), Package.DEFAULT_PACKAGE_NAME));
            }

        }
        catch (RuntimeException e)
        {
            LOG.error("Error importing file", e);
        }
		return importRunId;
	}
}
