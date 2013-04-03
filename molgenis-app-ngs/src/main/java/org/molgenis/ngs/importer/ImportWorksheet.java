//package org.molgenis.ngs.importer;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Logger;
//import org.molgenis.framework.db.Database;
//import org.molgenis.io.TupleReader;
//import org.molgenis.io.csv.CsvReader;
//import org.molgenis.omx.ngs.CapturingKit;
//import org.molgenis.omx.ngs.Flowcell;
//import org.molgenis.omx.ngs.FlowcellLaneSampleBarcode;
//import org.molgenis.omx.ngs.Machine;
//import org.molgenis.omx.ngs.NgsUser;
//import org.molgenis.omx.ngs.PrepKit;
//import org.molgenis.omx.ngs.Project;
//import org.molgenis.omx.ngs.Sample;
//import org.molgenis.omx.ngs.SampleBarcode;
//import org.molgenis.util.tuple.Tuple;
//
//public class ImportWorksheet
//{
//	private static Logger logger = Logger.getLogger(ImportWorksheet.class);
//
//	public static void main(String[] args) throws Exception
//	{
//		if (args.length != 2)
//		{
//			System.err.println("Usage: <GAF SequencingSampleDetails> <GAF SequencingProjects>");
//			return;
//		}
//
//		// GAFsheet SequencingSampleDetails and GAFsheet SequencingProjects
//		importSequencingSampleDetails(new File(args[0]), new File(args[1]), true);
//	}
//
//	private static void importSequencingSampleDetails(File csvs, File csvp, boolean debug) throws Exception
//	{
//		// Declare collections to hold collected variables
//		Map<String, NgsUser> users = new LinkedHashMap<String, NgsUser>();
//		Map<String, SampleBarcode> sampleBarcodes = new LinkedHashMap<String, SampleBarcode>();
//		Map<String, CapturingKit> capturingKits = new LinkedHashMap<String, CapturingKit>();
//		Map<String, PrepKit> prepKits = new LinkedHashMap<String, PrepKit>();
//		Map<String, Machine> machines = new LinkedHashMap<String, Machine>();
//		Map<String, Flowcell> flowcells = new LinkedHashMap<String, Flowcell>();
//		Map<String, Project> projects = new LinkedHashMap<String, Project>();
//		Map<String, Sample> samples = new LinkedHashMap<String, Sample>();
//		Map<String, List<FlowcellLaneSampleBarcode>> flowcellLaneSampleBarcodes = new LinkedHashMap<String, List<FlowcellLaneSampleBarcode>>();
//
//		// Import sheets
//		TupleReader readerp = null;
//		try
//		{
//			// First gather users from the projectSheet
//			readerp = new CsvReader(csvp);
//			for (Tuple rowp : readerp)
//			{
//				// ProjectAnalist(s)
//				if (rowp.getString("Analist") != null)
//				{
//					String[] analistParts = rowp.getString("Analist").split("/");
//					for (int i = 0; i < analistParts.length; i++)
//					{
//						NgsUser u = new NgsUser();
//						u.setUserName(analistParts[0].toLowerCase().trim());
//						u.setUserRole("Analist");
//						u.setUserGroup("GAF");
//						users.put(u.getUserName(), u);
//					}
//				}
//			}
//		}
//		finally
//		{
//			if (readerp != null) readerp.close();
//		}
//
//		TupleReader readers = null;
//
//		try
//		{
//			readers = new CsvReader(csvs);
//			for (Tuple row : readers)
//			{
//				// NgsUser customer
//				if (!row.isNull("contact"))
//				{
//					NgsUser u = new NgsUser();
//					// get the name and email from the contact value
//					String[] contactParts = row.getString("contact").split("<");
//					u.setUserName(contactParts[0].toLowerCase().trim());
//					if (contactParts[1] != null)
//					{
//						u.setUserEmail(contactParts[1].toLowerCase().trim().replace(">", ""));
//					}
//					u.setUserRole("Customer");
//					u.setUserGroup("None");
//					users.put(u.getUserName(), u);
//				}
//				if (!row.isNull("DataShippedTo"))
//				{
//					NgsUser u = new NgsUser();
//					String[] dataShippedToParts = row.getString("DataShippedTo").split("<");
//					u.setUserName(dataShippedToParts[0].toLowerCase().trim());
//					u.setUserRole("Customer");
//					u.setUserGroup("None");
//					users.put(u.getUserName(), u);
//				}
//
//				// NgsUser GAF
//				if (!row.isNull("GAF_QC_Name"))
//				{
//					NgsUser u = new NgsUser();
//					String[] gafQcNameParts = row.getString("GAF_QC_Name").split("<");
//					u.setUserName(gafQcNameParts[0].toLowerCase().trim());
//					u.setUserRole("Analist");
//					u.setUserGroup("GAF");
//					users.put(u.getUserName(), u);
//				}
//
//				// NgsUser GCC
//				if (!row.isNull("GCC_QC_Name"))
//				{
//					NgsUser u = new NgsUser();
//					String[] gccQcNameParts = row.getString("GCC_QC_Name").split("<");
//					u.setUserName(gccQcNameParts[0].toLowerCase().trim());
//					u.setUserRole("Bioinformatician");
//					u.setUserGroup("GCC");
//					users.put(u.getUserName(), u);
//				}
//				if (!row.isNull("DataShippedBy"))
//				{
//					NgsUser u = new NgsUser();
//					String[] dataShippedByParts = row.getString("DataShippedBy").split("<");
//					u.setUserName(dataShippedByParts[0].toLowerCase().trim());
//					u.setUserRole("Bioinformatician");
//					u.setUserGroup("GCC");
//					users.put(u.getUserName(), u);
//				}
//
//				// SampleBarcode
//				if (!row.isNull("barcode"))
//				{
//					SampleBarcode sb = new SampleBarcode();
//
//					// SampleBarcodeName
//					if (row.getString("barcode") != null)
//					{
//						if (row.getString("barcode").toLowerCase().trim().equals("none")
//								|| row.getString("barcode").toLowerCase().trim().contains("error"))
//						{
//							sb.setSampleBarcodeName("None");
//						}
//						else
//						{
//							sb.setSampleBarcodeName(row.getString("barcode").toUpperCase().trim());
//						}
//					}
//					else
//					{
//						sb.setSampleBarcodeName("None");
//					}
//
//					// BarcodeType
//					if (row.getString("barcodeType") != null)
//					{
//						if (row.getString("barcodeType").toLowerCase().trim().equals("none")
//								|| row.getString("barcodeType").toLowerCase().trim().contains("error"))
//						{
//							sb.setBarcodeType("None");
//						}
//						else
//						{
//							sb.setBarcodeType(row.getString("barcodeType").toUpperCase().trim());
//						}
//					}
//					else
//					{
//						sb.setBarcodeType("None");
//					}
//
//					sampleBarcodes.put(sb.getSampleBarcodeName(), sb);
//				}
//
//				// CapturingKit
//				if (!row.isNull("capturingKit"))
//				{
//					CapturingKit ck = new CapturingKit();
//
//					// CapturingKitName
//					if (row.getString("capturingKit") != null)
//					{
//						if (row.getString("capturingKit").toLowerCase().trim().equals("none"))
//						{
//							ck.setCapturingKitName("None");
//						}
//						else
//						{
//							ck.setCapturingKitName(row.getString("capturingKit").trim());
//						}
//					}
//					else
//					{
//						ck.setCapturingKitName("None");
//					}
//
//					capturingKits.put(ck.getCapturingKitName(), ck);
//				}
//
//				// PrepKit
//				if (!row.isNull("prepKit"))
//				{
//					PrepKit pk = new PrepKit();
//
//					// PrepKitName
//					if (row.getString("prepKit") != null)
//					{
//						if (row.getString("prepKit").toLowerCase().trim().equals("none")
//								|| row.getString("prepKit").toLowerCase().trim().equals("_"))
//						{
//							pk.setPrepKitName("None");
//						}
//						else
//						{
//							pk.setPrepKitName(row.getString("prepKit").toUpperCase().trim());
//						}
//					}
//					else
//					{
//						pk.setPrepKitName("None");
//					}
//
//					prepKits.put(pk.getPrepKitName(), pk);
//				}
//
//				// Machine
//				if (!row.isNull("sequencer"))
//				{
//					Machine m = new Machine();
//
//					if (row.getString("sequencer") != null)
//					{
//						if (row.getString("sequencer").toLowerCase().trim().equals("none"))
//						{
//							m.setMachineName("None");
//						}
//						else
//						{
//							m.setMachineName(row.getString("sequencer"));
//						}
//					}
//					else
//					{
//						m.setMachineName("None");
//					}
//
//					machines.put(m.getMachineName(), m);
//				}
//
//				// Flowcell
//				if (!row.isNull("flowcell"))
//				{
//					Flowcell f = new Flowcell();
//
//					// FlowcellName
//					if (row.getString("flowcell") != null)
//					{
//						f.setFlowcellName(row.getString("flowcell"));
//					}
//
//					// MachineId
//					if (row.getString("sequencer") != null)
//					{
//						f.setMachine_MachineName(row.getString("sequencer"));
//					}
//
//					// Run
//					if (row.getString("run") != null)
//					{
//						f.setRun(row.getString("run"));
//					}
//					// RunDate
//					f.setRunDate(getFixedDate(row.getString("sequencingStartDate")));
//
//					flowcells.put(f.getFlowcellName(), f);
//				}
//
//				// Project
//				if (!row.isNull("project"))
//				{
//					Project p = new Project();
//
//					// ProjectName
//					p.setProjectName(row.getString("project").toLowerCase().trim());
//
//					// get the name from the contact value
//					String contactName = "";
//					if (row.getString("contact") != null)
//					{
//						String[] nameEmailParts = row.getString("contact").split("<");
//						contactName = nameEmailParts[0].toLowerCase().trim();
//					}
//					p.setProjectCustomer_UserName(contactName);
//
//					// SeqType
//					if (row.getString("seqType") != null)
//					{
//						p.setSeqType(row.getString("seqType"));
//					}
//					else
//					{
//						p.setSeqType("Unknown");
//					}
//
//					// ProjectPlannedFinishDate
//					p.setProjectPlannedFinishDate(getFixedDate(row.getString("TargetDateShipment")));
//
//					// ResultShippedUser (how to get the id of the user)
//					String dataShippedByName = "";
//					if (row.getString("DataShippedBy") != null)
//					{
//						String[] dataShippedByNameParts = row.getString("DataShippedBy").split("<");
//						dataShippedByName = dataShippedByNameParts[0].toLowerCase().trim();
//					}
//					p.setResultShippedUser_UserName(dataShippedByName);
//
//					// ResultShippedTo
//					String dataShippedToName = "";
//					if (row.getString("DataShippedTo") != null)
//					{
//						String[] dataShippedToNameParts = row.getString("DataShippedTo").split("<");
//						dataShippedToName = dataShippedToNameParts[0].toLowerCase().trim();
//					}
//					p.setResultShippedTo_UserName(dataShippedToName);
//
//					// ResultShippedDate
//					p.setResultShippedDate(getFixedDate(row.getString("DataShippedDate")));
//
//					// GccAnalysis
//					boolean gccAnalysis = false;
//					if (row.getString("GCC_Analysis") != null)
//					{
//						String gccAnalysisValue = row.getString("GCC_Analysis");
//						if (gccAnalysisValue.toLowerCase().trim().equals("yes"))
//						{
//							gccAnalysis = true;
//						}
//					}
//					p.setGccAnalysis(gccAnalysis);
//
//					// TODO: Get remaining values from projectSheet
//					try
//					{
//						readerp = new CsvReader(csvp);
//
//						for (Tuple rowp : readerp)
//						{
//							if (rowp.getString("Project").toLowerCase().trim()
//									.equals(row.getString("project").toLowerCase().trim()))
//							{
//								// ProjectComment (original sheet contains no
//								// header)
//								if (rowp.getString("Comments") != null)
//								{
//									p.setProjectComment(rowp.getString("Comments"));
//								}
//
//								// ProjectAnalist(s)
//								if (rowp.getString("Analist") != null)
//								{
//									String[] analistParts = rowp.getString("Analist").split("/");
//									List<String> ngsUsers = new ArrayList<String>();
//									for (int i = 0; i < analistParts.length; i++)
//									{
//										ngsUsers.add(analistParts[i].toLowerCase().trim());
//									}
//									p.setProjectAnalist_UserName(ngsUsers);
//								}
//
//								// LaneAmount
//								if (rowp.getString("Aantal lanes") != null)
//								{
//									p.setLaneAmount(rowp.getDouble("Aantal lanes"));
//								}
//
//								// SampleAmount
//								if (rowp.getString("Aantal monsters") != null)
//								{
//									p.setSampleAmount(rowp.getInt("Aantal monsters"));
//								}
//
//								// DeclarationNr
//								if (rowp.getString("Declaratie nr") != null)
//								{
//									p.setDeclarationNr(rowp.getString("Declaratie nr"));
//								}
//							}
//						}
//					}
//					finally
//					{
//						if (readerp != null) readerp.close();
//					}
//					projects.put(p.getProjectName().toLowerCase().trim(), p);
//				}
//
//				// Sample
//				if (!row.isNull("internalSampleID"))
//				{
//					Sample s = new Sample();
//
//					// InternalId
//					if (row.getString("internalSampleID") != null)
//					{
//						s.setInternalId(row.getString("internalSampleID"));
//					}
//
//					// ExternalId
//					if (row.getString("externalSampleID") != null)
//					{
//						s.setExternalId(row.getString("externalSampleID"));
//					}
//
//					// SampleType
//					s.setSampleType("DNA"); // Question: Is this ok?
//
//					// SampleComment
//					if (row.getString("labStatusComments") != null)
//					{
//						s.setSampleComment(row.getString("labStatusComments"));
//					}
//
//					// ProjectId
//					if (row.getString("project") != null)
//					{
//						s.setProjectId_ProjectName(row.getString("project").toLowerCase().trim());
//					}
//
//					// ArrayFile
//					if (row.getString("arrayFile") != null)
//					{
//						s.setArrayFile(row.getString("arrayFile"));
//					}
//
//					// ArrayID
//					if (row.getString("arrayID") != null)
//					{
//						s.setArrayId(row.getString("arrayID"));
//					}
//
//					// LabStatus
//					if (row.getString("labStatusPhase") != null)
//					{
//						s.setLabStatus(row.getString("labStatusPhase"));
//					}
//
//					// PrepKit
//					if (row.getString("prepKit") != null)
//					{
//						if (row.getString("prepKit").trim().equalsIgnoreCase("none")
//								|| row.getString("prepKit").toLowerCase().trim().equals("_"))
//						{
//							s.setPrepKit_PrepKitName("None");
//						}
//						else
//						{
//							s.setPrepKit_PrepKitName(row.getString("prepKit").toUpperCase().trim());
//						}
//					}
//
//					// QcWetMet
//					if (row.getString("GAF_QC_Status") != null)
//					{
//						s.setQcWetMet(row.getString("GAF_QC_Status"));
//					}
//					else
//					{
//						s.setQcWetMet("Not determined");
//					}
//
//					// QcWetUser
//					if (row.getString("GAF_QC_Name") != null)
//					{
//						s.setQcWetUser_UserName(row.getString("GAF_QC_Name").toLowerCase().trim());
//					}
//
//					// QcWetDate
//					s.setQcWetDate(getFixedDate(row.getString("GAF_QC_Date")));
//
//					// QcDryMet
//					if (row.getString("GCC_QC_Status") != null)
//					{
//						s.setQcDryMet(row.getString("GCC_QC_Status"));
//					}
//					else
//					{
//						s.setQcDryMet("Not determined");
//					}
//
//					// QcDryUser
//					if (row.getString("GCC_QC_Name") != null)
//					{
//						s.setQcDryUser_UserName(row.getString("GCC_QC_Name").toLowerCase().trim());
//					}
//
//					// QcDryDate
//					s.setQcDryDate(getFixedDate(row.getString("GCC_QC_Date")));
//
//					samples.put(s.getInternalId(), s);
//				}
//
//				// FlowcellLaneSampleBarcode
//				if (!row.isNull("internalSampleID"))
//				{
//					// For each Lane
//					if (row.getString("lane") != null)
//					{
//						String[] lanes = row.getString("lane").split(",");
//						for (int i = 0; i < lanes.length; i++)
//						{
//							FlowcellLaneSampleBarcode flsb = new FlowcellLaneSampleBarcode();
//
//							flsb.setLane(lanes[i]);
//
//							// Sample
//							if (row.getString("internalSampleID") != null)
//							{
//								flsb.setSample_InternalId(row.getString("internalSampleID"));
//							}
//
//							// FlowcellLaneSampleBarcodeComment
//							if (row.getString("Comments") != null)
//							{
//								flsb.setFlowcellLaneSampleBarcodeComment(row.getString("Comments").replace("'", "`"));
//							}
//
//							// Flowcell
//							if (row.getString("flowcell") != null)
//							{
//								flsb.setFlowcell_FlowcellName(row.getString("flowcell"));
//							}
//
//							// SampleBarcode
//							if (row.getString("barcode") != null)
//							{
//								if (row.getString("barcode").toLowerCase().trim().equals("none")
//										|| row.getString("barcode").toLowerCase().trim().contains("error"))
//								{
//									flsb.setSampleBarcode_SampleBarcodeName("None");
//								}
//								else
//								{
//									flsb.setSampleBarcode_SampleBarcodeName(row.getString("barcode").toUpperCase()
//											.trim());
//								}
//							}
//							else
//							{
//								flsb.setSampleBarcode_SampleBarcodeName("None");
//							}
//
//							// CapturingKit
//							if (row.getString("capturingKit") != null)
//							{
//								if (row.getString("capturingKit").toLowerCase().trim().equals("none"))
//								{
//									flsb.setCapturingKit_CapturingKitName("None");
//								}
//								else
//								{
//									flsb.setCapturingKit_CapturingKitName(row.getString("capturingKit").trim());
//								}
//							}
//							else
//							{
//								flsb.setCapturingKit_CapturingKitName("None");
//							}
//
//							List<FlowcellLaneSampleBarcode> flsbList = flowcellLaneSampleBarcodes.get(flsb
//									.getSample_InternalId());
//							if (flsbList == null)
//							{
//								flsbList = new ArrayList<FlowcellLaneSampleBarcode>();
//								flowcellLaneSampleBarcodes.put(flsb.getSample_InternalId(), flsbList);
//							}
//							flsbList.add(flsb);
//						}
//					}
//				}
//			}
//		}
//		finally
//		{
//			if (readers != null) readers.close();
//		}
//
//		// Show all collected values
//		for (List<FlowcellLaneSampleBarcode> flsbList : flowcellLaneSampleBarcodes.values())
//		{
//			for (FlowcellLaneSampleBarcode flsb : flsbList)
//			{
//				logger.info(flsb);
//			}
//		}
//		for (Sample s : samples.values())
//		{
//			logger.info(s);
//		}
//		for (Project p : projects.values())
//		{
//			logger.info(p);
//		}
//		for (NgsUser u : users.values())
//		{
//			logger.info(u);
//		}
//		for (SampleBarcode sb : sampleBarcodes.values())
//		{
//			logger.info(sb);
//		}
//		for (CapturingKit ck : capturingKits.values())
//		{
//			logger.info(ck);
//		}
//		for (PrepKit pk : prepKits.values())
//		{
//			logger.info(pk);
//		}
//		for (Machine m : machines.values())
//		{
//			logger.info(m);
//		}
//		for (Flowcell f : flowcells.values())
//		{
//			logger.info(f);
//		}
//
//		// Put values in database
//		Database db = new app.JpaDatabase();
//		try
//		{
//			db.beginTx();
//
//			// Clean old stuff (reverse order of foreign keys)
//			if (debug)
//			{
//				db.remove(db.find(FlowcellLaneSampleBarcode.class));
//				db.remove(db.find(Sample.class));
//				db.remove(db.find(Project.class));
//				db.remove(db.find(Flowcell.class));
//				db.remove(db.find(Machine.class));
//				db.remove(db.find(PrepKit.class));
//				db.remove(db.find(CapturingKit.class));
//				db.remove(db.find(SampleBarcode.class));
//				db.remove(db.find(NgsUser.class));
//			}
//
//			logger.info("Imported " + db.add(new ArrayList<NgsUser>(users.values())) + " NgsUser(s)");
//			logger.info("Imported " + db.add(new ArrayList<SampleBarcode>(sampleBarcodes.values()))
//					+ " SampleBarcode(s)");
//			logger.info("Imported " + db.add(new ArrayList<CapturingKit>(capturingKits.values())) + " CapturingKit(s)");
//			logger.info("Imported " + db.add(new ArrayList<PrepKit>(prepKits.values())) + " PrepKit(s)");
//			logger.info("Imported " + db.add(new ArrayList<Machine>(machines.values())) + " Machine(s)");
//			logger.info("Imported " + db.add(new ArrayList<Flowcell>(flowcells.values())) + " Flowcell(s)");
//			logger.info("Imported " + db.add(new ArrayList<Project>(projects.values())) + " Project(s)");
//			logger.info("Imported " + db.add(new ArrayList<Sample>(samples.values())) + " Sample(s)");
//			// logger.info("Imported "
//			// + db.add(new
//			// ArrayList<FlowcellLaneSampleBarcode>(flowcellLaneSampleBarcodes.values()))
//			// + " FlowcellLaneSampleBarcode(s)");
//
//			// Debug
//			boolean crash = false;
//			int sampleRecordCounter = 0;
//			for (List<FlowcellLaneSampleBarcode> flsbList : flowcellLaneSampleBarcodes.values())
//			{
//				try
//				{
//					db.add(flsbList);
//					sampleRecordCounter += flsbList.size();
//				}
//				catch (Exception ex)
//				{
//					logger.error("error:" + ex.toString() + ". Value: " + StringUtils.join(flsbList, ","));
//					crash = true;
//				}
//				// logger.info(flsb);
//			}
//			if (!crash)
//			{
//				logger.info("Imported " + sampleRecordCounter + " FlowcellLaneSampleBarcode(s)");
//			}
//
//			db.commitTx();
//			db.close();
//
//			logger.info("Import completed succesfully.");
//		}
//		catch (Exception e)
//		{
//			db.rollbackTx();
//			e.printStackTrace();
//		}
//	}
//
//	private static Date getFixedDate(String s)
//	{
//		Date date = null;
//
//		try
//		{
//			if (s != null)
//			{
//				if (!s.toLowerCase().trim().equals("unknown"))
//				{
//					char[] d = s.trim().toCharArray();
//					if (d.length == 8)
//					{
//						date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(d[0] + "" + d[1] + "" + d[2]
//								+ "" + d[3] + "/" + d[4] + "" + d[5] + "/" + d[6] + "" + d[7]);
//					}
//					if (d.length == 6)
//					{
//						date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse("20" + d[0] + "" + d[1] + "/"
//								+ d[2] + "" + d[3] + "/" + d[4] + "" + d[5]);
//					}
//					if (d.length == 5)
//					{
//						date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse("20" + d[0] + "" + d[1] + "/0"
//								+ d[2] + "/" + d[3] + "" + d[4]);
//					}
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//		return date;
//	}
// }
