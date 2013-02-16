package org.molgenis.compute5.parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.molgenis.compute5.generators.TupleUtils;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/** Parser for parameters csv file(s). Includes the solving of templated values. */
public class ParametersCsvParser
{
	public static Parameters parse(File ... filesArray) throws IOException
	{
		List<File> filesList = new ArrayList<File>();
		filesList.addAll(Arrays.asList(filesArray));
		return parse(filesList);
	}

	public static Parameters parse(List<File> filesArray) throws IOException
	{
		try
		{
			final List<String> parameters = new ArrayList<String>();
			final List<WritableTuple> values = new ArrayList<WritableTuple>();

			if (filesArray.size() == 0) throw new IOException("Parameters.parse expects at least one file");

			// todo
			NavigableSet<File> todo = new TreeSet<File>();
			for (File f : filesArray)
			{
				todo.add(f.getAbsoluteFile());
			}

			// done
			Set<File> done = new TreeSet<File>();

			// first file, just read
			boolean firstRow = true;
			File currentFile = todo.first().getAbsoluteFile();
			for (Tuple t : new CsvReader(currentFile))
			{
				KeyValueTuple value = new KeyValueTuple();
				value.set(t);
				values.add(value);

				// check if this contains reference to workflow; need to
				// translate relative into absolute path
				if (!value.isNull("workflow"))
				{
					// in case of relative path, it is relative to current file
					File workflowFile = new File(value.getString("workflow"));
					if (!workflowFile.isAbsolute())
					{
						workflowFile = new File(currentFile.getCanonicalFile().getParentFile().getAbsolutePath() + "/"
								+ value.getString("workflow"));
					}
					value.set("workflow", workflowFile.getAbsolutePath());
				}

				// check if this file includes reference to other files
				if (firstRow)
				{
					// parameters.csv
					if (!value.isNull(Parameters.PARAMETER_COLUMN))
					{
						for (String parameterFileName : value.getList(Parameters.PARAMETER_COLUMN))
						{
							// in case of relative path, it is relative to
							// current file
							File parameterFile = new File(parameterFileName);
							if (!parameterFile.isAbsolute())
							{
								parameterFile = new File(currentFile.getCanonicalFile().getParentFile()
										.getAbsolutePath()
										+ "/" + parameterFileName);
							}
							todo.add(parameterFile);
						}
					}

					firstRow = false;
				}
			}
			done.add(currentFile);
			todo.remove(currentFile);

			// while not all files parsed, parse some more...
			while (todo.size() > 0)
			{
				currentFile = todo.first().getAbsoluteFile();
				firstRow = true;

				// remember fields for natural join, if any
				final List<String> joinFields = new ArrayList<String>();

				// if join, we create combinations in this new list
				final List<WritableTuple> newValues = new ArrayList<WritableTuple>();

				for (Tuple t : new CsvReader(currentFile))
				{
					if (firstRow)
					{
						// check if this file includes reference to other files
						if (firstRow)
						{
							if (!t.isNull(Parameters.PARAMETER_COLUMN))
							{
								for (String parameterFileName : t.getList(Parameters.PARAMETER_COLUMN))
								{
									// in case of relative path, it is relative
									// to current file
									File parameterFile = new File(parameterFileName);
									if (!parameterFile.isAbsolute())
									{
										parameterFile = new File(currentFile.getCanonicalFile().getParentFile()
												.getAbsolutePath()
												+ "/" + parameterFileName);
									}
									todo.add(parameterFile);

									boolean isDone = false;

									// check if we already parsed this file
									for (File test : done)
									{
										if (test.getCanonicalPath().equals(parameterFile.getCanonicalPath())) isDone = true;
									}
									if (!isDone) todo.add(parameterFile);
								}
							}
							firstRow = false;
						}

						for (String col : t.getColNames())
						{
							// check for natural join
							if (parameters.contains(col))
							{
								joinFields.add(col);
							}
							// verify no '.' in name
							if (col.contains("."))
							{
								throw new IOException("parsing " + currentFile.getName()
										+ " failed: column names cannot contain '.'");
							}

							parameters.add(col);
						}

						firstRow = false;
					}

					if (joinFields.size() > 0)
					{
						// remember joined, if fails union
						boolean joined = false;
						for (WritableTuple original : values)
						{

							// check for join
							boolean join = true;

							for (String col : joinFields)
							{
								if (original.isNull(col) || !original.get(col).equals(t.get(col)))
								{
									join = false;
								}
							}

							// join
							if (join)
							{
								KeyValueTuple newValue = new KeyValueTuple(original);
								newValue.set(t);
								newValues.add(newValue);
								joined = true;
							}
						}
						if (!joined)
						{
							// join failed, added at the end
							// TODO should we warn?
							values.add(new KeyValueTuple(t));
						}
					}
					else
					{
						for (Tuple original : values)
						{
							WritableTuple newValue = new KeyValueTuple(original);
							newValue.set(t);
							newValues.add(newValue);
						}
					}

					if (newValues.size() > 0)
					{
						values.clear();
						values.addAll(newValues);
					}

					// check if this contains reference to workflow; need to
					// translate relative into absolute path
					for (WritableTuple value : values)
					{
						if (!value.isNull("workflow"))
						{
							// in case of relative path, it is relative to
							// current file
							File workflowFile = new File(value.getString("workflow"));
							if (!workflowFile.isAbsolute())
							{
								workflowFile = new File(currentFile.getCanonicalFile().getParentFile()
										.getAbsolutePath()
										+ "/" + value.getString("workflow"));
							}
							value.set("workflow", workflowFile.getAbsolutePath());
						}
					}
				}

				done.add(currentFile);
				todo.remove(currentFile);
			}

			// solve the templates
			TupleUtils.solve(values);

			// mark all columns as 'user.*'
			List<WritableTuple> result = new ArrayList<WritableTuple>();
			for (Tuple v : values)
			{
				KeyValueTuple t = new KeyValueTuple();
				for (String col : v.getColNames())
				{
					t.set("user." + col, v.get(col));
				}
				result.add(t);
			}

			// finaly, add row ids
			int count = 0;
			for (WritableTuple value : result)
			{
				value.set(Parameters.ID_COLUMN, count++);
			}

			// clean files array
			filesArray.clear();
			filesArray.addAll(done);

			Parameters p = new Parameters();
			p.setValues(result);
			return p;
		}
		catch (IOException e)
		{
			throw new IOException("Parsing of parameters csv failed: " + e.getMessage());
		}
	}
}