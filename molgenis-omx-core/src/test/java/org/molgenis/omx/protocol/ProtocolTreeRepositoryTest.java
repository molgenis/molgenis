package org.molgenis.omx.protocol;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.mockito.Matchers;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

@SuppressWarnings("resource")
public class ProtocolTreeRepositoryTest
{
	private DataService dataService;
	private ProtocolTreeRepository protocolTreeRepository;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		dataService = mock(DataService.class);

		ObservableFeature feature10 = when(mock(ObservableFeature.class).getId()).thenReturn(10).getMock();
		when(feature10.getName()).thenReturn("f10");
		ObservableFeature feature11 = when(mock(ObservableFeature.class).getId()).thenReturn(11).getMock();
		when(feature11.getName()).thenReturn("f11");
		ObservableFeature feature12 = when(mock(ObservableFeature.class).getId()).thenReturn(12).getMock();
		when(feature12.getName()).thenReturn("f12");

		Protocol subProtocol1 = when(mock(Protocol.class).getId()).thenReturn(1).getMock();
		when(subProtocol1.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(subProtocol1.getFeatures()).thenReturn(Arrays.asList(feature10, feature11));
		when(subProtocol1.getName()).thenReturn("p1");

		Protocol subProtocol2 = when(mock(Protocol.class).getId()).thenReturn(2).getMock();
		when(subProtocol2.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(subProtocol2.getFeatures()).thenReturn(Arrays.asList(feature12));
		when(subProtocol2.getName()).thenReturn("p2");

		Protocol protocol = when(mock(Protocol.class).getId()).thenReturn(0).getMock();
		when(protocol.getSubprotocols()).thenReturn(Arrays.asList(subProtocol1, subProtocol2));
		when(protocol.getName()).thenReturn("p0");

		when(
				dataService.findAll(Category.ENTITY_NAME,
						new QueryImpl().eq(anyString(), any(ObservableFeature.class)),
						Matchers.notNull(Characteristic.class.getClass()))).thenReturn(
				Collections.<Category> emptyList());

		protocolTreeRepository = new ProtocolTreeRepository(protocol, dataService, "test");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ProtocolTreeRepository()
	{
		new ProtocolTreeRepository(null, null, null);
	}

	@Test
	public void getAllColumns()
	{
		assertEquals(Iterables.size(protocolTreeRepository.getAttributes()), 9);
	}

	@Test
	public void count()
	{
		assertEquals(protocolTreeRepository.count(), 6);
	}

	@Test
	public void getCountOneProtocolWithOneFeature()
	{
		int featureId = 1;
		ObservableFeature feature = when(mock(ObservableFeature.class).getId()).thenReturn(featureId).getMock();
		when(feature.getName()).thenReturn("f10");

		int protocolId = 0;
		Protocol protocol = when(mock(Protocol.class).getId()).thenReturn(protocolId).getMock();
		when(protocol.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(protocol.getFeatures()).thenReturn(Arrays.<ObservableFeature> asList(feature));
		when(protocol.getName()).thenReturn("p0");

		assertEquals(new ProtocolTreeRepository(protocol, dataService, "test").count(), 2);
	}

	@Test
	public void getCountProtocolWithSubProtocolAndFeature()
	{
		ObservableFeature feature1 = when(mock(ObservableFeature.class).getId()).thenReturn(10).getMock();
		when(feature1.getName()).thenReturn("f10");

		Protocol protocol1 = when(mock(Protocol.class).getId()).thenReturn(1).getMock();
		when(protocol1.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(protocol1.getFeatures()).thenReturn(Collections.<ObservableFeature> emptyList());
		when(protocol1.getName()).thenReturn("p1");

		Protocol protocol0 = when(mock(Protocol.class).getId()).thenReturn(0).getMock();
		when(protocol0.getSubprotocols()).thenReturn(Collections.singletonList(protocol1));
		when(protocol0.getFeatures()).thenReturn(Arrays.<ObservableFeature> asList(feature1));
		when(protocol0.getName()).thenReturn("p0");

		assertEquals(new ProtocolTreeRepository(protocol0, dataService, "test").count(), 3);
	}

	@Test
	public void iterator()
	{
		Iterator<Entity> it = protocolTreeRepository.iterator();
		assertTrue(it.hasNext());
		Entity tuple0 = it.next();
		assertEquals(tuple0.get("name"), "p0");
		assertEquals(tuple0.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple0.get("path"), "0");
		assertTrue(it.hasNext());
		Entity tuple1 = it.next();
		assertEquals(tuple1.get("name"), "p1");
		assertEquals(tuple1.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple1.get("path"), "0.1");
		assertTrue(it.hasNext());
		Entity tuple2 = it.next();
		assertEquals(tuple2.get("name"), "f10");
		assertEquals(tuple2.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple2.get("path"), "0.1.F10");
		assertTrue(it.hasNext());
		Entity tuple3 = it.next();
		assertEquals(tuple3.get("name"), "f11");
		assertEquals(tuple3.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple3.get("path"), "0.1.F11");
		assertTrue(it.hasNext());
		Entity tuple4 = it.next();
		assertEquals(tuple4.get("name"), "p2");
		assertEquals(tuple4.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple4.get("path"), "0.2");
		assertTrue(it.hasNext());
		Entity tuple5 = it.next();
		assertEquals(tuple5.get("name"), "f12");
		assertEquals(tuple5.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple5.get("path"), "0.2.F12");
	}

	@Test
	public void iteratorProtocolWithSubProtocolAndFeature()
	{
		ObservableFeature feature1 = when(mock(ObservableFeature.class).getId()).thenReturn(10).getMock();
		when(feature1.getName()).thenReturn("f10");

		Protocol protocol1 = when(mock(Protocol.class).getId()).thenReturn(1).getMock();
		when(protocol1.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(protocol1.getFeatures()).thenReturn(Collections.<ObservableFeature> emptyList());
		when(protocol1.getName()).thenReturn("p1");

		Protocol protocol0 = when(mock(Protocol.class).getId()).thenReturn(0).getMock();
		when(protocol0.getSubprotocols()).thenReturn(Collections.singletonList(protocol1));
		when(protocol0.getFeatures()).thenReturn(Arrays.<ObservableFeature> asList(feature1));
		when(protocol0.getName()).thenReturn("p0");

		ProtocolTreeRepository protocolTreeRepository = new ProtocolTreeRepository(protocol0, dataService, "test");
		Iterator<Entity> it = protocolTreeRepository.iterator();
		assertTrue(it.hasNext());
		Entity tuple0 = it.next();
		assertEquals(tuple0.get("name"), "p0");
		assertEquals(tuple0.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple0.get("path"), "0");
		assertTrue(it.hasNext());
		Entity tuple1 = it.next();
		assertEquals(tuple1.get("name"), "p1");
		assertEquals(tuple1.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple1.get("path"), "0.1");
		assertTrue(it.hasNext());
		Entity tuple2 = it.next();
		assertEquals(tuple2.get("name"), "f10");
		assertEquals(tuple2.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple2.get("path"), "0.F10");
		assertFalse(it.hasNext());
	}

	@Test
	public void iteratorOneProtocolOneFeature()
	{
		int featureId = 1;
		ObservableFeature feature = when(mock(ObservableFeature.class).getId()).thenReturn(featureId).getMock();
		when(feature.getName()).thenReturn("f10");

		int protocolId = 0;
		Protocol protocol = when(mock(Protocol.class).getId()).thenReturn(protocolId).getMock();
		when(protocol.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(protocol.getFeatures()).thenReturn(Arrays.<ObservableFeature> asList(feature));
		when(protocol.getName()).thenReturn("p0");

		ProtocolTreeRepository protocolTreeRepository2 = new ProtocolTreeRepository(protocol, dataService, "test");
		Iterator<Entity> it = protocolTreeRepository2.iterator();
		assertTrue(it.hasNext());
		Entity tuple0 = it.next();
		assertEquals(tuple0.get("name"), "p0");
		assertEquals(tuple0.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple0.get("path"), String.valueOf(protocolId));
		assertTrue(it.hasNext());
		Entity tuple1 = it.next();
		assertEquals(tuple1.get("name"), "f10");
		assertEquals(tuple1.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple1.get("path"), protocolId + ".F" + featureId);
		assertFalse(it.hasNext());
	}
}
