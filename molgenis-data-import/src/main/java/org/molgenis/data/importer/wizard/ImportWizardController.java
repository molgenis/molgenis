package org.molgenis.data.importer.wizard;

import org.molgenis.auth.*;
import org.molgenis.data.*;
import org.molgenis.data.importer.*;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.support.Href;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.Permission;
import org.molgenis.security.permission.Permissions;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileExtensionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.GroupMetaData.GROUP;
import static org.molgenis.data.importer.wizard.ImportWizardController.URI;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.security.core.Permission.*;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Controller
@RequestMapping(URI)
public class ImportWizardController extends AbstractWizardController
{
	public static final String ID = "importwizard";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final UploadWizardPage uploadWizardPage;
	private final OptionsWizardPage optionsWizardPage;
	private final ValidationResultWizardPage validationResultWizardPage;
	private final ImportResultsWizardPage importResultsWizardPage;
	private final PackageWizardPage packageWizardPage;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;
	private final UserAccountService userAccountService;
	private final GroupAuthorityFactory groupAuthorityFactory;

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
			GrantedAuthoritiesMapper grantedAuthoritiesMapper, UserAccountService userAccountService,
			ImportServiceFactory importServiceFactory, FileStore fileStore,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportRunService importRunService,
			GroupAuthorityFactory groupAuthorityFactory)
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
		this.userAccountService = userAccountService;
		this.dataService = dataService;
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importRunService = importRunService;
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
		this.dataService = dataService;
		this.asyncImportJobs = Executors.newSingleThreadExecutor();
	}

	public ImportWizardController(UploadWizardPage uploadWizardPage, OptionsWizardPage optionsWizardPage,
			PackageWizardPage packageWizardPage, ValidationResultWizardPage validationResultWizardPage,
			ImportResultsWizardPage importResultsWizardPage, DataService dataService,
			GrantedAuthoritiesMapper grantedAuthoritiesMapper, UserAccountService userAccountService,
			ImportServiceFactory importServiceFactory, FileStore fileStore,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, ImportRunService importRunService,
			ExecutorService executorService, GroupAuthorityFactory groupAuthorityFactory)
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
		this.userAccountService = userAccountService;
		this.dataService = dataService;
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
		this.importServiceFactory = importServiceFactory;
		this.fileStore = fileStore;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importRunService = importRunService;
		this.dataService = dataService;
		this.asyncImportJobs = executorService;
		this.groupAuthorityFactory = groupAuthorityFactory;
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

	@RequestMapping(value = "/entityclass/group/{groupId}", method = RequestMethod.GET)
	@ResponseBody
	public Permissions getGroupEntityClassPermissions(@PathVariable String groupId, WebRequest webRequest)
	{
		boolean allowed = false;
		for (Group group : userAccountService.getCurrentUserGroups())
		{
			if (group.getId().equals(groupId))
			{
				allowed = true;
			}
		}
		if (!allowed && !userAccountService.getCurrentUser().isSuperuser())
		{
			throw new RuntimeException("Current user does not belong to the requested group.");
		}
		String entitiesString = webRequest.getParameter("entityIds");
		List<String> entities = Arrays.asList(entitiesString.split(","));

		Group group = dataService.findOneById(GROUP, groupId, Group.class);
		if (group == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		List<Authority> groupPermissions = getGroupPermissions(group);
		Permissions permissions = createPermissions(groupPermissions, entities);
		permissions.setGroupId(groupId);
		return permissions;

	}

	@RequestMapping(value = "/add/entityclass/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void addGroupEntityClassPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		dataService.getEntityNames().forEach(entityClassId ->
		{
			GroupAuthority authority = getGroupAuthority(groupId, entityClassId);

			boolean newGroupAuthority;
			if(authority == null) {
				newGroupAuthority = true;
				authority = groupAuthorityFactory.create();
			} else {
				newGroupAuthority = false;
			}

			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value != null && (SecurityUtils
					.currentUserHasRole(SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + entityClassId)
					|| userAccountService.getCurrentUser().isSuperuser()))
			{
				if (value.equalsIgnoreCase(READ.toString()) || value.equalsIgnoreCase(COUNT.toString()) || value
						.equalsIgnoreCase(WRITE.toString()) || value.equalsIgnoreCase(WRITEMETA.toString()))
				{
					authority.setGroup(dataService.findOneById(GROUP, groupId, Group.class));
					authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_" + entityClassId);
					if (newGroupAuthority)
					{
						authority.setId(UUID.randomUUID().toString());
						dataService.add(GROUP_AUTHORITY, authority);
					}
					else
					{
						dataService.update(GROUP_AUTHORITY, authority);
					}
				}
				else if (value.equalsIgnoreCase(NONE.toString()))
				{
					if (authority.getId() != null)
					{
						dataService.deleteById(GROUP_AUTHORITY, authority.getId());
					}
				}
				else
				{
					throw new RuntimeException(
							"Unknown value: " + value + " for permission on entity: " + entityClassId);
				}
			}
			else
			{
				if (value != null) throw new MolgenisDataAccessException(
						"Current user is not allowed to change the permissions for this entity: " + entityClassId);
			}
		});
	}

	private List<Authority> getGroupPermissions(Group group)
	{
		return dataService.findAll(GROUP_AUTHORITY,
				new QueryImpl<GroupAuthority>().eq(GroupAuthorityMetaData.GROUP, group),
				GroupAuthority.class).collect(Collectors.toList());
	}

	private Permissions createPermissions(List<? extends Authority> entityAuthorities, List<String> entityIds)
	{
		Permissions permissions = new Permissions();

		if (entityIds != null)
		{
			Map<String, String> entityClassMap = new TreeMap<String, String>();
			for (String entityClassId : entityIds)
			{
				entityClassMap.put(entityClassId, entityClassId);
			}
			permissions.setEntityIds(entityClassMap);
		}

		for (Authority authority : entityAuthorities)
		{

			// add permissions for authorities that match prefix
			if (authority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_PREFIX))
			{
				Permission permission = new Permission();

				String authorityType = getAuthorityType(authority.getRole());
				String authorityPluginId = getAuthorityEntityId(authority.getRole());
				permission.setType(authorityType);
				if (authority instanceof GroupAuthority)
				{
					permission.setGroup(((GroupAuthority) authority).getGroup().getName());
					permissions.addGroupPermission(authorityPluginId, permission);
				}
			}

			// add permissions for inherited authorities from authority that match prefix
			SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getRole());
			Collection<? extends GrantedAuthority> hierarchyAuthorities = grantedAuthoritiesMapper
					.mapAuthorities(Collections.singletonList(grantedAuthority));
			hierarchyAuthorities.remove(grantedAuthority);

			for (GrantedAuthority hierarchyAuthority : hierarchyAuthorities)
			{
				if (hierarchyAuthority.getAuthority().startsWith(SecurityUtils.AUTHORITY_ENTITY_PREFIX))
				{
					String authorityPluginId = getAuthorityEntityId(hierarchyAuthority.getAuthority());

					Permission hierarchyPermission = new Permission();
					hierarchyPermission.setType(getAuthorityType(hierarchyAuthority.getAuthority()));
					permissions.addHierarchyPermission(authorityPluginId, hierarchyPermission);
				}
			}
		}

		permissions.sort();

		return permissions;
	}

	/**
	 * Returns a group authority based on group and entity class identifier.
	 *
	 * @param groupId group identifier
	 * @param entityClassId entity class identifier
	 * @return existing group authority or <code>null</code> if no matching group authority exists.
	 */
	private GroupAuthority getGroupAuthority(String groupId, String entityClassId)
	{
		Stream<GroupAuthority> stream = dataService.findAll(GROUP_AUTHORITY,
				new QueryImpl<GroupAuthority>().eq(GroupAuthorityMetaData.GROUP, groupId),
				GroupAuthority.class);
		GroupAuthority existingGroupAuthority = null;
		for (Iterator<GroupAuthority> it = stream.iterator(); it.hasNext(); )
		{
			GroupAuthority groupAuthority = it.next();
			String entity = "";
			if (groupAuthority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_COUNT_PREFIX) || groupAuthority
					.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX))
			{
				entity = groupAuthority.getRole().substring(SecurityUtils.AUTHORITY_ENTITY_COUNT_PREFIX.length());
			}
			else if (groupAuthority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX))
			{
				entity = groupAuthority.getRole().substring(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX.length());
			}
			else if (groupAuthority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX))
			{
				entity = groupAuthority.getRole().substring(SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX.length());
			}
			if (entity.equals(entityClassId))
			{
				existingGroupAuthority = groupAuthority;
			}
		}
		return existingGroupAuthority;

	}

	private String getAuthorityEntityId(String role)
	{
		role = role.substring(SecurityUtils.AUTHORITY_ENTITY_PREFIX.length());
		return role.substring(role.indexOf('_') + 1).toLowerCase();
	}

	private String getAuthorityType(String role)
	{
		role = role.substring(SecurityUtils.AUTHORITY_ENTITY_PREFIX.length());
		return role.substring(0, role.indexOf('_')).toLowerCase();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/importByUrl")
	@ResponseBody
	public ResponseEntity<String> importFileByUrl(HttpServletRequest request, @RequestParam("url") String url,
			@RequestParam(value = "entityName", required = false) String entityName,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "notify", required = false) Boolean notify) throws IOException, URISyntaxException
	{
		ImportRun importRun;
		try
		{
			File tmpFile = fileLocationToStoredRenamedFile(url, entityName);
			importRun = importFile(request, tmpFile, action, notify);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
			return ResponseEntity.badRequest().contentType(TEXT_PLAIN).body(e.getMessage());
		}
		return createCreatedResponseEntity(importRun);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/importFile")
	public ResponseEntity<String> importFile(HttpServletRequest request,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "entityName", required = false) String entityName,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "notify", required = false) Boolean notify) throws IOException, URISyntaxException
	{
		ImportRun importRun;
		String filename;
		try
		{
			filename = getFilename(file.getOriginalFilename(), entityName);
			File tmpFile = fileStore.store(file.getInputStream(), filename);
			importRun = importFile(request, tmpFile, action, notify);
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
		String href = Href.concatEntityHref("/api/v2", importRun.getEntityType().getName(), importRun.getIdValue());
		return ResponseEntity.created(new java.net.URI(href)).contentType(TEXT_PLAIN).body(href);
	}

	private File fileLocationToStoredRenamedFile(String fileLocation, String entityName) throws IOException
	{
		Path path = Paths.get(fileLocation);
		String filename = path.getFileName().toString();
		URL url = new URL(fileLocation);

		return fileStore.store(url.openStream(), getFilename(filename, entityName));
	}

	private String getFilename(String originalFileName, String entityName)
	{
		String filename;
		String extension = FileExtensionUtils
				.findExtensionFromPossibilities(originalFileName, GenericImporterExtensions.getAll());
		if (entityName == null)
		{
			filename = originalFileName;
		}
		else
		{
			filename = entityName + "." + extension;
			if (!extension.equals("vcf") && (!extension.equals("vcf.gz") && (!extension.equals("vcf.zip"))))
				LOG.warn("Specifing a filename for a non-VCF file has no effect on entity names.");
		}
		return filename;
	}

	private ImportRun importFile(HttpServletRequest request, File file, String action, Boolean notify)
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
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(file);

		importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername(), Boolean.TRUE.equals(notify));
		asyncImportJobs.execute(
				new ImportJob(importService, SecurityContextHolder.getContext(), repositoryCollection, databaseAction,
						importRun.getId(), importRunService, request.getSession(), PACKAGE_DEFAULT));

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
						"Invalid action:[" + action.toUpperCase() + "] valid values: " + (Arrays
								.toString(DatabaseAction.values())));
			}
		}

		String extension = FileExtensionUtils
				.findExtensionFromPossibilities(file.getName(), GenericImporterExtensions.getAll());

		if (extension.equals("vcf") || extension.equals("vcf.gz") || extension.equals("vcf.zip"))
		{
			NameValidator.validateName(file.getName().replace("." + extension, ""));
			if (!DatabaseAction.ADD.equals(databaseAction))
			{
				throw new IllegalArgumentException(
						"Update mode " + databaseAction + " is not supported, only ADD is supported for VCF");
			}
		}
		return databaseAction;
	}
}