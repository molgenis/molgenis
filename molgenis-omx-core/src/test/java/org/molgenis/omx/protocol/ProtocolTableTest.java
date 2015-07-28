package org.molgenis.omx.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.util.tuple.Tuple;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProtocolTableTest
{
	private Database database;
	private ProtocolTable protocolTable;

	@BeforeMethod
	public void setUp() throws DatabaseException, TableException
	{
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
		when(subProtocol2.getSubprotocols_Id()).thenReturn(Collections.<Integer> emptyList());
		when(subProtocol2.getFeatures()).thenReturn(Arrays.asList(feature12));
		when(subProtocol2.getName()).thenReturn("p2");

		Protocol protocol = when(mock(Protocol.class).getId()).thenReturn(0).getMock();
		when(protocol.getSubprotocols()).thenReturn(Arrays.asList(subProtocol1, subProtocol2));
		when(protocol.getName()).thenReturn("p0");

		database = mock(Database.class);

		protocolTable = new ProtocolTable(protocol, database);
	}

	@Test(expectedExceptions = TableException.class)
	public void ProtocolTable() throws TableException
	{
		new ProtocolTable(null, null);
	}

	@Test
	public void getAllColumns() throws TableException
	{
		assertEquals(protocolTable.getAllColumns().size(), 7);
	}

	@Test
	public void getCount() throws DatabaseException, TableException
	{
		assertEquals(protocolTable.getCount(), 5);
	}

	@Test
	public void getCountOneProtocolWithOneFeature() throws DatabaseException, TableException
	{
		int featureId = 1;
		ObservableFeature feature = when(mock(ObservableFeature.class).getId()).thenReturn(featureId).getMock();
		when(feature.getName()).thenReturn("f10");

		int protocolId = 0;
		Protocol protocol = when(mock(Protocol.class).getId()).thenReturn(protocolId).getMock();
		when(protocol.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(protocol.getFeatures()).thenReturn(Arrays.<ObservableFeature> asList(feature));
		when(protocol.getName()).thenReturn("p0");

		assertEquals(new ProtocolTable(protocol, database).getCount(), 1); // excluding root protocol
	}

	@Test
	public void getCountProtocolWithSubProtocolAndFeature() throws DatabaseException, TableException
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

		assertEquals(new ProtocolTable(protocol0, database).getCount(), 2); // excluding root protocol
	}

	@Test
	public void getDb()
	{
		assertEquals(protocolTable.getDb(), database);
	}

	@Test
	public void iterator()
	{
		Iterator<Tuple> it = protocolTable.iterator();
		assertTrue(it.hasNext());
		Tuple tuple0 = it.next();
		assertEquals(tuple0.get("name"), "p1");
		assertEquals(tuple0.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple0.get("path"), "0.1");
		assertTrue(it.hasNext());
		Tuple tuple1 = it.next();
		assertEquals(tuple1.get("name"), "f10");
		assertEquals(tuple1.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple1.get("path"), "0.1.F10");
		assertTrue(it.hasNext());
		Tuple tuple2 = it.next();
		assertEquals(tuple2.get("name"), "f11");
		assertEquals(tuple2.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple2.get("path"), "0.1.F11");
		assertTrue(it.hasNext());
		Tuple tuple3 = it.next();
		assertEquals(tuple3.get("name"), "p2");
		assertEquals(tuple3.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple3.get("path"), "0.2");
		assertTrue(it.hasNext());
		Tuple tuple4 = it.next();
		assertEquals(tuple4.get("name"), "f12");
		assertEquals(tuple4.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple4.get("path"), "0.2.F12");
	}

	@Test
	public void iteratorProtocolWithSubProtocolAndFeature() throws TableException
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

		ProtocolTable protocolTable = new ProtocolTable(protocol0, database);
		Iterator<Tuple> it = protocolTable.iterator();
		assertTrue(it.hasNext());
		Tuple tuple0 = it.next();
		assertEquals(tuple0.get("name"), "p1");
		assertEquals(tuple0.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertEquals(tuple0.get("path"), "0.1");
		assertTrue(it.hasNext());
		Tuple tuple1 = it.next();
		assertEquals(tuple1.get("name"), "f10");
		assertEquals(tuple1.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertEquals(tuple1.get("path"), "0.F10");
		assertFalse(it.hasNext());
	}

	@Test
	public void iteratorOneProtocolOneFeature() throws TableException
	{
		int featureId = 1;
		ObservableFeature feature = when(mock(ObservableFeature.class).getId()).thenReturn(featureId).getMock();
		when(feature.getName()).thenReturn("f10");

		int protocolId = 0;
		Protocol protocol = when(mock(Protocol.class).getId()).thenReturn(protocolId).getMock();
		when(protocol.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(protocol.getFeatures()).thenReturn(Arrays.<ObservableFeature> asList(feature));
		when(protocol.getName()).thenReturn("p0");

		ProtocolTable protocolTable2 = new ProtocolTable(protocol, database);
		Iterator<Tuple> it = protocolTable2.iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getString("path"), protocolId + ".F" + featureId);
		assertFalse(it.hasNext());
	}

	@Test
	public void setDb()
	{
		Database anotherDatabase = mock(Database.class);
		protocolTable.setDb(anotherDatabase);
		assertEquals(protocolTable.getDb(), anotherDatabase);
	}
}
