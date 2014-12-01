package org.molgenis.omx.catalogmanager;

import java.util.List;
import java.util.ListIterator;

import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OmxCatalogFolder extends AbstractOmxCatalogItem implements CatalogFolder
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

		// Remove inactive protocols
		ListIterator<Protocol> it = subProtocols.listIterator();
		while (it.hasNext())
		{
			Protocol prod = it.next();
			if ((prod.getActive() != null) && !prod.getActive().booleanValue())
			{
				it.remove();
			}
		}

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

	@Override
	public String getCode()
	{
		// TODO remove method
		return null;
	}

	@Override
	public String getCodeSystem()
	{
		// TODO remove method
		return null;
	}

	@Override
	public Iterable<CatalogFolder> getPath()
	{
		return getPath(protocol);
	}

	@Override
	public String getExternalId()
	{
		return protocol.getIdentifier();
	}
}
