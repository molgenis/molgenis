package org.molgenis.security.permission;

import org.molgenis.auth.Role;
import org.molgenis.data.DataService;
import org.molgenis.data.security.acl.SecurityId;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.acl.EntityAce;
import org.molgenis.data.security.acl.EntityAcl;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.RoleMetadata.ROLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.QueryRule.Operator.SEARCH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/permission")
public class MolgenisPermissionController
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final EntityAclService entityAclService;
	private final DataService dataService;
	private final LanguageService languageService;

	@Autowired
	public MolgenisPermissionController(MolgenisPermissionService molgenisPermissionService,
			EntityAclService entityAclService, LanguageService languageService, DataService dataService)
	{
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.entityAclService = requireNonNull(entityAclService);
		this.languageService = requireNonNull(languageService);
		this.dataService = requireNonNull(dataService);
	}

	@RequestMapping(value = "/{entityTypeId}/read", method = GET)
	@ResponseBody
	public boolean hasReadPermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityTypeId,
				org.molgenis.security.core.Permission.READ);
	}

	@RequestMapping(value = "/{entityTypeId}/write", method = GET)
	@ResponseBody
	public boolean hasWritePermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityTypeId,
				org.molgenis.security.core.Permission.WRITE);
	}

	@RequestMapping(value = "/sid", method = GET)
	@ResponseBody
	public Collection<SecurityId> getSids()
	{
		return dataService.findAll(ROLE, Role.class)
						  .map(role -> SecurityId.createForAuthority(role.getLabel()))
						  .collect(toList());
	}

	/**
	 * Retrieves the ACLs for entities of a specific EntityType
	 *
	 * @param entityTypeId entity type ID
	 * @param filter       search string to filter on
	 * @param pageSize     number of ACLs to retrieve
	 * @return the retrieved {@link EntityAcl}s
	 */
	@RequestMapping(value = "/acls", produces = "application/json", method = GET)
	@ResponseBody
	public List<Map> entityAcls(@RequestParam String entityTypeId, @RequestParam(required = false) String filter,
			@RequestParam(defaultValue = "10") int pageSize)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		String languageCode = languageService.getCurrentUserLanguageCode();
		String labelAttribute = entityType.getLabelAttribute(languageCode).getName();

		QueryImpl<Entity> query = new QueryImpl<>();
		if (!isEmpty(filter))
		{
			query.addRule(new QueryRule(SEARCH, filter));
		}
		query.pageSize(pageSize);
		return dataService.findAll(entityTypeId, query).map(entity ->
		{
			EntityIdentity entityIdentity = EntityIdentity.create(entityTypeId, entity.getIdValue());
			EntityAcl acl = entityAclService.readAcl(entityIdentity);
			List<ImmutableMap<String, Object>> aces = acl.getEntries().stream().map(this::getAceMap).collect(toList());
			return ImmutableMap.of("entityId", entity.getIdValue(), "entityLabel", entity.getString(labelAttribute),
					"owner", acl.getOwner(), "aces", aces);
		}).collect(toList());
	}

	private ImmutableMap<String, Object> getAceMap(EntityAce ace)
	{
		ImmutableList.Builder<String> permissions = ImmutableList.builder();
		for (org.molgenis.security.core.Permission permission : ace.getPermissions())
		{
			permissions.add(permission.name());
		}
		return ImmutableMap.of("granted", ace.isGranting(), "permissions", permissions.build(), "sid",
				ace.getSecurityId());
	}


}
