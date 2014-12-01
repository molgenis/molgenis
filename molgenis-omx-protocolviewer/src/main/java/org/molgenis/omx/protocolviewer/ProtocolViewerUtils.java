package org.molgenis.omx.protocolviewer;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.catalogmanager.OmxCatalogFolder;
import org.molgenis.omx.observ.Protocol;

public class ProtocolViewerUtils
{
	private ProtocolViewerUtils()
	{
	}

	/**
	 * Returns a list of OMX catalog items that can be ordered
	 * 
	 * @param catalogFolderId
	 *            the id of the protocol that is being used as root to find orderable folders. The catalogFolderId
	 *            itself can be a orderable folder
	 * @return List<OmxCatalogItem>
	 * @throws UnknownCatalogException
	 */
	public static List<CatalogFolder> findOrderableItems(String catalogFolderId, DataService dataService)
			throws UnknownCatalogException
	{
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, new QueryImpl().eq(Protocol.ID, catalogFolderId)
				.and().eq(Protocol.ACTIVE, true), Protocol.class);

		if (protocol == null)
		{
			throw new UnknownCatalogException("Unknown catalog item [" + catalogFolderId + "]");
		}

		List<CatalogFolder> orderableItems = new ArrayList<CatalogFolder>();
		addProtocolToOrderableItems(protocol, orderableItems);

		return orderableItems;
	}

	/**
	 * Add groups (catalog folders) to the orderableCatalogFolders list A group: is a protocol that has features and is
	 * active.
	 * 
	 * @param protocol
	 *            : omx protocol
	 * @param orderableCatalogFolders
	 *            : catalog folders that include items that can be ordered.
	 */
	private static void addProtocolToOrderableItems(Protocol protocol, List<CatalogFolder> orderableCatalogFolders)
	{
		if (!protocol.getFeatures().isEmpty() && true == protocol.getActive())
		{
			orderableCatalogFolders.add(new OmxCatalogFolder(protocol));
		}
		for (Protocol subProtocol : protocol.getSubprotocols())
		{
			addProtocolToOrderableItems(subProtocol, orderableCatalogFolders);
		}
	}
}
