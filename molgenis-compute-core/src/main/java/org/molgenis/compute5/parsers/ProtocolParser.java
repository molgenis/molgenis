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
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Protocol;

/**
 * Parser for protocol ftl file
 * 
 * parameters are defined as follows:
 * 
 * #string NAME DESCRIPTION #list NAME DESCRIPTION #output NAME VALUE DESCRIPTION
 */
// FIXME: add parsing for cores, mem, etc
public class ProtocolParser
{

	/**
	 * 
	 * @param workflowDir
	 *            , used as primary search path. If missing it uses runtime path/absolute path
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
			Protocol p = new Protocol(protocolFile);
			String description = "";
			String template = "";

			BufferedReader reader = new BufferedReader(new FileReader(templateFile));
			try
			{

				// Then read the non-# as template

				// need to harvest all lines that start with #
				// need to harvest all other lines
				String line;
				while ((line = reader.readLine()) != null)
				{
					// Always add line to protocol
					template += line + "\n";

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
								for (int i = 1; i < els.size(); i++)
								{
									if (els.get(i).startsWith(Parameters.QUEUE)) p.setQueue(els.get(i).substring(
											Parameters.QUEUE.length() + 1));

									if (els.get(i).startsWith(Parameters.WALLTIME)) p.setWalltime(els.get(i).substring(
											Parameters.WALLTIME.length() + 1));

									if (els.get(i).startsWith(Parameters.NODES)) p.setNodes(els.get(i).substring(
											Parameters.NODES.length() + 1));

									if (els.get(i).startsWith(Parameters.PPN)) p.setPpn(els.get(i).substring(
											Parameters.PPN.length() + 1));

									if (els.get(i).startsWith(Parameters.MEMORY)) p.setMemory(els.get(i).substring(
											Parameters.MEMORY.length() + 1));

								}
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
							else if (els.get(0).equals(Parameters.INPUT) || els.get(0).equals(Parameters.STRING)
									|| els.get(0).equals(Parameters.LIST_INPUT))
							{
								// assume name column
								if (els.size() < 2) throw new IOException(
										"param requires 'name', e.g. '#string input1'");

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

							// output, syntax = "#output outputVarName description"
							else if (els.get(0).equals("output"))
							{
								if (els.size() < 2) throw new IOException(
										"output requires 'name', e.g. '#output myOutputVariable'");
								if (3 < els.size()) throw new IOException(
										"Output cannot have more than 3 arguments.\nSyntax is: #output outputVarName \"description\", where description is optional.");

								Output o = new Output(els.get(1));
								// o.setValue(els.get(2));
								// The value of the output parameter may not be set
								// in the header
								// This must be done in the template, instead!
								o.setValue(Parameters.NOTAVAILABLE);

								// description is everything else
								String inputDescription = "";
								for (int i = 2; i < els.size(); i++)
								{
									inputDescription += " " + els.get(i);
								}
								if (inputDescription.length() > 0) o.setDescription(inputDescription.trim());

								p.getOutputs().add(o);
							}
						}
					}
				}
			}
			finally
			{
				reader.close();
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