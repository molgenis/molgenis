package org.molgenis.omx.dataset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.framework.tupletable.impl.CsvTable;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.Individual;
import org.molgenis.util.tuple.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import app.DatabaseFactory;

public class DataSetTableTest
{
	@Test
	public void getAllColumns() throws TableException, DatabaseException
	{
		DataSet dataSet = when(mock(DataSet.class).getProtocolUsed_Id()).thenReturn(1).getMock();

		Protocol p0 = mock(Protocol.class);
		when(p0.getFeatures_Id()).thenReturn(Arrays.asList(10));
		when(p0.getSubprotocols_Id()).thenReturn(Arrays.asList(1, 2));
		Protocol p1 = mock(Protocol.class);
		when(p1.getFeatures_Id()).thenReturn(Arrays.asList(11));
		Protocol p2 = mock(Protocol.class);
		when(p2.getFeatures_Id()).thenReturn(Arrays.asList(12));

		ObservableFeature f10 = mock(ObservableFeature.class);
		when(f10.getIdentifier()).thenReturn("10");
		when(f10.getName()).thenReturn("name10");
		ObservableFeature f11 = mock(ObservableFeature.class);
		when(f11.getIdentifier()).thenReturn("11");
		when(f11.getName()).thenReturn("name11");
		ObservableFeature f12 = mock(ObservableFeature.class);
		when(f12.getIdentifier()).thenReturn("12");
		when(f12.getName()).thenReturn("name12");

		Database db = mock(Database.class);
		when(db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.EQUALS, 1))).thenReturn(
				Collections.singletonList(p0));
		when(db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.IN, Arrays.asList(1, 2)))).thenReturn(
				Arrays.asList(p1, p2));
		when(
				db.find(ObservableFeature.class,
						new QueryRule(ObservableFeature.ID, Operator.IN, Arrays.asList(10, 11, 12)))).thenReturn(
				Arrays.asList(f10, f11, f12));

		List<Field> cols = new DataSetTable(dataSet, db).getAllColumns();
		assertEquals("10", cols.get(0).getName());
		assertEquals("name10", cols.get(0).getLabel());
		assertEquals("11", cols.get(1).getName());
		assertEquals("name11", cols.get(1).getLabel());
		assertEquals("12", cols.get(2).getName());
		assertEquals("name12", cols.get(2).getLabel());
		assertEquals(3, cols.size());
	}
}