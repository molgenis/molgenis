package org.molgenis.data.meta;


/**
 * Quick and dirty hack to allow the easy decoration of the {@link MetaDataService}
 */
public interface MetaDataServiceDecoratorFactory
{
	public MetaDataService decorate(MetaDataService metaDataService);
}
