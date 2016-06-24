package org.molgenis.data;

/**
 * Temporary class that allows repository decoration (e.g. security, indexing) without a module using implementations of
 * this class requiring dependencies on other modules.
 */
public interface RepositoryDecoratorFactory
{
	/**
	 * Creates a decorated repository based on the given repository
	 * 
	 * @param repository
	 * @return
	 */
	Repository createDecoratedRepository(Repository repository);
}
