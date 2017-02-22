package org.molgenis.data.meta;

public interface IdentifierLookupService
{
	/**
	 * Returns the entity id for the given fully qualified entity name.
	 *
	 * @param fullyQualifiedEntityName fully qualified entity name (e.g. package.myEntity or myEntity)
	 * @return entity type identifier or {@code null} if no entity type exists for the given fully qualified name
	 */
	String getEntityTypeId(String fullyQualifiedEntityName);

	/**
	 * Returns the package id for the given fully qualified package name.
	 *
	 * @param fullyQualifiedPackageName fully qualified package name (e.g. root.package or package)
	 * @return package identifier or {@code null} if no package exists for the given fully qualified name
	 */
	String getPackageId(String fullyQualifiedPackageName);
}