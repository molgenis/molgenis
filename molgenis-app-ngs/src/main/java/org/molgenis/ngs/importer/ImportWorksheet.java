package org.molgenis.ngs.importer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
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
import org.molgenis.omx.ngs.Sample;
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
		Map<String, Sample> samples = new LinkedHashMap<String, Sample>();

		// Import sheet
		TupleReader reader = new CsvReader(csv);

		for (Tuple row : reader)
		{
			// NgsUser customer
			if (!row.isNull("contact"))
			{
				NgsUser u = new NgsUser();
				// get the name and email from the contact value
				String[] contactParts = row.getString("contact").split("<");
				u.setUserName(contactParts[0].toLowerCase().trim());
				if (contactParts[1] != null)
				{
					u.setUserEmail(contactParts[1].toLowerCase().trim().replace(">", ""));
				}
				u.setUserRole("Customer");
				u.setUserGroup("None");
				users.put(u.getUserName(), u);
			}
			if (!row.isNull("DataShippedTo"))
			{
				NgsUser u = new NgsUser();
				String[] dataShippedToParts = row.getString("DataShippedTo").split("<");
				u.setUserName(dataShippedToParts[0].toLowerCase().trim());
				u.setUserRole("Customer");
				u.setUserGroup("None");
				users.put(u.getUserName(), u);
			}

			// NgsUser GAF
			if (!row.isNull("GAF_QC_Name"))
			{
				NgsUser u = new NgsUser();
				String[] gafQcNameParts = row.getString("GAF_QC_Name").split("<");
				u.setUserName(gafQcNameParts[0].toLowerCase().trim());
				u.setUserRole("Analist");
				u.setUserGroup("GAF");
				users.put(u.getUserName(), u);
			}

			// NgsUser GCC
			if (!row.isNull("GCC_QC_Name"))
			{
				NgsUser u = new NgsUser();
				String[] gccQcNameParts = row.getString("GCC_QC_Name").split("<");
				u.setUserName(gccQcNameParts[0].toLowerCase().trim());
				u.setUserRole("Bioinformatician");
				u.setUserGroup("GCC");
				users.put(u.getUserName(), u);
			}
			if (!row.isNull("DataShippedBy"))
			{
				NgsUser u = new NgsUser();
				String[] dataShippedByParts = row.getString("DataShippedBy").split("<");
				u.setUserName(dataShippedByParts[0].toLowerCase().trim());
				u.setUserRole("Bioinformatician");
				u.setUserGroup("GCC");
				users.put(u.getUserName(), u);
			}

			// SampleBarcode
			if (!row.isNull("barcode"))
			{
				SampleBarcode sb = new SampleBarcode();

				// SampleBarcodeName
				if (row.getString("barcode") != null)
				{
					if (row.getString("barcode").toLowerCase().trim().equals("none")
							|| row.getString("barcode").toLowerCase().trim().equals("error"))
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
					if (row.getString("barcodeType").toLowerCase().trim().equals("none")
							|| row.getString("barcodeType").toLowerCase().trim().equals("error"))
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
					if (row.getString("capturingKit").toLowerCase().trim().equals("none"))
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
					if (row.getString("prepKit").toLowerCase().trim().equals("none")
							|| row.getString("prepKit").toLowerCase().trim().equals("_"))
					{
						pk.setPrepKitName("None");
					}
					else
					{
						pk.setPrepKitName(row.getString("prepKit").toUpperCase().trim());
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
					if (row.getString("sequencer").toLowerCase().trim().equals("none"))
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

				// RunDate
				// sequencingStartDate notation should be as 110218 "yymmdd",
				// some values exist like 10429 "yymdd"
				Date runDate = null;
				if (row.getString("sequencingStartDate") != null)
				{
					if (!row.getString("sequencingStartDate").toLowerCase().trim().equals("unknown"))
					{
						char[] d = row.getString("sequencingStartDate").trim().toCharArray();
						if (d.length == 8)
						{
							runDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse(d[0] + d[1] + d[2]
									+ d[3] + "/" + d[4] + d[5] + "/" + d[6] + d[7]);
						}
						if (d.length == 6)
						{
							runDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse("20" + d[0] + d[1] + "/"
									+ d[2] + d[3] + "/" + d[4] + d[5]);
						}
						if (d.length == 5)
						{
							runDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse("20" + d[0] + d[1]
									+ "/0" + d[2] + "/" + d[3] + d[4]);
						}
					}
				}
				f.setRunDate(runDate);

				flowcells.put(f.getFlowcellName(), f);
			}

			// Project
			if (!row.isNull("project"))
			{
				Project p = new Project();

				// ProjectName
				p.setProjectName(row.getString("project").toLowerCase().trim());

				// get the name from the contact value
				String contactName = "";
				if (row.getString("contact") != null)
				{
					String[] nameEmailParts = row.getString("contact").split("<");
					contactName = nameEmailParts[0].toLowerCase().trim();
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

				// ProjectPlannedFinishDate
				Date projectPlannedFinishDate = null;
				if (row.getString("TargetDateShipment") != null)
				{
					if (!row.getString("TargetDateShipment").toLowerCase().trim().equals("unknown"))
					{
						char[] d = row.getString("TargetDateShipment").trim().toCharArray();
						if (d.length == 8)
						{
							projectPlannedFinishDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse(d[0]
									+ d[1] + d[2] + d[3] + "/" + d[4] + d[5] + "/" + d[6] + d[7]);
						}
						if (d.length == 6)
						{
							projectPlannedFinishDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse("20"
									+ d[0] + d[1] + "/" + d[2] + d[3] + "/" + d[4] + d[5]);
						}
						if (d.length == 5)
						{
							projectPlannedFinishDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse("20"
									+ d[0] + d[1] + "/0" + d[2] + "/" + d[3] + d[4]);
						}
					}
				}
				p.setProjectPlannedFinishDate(projectPlannedFinishDate);

				// ResultShippedUser (how to get the id of the user)
				String dataShippedByName = "";
				if (row.getString("DataShippedBy") != null)
				{
					String[] dataShippedByNameParts = row.getString("DataShippedBy").split("<");
					dataShippedByName = dataShippedByNameParts[0].toLowerCase().trim();
				}
				p.setResultShippedUser_UserName(dataShippedByName);

				// ResultShippedTo
				String dataShippedToName = "";
				if (row.getString("DataShippedTo") != null)
				{
					String[] dataShippedToNameParts = row.getString("DataShippedTo").split("<");
					dataShippedToName = dataShippedToNameParts[0].toLowerCase().trim();
				}
				p.setResultShippedTo_UserName(dataShippedToName);

				// ResultShippedDate
				Date resultShippedDate = null;
				if (row.getString("DataShippedDate") != null)
				{
					if (!row.getString("DataShippedDate").toLowerCase().trim().equals("unknown"))
					{
						char[] d = row.getString("DataShippedDate").trim().toCharArray();
						if (d.length == 8)
						{
							resultShippedDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse(d[0] + d[1]
									+ d[2] + d[3] + "/" + d[4] + d[5] + "/" + d[6] + d[7]);
						}
						if (d.length == 6)
						{
							resultShippedDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse("20" + d[0]
									+ d[1] + "/" + d[2] + d[3] + "/" + d[4] + d[5]);
						}
						if (d.length == 5)
						{
							resultShippedDate = new SimpleDateFormat("yyyy/mm/dd", Locale.ENGLISH).parse("20" + d[0]
									+ d[1] + "/0" + d[2] + "/" + d[3] + d[4]);
						}
					}
				}
				p.setResultShippedDate(resultShippedDate);

				// GccAnalysis
				boolean gccAnalysis = false;
				if (row.getString("GCC_Analysis") != null)
				{
					String gccAnalysisValue = row.getString("GCC_Analysis");
					if (gccAnalysisValue.toLowerCase().trim().equals("yes"))
					{
						gccAnalysis = true;
					}
				}
				p.setGccAnalysis(gccAnalysis);

				projects.put(p.getProjectName().toLowerCase(), p);
			}

			// Sample
			if (!row.isNull("internalSampleID"))
			{
				Sample s = new Sample();

				// InternalId
				if (row.getString("internalSampleID") != null)
				{
					s.setInternalId(row.getString("internalSampleID"));
				}

				// ExternalId
				if (row.getString("externalSampleID") != null)
				{
					s.setExternalId(row.getString("externalSampleID"));
				}

				// SampleType
				s.setSampleType("DNA");

				// SampleComment
				if (row.getString("labStatusComments") != null)
				{
					s.setSampleComment(row.getString("labStatusComments"));
				}

				// ProjectId
				if (row.getString("project") != null)
				{
					s.setProjectId_ProjectName(row.getString("project").toLowerCase().trim());
				}

				// ArrayFile
				if (row.getString("arrayFile") != null)
				{
					s.setArrayFile(row.getString("arrayFile"));
				}

				// ArrayID
				if (row.getString("arrayID") != null)
				{
					s.setArrayId(row.getString("arrayID"));
				}

				// LabStatus
				if (row.getString("labStatusPhase") != null)
				{
					s.setLabStatus(row.getString("labStatusPhase"));
				}

				// PrepKit
				if (row.getString("prepKit") != null)
				{
					if (row.getString("prepKit").toLowerCase().trim().equals("none")
							|| row.getString("prepKit").toLowerCase().trim().equals("_"))
					{
						s.setPrepKit_PrepKitName("None");
					}
					else
					{
						s.setPrepKit_PrepKitName(row.getString("prepKit").toUpperCase().trim());
					}
				}

				// QcWetMet ?

				// QcWetUser ?

				// QcWetDate ?

				// QcDryMet

				// QcDryUser

				// QcDryDate

				samples.put(s.getInternalId(), s);
			}

			// FlowcellLaneSampleBarcode

		}
		reader.close();

		// Show all collected values
		for (Sample s : samples.values())
		{
			System.out.println(s);
		}
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
				db.remove(db.find(Sample.class));
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
			System.out.println("Imported " + db.add(new ArrayList<Sample>(samples.values())) + " Sample");

			db.commitTx();
			db.close();

			System.out.println("Import completed succesfully.");
		}
		catch (Exception e)
		{
			db.rollbackTx();
			e.printStackTrace();
		}
	}

	private static void importSequencingProjects(File csv, boolean debug) throws Exception
	{
		// From sheet GAFSequencingProjects

		// ProjectComment
		// ProjectAnalist(s)
		// LaneAmount
		// SampleAmount
		// DeclarationNr
	}
}
