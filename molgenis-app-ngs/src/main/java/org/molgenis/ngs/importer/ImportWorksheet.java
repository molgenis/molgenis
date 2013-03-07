package org.molgenis.ngs.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.io.TupleReader;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.omx.ngs.CapturingKit;
import org.molgenis.omx.ngs.Flowcell;
import org.molgenis.omx.ngs.Machine;
import org.molgenis.omx.ngs.NgsUser;
import org.molgenis.omx.ngs.PrepKit;
import org.molgenis.omx.ngs.Project;
import org.molgenis.omx.ngs.SampleBarcode;
import org.molgenis.util.tuple.Tuple;

public class ImportWorksheet
{
	public static void main(String[] args) throws Exception
	{
		// GAFsheet SequencingSampleDetails
		importSequencingSampleDetails(new File("D:/Downloads/MolgenisStuff/GAFSequencingSampleDetails.csv"), true);
		// GAFsheet SequencingProjects
		importSequencingProjects(new File("D:/Downloads/MolgenisStuff/GAFSequencingProjects.csv"), true);
	}

	private static void importSequencingSampleDetails(File csv, boolean debug) throws Exception
	{
		// Declare collections to hold collected variables
		Map<String, NgsUser> users = new LinkedHashMap<String, NgsUser>();
		Map<String, SampleBarcode> sampleBarcodes = new LinkedHashMap<String, SampleBarcode>();
		Map<String, CapturingKit> capturingKits = new LinkedHashMap<String, CapturingKit>();
		Map<String, PrepKit> prepKits = new LinkedHashMap<String, PrepKit>();
		Map<String, Machine> machines = new LinkedHashMap<String, Machine>();
		Map<String, Flowcell> flowcells = new LinkedHashMap<String, Flowcell>();
		Map<String, Project> projects = new LinkedHashMap<String, Project>();

		// Import sheet
		TupleReader reader = new CsvReader(csv);

		for (Tuple row : reader)
		{
			// NgsUser
			if (!row.isNull("contact"))
			{
				NgsUser u = new NgsUser();
				// customer

				// get the name and email from the contact value
				String[] nameEmailParts = row.getString("contact").split("<");
				u.setUserName(nameEmailParts[0].toLowerCase().trim());
				if (nameEmailParts[1] != null)
				{
					u.setUserEmail(nameEmailParts[1].toLowerCase().trim().replace(">", ""));
				}

				// u.setUserName(row.getString("contact"));
				u.setUserRole("Customer");
				u.setUserGroup("None");

				// check if exists, and same
				// NgsUser exist = users.get(u.getUserName().toLowerCase());
				// if (exist != null && !exist.equals(u))
				// {
				// throw new Exception("Inconsistent: " + u + "!=" + exist);
				// }
				// else
				// {
				// users.put(u.getUserName().toLowerCase(), u);
				// }

				users.put(u.getUserName().toLowerCase(), u);

				// GAF

				// GCC
			}

			// SampleBarcode
			if (!row.isNull("barcode"))
			{
				SampleBarcode sb = new SampleBarcode();

				// SampleBarcodeName
				if (row.getString("barcode") != null)
				{
					if (row.getString("barcode").toLowerCase().trim().contains("none")
							|| row.getString("barcode").toLowerCase().trim().contains("error"))
					{
						sb.setSampleBarcodeName("None");
					}
					else
					{
						sb.setSampleBarcodeName(row.getString("barcode").toUpperCase().trim());
					}
				}
				else
				{
					sb.setSampleBarcodeName("None");
				}

				// BarcodeType
				if (row.getString("barcodeType") != null)
				{
					if (row.getString("barcodeType").toLowerCase().trim().contains("none")
							|| row.getString("barcodeType").toLowerCase().trim().contains("error"))
					{
						sb.setBarcodeType("None");
					}
					else
					{
						sb.setBarcodeType(row.getString("barcodeType").toUpperCase().trim());
					}
				}
				else
				{
					sb.setBarcodeType("None");
				}

				sampleBarcodes.put(sb.getSampleBarcodeName(), sb);
			}

			// CapturingKit
			if (!row.isNull("capturingKit"))
			{
				CapturingKit ck = new CapturingKit();

				if (row.getString("capturingKit") != null)
				{
					if (row.getString("capturingKit").toLowerCase().trim().contains("none"))
					{
						ck.setCapturingKitName("None");
					}
					else
					{
						ck.setCapturingKitName(row.getString("capturingKit").trim());
					}
				}
				else
				{
					ck.setCapturingKitName("None");
				}

				// Debug
				if (ck.getCapturingKitName() == "none")
				{
					System.out.println(ck.getCapturingKitName());
				}

				capturingKits.put(ck.getCapturingKitName(), ck);
			}

			// PrepKit
			if (!row.isNull("prepKit"))
			{
				PrepKit pk = new PrepKit();

				if (row.getString("prepKit") != null)
				{
					if (row.getString("prepKit").toLowerCase().trim().contains("none")
							|| row.getString("prepKit").toLowerCase().trim().contains("_"))
					{
						pk.setPrepKitName("None");
					}
					else
					{
						pk.setPrepKitName(row.getString("prepKit").toUpperCase());
					}
				}
				else
				{
					pk.setPrepKitName("None");
				}

				prepKits.put(pk.getPrepKitName(), pk);
			}

			// Machine
			if (!row.isNull("sequencer"))
			{
				Machine m = new Machine();

				if (row.getString("sequencer") != null)
				{
					if (row.getString("sequencer").toLowerCase().trim().contains("none"))
					{
						m.setMachineName("None");
					}
					else
					{
						m.setMachineName(row.getString("sequencer"));
					}
				}
				else
				{
					m.setMachineName("None");
				}

				machines.put(m.getMachineName(), m);
			}

			// Flowcell
			if (!row.isNull("flowcell"))
			{
				Flowcell f = new Flowcell();
				f.setFlowcellName(row.getString("flowcell"));
				f.setMachine_MachineName(row.getString("sequencer"));
				f.setRun(row.getString("run"));

				// sequencingStartDate notation should be as 110218 "yymmdd"
				// TODO: Some values exist like 10429 "yymdd" :/
				// Date runDate = null;
				// if (row.getString("sequencingStartDate") != null)
				// {
				// char[] d =
				// row.getString("sequencingStartDate").trim().toCharArray();
				// runDate = new SimpleDateFormat("MM/dd/yy",
				// Locale.ENGLISH).parse(d[2] + d[3] + "/" + d[4] + d[5]
				// + "/" + d[0] + d[1]);
				// }
				// f.setRunDate(runDate);

				flowcells.put(f.getFlowcellName(), f);
			}

			// Project
			if (!row.isNull("project"))
			{
				Project p = new Project();

				p.setProjectName(row.getString("project"));
				// get the name from the contact value
				String contactName = "";
				if (row.getString("contact") != null)
				{
					String[] nameEmailParts = row.getString("contact").split("<");
					contactName = nameEmailParts[0].toLowerCase().trim();
					// u.setUserName(nameEmailParts[0].toLowerCase().trim());
				}
				p.setProjectCustomer_UserName(contactName);

				if (row.getString("seqType") != null)
				{
					p.setSeqType(row.getString("seqType"));
				}
				else
				{
					p.setSeqType("Unknown");
				}

				// Cast existing values into the corresponding boolean values
				boolean gccAnalysis = false;
				if (row.getString("GCC_Analysis") != null)
				{
					String gccAnalysisValue = row.getString("GCC_Analysis");
					if (gccAnalysisValue.toLowerCase().trim().contains("yes"))
					{
						gccAnalysis = true;
					}
				}
				p.setGccAnalysis(gccAnalysis);

				projects.put(p.getProjectName().toLowerCase(), p);
			}

			// Sample

			// FlowcellLaneSampleBarcode

		}
		reader.close();

		// Show all collected values
		for (Project p : projects.values())
		{
			System.out.println(p);
		}
		for (NgsUser u : users.values())
		{
			System.out.println(u);
		}
		for (SampleBarcode sb : sampleBarcodes.values())
		{
			System.out.println(sb);
		}
		for (CapturingKit ck : capturingKits.values())
		{
			System.out.println(ck);
		}
		for (PrepKit pk : prepKits.values())
		{
			System.out.println(pk);
		}
		for (Machine m : machines.values())
		{
			System.out.println(m);
		}
		for (Flowcell f : flowcells.values())
		{
			System.out.println(f);
		}

		// put in database
		Database db = new app.JpaDatabase();
		try
		{
			db.beginTx();

			// clean old stuff (reverse order of foreign keys)
			if (debug)
			{
				db.remove(db.find(Project.class));
				db.remove(db.find(Flowcell.class));
				db.remove(db.find(Machine.class));
				db.remove(db.find(PrepKit.class));
				db.remove(db.find(CapturingKit.class));
				db.remove(db.find(SampleBarcode.class));
				db.remove(db.find(NgsUser.class));
			}

			System.out.println("Imported " + db.add(new ArrayList<NgsUser>(users.values())) + " NgsUser");
			System.out.println("Imported " + db.add(new ArrayList<SampleBarcode>(sampleBarcodes.values()))
					+ " SampleBarcodes");
			System.out.println("Imported " + db.add(new ArrayList<CapturingKit>(capturingKits.values()))
					+ " CapturingKits");
			System.out.println("Imported " + db.add(new ArrayList<PrepKit>(prepKits.values())) + " PrepKits");
			System.out.println("Imported " + db.add(new ArrayList<Machine>(machines.values())) + " Machines");
			System.out.println("Imported " + db.add(new ArrayList<Flowcell>(flowcells.values())) + " Flowcell");
			System.out.println("Imported " + db.add(new ArrayList<Project>(projects.values())) + " Project");

			db.commitTx();
			db.close();

			System.out.println("Import completed succefully.");
		}
		catch (Exception e)
		{
			db.rollbackTx();
			e.printStackTrace();
		}
	}

	private static void importSequencingProjects(File csv, boolean debug) throws Exception
	{

	}
}
