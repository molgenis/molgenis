package org.molgenis.omx.catalogmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.Protocol;

import com.google.common.collect.Lists;

public class OmxCatalogItem implements CatalogItem
{
	private final Protocol protocol;

	public OmxCatalogItem(Protocol protocol)
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
	public String getCode()
	{
		return null;
	}

	@Override
	public String getCodeSystem()
	{
		return null;
	}

	@Override
	public Iterable<String> getPath()
	{
		List<String> protocolPath = new ArrayList<String>();
		Collection<Protocol> protocols = Collections.singletonList(protocol);
		while (protocols != null && !protocols.isEmpty())
		{
			if (protocols.size() != 1)
			{
				throw new RuntimeException("Catalog item (group) must belong to one catalog (instead of "
						+ protocols.size() + ')');
			}
			Protocol protocol = protocols.iterator().next();
			protocolPath.add(protocol.getId().toString());
			protocols = protocol.getSubprotocolsProtocolCollection();
		}
		return Lists.reverse(protocolPath);
	}
}
