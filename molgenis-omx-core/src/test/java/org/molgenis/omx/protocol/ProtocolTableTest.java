package org.molgenis.omx.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
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
		ObservableFeature feature10 = mock(ObservableFeature.class);
		when(feature10.getName()).thenReturn("f10");
		ObservableFeature feature11 = mock(ObservableFeature.class);
		when(feature11.getName()).thenReturn("f11");
		ObservableFeature feature12 = mock(ObservableFeature.class);
		when(feature12.getName()).thenReturn("f12");

		Protocol subProtocol1 = mock(Protocol.class);
		when(subProtocol1.getSubprotocols()).thenReturn(Collections.<Protocol> emptyList());
		when(subProtocol1.getFeatures()).thenReturn(Arrays.asList(feature10, feature11));
		when(subProtocol1.getName()).thenReturn("p1");

		Protocol subProtocol2 = mock(Protocol.class);
		when(subProtocol2.getSubprotocols_Id()).thenReturn(Collections.<Integer> emptyList());
		when(subProtocol2.getFeatures()).thenReturn(Arrays.asList(feature12));
		when(subProtocol2.getName()).thenReturn("p2");

		Protocol protocol = mock(Protocol.class);
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
		assertTrue(it.hasNext());
		Tuple tuple1 = it.next();
		assertEquals(tuple1.get("name"), "f10");
		assertEquals(tuple1.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertTrue(it.hasNext());
		Tuple tuple2 = it.next();
		assertEquals(tuple2.get("name"), "f11");
		assertEquals(tuple2.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
		assertTrue(it.hasNext());
		Tuple tuple3 = it.next();
		assertEquals(tuple3.get("name"), "p2");
		assertEquals(tuple3.get("type"), Protocol.class.getSimpleName().toLowerCase());
		assertTrue(it.hasNext());
		Tuple tuple4 = it.next();
		assertEquals(tuple4.get("name"), "f12");
		assertEquals(tuple4.get("type"), ObservableFeature.class.getSimpleName().toLowerCase());
	}

	@Test
	public void setDb()
	{
		Database anotherDatabase = mock(Database.class);
		protocolTable.setDb(anotherDatabase);
		assertEquals(protocolTable.getDb(), anotherDatabase);
	}
}
