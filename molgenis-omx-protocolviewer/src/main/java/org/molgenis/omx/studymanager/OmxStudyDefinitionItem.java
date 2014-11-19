package org.molgenis.omx.studymanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.Protocol;

import com.google.common.collect.Lists;

public class OmxStudyDefinitionItem implements CatalogItem
{
	private final Protocol protocol;
	private final Integer catalogId;

	public OmxStudyDefinitionItem(Protocol protocol, Integer catalogId)
	{
		if (protocol == null) throw new IllegalArgumentException("observableFeature is null");
		if (catalogId == null) throw new IllegalArgumentException("catalogId is null");
		this.protocol = protocol;
		this.catalogId = catalogId;
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
	public List<String> getPath()
	{
		List<String> protocolPath = new ArrayList<String>();
		Collection<Protocol> protocols = Arrays.asList(protocol);
		boolean rootReached = false;
		while (protocols != null && !protocols.isEmpty() && !rootReached)
		{
			if (protocols.size() != 1)
			{
				throw new RuntimeException("Catalog item (group) must belong to one catalog (instead of "
						+ protocols.size() + ')');
			}
			Protocol protocol = protocols.iterator().next();
			// Stop when catalog protocol is found (this is the root)
			if (protocol.getId().equals(catalogId)) rootReached = true;
			protocolPath.add(protocol.getId().toString());
			protocols = protocol.getSubprotocolsProtocolCollection();
		}

		return Lists.reverse(protocolPath);
	}
}
