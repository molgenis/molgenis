package org.molgenis.dataexplorer.decorator;

import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.PermissionChecker;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.EntityTypePermission.MANAGE_REPORT;
import static org.molgenis.data.security.EntityTypePermission.VIEW_REPORT;

public class EntityReportPermissionChecker implements PermissionChecker<FreemarkerTemplate>
{
	private static final Pattern ENTITY_REPORT_PATTERN = Pattern.compile("view-entityreport-specific-(.+).ftl");

	private static Optional<String> getEntityTypeId(String templateName)
	{
		Matcher m = ENTITY_REPORT_PATTERN.matcher(templateName);
		if (m.matches())
		{
			return Optional.of(m.group(1));
		}
		return Optional.empty();
	}

	private final UserPermissionEvaluator permissionEvaluator;
	private final Repository<FreemarkerTemplate> delegateRepository;

	public EntityReportPermissionChecker(UserPermissionEvaluator permissionEvaluator,
			Repository<FreemarkerTemplate> delegateRepository)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
		this.delegateRepository = requireNonNull(delegateRepository);
	}

	@Override
	public boolean isAddAllowed(FreemarkerTemplate template)
	{
		return hasEntityTypePermission(template, MANAGE_REPORT);
	}

	@Override
	public boolean isReadAllowed(Object id)
	{
		return hasEntityTypePermission(load(id), VIEW_REPORT);
	}

	@Override
	public boolean isUpdateAllowed(Object id)
	{
		return hasEntityTypePermission(load(id), MANAGE_REPORT);
	}

	@Override
	public boolean isDeleteAllowed(Object id)
	{
		return hasEntityTypePermission(load(id), MANAGE_REPORT);
	}

	private FreemarkerTemplate load(Object id)
	{
		return delegateRepository.findOneById(id);
	}

	private boolean hasEntityTypePermission(FreemarkerTemplate template, EntityTypePermission permission)
	{
		return getEntityTypeId(template.getName()).map(EntityTypeIdentity::new)
												  .map(id -> permissionEvaluator.hasPermission(id, permission))
												  .orElseGet(SecurityUtils::currentUserIsSuOrSystem);
	}
}
