package org.molgenis.compute5.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.compute5.generators.TupleUtils;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/** Parser for parameters csv file(s). Includes the solving of templated values. */
public class ParametersCsvParser
{
	public static Parameters parse(File... filesArray) throws IOException
	{
		// convert filesArray and
		// call the parse below
		List<File> fileLst = new ArrayList<File>();
		for (File f : filesArray)
			fileLst.add(f);

		return parse(fileLst);
	}

	public static Parameters parse(List<File> filesArray) throws IOException
	{
		Set<String> fileSet = new HashSet<String>();
		for (File f : filesArray)
		{
			fileSet.add(f.getAbsolutePath().toString());
		}

		Parameters targets = parseParamFiles(null, fileSet);

		// solve the templates
		TupleUtils.solve(targets.getValues());

		// mark all columns as 'user_*'
		int count = 0;
		List<WritableTuple> userTargets = new ArrayList<WritableTuple>();
		for (WritableTuple v : targets.getValues())
		{
			KeyValueTuple t = new KeyValueTuple();
			for (String col : v.getColNames())
			{
				t.set(Parameters.USER_PREFIX + col, v.get(col));
			}
			t.set(Parameters.ID_COLUMN, count++);
			userTargets.add(t);
		}

		targets = new Parameters();
		targets.setValues(userTargets);

		return targets;
	}

	/**
	 * Parse paramFileSet into Parameters targets.
	 * 
	 * @param targets
	 *            contains Parameters after parsing paramFileSet
	 * @param paramFileSet
	 *            Set of parameter files to parse
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static Parameters parseParamFiles(Parameters targets, Set<String> paramFileSet) throws IOException
	{
		System.out.println(">> Start of parseParamFiles " + paramFileSet.toString());
		// Pre-process input in (1) and (2):
		// (1) ensure targets initialized
		if (targets == null) targets = new Parameters();

		// if no files to parse, then we're done
		if (paramFileSet.isEmpty()) return targets;

		// get a file to parse
		String fString = paramFileSet.iterator().next();
		File f = new File(fString);

		// remove file from the set we have to parse
		paramFileSet.remove(fString);

		// initialize set of files we have parsed
		Set<String> paramFileSetDone = new HashSet<String>();

		// if targets exist then get parsed file set
		if (0 < targets.getValues().size()) paramFileSetDone = (Set<String>) targets.getValues().get(0)
				.get(Parameters.PARAMETER_COLUMN);

		// if we have already parsed this file then skip file f
		if (paramFileSetDone.contains(fString))
		{
			return parseParamFiles(targets, paramFileSet);
		}
		else
		{
			// parse file f

			// add parsed file to the list of parsed files and ensure we'll not
			// do this file again
			paramFileSetDone.add(fString);

			// get file f as list of tuples
			List<Tuple> tupleLst = asTuples(f);

			// If path to workflow is relative then prepend its parent's path
			// (f).
			tupleLst = updateWorkflowPath(tupleLst, f);

			// get other param files we have to parse, and validate that all
			// values in 'parameters' column equal. If file path is relative
			// then prepend its parent's path (f)
			HashSet<String> newParamFileSet = getParamFiles(tupleLst, f);

			// Remove all files that are already done
			newParamFileSet.removeAll(paramFileSetDone);

			// merge new paramFileSet with current one
			paramFileSet.addAll(newParamFileSet);

			// expand tupleLst on col's with lists/iterators (except
			// 'parameters')
			tupleLst = expand(tupleLst);

			// join on overlapping col's (except 'parameters')
			targets = join(targets, tupleLst);

			// update targets with 'parsed file'
			targets = addParsedFile(targets, paramFileSetDone);

			// parse rest of param files
			return parseParamFiles(targets, paramFileSet);
		}
	}

	/**
	 * Expand tupleLst
	 * 
	 * @param tupleLst
	 */
	private static List<Tuple> expand(List<Tuple> tupleLst)
	{
		// all expanded tuples
		List<Tuple> resultLst = new ArrayList<Tuple>();

		for (Tuple t : tupleLst)
		{
			// expanded tuples for this tuple
			List<WritableTuple> expandedTupleLst = new ArrayList<WritableTuple>();
			expandedTupleLst.add(new KeyValueTuple(t));

			for (String col : t.getColNames())
			{
				if (col.equals(Parameters.PARAMETER_COLUMN)) continue;

				List<String> values = asList(t, col);

				// expand each of the tuples in expandedTupleLst with values in
				// this column
				List<WritableTuple> expandedTupleLstTmp = new ArrayList<WritableTuple>();
				for (WritableTuple wt : expandedTupleLst)
				{
					for (String v : values)
					{
						// expanded wt
						WritableTuple ewt = new KeyValueTuple(wt);
						ewt.set(col, v);
						expandedTupleLstTmp.add(ewt);
					}
				}

				expandedTupleLst.clear();
				expandedTupleLst.addAll(expandedTupleLstTmp);
			}

			resultLst.addAll(expandedTupleLst);
		}

		return resultLst;
	}

	private static List<String> asList(Tuple t, String col)
	{
		String s = t.getString(col);

		Pattern pattern = Pattern.compile("([+-]?[0-9]+)\\.\\.([+-]?[0-9]+)");
		Matcher matcher = pattern.matcher(s);

		// first try as sequence, eg 3..5 (meaning 3, 4, 5)
		if (matcher.find())
		{
			List<String> seq = new ArrayList<String>();
			int first = Integer.parseInt(matcher.group(1));
			int second = Integer.parseInt(matcher.group(2));
			int from = Math.min(first, second);
			int to = Math.max(first, second);

			for (Integer i = from; i <= to; i++)
				seq.add(i.toString());

			return seq;
		}
		else
		{
			// no sequence, then return as list (values will be converted to
			// list with only that value)
			return t.getList(col);
		}
	}

	/**
	 * Update targets with actual parsed files
	 * 
	 * @param targets
	 * @param paramFileSetDone
	 */
	private static Parameters addParsedFile(Parameters targets, Set<String> paramFileSetDone)
	{
		for (WritableTuple t : targets.getValues())
		{
			t.set(Parameters.PARAMETER_COLUMN, paramFileSetDone);
		}

		return targets;
	}

	/**
	 * Merge tupleLst with targets based on overlapping columns (except
	 * 'parameters')
	 * 
	 * @param targets
	 * @param right
	 */
	private static Parameters join(Parameters targets, List<Tuple> right)
	{
		// joined tuples that we want to return
		List<WritableTuple> joined = new ArrayList<WritableTuple>();

		// current tuples
		List<WritableTuple> left = targets.getValues();

		if (0 == right.size())
		{
			// nothing to join
			return targets;
		}
		else if (0 == left.size())
		{
			// nothing to join, convert 'right' into targets
			for (Tuple t : right)
			{
				KeyValueTuple newValue = new KeyValueTuple(t);
				joined.add((WritableTuple) newValue);
			}
		}
		else
		{
			// determine intersection of col names (except param column):
			// joinFields
			Set<String> joinFields = new HashSet<String>();
			for (String s : left.get(0).getColNames())
				joinFields.add(s);

			Set<String> rightFields = new HashSet<String>();
			for (String s : right.get(0).getColNames())
				rightFields.add(s);

			joinFields.remove(Parameters.PARAMETER_COLUMN);
			joinFields.retainAll(rightFields);

			for (Tuple l : left)
			{
				for (Tuple r : right)
				{
					// determine whether tuples match and thus should be joinded
					boolean match = true;
					Iterator<String> it = joinFields.iterator();
					while (it.hasNext())
					{
						String field = it.next();
						if (!l.getString(field).equals(r.getString(field))) match = false;
					}

					// if joinFields match, then join into new tuple and add
					// that to 'joined'
					if (match)
					{
						WritableTuple t = new KeyValueTuple();
						t.set(r);
						t.set(l);
						joined.add(t);
					}
				}
			}

		}

		targets = new Parameters();
		targets.setValues(joined);

		return targets;
	}

	/**
	 * (1) Parse file f as list of Tuples and (2) validate that no parameters
	 * contain the 'step_param' separator
	 * 
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	private static List<Tuple> asTuples(File f) throws IOException
	{
		List<Tuple> tLst = new ArrayList<Tuple>();
		for (Tuple t : new CsvReader(f))
		{
			tLst.add(t);
			
			for (String p : t.getColNames())
				if (p.contains(Parameters.STEP_PARAM_SEP)) throw new IOException("Parsing " + f.getName()
						+ " failed: column names may not contain '" + Parameters.STEP_PARAM_SEP + "'");
		}
		return tLst;
	}

	/**
	 * (1) Validate that all values (set of files) in 'parameters' column are
	 * equal and (2) return them as a set. (3) If a file does not have an
	 * absolute path, then use the path of its parent as a starting point.
	 * 
	 * @param tupleLst
	 * @return set of files (in AbsoluteFile notation) to be included
	 * @throws IOException
	 */
	private static HashSet<String> getParamFiles(List<Tuple> tupleLst, File f) throws IOException
	{
		boolean noParamColumnFoundYet = true;

		// use this string to validate that all values in parameter column are
		// equal
		String paramFilesString = null;

		// transform list into file set
		HashSet<String> fileSet = new HashSet<String>();

		for (Tuple t : tupleLst)
		{
			for (String colName : t.getColNames())
			{
				if (colName.equals(Parameters.PARAMETER_COLUMN))
				{
					if (noParamColumnFoundYet)
					{
						// first row, param column found
						noParamColumnFoundYet = false;

						// should be equal for all following tuples:
						paramFilesString = t.getString(colName);

						// iterate through list and add absolute paths to
						// return-set
						for (String fString : t.getList(colName))
						{
							// if file has no absolute path, then use the path
							// of its parent (file f) as path
							if (fString.charAt(0) == '/')
							{
								fileSet.add(fString);
							}
							else
							{
								fileSet.add(f.getParent() + File.separator + fString);
							}
						}
					}
					else
					{
						if (!t.getString(colName).equals(paramFilesString)) throw new IOException("Values in '"
								+ Parameters.PARAMETER_COLUMN + "' column are not equal in file '" + f.toString()
								+ "', please fix:\n'" + t.getString(colName) + "' is different from '" + paramFilesString + "'.\n"
								+ "You could put all values 'comma-separated' in each cell and repeat that on each line in your file, e.g.:\n" +
								"\"" + t.getString(colName) + "," + paramFilesString + "\"");
					}
				}
			}
			// if first row did not contain parameter column, then next rows
			// won't either, so return empty set
			if (noParamColumnFoundYet) return fileSet;

		}

		return fileSet;
	}

	/**
	 * If path to workflow file relative, then prepend parent's path (f)
	 * 
	 * @param tupleLst
	 * @return
	 */
	private static List<Tuple> updateWorkflowPath(List<Tuple> tupleLst, File f)
	{
		List<Tuple> tupleLstUpdated = new ArrayList<Tuple>();

		for (Tuple t : tupleLst)
		{
			WritableTuple wt = new KeyValueTuple(t);

			for (String colName : t.getColNames())
			{
				if (colName.equals(Parameters.WORKFLOW_COLUMN_INITIAL))
				{
					List<String> wfLst = new ArrayList<String>();
					// iterate through list and add absolute paths to
					// return-set
					for (String fString : t.getList(colName))
					{
						// if file has no absolute path, then use the path
						// of its parent (file f) as path
						if (fString.charAt(0) == '/')
						{
							wfLst.add(fString);
						}
						else
						{
							wfLst.add(f.getParent() + File.separator + fString);
						}
					}

					// put updated paths back in tuple
					if (wfLst.size() == 1)
					{
						wt.set(colName, wfLst.get(0));
					}
					else
					{
						wt.set(colName, wfLst);
					}
				}
			}

			tupleLstUpdated.add(wt);
		}

		return tupleLstUpdated;
	}

	public static Parameters parseMorris(List<File> filesArray) throws IOException
	{
		try
		{
			final List<String> parameters = new ArrayList<String>();
			final List<WritableTuple> values = new ArrayList<WritableTuple>();

			if (filesArray.size() == 0) throw new IOException("Parameters.parse expects at least one file");

			// todo
			NavigableSet<String> todo = new TreeSet<String>();
			for (File f : filesArray)
			{
				todo.add(f.getAbsoluteFile().toString());
			}

			// done
			Set<String> done = new TreeSet<String>();

			// first file, just read
			boolean firstRow = true;
			File currentFile = new File(todo.first());
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
							// MD: Why we don't update 'value' with the expanded
							// paths?
							todo.add(parameterFile.toString());
						}
					}

					firstRow = false;
				}
			}
			done.add(currentFile.toString());
			todo.remove(currentFile.toString());

			// while not all files parsed, parse some more...
			while (todo.size() > 0)
			{
				currentFile = new File(todo.first()).getAbsoluteFile();
				firstRow = true;

				// remember fields for natural join, if any
				final List<String> joinFields = new ArrayList<String>();

				// if join, we create combinations in this new list
				final List<WritableTuple> newValues = new ArrayList<WritableTuple>();

				for (Tuple t : new CsvReader(currentFile)) // Tuple t is a row
															// in currentFile
				{
					if (firstRow)
					{
						// check if this file includes reference to other files
						if (firstRow) // ????????????????????? MD
										// ?????????????????????: firstRow ==
										// true, we already knew that :-s
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
									todo.add(parameterFile.toString());

									boolean isDone = false;

									// check if we already parsed this file
									for (String test : done)
									{
										File testFile = new File(test);
										if (testFile.getCanonicalPath().equals(parameterFile.getCanonicalPath())) isDone = true;
									}
									if (!isDone) todo.add(parameterFile.toString());
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
					// no joinFields
					{
						for (Tuple original : values)
						{
							WritableTuple newValue = new KeyValueTuple(original);
							newValue.set(t); // overwrite original with values
												// of this tuple
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
							value.set("workflow", workflowFile.getAbsolutePath()); // MD:
																					// move
																					// this
																					// line
																					// within
																					// if-clause?
						}
					}
				}

				done.add(currentFile.toString());
				todo.remove(currentFile.toString());
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

			// finally, add row ids
			int count = 0;
			for (WritableTuple value : result)
			{
				value.set(Parameters.ID_COLUMN, count++);
			}

			// clean files array
			filesArray.clear();
			for (String f : done)
			{
				filesArray.add(new File(f));
			}

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