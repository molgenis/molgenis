package org.molgenis.data.meta;

/**
 * Temporary class that allows entity meta data repository decoration (e.g. security, indexing) without a module using
 * implementations of this class requiring dependencies on other modules.
 */
public interface EntityMetaDataRepositoryDecoratorFactory
{
	/**
	 * Creates a decorated repository based on the given repository
	 * 
	 * @param repository
	 * @return
	 */
	EntityMetaDataRepository createDecoratedRepository(EntityMetaDataRepository repository);
}
