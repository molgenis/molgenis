package org.molgenis.compute5.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Output;
import org.molgenis.compute5.model.Protocol;

/**
 * Parser for protocol ftl file
 * 
 * parameters are defined as follows:
 * 
 * #string NAME DESCRIPTION #list NAME DESCRIPTION #output NAME VALUE
 * DESCRIPTION
 */
// FIXME: add parsing for cores, mem, etc
public class ProtocolFtlParser
{

	/**
	 * 
	 * @param workflowDir
	 *            , used as primary search path. If missing it uses runtime
	 *            path/absolute path
	 * @param protocolFile
	 * @return
	 * @throws IOException
	 */
	public static Protocol parse(File workflowDir, String protocolFile) throws IOException
	{
		try
		{
			// first test path within workflowDir
			File templateFile = new File(workflowDir.getAbsolutePath() + "/" + protocolFile);
			if (!templateFile.exists())
			{
				templateFile = new File(protocolFile);
				if (!templateFile.exists()) throw new IOException("protocol '" + protocolFile + "' cannot be found");
			}

			// start reading
			 BufferedReader reader = new BufferedReader(new
			 FileReader(templateFile));
			Protocol p = new Protocol(protocolFile);

			// Then read the non-# as template

			// need to harvest all lines that start with #
			// need to harvest all other lines
			String line;
			String description = "";
			String template = "";
			while ((line = reader.readLine()) != null)
			{

				if (line.startsWith("#"))
				{
					// remove #, trim spaces, then split on " "
					line = line.substring(1).trim();
					List<String> els = new ArrayList<String>();
					Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
					while (m.find())
					    els.add(m.group(1));

					if (els.size() > 0)
					{
						if (els.get(0).equals("MOLGENIS"))
						{
							// todo
						}
						// description?
						else if (els.get(0).equals("description") && els.size() > 1)
						{
							// add all elements
							for (int i = 1; i < els.size(); i++)
							{
								description += els.get(i) + " ";
							}
							description += "\n";
						}

						// input
						else if (els.get(0).equals("input") || els.get(0).equals("string") || els.get(0).equals("list"))
						{
							// assume name column
							if (els.size() < 2) throw new IOException("param requires 'name', e.g. '#string input1'");

							Input input = new Input(els.get(1));

							input.setType(els.get(0));

							// description is everything else
							String inputDescription = "";
							for (int i = 2; i < els.size(); i++)
							{
								inputDescription += " " + els.get(i);
							}
							if (inputDescription.length() > 0) input.setDescription(inputDescription.trim());

							p.getInputs().add(input);
						}

						// MOLGENIS
						else if (els.get(0).equals("MOLGENIS"))
						{
							// TODO
						}

						// output
						else if (els.get(0).equals("output"))
						{
							if (els.size() < 2) throw new IOException("output requires 'name', e.g. '#output output1'");
							if (els.size() < 3) throw new IOException(
									"output requires 'output', e.g. '#output output1 ${input1}'");

							Output o = new Output(els.get(1));
							o.setValue(els.get(2));
							
							// description is everything else
							String inputDescription = "";
							for (int i = 2; i < els.size(); i++)
							{
								inputDescription += " " + els.get(i);
							}
							if (inputDescription.length() > 0) o.setDescription(inputDescription.trim());

							p.getOutputs().add(o);
						}

						// otherwise we don't understand
						else
						{
							template += line + "\n";
						}

					}
				}

				// otherwise just add to template
				else
				{
					template += line + "\n";
				}

			}
			p.setDescription(description);
			p.setTemplate(template);
			return p;
		}
		catch (Exception e)
		{
			throw new IOException("Parsing of protocol " + protocolFile + " failed: " + e.getMessage());
		}

	}
}