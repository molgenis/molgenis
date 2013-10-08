package org.molgenis.omx.utils;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.omx.observ.Protocol;

public class ProtocolUtils
{
	/**
	 * Returns the descendants of the given protocol including the given protocol
	 * 
	 * @param protocol
	 * @return
	 */
	public static List<Protocol> getProtocolDescendants(Protocol protocol)
	{
		return getProtocolDescendants(protocol, true);
	}

	/**
	 * Returns the descendants of the given protocol
	 * 
	 * @param protocol
	 * @param includeProtocol
	 *            whether the given protocol is in the descendants list
	 * @return
	 */
	public static List<Protocol> getProtocolDescendants(Protocol protocol, boolean includeProtocol)
	{
		List<Protocol> protocols = new ArrayList<Protocol>();
		getProtocolDescendantsRec(protocol, protocols, includeProtocol);
		return protocols;
	}

	private static void getProtocolDescendantsRec(Protocol protocol, List<Protocol> protocols, boolean includeProtocol)
	{
		if (includeProtocol) protocols.add(protocol);

		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (subProtocols != null && !subProtocols.isEmpty())
		{
			for (Protocol subProtocol : subProtocols)
				getProtocolDescendantsRec(subProtocol, protocols, true);
		}
	}
}
