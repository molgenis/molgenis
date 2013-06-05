package org.molgenis.ngs.importer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.jpa.JpaDatabase;
import org.molgenis.io.TupleReader;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.omx.ngs.CapturingKit;
import org.molgenis.omx.ngs.Flowcell;
import org.molgenis.omx.ngs.FlowcellLane;
import org.molgenis.omx.ngs.Machine;
import org.molgenis.omx.ngs.NgsUser;
import org.molgenis.omx.ngs.PrepKit;
import org.molgenis.omx.ngs.Project;
import org.molgenis.omx.ngs.Sample;
import org.molgenis.omx.ngs.SampleBarcode;
import org.molgenis.omx.ngs.SampleBarcodeType;
import org.molgenis.util.tuple.Tuple;

public class ImportWorksheet
{
	private static Logger logger = Logger.getLogger(ImportWorksheet.class);
	private static File ngsUserFile = null;
	private static List<String> originalUserNames = new ArrayList<String>();
	private static List<String> userNamesNotDetected = new ArrayList<String>();

	public static void main(String[] args) throws Exception
	{
		boolean debug = false;

		if (args.length != 4)
		{
			System.err
					.println("Usage: <GAF SequencingSampleDetails csv file> <GAF SequencingProjects csv file> <GAF NgsUsers csv file> <Empty database? (yes/no)>");
			return;
		}

		if ((args.length == 4))
		{
			// Debug in eclipse
			args[3] = "yes";

			if (args[3] != "yes" && args[3] != "no")
			{
				System.err.println("Use yes or no");
				return;
			}
			else
			{
				if (args[3].equalsIgnoreCase("yes"))
				{
					debug = true;
				}

				// GAFsheet NgsUsers fixed names
				ngsUserFile = new File(args[2]);

				// GAFsheet SequencingSampleDetails and GAFsheet
				// SequencingProjects
				importSequencingSampleDetails(new File(args[0]), new File(args[1]), debug);
				// In case a new dataset contains different or new names show
				// the original names and the missing names.
				showOriginalUserNames(originalUserNames, userNamesNotDetected, debug);
			}
		}
	}

	private static void showOriginalUserNames(List<String> originalNames, List<String> notDetected, boolean debug)
	{
		try
		{
			if (debug)
			{
				Map<String, String> on = new LinkedHashMap<String, String>();
				for (String s : originalNames)
				{
					on.put(s, s);
				}
				for (String o : on.values())
				{
					logger.info(o);
				}

				Map<String, String> nd = new LinkedHashMap<String, String>();
				for (String s : notDetected)
				{
					nd.put(s, s);
				}
				for (String d : nd.values())
				{
					logger.info("Not registered user yet:\t" + d);
				}
			}
		}
		catch (Exception e)
		{
			e.getStackTrace();
		}
	}

	private static void importSequencingSampleDetails(File csvs, File csvp, boolean debug) throws Exception
	{
		// Declare collections to hold collected variables
		Map<String, NgsUser> users = new LinkedHashMap<String, NgsUser>();
		Map<String, SampleBarcodeType> sampleBarcodeTypes = new LinkedHashMap<String, SampleBarcodeType>();
		Map<String, SampleBarcode> sampleBarcodes = new LinkedHashMap<String, SampleBarcode>();
		Map<String, CapturingKit> capturingKits = new LinkedHashMap<String, CapturingKit>();
		Map<String, PrepKit> prepKits = new LinkedHashMap<String, PrepKit>();
		Map<String, Machine> machines = new LinkedHashMap<String, Machine>();
		Map<String, Flowcell> flowcells = new LinkedHashMap<String, Flowcell>();
		Map<String, Project> projects = new LinkedHashMap<String, Project>();
		Map<String, Sample> samples = new LinkedHashMap<String, Sample>();
		Map<String, FlowcellLane> flowcellLanesTmp = new LinkedHashMap<String, FlowcellLane>();
		Map<String, FlowcellLane> flowcellLanes = new LinkedHashMap<String, FlowcellLane>();

		// Import sheets
		TupleReader readerp = null;
		try
		{
			// First gather users from the projectSheet
			readerp = new CsvReader(csvp);
			for (Tuple rowp : readerp)
			{
				// ProjectAnalist(s)
				if (rowp.getString("Analist") != null)
				{
					String[] analistParts = rowp.getString("Analist").split("/");
					for (int i = 0; i < analistParts.length; i++)
					{
						NgsUser u = fixNgsUser(analistParts[0].toLowerCase().trim(), ngsUserFile);
						users.put(u.getUserName(), u);
					}
				}
			}
		}
		finally
		{
			if (readerp != null) readerp.close();
		}

		TupleReader readers = null;
		try
		{
			readers = new CsvReader(csvs);
			for (Tuple row : readers)
			{
				// NgsUser customer
				if (!row.isNull("contact"))
				{
					String[] contactParts = row.getString("contact").split("<");
					NgsUser u = fixNgsUser(contactParts[0].toLowerCase().trim(), ngsUserFile);
					users.put(u.getUserName(), u);
				}
				if (!row.isNull("DataShippedTo"))
				{
					String[] dataShippedToParts = row.getString("DataShippedTo").split("<");
					NgsUser u = fixNgsUser(dataShippedToParts[0].toLowerCase().trim(), ngsUserFile);
					users.put(u.getUserName(), u);
				}

				// NgsUser GAF
				if (!row.isNull("GAF_QC_Name"))
				{
					String[] gafQcNameParts = row.getString("GAF_QC_Name").split("<");
					NgsUser u = fixNgsUser(gafQcNameParts[0].toLowerCase().trim(), ngsUserFile);
					users.put(u.getUserName(), u);
				}

				// NgsUser GCC
				if (!row.isNull("GCC_QC_Name"))
				{
					String[] gccQcNameParts = row.getString("GCC_QC_Name").split("<");
					NgsUser u = fixNgsUser(gccQcNameParts[0].toLowerCase().trim(), ngsUserFile);
					users.put(u.getUserName(), u);
				}
				if (!row.isNull("DataShippedBy"))
				{
					String[] dataShippedByParts = row.getString("DataShippedBy").split("<");
					NgsUser u = fixNgsUser(dataShippedByParts[0].toLowerCase().trim(), ngsUserFile);
					users.put(u.getUserName(), u);
				}

				// SampleBarcodeType
				if (!row.isNull("barcodeType"))
				{
					SampleBarcodeType sbt = new SampleBarcodeType();

					if (row.getString("barcodeType") != null)
					{
						if (!row.getString("barcodeType").toLowerCase().trim().equals("none")
								&& !row.getString("barcodeType").toLowerCase().trim().contains("error"))
						{
							sbt.setSampleBarcodeTypeName(row.getString("barcodeType").toUpperCase().trim());

							sampleBarcodeTypes.put(sbt.getSampleBarcodeTypeName(), sbt);
						}
					}
				}

				// SampleBarcode
				if (row.getString("barcodeMenu") != null) // GAF 01 ACTGTC
				{
					SampleBarcode sb = new SampleBarcode();

					String[] bmp = row.getString("barcodeMenu").split(" ");

					if (bmp.length == 3) // Valid data
					{
						if (!bmp[0].equalsIgnoreCase(""))
						{
							sb.setSampleBarcodeType_SampleBarcodeTypeName(bmp[0].toUpperCase().trim());
						}
						if (!bmp[1].equalsIgnoreCase(""))
						{
							sb.setSampleBarcodeNr(bmp[1]);
						}
						if (!bmp[2].equalsIgnoreCase(""))
						{
							sb.setSampleBarcodeSequence(bmp[2].toUpperCase().trim());
						}

						sb.setSampleBarcodeName(row.getString("barcodeMenu"));

						sampleBarcodes.put(sb.getSampleBarcodeName(), sb);
					}
				}

				// CapturingKit
				if (!row.isNull("capturingKit"))
				{
					CapturingKit ck = new CapturingKit();

					// CapturingKitName
					if (row.getString("capturingKit") != null)
					{
						if (!row.getString("capturingKit").toLowerCase().trim().equals("none"))
						{
							ck.setCapturingKitName(row.getString("capturingKit").trim());

							capturingKits.put(ck.getCapturingKitName(), ck);
						}

					}
				}

				// PrepKit
				if (!row.isNull("prepKit"))
				{
					PrepKit pk = new PrepKit();

					// PrepKitName
					if (row.getString("prepKit") != null)
					{
						if (!row.getString("prepKit").toLowerCase().trim().equals("none")
								&& !row.getString("prepKit").toLowerCase().trim().equals("_"))
						{
							pk.setPrepKitName(row.getString("prepKit").toUpperCase().trim());

							prepKits.put(pk.getPrepKitName(), pk);
						}
					}
				}

				// Machine
				if (!row.isNull("sequencer"))
				{
					Machine m = new Machine();

					// MachineName
					if (row.getString("sequencer") != null)
					{
						if (!row.getString("sequencer").toLowerCase().trim().equals("none"))
						{
							m.setMachineName(row.getString("sequencer"));

							machines.put(m.getMachineName(), m);
						}
					}
				}

				// Flowcell
				if (!row.isNull("flowcell"))
				{
					Flowcell f = new Flowcell();

					// FlowcellName and FlowcellDirection
					if (row.getString("flowcell") != null)
					{
						char[] fc = row.getString("flowcell").trim().toCharArray();

						if (("" + fc[0]).equalsIgnoreCase("A") || ("" + fc[0]).equalsIgnoreCase("B"))
						{
							// First character is the Direction
							f.setFlowcellDirection("" + fc[0]);

							// The remaining nine characters are the Name
							f.setFlowcellName(row.getString("flowcell")
									.substring(1, row.getString("flowcell").length()));
						}

						// In case the Direction was not filled in only assign
						// the Name
						if (!("" + fc[0]).equalsIgnoreCase("A") && !("" + fc[0]).equalsIgnoreCase("B"))
						{
							f.setFlowcellName(row.getString("flowcell"));
						}
					}

					// MachineId
					if (row.getString("sequencer") != null)
					{
						if (!row.getString("sequencer").toLowerCase().trim().equals("none"))
						{
							f.setMachine_MachineName(row.getString("sequencer"));
						}
					}

					// Run
					if (row.getString("run") != null)
					{
						f.setRun(getFixedRunValue(row.getString("run")));
					}

					// RunDate
					f.setRunDate(getFixedDate(row.getString("sequencingStartDate")));

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
						contactName = getFixedUserName(nameEmailParts[0].toLowerCase().trim(), ngsUserFile);
					}
					p.setProjectCustomer_UserName(contactName);

					// SeqType
					if (row.getString("seqType") != null)
					{
						p.setSeqType(row.getString("seqType"));
					}
					else
					{
						p.setSeqType("Unknown");
					}

					// ProjectPlannedFinishDate
					p.setProjectPlannedFinishDate(getFixedDate(row.getString("TargetDateShipment")));

					// ResultShippedUser (how to get the id of the user)
					String dataShippedByName = "";
					if (row.getString("DataShippedBy") != null)
					{
						String[] dataShippedByNameParts = row.getString("DataShippedBy").split("<");
						dataShippedByName = getFixedUserName(dataShippedByNameParts[0].toLowerCase().trim(),
								ngsUserFile);
					}
					p.setResultShippedUser_UserName(dataShippedByName);

					// ResultShippedTo
					String dataShippedToName = "";
					if (row.getString("DataShippedTo") != null)
					{
						String[] dataShippedToNameParts = row.getString("DataShippedTo").split("<");
						dataShippedToName = getFixedUserName(dataShippedToNameParts[0].toLowerCase().trim(),
								ngsUserFile);
					}
					p.setResultShippedTo_UserName(dataShippedToName);

					// ResultShippedDate
					p.setResultShippedDate(getFixedDate(row.getString("DataShippedDate")));

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

					// SampleType
					p.setSampleType("DNA");

					// PrepKit
					if (row.getString("prepKit") != null)
					{
						if (!row.getString("prepKit").trim().equalsIgnoreCase("none")
								&& !row.getString("prepKit").toLowerCase().trim().equals("_"))
						{
							p.setPrepKit_PrepKitName(row.getString("prepKit").toUpperCase().trim());
						}
					}

					// Get remaining values from projectSheet
					try
					{
						readerp = new CsvReader(csvp);

						for (Tuple rowp : readerp)
						{
							if (rowp.getString("Project").toLowerCase().trim()
									.equals(row.getString("project").toLowerCase().trim()))
							{
								// ProjectComment
								if (rowp.getString("Comments") != null)
								{
									p.setProjectComment(rowp.getString("Comments"));
								}

								// ProjectAnalist(s)
								if (rowp.getString("Analist") != null)
								{
									String[] analistParts = rowp.getString("Analist").split("/");
									List<String> ngsUsers = new ArrayList<String>();
									for (int i = 0; i < analistParts.length; i++)
									{
										// ngsUsers.add(analistParts[i].toLowerCase().trim());
										ngsUsers.add(getFixedUserName(analistParts[i].toLowerCase().trim(), ngsUserFile));
									}
									p.setProjectAnalist_UserName(ngsUsers);
								}

								// LaneAmount
								if (rowp.getString("Aantal lanes") != null)
								{
									p.setLaneAmount(rowp.getInt("Aantal lanes"));
								}

								// SampleAmount
								if (rowp.getString("Aantal monsters") != null)
								{
									p.setSampleAmount(rowp.getInt("Aantal monsters"));
								}

								// DeclarationNr
								if (rowp.getString("Declaratie nr") != null)
								{
									p.setDeclarationNr(rowp.getString("Declaratie nr"));
								}
							}
						}
					}
					finally
					{
						if (readerp != null) readerp.close();
					}
					projects.put(p.getProjectName().toLowerCase().trim(), p);
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
						s.setLabStatus(row.getString("labStatusPhase").trim());
					}

					if (row.getString("barcodeMenu") != null)
					{
						String[] bmp = row.getString("barcodeMenu").split(" ");

						if (bmp.length == 3) // Valid data
						{
							s.setSampleBarcode_SampleBarcodeName(row.getString("barcodeMenu"));
						}
					}

					// CapturingKit
					if (row.getString("capturingKit") != null)
					{
						if (!row.getString("capturingKit").toLowerCase().trim().equals("none"))
						{
							s.setCapturingKit_CapturingKitName(row.getString("capturingKit").trim());
						}
					}

					samples.put(s.getInternalId(), s);
				}

				// FlowcellLaneTmp: Collect all samples which are added to lanes
				// on a flowcell
				if (!row.isNull("internalSampleID"))
				{
					// For each Lane
					if (row.getString("lane") != null)
					{
						String[] lanes = row.getString("lane").split(",");
						for (int i = 0; i < lanes.length; i++)
						{
							FlowcellLane fl = new FlowcellLane();

							fl.setLane(lanes[i]);

							// Sample
							if (row.getString("internalSampleID") != null)
							{
								fl.setSample_InternalId(row.getString("internalSampleID"));
							}

							// FlowcellLaneComment
							if (row.getString("Comments") != null)
							{
								fl.setFlowcellLaneComment(row.getString("Comments").replace("'", "`"));
							}

							// Flowcell
							if (row.getString("flowcell") != null)
							{
								String _FlowcellName = "";
								char[] fc = row.getString("flowcell").trim().toCharArray();

								if (("" + fc[0]).equalsIgnoreCase("A") || ("" + fc[0]).equalsIgnoreCase("B"))
								{
									_FlowcellName = row.getString("flowcell").substring(1,
											row.getString("flowcell").length());
								}

								// If Direction not exists only assign the Name
								if (!("" + fc[0]).equalsIgnoreCase("A") && !("" + fc[0]).equalsIgnoreCase("B"))
								{
									_FlowcellName = row.getString("flowcell");
								}

								fl.setFlowcell_FlowcellName(_FlowcellName);
							}

							// QcWetMet
							if (row.getString("GAF_QC_Status") != null)
							{
								fl.setQcWetMet(row.getString("GAF_QC_Status"));
							}
							else
							{
								fl.setQcWetMet("Not determined");
							}

							// QcWetUser
							if (row.getString("GAF_QC_Name") != null)
							{
								fl.setQcWetUser_UserName(getFixedUserName(row.getString("GAF_QC_Name").toLowerCase()
										.trim(), ngsUserFile));
							}

							// QcWetDate
							fl.setQcWetDate(getFixedDate(row.getString("GAF_QC_Date")));

							// QcDryMet
							if (row.getString("GCC_QC_Status") != null)
							{
								fl.setQcDryMet(row.getString("GCC_QC_Status"));
							}
							else
							{
								fl.setQcDryMet("Not determined");
							}

							// QcDryUser
							if (row.getString("GCC_QC_Name") != null)
							{
								fl.setQcDryUser_UserName(getFixedUserName(row.getString("GCC_QC_Name").toLowerCase()
										.trim(), ngsUserFile));
							}

							// QcDryDate
							fl.setQcDryDate(getFixedDate(row.getString("GCC_QC_Date")));

							flowcellLanesTmp.put(fl.getSample_InternalId(), fl);
						}
					}
				}
			}
		}
		finally
		{
			if (readers != null) readers.close();
		}

		// Create a max InternalId value
		String _MaxInternalId = "5000";

		// Loop through the FlowcellLanes(Tmp) from the GAF data, determine what
		// are Samples and what are Pools and assign them to justified
		// FlowcellLanes
		for (FlowcellLane fl : flowcellLanesTmp.values())
		{
			List<FlowcellLane> fls = new ArrayList<FlowcellLane>();

			for (FlowcellLane flb : flowcellLanesTmp.values())
			{
				if (fl.getFlowcell_FlowcellName().equalsIgnoreCase(flb.getFlowcell_FlowcellName())
						&& fl.getLane().equalsIgnoreCase(flb.getLane()) && fl.getFlowcell_FlowcellName() != null
						&& fl.getLane() != null && flb.getFlowcell_FlowcellName() != null && flb.getLane() != null)
				{
					// Check if record is already in the list
					boolean _Present = false;
					for (int i = 0; i < fls.size(); i++)
					{
						if (fls.get(i).equals(flb)) // TODO: this is not working
						{
							_Present = true;
						}
					}
					if (!_Present)
					{
						fls.add(flb);
					}
				}
			}

			// Single Sample
			if (fls.size() == 1)
			{
				FlowcellLane flss = new FlowcellLane();

				if (fls.get(0).getLane() != null)
				{
					flss.setLane(fls.get(0).getLane());
				}
				if (fls.get(0).getSample_InternalId() != null)
				{
					flss.setSample_InternalId(fls.get(0).getSample_InternalId());
				}
				if (fls.get(0).getFlowcellLaneComment() != null)
				{
					flss.setFlowcellLaneComment(fls.get(0).getFlowcellLaneComment());
				}
				if (fls.get(0).getFlowcell_FlowcellName() != null)
				{
					flss.setFlowcell_FlowcellName(fls.get(0).getFlowcell_FlowcellName());
				}
				if (fls.get(0).getQcWetMet() != null)
				{
					flss.setQcWetMet(fls.get(0).getQcWetMet());
				}
				if (fls.get(0).getQcWetUser_UserName() != null)
				{
					flss.setQcWetUser_UserName(fls.get(0).getQcWetUser_UserName());
				}
				if (fls.get(0).getQcWetDate() != null)
				{
					flss.setQcWetDate(fls.get(0).getQcWetDate());
				}
				if (fls.get(0).getQcDryMet() != null)
				{
					flss.setQcDryMet(fls.get(0).getQcDryMet());
				}
				if (fls.get(0).getQcDryUser_UserName() != null)
				{
					flss.setQcDryUser_UserName(fls.get(0).getQcDryUser_UserName());
				}
				if (fls.get(0).getQcDryDate() != null)
				{
					flss.setQcDryDate(fls.get(0).getQcDryDate());
				}

				flowcellLanes.put(flss.getSample_InternalId(), flss);
			}

			// Pooled sample
			if (fls.size() > 1)
			{
				// Create a Pooled Sample
				int _NewInternalId = Integer.parseInt(_MaxInternalId);
				FlowcellLane flsp = new FlowcellLane();
				Sample ps = new Sample();

				// InternalId
				ps.setInternalId("" + _NewInternalId);

				// SamplesInPool
				List<String> sList = new ArrayList<String>();
				for (int i = 0; i < fls.size(); i++)
				{
					if (fls.get(i).getSample_InternalId() != null)
					{
						sList.add(fls.get(i).getSample_InternalId());
					}
				}

				if (sList.size() > 1)
				{
					ps.setSampleInPool_InternalId(sList);

					// Add SamplePool to samples collection
					samples.put(ps.getInternalId(), ps);

					// Add SamplePool to a Flowcelllane
					flsp.setSample_InternalId(ps.getInternalId());

					flowcellLanes.put(flsp.getSample_InternalId(), flsp);

					// Increment the InternalId
					_MaxInternalId = "" + (_NewInternalId + 1);
				}
			}
		}

		// Show all collected values
		if (debug)
		{
			for (FlowcellLane fl : flowcellLanes.values())
			{
				logger.info(fl);
			}

			for (Sample s : samples.values())
			{
				logger.info(s);
			}

			for (Project p : projects.values())
			{
				logger.info(p);
			}

			for (Flowcell f : flowcells.values())
			{
				logger.info(f);
			}

			for (Machine m : machines.values())
			{
				logger.info(m);
			}

			for (PrepKit pk : prepKits.values())
			{
				logger.info(pk);
			}

			for (CapturingKit ck : capturingKits.values())
			{
				logger.info(ck);
			}

			for (SampleBarcode sb : sampleBarcodes.values())
			{
				logger.info(sb);
			}

			for (SampleBarcodeType sbt : sampleBarcodeTypes.values())
			{
				logger.info(sbt);
			}

			for (NgsUser u : users.values())
			{
				logger.info(u);
			}
		}

		logger.info("Collected:\t" + flowcellLanes.size() + " FlowcellLane(s)");
		logger.info("Collected:\t" + samples.size() + " Sample(s)");
		logger.info("Collected:\t" + projects.size() + " Project(s)");
		logger.info("Collected:\t" + flowcells.size() + " Flowcell(s)");
		logger.info("Collected:\t" + machines.size() + " Sequencer(s)");
		logger.info("Collected:\t" + prepKits.size() + " Preparation kit(s)");
		logger.info("Collected:\t" + capturingKits.size() + " Capturing kit(s)");
		logger.info("Collected:\t" + sampleBarcodes.size() + " Barcode(s)");
		logger.info("Collected:\t" + sampleBarcodeTypes.size() + " BarcodeType(s)");
		logger.info("Collected:\t" + users.size() + " NgsUser(s)");

		// Put values in database
		Database db = new org.molgenis.JpaDatabase(
				Persistence.createEntityManagerFactory(JpaDatabase.DEFAULT_PERSISTENCE_UNIT_NAME));
		try
		{
			db.beginTx();

			// Clean old stuff (reverse order of foreign keys)
			if (debug)
			{
				db.remove(db.find(FlowcellLane.class));
				db.remove(db.find(Sample.class));
				db.remove(db.find(Project.class));
				db.remove(db.find(Flowcell.class));
				db.remove(db.find(Machine.class));
				db.remove(db.find(PrepKit.class));
				db.remove(db.find(CapturingKit.class));
				db.remove(db.find(SampleBarcode.class));
				db.remove(db.find(SampleBarcodeType.class));
				db.remove(db.find(NgsUser.class));
			}

			logger.info("Imported:\t" + db.add(new ArrayList<NgsUser>(users.values())) + " NgsUser(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<SampleBarcodeType>(sampleBarcodeTypes.values()))
					+ " BarcodeType(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<SampleBarcode>(sampleBarcodes.values())) + " Barcode(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<CapturingKit>(capturingKits.values()))
					+ " Capturing kit(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<PrepKit>(prepKits.values())) + " Preparation kit(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<Machine>(machines.values())) + " Sequencer(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<Flowcell>(flowcells.values())) + " Flowcell(s)");
			logger.info("Imported:\t" + db.add(new ArrayList<Project>(projects.values())) + " Project(s)");

			// Fix for importing the StackOverFlow error caused by Sample and
			// FlowcellLane collections.
			boolean crash = false;

			for (Sample s : samples.values())
			{
				try
				{
					db.add(s);
				}
				catch (Exception ex)
				{
					crash = true;
				}
			}
			for (FlowcellLane fl : flowcellLanes.values())
			{
				try
				{
					db.add(fl);
				}
				catch (Exception ex)
				{
					crash = true;
				}
			}

			if (!crash)
			{
				db.commitTx();
				logger.info("Imported:\t" + samples.size() + " Sample(s)");
				logger.info("Imported:\t" + flowcellLanes.size() + " FlowcellLane(s)");
				logger.info("Import completed succesfully.");
			}
			else
			{
				logger.info("Import failed :(");
			}
		}
		catch (Exception e)
		{
			db.rollbackTx();
			e.printStackTrace();
		}

		db.close();
	}

	private static Date getFixedDate(String s)
	{
		Date date = null;

		try
		{
			if (s != null)
			{
				if (!s.toLowerCase().trim().equals("unknown"))
				{
					char[] d = s.trim().toCharArray();
					if (d.length == 8)
					{
						date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(d[0] + "" + d[1] + "" + d[2]
								+ "" + d[3] + "/" + d[4] + "" + d[5] + "/" + d[6] + "" + d[7]);
					}
					if (d.length == 6)
					{
						date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse("20" + d[0] + "" + d[1] + "/"
								+ d[2] + "" + d[3] + "/" + d[4] + "" + d[5]);
					}
					if (d.length == 5)
					{
						date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse("20" + d[0] + "" + d[1] + "/0"
								+ d[2] + "/" + d[3] + "" + d[4]);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return date;
	}

	private static NgsUser fixNgsUser(String userName, File csvu) throws Exception
	{
		NgsUser fixedNgsUser = new NgsUser();
		TupleReader readeru = null;

		try
		{
			boolean detected = false;
			readeru = new CsvReader(csvu);

			// Assign fixed name, email, role and group
			for (Tuple rowu : readeru)
			{
				originalUserNames.add(userName);

				if (!rowu.isNull("Current UserName:"))
				{
					if (rowu.getString("Current UserName:").equalsIgnoreCase(userName))
					{
						detected = true;

						if (!rowu.isNull("Real UserName:"))
						{
							fixedNgsUser.setUserName(rowu.getString("Real UserName:"));
						}

						if (!rowu.isNull("UserEmail:"))
						{
							fixedNgsUser.setUserEmail(rowu.getString("UserEmail:"));
						}

						if (!rowu.isNull("UserRole:"))
						{
							fixedNgsUser.setUserRole(rowu.getString("UserRole:"));
						}

						if (!rowu.isNull("UserGroup:"))
						{
							fixedNgsUser.setUserGroup(rowu.getString("UserGroup:"));
						}
					}
				}
			}
			if (!detected)
			{
				userNamesNotDetected.add(userName);
			}
		}
		finally
		{
			if (readeru != null)
			{
				readeru.close();
			}
		}

		return fixedNgsUser;
	}

	private static String getFixedUserName(String userName, File csvu) throws Exception
	{
		String fixedNgsUserName = "";
		TupleReader readeru = null;

		try
		{
			readeru = new CsvReader(csvu);

			// Assign fixed name, email, role and group
			for (Tuple rowu : readeru)
			{
				if (!rowu.isNull("Current UserName:"))
				{
					if (rowu.getString("Current UserName:").equalsIgnoreCase(userName))
					{
						if (!rowu.isNull("Real UserName:"))
						{
							fixedNgsUserName = rowu.getString("Real UserName:");
						}
					}
				}
			}
		}
		finally
		{
			if (readeru != null)
			{
				readeru.close();
			}
		}

		return fixedNgsUserName;
	}

	private static String getFixedRunValue(String runValue) throws Exception
	{
		String fixedRunValue = runValue;

		try
		{
			while (fixedRunValue.toCharArray().length < 4)
			{
				String fixedRunValueTmp = "0" + fixedRunValue;
				fixedRunValue = fixedRunValueTmp;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return fixedRunValue;
	}

}
