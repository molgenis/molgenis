package org.molgenis.security.permission;

import com.google.common.collect.ImmutableMap;
import org.molgenis.auth.Role;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.PermissionService;
import org.molgenis.data.security.acl.*;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.molgenis.auth.RoleMetadata.ROLE;
import static org.molgenis.data.QueryRule.Operator.SEARCH;

@Controller
@RequestMapping("/permission")
public class MolgenisPermissionController
{
	private final PermissionService molgenisPermissionService;
	private final EntityAclService entityAclService;
	private final EntityAclManager entityAclManager;
	private final DataService dataService;
	private final LanguageService languageService;

	@Autowired
	public MolgenisPermissionController(PermissionService molgenisPermissionService, EntityAclService entityAclService,
			EntityAclManager entityAclManager, LanguageService languageService, DataService dataService)
	{
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.entityAclService = requireNonNull(entityAclService);
		this.entityAclManager = requireNonNull(entityAclManager);
		this.languageService = requireNonNull(languageService);
		this.dataService = requireNonNull(dataService);
	}

	@GetMapping(value = "/{entityTypeId}/read")
	@ResponseBody
	public boolean hasReadPermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntityType(entityTypeId,
				org.molgenis.security.core.Permission.READ);
	}

	@GetMapping(value = "/{entityTypeId}/write")
	@ResponseBody
	public boolean hasWritePermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntityType(entityTypeId,
				org.molgenis.security.core.Permission.WRITE);
	}

	@GetMapping(value = "/sid")
	@ResponseBody
	public Collection<SecurityId> getSids()
	{
		return dataService.findAll(ROLE, Role.class)
						  .map(role -> SecurityId.createForAuthority(role.getLabel()))
						  .collect(toList());
	}

	@PutMapping(value = "/acl")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void save(@RequestBody EntityAcl acl)
	{
		entityAclManager.updateAcl(acl);
	}

	@GetMapping(value = "/acl")
	@ResponseBody
	public ResponseEntity<EntityAcl> getEntityAcl(@RequestParam String entityTypeId, @RequestParam String entityId)
	{
		EntityIdentity identity = EntityIdentity.create(entityTypeId, entityId);
		try
		{
			EntityAcl acl = entityAclService.readAcl(identity);
			if (acl == null)
			{
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(acl);
		}
		catch (MolgenisDataAccessException err)
		{
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	/**
	 * Retrieves the ACLs for entities of a specific EntityType
	 *
	 * @param entityTypeId entity type ID
	 * @param filter       search string to filter on
	 * @param pageSize     number of ACLs to retrieve
	 * @return the retrieved {@link EntityAcl}s
	 */
	@GetMapping(value = "/acls", produces = "application/json")
	@ResponseBody

	public List<Map> entityAcls(@RequestParam String entityTypeId, @RequestParam(required = false) String filter,
			@RequestParam(defaultValue = "10") int pageSize)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		String languageCode = languageService.getCurrentUserLanguageCode();
		String labelAttribute = entityType.getLabelAttribute(languageCode).getName();

		QueryImpl<Entity> query = new QueryImpl<>();
		if (isNotBlank(filter))
		{
			query.addRule(new QueryRule(SEARCH, filter.trim()));
		}
		query.pageSize(pageSize);
		return dataService.findAll(entityTypeId, query).map(entity ->
		{
			EntityIdentity entityIdentity = EntityIdentity.create(entityTypeId, entity.getIdValue());
			EntityAcl acl = entityAclService.readAcl(entityIdentity);
			return ImmutableMap.of("entity", entity.getIdValue(), "entityLabel", entity.getString(labelAttribute),
					"acl", acl);
		}).collect(toList());
	}

}
