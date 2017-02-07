package org.molgenis.data.meta;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

@Component
public class IdentifierLookupServiceImpl implements IdentifierLookupService
{
	private final DataService dataService;

	@Autowired
	public IdentifierLookupServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	@RunAsSystem
	public String getEntityTypeId(String fullyQualifiedEntityName)
	{
		String[] entityNameTokens = StringUtils.split(fullyQualifiedEntityName, Package.PACKAGE_SEPARATOR);

		String entityTypeId;
		if (entityNameTokens.length == 1)
		{
			entityTypeId = getEntityTypeIdNoPackage(entityNameTokens[0]);
		}
		else
		{
			entityTypeId = getEntityTypeIdPackage(entityNameTokens);
		}
		return entityTypeId;
	}

	@Override
	@RunAsSystem
	public String getPackageId(String fullyQualifiedPackageName)
	{
		String[] packagePath = fullyQualifiedPackageName.split(Package.PACKAGE_SEPARATOR);

		String packageId;
		if (packagePath.length == 1)
		{
			packageId = getPackageIdNoParentPackage(fullyQualifiedPackageName);
		}
		else
		{
			packageId = getPackageIdParentPackage(packagePath);
		}
		return packageId;
	}

	private String getEntityTypeIdNoPackage(String entityName)
	{
		EntityType entityType = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				.eq(EntityTypeMetadata.NAME, entityName).and().eq(EntityTypeMetadata.PACKAGE, null)
				.fetch(new Fetch().field(EntityTypeMetadata.ID)).findOne();
		return entityType != null ? entityType.getId() : null;
	}

	private String getEntityTypeIdPackage(String[] entityNameTokens)
	{
		String entityName = entityNameTokens[entityNameTokens.length - 1];
		String[] packageTokens = copyOfRange(entityNameTokens, 0, entityNameTokens.length - 1);

		Stream<EntityType> entityTypeStream = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				.eq(EntityTypeMetadata.NAME, entityName)
				.fetch(new Fetch().field(EntityTypeMetadata.ID).field(EntityTypeMetadata.PACKAGE)).findAll();

		return entityTypeStream.filter(entityType -> isMatch(entityType.getPackage(), packageTokens))
				.map(EntityType::getId).findFirst().orElse(null);
	}

	private String getPackageIdNoParentPackage(String packageName)
	{
		Package package_ = dataService.query(PACKAGE, Package.class).eq(PackageMetadata.NAME, packageName).and()
				.eq(PackageMetadata.PARENT, null).fetch(new Fetch().field(PackageMetadata.ID)).findOne();
		return package_ != null ? package_.getId() : null;
	}

	private String getPackageIdParentPackage(String[] packagePath)
	{
		String packageName = packagePath[packagePath.length - 1];
		Stream<Package> packageStream = dataService.query(PACKAGE, Package.class)
				.eq(PackageMetadata.NAME, packageName)
				.fetch(new Fetch().field(PackageMetadata.ID).field(PackageMetadata.PARENT)).findAll();

		return packageStream.filter(package_ -> isMatch(package_, packagePath)).map(Package::getId).findFirst()
				.orElse(null);
	}

	private static boolean isMatch(Package package_, String[] packageTokens)
	{
		if (package_ == null)
		{
			return false;
		}

		Package packageAtDepth = package_;
		for (int depth = packageTokens.length - 1; depth >= 0; --depth)
		{
			if (packageAtDepth == null || !packageAtDepth.getName().equals(packageTokens[depth]))
			{
				return false;
			}
			packageAtDepth = packageAtDepth.getParent();
		}
		return packageAtDepth == null;
	}
}