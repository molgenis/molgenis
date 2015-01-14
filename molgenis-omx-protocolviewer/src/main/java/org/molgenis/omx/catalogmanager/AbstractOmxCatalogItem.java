package org.molgenis.omx.catalogmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.Protocol;

import com.google.common.collect.Lists;

public abstract class AbstractOmxCatalogItem implements CatalogItem
{
	/**
	 * Get the parent --> child --> child --> etc. path in the tree for the given protocol
	 * 
	 * @param srcProtocol
	 * @return
	 */
	protected static Iterable<CatalogFolder> getPath(Protocol srcProtocol)
	{
		List<CatalogFolder> protocolPath = new ArrayList<CatalogFolder>();
		Collection<Protocol> protocols = Collections.singletonList(srcProtocol);
		while (protocols != null && !protocols.isEmpty())
		{
			if (protocols.size() != 1)
			{
				throw new RuntimeException("Catalog item (group) must belong to one catalog (instead of "
						+ protocols.size() + ')');
			}
			Protocol protocol = protocols.iterator().next();
			protocolPath.add(new OmxCatalogFolder(protocol));
			protocols = protocol.getSubprotocolsProtocolCollection();
		}
		return Lists.reverse(protocolPath);
	}
	
	public String getGroup()
	{
		List<String> pathItemNames = new ArrayList<String>();
		for(CatalogFolder pathItem: getPath())
		{
			pathItemNames.add(pathItem.getName());
		}
		return StringUtils.join(pathItemNames, '→');
	}
}
