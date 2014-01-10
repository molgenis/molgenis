package org.molgenis.omx.catalogmanager;

import java.util.List;

import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OmxCatalogFolder implements CatalogFolder
{
	private final Protocol protocol;

	public OmxCatalogFolder(Protocol protocol)
	{
		if (protocol == null) throw new IllegalArgumentException("Protocol is null");
		this.protocol = protocol;
	}

	@Override
	public String getId()
	{
		return protocol.getId().toString();
	}

	@Override
	public String getName()
	{
		return protocol.getName();
	}

	@Override
	public String getDescription()
	{
		return protocol.getDescription();
	}

	@Override
	public List<CatalogFolder> getChildren()
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		return subProtocols != null ? Lists.transform(subProtocols, new Function<Protocol, CatalogFolder>()
		{
			@Override
			public CatalogFolder apply(Protocol subProtocol)
			{
				return new OmxCatalogFolder(subProtocol);
			}
		}) : null;
	}

	@Override
	public List<CatalogItem> getItems()
	{
		List<ObservableFeature> features = protocol.getFeatures();
		return features != null ? Lists.transform(features, new Function<ObservableFeature, CatalogItem>()
		{
			@Override
			public CatalogItem apply(ObservableFeature observableFeature)
			{
				return new OmxCatalogItem(observableFeature);
			}
		}) : null;
	}
}
