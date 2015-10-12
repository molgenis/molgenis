package org.molgenis.data.importer;

import static org.molgenis.data.importer.ImportWizardController.URI;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.molgenis.auth.Authority;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.Permission;
import org.molgenis.security.permission.Permissions;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.Lists;

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
	private final DataService dataService;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;
	private final UserAccountService userAccountService;

	@Autowired
	public ImportWizardController(UploadWizardPage uploadWizardPage, OptionsWizardPage optionsWizardPage,
			PackageWizardPage packageWizardPage, ValidationResultWizardPage validationResultWizardPage,
			ImportResultsWizardPage importResultsWizardPage, DataService dataService,
			GrantedAuthoritiesMapper grantedAuthoritiesMapper, UserAccountService userAccountService)
	{
		super(URI, "importWizard");
		if (uploadWizardPage == null) throw new IllegalArgumentException("UploadWizardPage is null");
		if (optionsWizardPage == null) throw new IllegalArgumentException("OptionsWizardPage is null");
		if (validationResultWizardPage == null) throw new IllegalArgumentException("ValidationResultWizardPage is null");
		if (importResultsWizardPage == null) throw new IllegalArgumentException("ImportResultsWizardPage is null");
		this.uploadWizardPage = uploadWizardPage;
		this.optionsWizardPage = optionsWizardPage;
		this.validationResultWizardPage = validationResultWizardPage;
		this.importResultsWizardPage = importResultsWizardPage;
		this.packageWizardPage = packageWizardPage;
		this.userAccountService = userAccountService;
		this.dataService = dataService;
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
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
		for (MolgenisGroup molgenisGroup : userAccountService.getCurrentUserGroups())
		{
			if (molgenisGroup.getId().equals(groupId))
			{
				allowed = true;
			}
		}
		if (!allowed && !userAccountService.getCurrentUser().getSuperuser())
		{
			throw new RuntimeException("Current user does not belong to the requested group.");
		}
		String entitiesString = webRequest.getParameter("entityIds");
		List<String> entities = Arrays.asList(entitiesString.split(","));

		MolgenisGroup molgenisGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId, MolgenisGroup.class);
		if (molgenisGroup == null) throw new RuntimeException("unknown group id [" + groupId + "]");
		List<Authority> groupPermissions = getGroupPermissions(molgenisGroup);
		Permissions permissions = createPermissions(groupPermissions, entities);
		permissions.setGroupId(groupId);
		return permissions;

	}

	@RequestMapping(value = "/add/entityclass/group", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void addGroupEntityClassPermissions(@RequestParam String groupId, WebRequest webRequest)
	{
		List<String> entities = Lists.newArrayList(dataService.getEntityNames());

		for (String entityClassId : entities)
		{

			GroupAuthority authority = getGroupAuthority(groupId, entityClassId);
			String param = "radio-" + entityClassId;
			String value = webRequest.getParameter(param);
			if (value != null
					&& (SecurityUtils.currentUserHasRole(SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX
							+ entityClassId.toUpperCase()) || userAccountService.getCurrentUser().getSuperuser()))
			{
				if ((value.equalsIgnoreCase(org.molgenis.security.core.Permission.READ.toString())
						|| value.equalsIgnoreCase(org.molgenis.security.core.Permission.COUNT.toString()) || value
							.equalsIgnoreCase(org.molgenis.security.core.Permission.WRITE.toString())))
				{
					authority.setMolgenisGroup(dataService.findOne(MolgenisGroup.ENTITY_NAME, groupId,
							MolgenisGroup.class));
					authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX + value.toUpperCase() + "_"
							+ entityClassId.toUpperCase());
					if (authority.getId() == null)
					{
						authority.setId(UUID.randomUUID().toString());
						dataService.add(GroupAuthority.ENTITY_NAME, authority);
					}
					else dataService.update(GroupAuthority.ENTITY_NAME, authority);
				}
				else if (value.equalsIgnoreCase(org.molgenis.security.core.Permission.NONE.toString()))
				{
					if (authority.getId() != null) dataService.delete(GroupAuthority.ENTITY_NAME, authority.getId());
				}
				else
				{
					throw new RuntimeException("Unknown value: " + value + " for permission on entity: "
							+ entityClassId);
				}
			}
			else
			{
				if (value != null) throw new MolgenisDataAccessException(
						"Current user is not allowed to change the permissions for this entity: " + entityClassId);
			}
		}
	}

	private List<Authority> getGroupPermissions(MolgenisGroup molgenisGroup)
	{
		Iterable<GroupAuthority> authorities = dataService.findAll(GroupAuthority.ENTITY_NAME,
				new QueryImpl().eq(GroupAuthority.MOLGENISGROUP, molgenisGroup), GroupAuthority.class);

		return Lists.newArrayList(authorities);
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
					permission.setGroup(((GroupAuthority) authority).getMolgenisGroup().getName());
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

	private GroupAuthority getGroupAuthority(String groupId, String entityClassId)
	{
		GroupAuthority authority = new GroupAuthority();
		for (GroupAuthority groupAuthority : dataService.findAll(GroupAuthority.ENTITY_NAME,
				new QueryImpl().eq(GroupAuthority.MOLGENISGROUP, groupId), GroupAuthority.class))
		{
			String entity = "";
			if (groupAuthority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_COUNT_PREFIX)
					|| groupAuthority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX))
			{
				entity = groupAuthority.getRole().substring(SecurityUtils.AUTHORITY_ENTITY_COUNT_PREFIX.length());
			}
			else if (groupAuthority.getRole().startsWith(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX))
			{
				entity = groupAuthority.getRole().substring(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX.length());
			}
			if (entity.equals(entityClassId.toUpperCase()))
			{
				authority = groupAuthority;
			}
		}
		return authority;
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
}
