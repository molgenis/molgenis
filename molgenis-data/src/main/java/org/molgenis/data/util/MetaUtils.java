package org.molgenis.data.util;

import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Package;

import javax.annotation.Nullable;

import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

public class MetaUtils
{
	private MetaUtils()
	{
	}

	public static Fetch getEntityTypeFetch()
	{
		// TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
		return new Fetch().field(ID)
						  .field(PACKAGE)
						  .field(LABEL)
						  .field(DESCRIPTION)
						  .field(ATTRIBUTES)
						  .field(IS_ABSTRACT)
						  .field(EXTENDS)
						  .field(TAGS)
						  .field(BACKEND);
	}

	/**
	 * Returns whether the given package is a system package, i.e. it is the root system package or a descendent of the
	 * root system package.
	 *
	 * @param aPackage package
	 * @return whether package is a system package
	 */
	public static boolean isSystemPackage(@Nullable Package aPackage)
	{
		if (aPackage == null)
		{
			return false;
		}
		//Needs to run as system since you might not have permission to read the sys package
		//Once we have READ_META permission checks on the packages this runAsSystem can be removed
		//https://github.com/molgenis/molgenis/issues/7528
		return runAsSystem(() -> aPackage.getId().equals(PACKAGE_SYSTEM) || (aPackage.getRootPackage() != null
				&& aPackage.getRootPackage().getId().equals(PACKAGE_SYSTEM)));
	}
}
