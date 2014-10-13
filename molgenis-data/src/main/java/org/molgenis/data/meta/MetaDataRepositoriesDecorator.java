package org.molgenis.data.meta;

/**
 * Quick and dirty hack to allow the easy decoration of the MetaDataRepositories
 */
public interface MetaDataRepositoriesDecorator
{
	public MetaDataRepositories decorate(MetaDataRepositories metaDataRepositories);
}
