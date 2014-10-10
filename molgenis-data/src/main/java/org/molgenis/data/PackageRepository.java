package org.molgenis.data;

public interface PackageRepository
{
	/**
	 * Get all packages
	 * 
	 * @return
	 */
	Iterable<Package> getPackages();

	/**
	 * Get a package by it's qualified name. Returns null if not found
	 * 
	 * @param name
	 * @return
	 */
	Package getPackage(String name);

	/**
	 * Add a new Package
	 * 
	 * @param p
	 */
	void addPackage(Package p);

	/**
	 * Get all subpackages of a package
	 * 
	 * @param pack
	 * @return
	 */
	Iterable<Package> getSubPackages(Package p);

	// TODO updatePackage
}
