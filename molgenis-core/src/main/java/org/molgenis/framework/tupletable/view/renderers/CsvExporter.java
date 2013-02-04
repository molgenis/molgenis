package org.molgenis.framework.tupletable.view.renderers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.AbstractTuple;
import org.molgenis.util.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Export TupleTable to CSV file
 */
public class CsvExporter extends AbstractExporter
{
	public CsvExporter(TupleTable table)
	{
		super(table);
	}

	@Override
	public void export(OutputStream os) throws IOException, TableException
	{
		CsvWriter csvWriter = new CsvWriter(os);

		// save table state
		int colOffset = tupleTable.getColOffset();
		int colLimit = tupleTable.getColLimit();
		int rowOffset = tupleTable.getOffset();
		int rowLimit = tupleTable.getLimit();

		// update table state
		tupleTable.setColOffset(0);
		tupleTable.setColLimit(0);
		tupleTable.setOffset(0);
		tupleTable.setLimit(0);

		try
		{
			csvWriter.writeColNames(new FieldHeaderTuple(tupleTable.getColumns()).getColNames());
			for (Tuple row : tupleTable)
				csvWriter.write(row);
		}
		finally
		{
			IOUtils.closeQuietly(csvWriter);

			// restore table state
			tupleTable.setColOffset(colOffset);
			tupleTable.setColLimit(colLimit);
			tupleTable.setOffset(rowOffset);
			tupleTable.setLimit(rowLimit);
		}
	}

	private static class FieldHeaderTuple extends AbstractTuple
	{
		private static final long serialVersionUID = 1L;

		private final List<Field> fields;

		public FieldHeaderTuple(List<Field> fields)
		{
			if (fields == null) throw new IllegalArgumentException("fields is null");
			this.fields = fields;
		}

		@Override
		public int getNrCols()
		{
			return fields.size();
		}

		@Override
		public Iterable<String> getColNames()
		{
			return Iterables.transform(fields, new Function<Field, String>()
			{
				@Override
				@Nullable
				public String apply(@Nullable
				Field arg0)
				{
					return arg0 != null ? arg0.getSqlName() : null;
				}
			});
		}

		@Override
		public Object get(String colName)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object get(int col)
		{
			throw new UnsupportedOperationException();
		}
	}
}
