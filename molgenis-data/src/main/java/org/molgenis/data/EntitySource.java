package org.molgenis.data;

/**
 * EntitySource can provide one or more repositories. E.g. EntityManager, Excel file, ... Each repository has a unique
 * url within the source.
 */
public interface EntitySource extends RepositoryCollection
{
	/**
	 * The url of this DataSource. Concrete subclasses know how to deal with this (like a driver). Every DataSource has
	 * a unique url
	 * 
	 * example: excel://Users/john/Documents/matrix.xls
	 */
	String getUrl();
}
