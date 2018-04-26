package org.molgenis.data.meta.system;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Registry containing all {@link SystemPackage}.
 */
@Component
public class SystemPackageRegistry
{
	private final Logger LOG = LoggerFactory.getLogger(SystemPackageRegistry.class);

	// note: a list instead of map is used since system packages might not be initialized when added to the registry
	private final List<SystemPackage> systemPackages;

	public SystemPackageRegistry()
	{
		systemPackages = new ArrayList<>(32);
	}

	void addSystemPackage(SystemPackage systemPackage)
	{
		LOG.trace("Registering system package [{}] ...", systemPackage.getId());
		systemPackages.add(systemPackage);
	}

	public boolean containsPackage(Package package_)
	{
		for (SystemPackage systemPackage : systemPackages)
		{
			if (systemPackage.getId().equals(package_.getId())) return true;
		}
		return false;
	}

	Stream<SystemPackage> getSystemPackages()
	{
		return systemPackages.stream();
	}
}
