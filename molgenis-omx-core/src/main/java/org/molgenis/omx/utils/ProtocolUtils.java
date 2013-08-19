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
		List<Protocol> protocols = new ArrayList<Protocol>();
		getProtocolDescendantsRec(protocol, protocols);
		return protocols;
	}

	private static void getProtocolDescendantsRec(Protocol protocol, List<Protocol> protocols)
	{
		protocols.add(protocol);

		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (subProtocols != null && !subProtocols.isEmpty())
		{
			for (Protocol subProtocol : subProtocols)
				getProtocolDescendantsRec(subProtocol, protocols);
		}
	}
}
