package org.molgenis.data;

/**
 * Creates an Entityource based on an url
 */
public interface EntitySourceFactory
{
	/**
	 * Returns the url prefix of the DataSource For example for excel DataSources the prefix is 'excel' The urls are
	 * like excel://Users/john/Documents/matrix.xls
	 */
	String getUrlPrefix();

	/**
	 * Creates a new EntitySource
	 */
	EntitySource create(String url);
}
