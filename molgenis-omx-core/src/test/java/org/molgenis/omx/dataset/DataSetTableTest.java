package org.molgenis.omx.dataset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.molgenis.JDBCMetaDatabase;
import org.molgenis.data.DataService;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.testng.annotations.Test;

public class DataSetTableTest
{
	@Test
	public void getAllColumns() throws TableException, DatabaseException
	{

		ObservableFeature f10 = mock(ObservableFeature.class);
		when(f10.getIdentifier()).thenReturn("10");
		when(f10.getName()).thenReturn("name10");
		when(f10.getDataType()).thenReturn("string");

		ObservableFeature f11 = mock(ObservableFeature.class);
		when(f11.getIdentifier()).thenReturn("11");
		when(f11.getName()).thenReturn("name11");
		when(f11.getDataType()).thenReturn("string");

		ObservableFeature f12 = mock(ObservableFeature.class);
		when(f12.getIdentifier()).thenReturn("12");
		when(f12.getName()).thenReturn("name12");
		when(f12.getDataType()).thenReturn("string");

		Protocol p1 = mock(Protocol.class);
		when(p1.getFeatures()).thenReturn(Arrays.asList(f11));

		Protocol p2 = mock(Protocol.class);
		when(p2.getFeatures()).thenReturn(Arrays.asList(f12));

		Protocol p0 = mock(Protocol.class);
		when(p0.getFeatures()).thenReturn(Arrays.asList(f10));
		when(p0.getSubprotocols()).thenReturn(Arrays.asList(p1, p2));

		DataSet dataSet = when(mock(DataSet.class).getProtocolUsed()).thenReturn(p0).getMock();

		DataService dataService = mock(DataService.class);
		JDBCMetaDatabase jdbcMetaDatabase = mock(JDBCMetaDatabase.class);
		Entity entity = mock(Entity.class);

		when(jdbcMetaDatabase.getEntity(ObservableFeature.class.getSimpleName())).thenReturn(entity);

		List<Field> cols = new DataSetTable(dataSet, dataService, jdbcMetaDatabase).getAllColumns();
		assertEquals("10", cols.get(0).getName());
		assertEquals("name10", cols.get(0).getLabel());
		assertEquals("11", cols.get(1).getName());
		assertEquals("name11", cols.get(1).getLabel());
		assertEquals("12", cols.get(2).getName());
		assertEquals("name12", cols.get(2).getLabel());
		assertEquals("id", cols.get(3).getName());
		assertEquals("id", cols.get(3).getName());
		assertEquals(4, cols.size());
	}
}