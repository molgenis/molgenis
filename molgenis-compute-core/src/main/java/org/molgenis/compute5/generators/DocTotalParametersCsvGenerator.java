package org.molgenis.compute5.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.molgenis.compute5.model.Parameters;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.util.tuple.WritableTuple;

/** Generates graphvis diagram */
public class DocTotalParametersCsvGenerator
{
	public void generate(File file, Parameters parameters) throws IOException
	{
		try
		{
			file.getParentFile().mkdirs();
			List<WritableTuple> values = parameters.getValues();

			CsvWriter w = new CsvWriter(new FileWriter(file));

			for (int i = 0; i < values.size(); i++)
			{
				if (i == 0)
				{
					w.writeColNames(values.get(0).getColNames());
				}
				w.write(values.get(i));
			}
			
			w.close();
			
			System.out.println("Generated "+file.getAbsolutePath());
		}
		catch (Exception e)
		{
			throw new IOException("Failed to write all parameters to "+file+": " + e.getMessage());
		}
	}
}
