package org.molgenis.data.meta;

/**
 * Quick and dirty hack to allow the easy decoration of the {@link WritableMetaDataService}
 */
public interface WritableMetaDataServiceDecorator
{
	public WritableMetaDataService decorate(WritableMetaDataService metaDataRepositories);
}
