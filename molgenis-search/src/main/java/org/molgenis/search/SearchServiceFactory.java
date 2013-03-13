package org.molgenis.search;

import java.io.Closeable;

/**
 * Creates a SearchService. Close after use. Can be a singleton
 * 
 * @author erwin
 * 
 */
public interface SearchServiceFactory extends Closeable
{
	SearchService create();
}
