package org.molgenis.omx.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.molgenis.omx.observ.Protocol;
import org.testng.annotations.Test;

public class ProtocolUtilsTest
{

	@Test
	public void getProtocolDescendants()
	{
		Protocol protocol = mock(Protocol.class);
		Protocol subProtocol1 = mock(Protocol.class);
		Protocol subProtocol2 = mock(Protocol.class);
		Protocol subProtocol3 = mock(Protocol.class);
		Protocol subProtocol4 = mock(Protocol.class);

		when(protocol.getSubprotocols()).thenReturn(Arrays.asList(subProtocol1, subProtocol2));
		when(subProtocol2.getSubprotocols()).thenReturn(Arrays.asList(subProtocol3, subProtocol4));

		List<Protocol> protocolDescendants = ProtocolUtils.getProtocolDescendants(protocol);
		assertEquals(protocolDescendants.size(), 5);
		assertTrue(protocolDescendants.contains(protocol));
		assertTrue(protocolDescendants.contains(subProtocol1));
		assertTrue(protocolDescendants.contains(subProtocol2));
		assertTrue(protocolDescendants.contains(subProtocol3));
		assertTrue(protocolDescendants.contains(subProtocol4));
	}

	@Test
	public void getProtocolDescendants_excludeSelf()
	{
		Protocol protocol = mock(Protocol.class);
		Protocol subProtocol1 = mock(Protocol.class);
		Protocol subProtocol2 = mock(Protocol.class);
		Protocol subProtocol3 = mock(Protocol.class);
		Protocol subProtocol4 = mock(Protocol.class);

		when(protocol.getSubprotocols()).thenReturn(Arrays.asList(subProtocol1, subProtocol2));
		when(subProtocol2.getSubprotocols()).thenReturn(Arrays.asList(subProtocol3, subProtocol4));

		List<Protocol> protocolDescendants = ProtocolUtils.getProtocolDescendants(protocol, false);
		assertEquals(protocolDescendants.size(), 4);
		assertTrue(protocolDescendants.contains(subProtocol1));
		assertTrue(protocolDescendants.contains(subProtocol2));
		assertTrue(protocolDescendants.contains(subProtocol3));
		assertTrue(protocolDescendants.contains(subProtocol4));
	}
}
