package org.molgenis.data;


public interface PackageRepository
{
	Iterable<Package> getPackages();

	Package getPackage(String name);

	void addPackage(Package p);
}
