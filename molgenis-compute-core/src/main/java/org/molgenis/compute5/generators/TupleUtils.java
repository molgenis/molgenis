package org.molgenis.compute5.generators;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TupleUtils
{
	/**
	 * Collapse tuples on targets
	 * 
	 * @param parameters
	 * @param targets
	 * @return
	 */
	public static List<WritableTuple> collapse(List<? extends Tuple> parameters, List<String> targets)
	{
		Map<String, WritableTuple> result = new LinkedHashMap<String, WritableTuple>();

		for (Tuple row : parameters)
		{
			// generate key
			String key = "";
			for (String target : targets)
				key += row.getString(target) + "_";

			// if first, create tuple, create lists for non-targets
			if (result.get(key) == null)
			{
				KeyValueTuple collapsedRow = new KeyValueTuple();
				for (String col : row.getColNames())
				{
					if (targets.contains(col))
					{
						collapsedRow.set(col, row.get(col));
					}
					else
					{
						List<Object> list = new ArrayList<Object>();
						list.add(row.get(col));
						collapsedRow.set(col, list);
					}
				}
				result.put(key, collapsedRow);
			}
			else
			{
				for (String col : row.getColNames())
				{
					if (!targets.contains(col))
					{
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) result.get(key).get(col);
						list.add(row.getString(col));
						result.get(key).set(col, list);
					}
				}
			}
		}

		return new ArrayList<WritableTuple>(result.values());
	}

	/**
	 * Tuples can have values that are freemarker templates, e.g. ${other
	 * column}. This method will solve that
	 * 
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static void solve(List<WritableTuple> values) throws IOException
	{
		// Freemarker configuration
		Configuration conf = new Configuration();

		boolean done = false;
		while (!done)
		{
			boolean updated = false;

			String original, value;
			Template template;
			StringWriter out;
			String unsolved = "";

			for (WritableTuple t : values)
			{
				for (String col : t.getColNames())
				{
					original = t.getString(col);
					if (original.contains("${"))
					{
						// check for self reference (??)
						if (original.contains("${" + col + "}")) throw new IOException("could not solve " + col + "='"
								+ original + "' because template references to self");

						template = new Template(col, new StringReader(original), conf);
						out = new StringWriter();
						try
						{
							Map<String, Object> map = toMap(t);
							template.process(map, out);
							value = out.toString();
							if (!value.equals(original))
							{
								updated = true;
								t.set(col, value);
							}
						}
						catch (Exception e)
						{
							unsolved += "could not solve " + col + "='" + original + "': " + e.getMessage() + "\n";
						}
					}
				}
			}

			if (!updated)
			{
				if (unsolved.length() > 0)
				{
					throw new IOException(unsolved);
				}
				done = true;
			}
		}
	}

	@SuppressWarnings("unchecked")
	/** Convert a tuple into a map. Columns with a '.' in them will be nested submaps. */
	public static Map<String, Object> toMap(Tuple t)
	{
		// TODO: can we not make Tuple extend Map???
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (String key : t.getColNames())
		{
			if (key.contains("."))
			{
				// nested maps using '.'!
				String[] els = key.split("\\.");

				Map<String, Object> map = result;
				for (String el : els)
				{
					// if last, simply put value
					if (el.equals(els[els.length - 1]))
					{
						map.put(el, t.get(key));
					}
					// nest map
					else
					{
						// create map if needed
						if (!map.containsKey(el))
						{
							map.put(el, new LinkedHashMap<String, Object>());
						}
						map = (Map<String, Object>) map.get(el);
					}
				}
			}
			else
			{
				result.put(key, t.get(key));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	/** 
	 * Uncollapse a tuple using an idColumn
	 *  
	 * @param values
	 * @param idColumn
	 * @return
	 */
	public static <E extends Tuple> List<E> uncollapse(List<E> values, String idColumn)
	{
		List<E> result = new ArrayList<E>();

		for (E original : values)
		{
			if (!(original.get(idColumn) instanceof List))
			{
				return values;
			}
			else
			{
				for (int i = 0; i < original.getList(idColumn).size(); i++)
				{
					KeyValueTuple copy = new KeyValueTuple();
					for (String col : original.getColNames())
					{
						if (original.get(col) instanceof List)
						{
							copy.set(col, original.getList(col).get(i));
						}
						else
						{
							copy.set(col, original.get(col));
						}
					}
					result.add((E) copy);
				}
			}
		}

		return result;
	}
}
