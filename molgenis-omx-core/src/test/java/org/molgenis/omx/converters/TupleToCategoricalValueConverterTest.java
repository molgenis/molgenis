package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToCategoricalValueConverterTest
{

	@Test
	public void toCell()
	{
		Database database = mock(Database.class);
		String catIdentifier = "category1";
		String catName = "category #1";
		Category category = new Category();
		category.setIdentifier(catIdentifier);
		category.setName(catName);
		CategoricalValue value = new CategoricalValue();
		value.setValue(category);
		Cell<String> cell = new TupleToCategoricalValueConverter(database).toCell(value);
		assertEquals(cell.getKey(), catIdentifier);
		assertEquals(cell.getValue(), catName);
	}

	@Test
	public void fromTuple() throws ValueConverterException, DatabaseException
	{
		String catName = "category #1";
		Category category = new Category();
		category.setName(catName);

		String valueCode = "code1";
		ObservableFeature feature = mock(ObservableFeature.class);
		Database database = mock(Database.class);
		when(
				database.find(Category.class, new QueryRule(Category.OBSERVABLEFEATURE, Operator.EQUALS, feature),
						new QueryRule(Category.VALUECODE, Operator.EQUALS, valueCode))).thenReturn(
				Arrays.asList(category));
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, valueCode);
		CategoricalValue value = new TupleToCategoricalValueConverter(database).fromTuple(tuple, colName, feature);
		assertEquals(value.getValue(), category);
	}
}
