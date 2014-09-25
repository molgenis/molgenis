package org.molgenis.data.meta;

/**
 * Temporary class that allows attribute meta data repository decoration (e.g. security, indexing) without a module
 * using implementations of this class requiring dependencies on other modules.
 */
public interface AttributeMetaDataRepositoryDecoratorFactory
{
	/**
	 * Creates a decorated repository based on the given repository
	 * 
	 * @param repository
	 * @return
	 */
	AttributeMetaDataRepository createDecoratedRepository(AttributeMetaDataRepository repository);
}
