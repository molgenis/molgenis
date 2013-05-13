package org.molgenis.cbm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.EasyPluginController;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.jaxb.CbmNode;
import org.molgenis.jaxb.CollectionProtocol;
import org.molgenis.jaxb.Diagnosis;
import org.molgenis.jaxb.ParticipantCollectionSummary;
import org.molgenis.jaxb.Race;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.KeyValueTuple;

public class CbmToOmxConverter extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private File currentFile;
	private final File outputDir = new File(System.getProperty("java.io.tmpdir"));
	private final List<String> listFiles = Arrays.asList("dataset.csv", "dataset_cbm.csv", "protocol.csv",
			"observablefeature.csv");

	public CbmToOmxConverter(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return CbmToOmxConverter.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + CbmToOmxConverter.class.getName().replace('.', '/') + ".ftl";
	}

	@SuppressWarnings("resource")
	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws Exception
	{
		if (request.getString("__action").equals("upload"))
		{
			// get uploaded file and do checks
			File file = request.getFile("upload");

			if (file == null)
			{
				throw new Exception("No file selected.");
			}
			else if (!file.getName().endsWith(".xml"))
			{
				throw new Exception("File does not end with '.xml', other formats are not supported.");
			}
			// Create the files needed
			CsvWriter dataCBM = new CsvWriter(new File(outputDir, "dataset_cbm.csv"));
			CsvWriter observableFeature = new CsvWriter(new File(outputDir, "observablefeature.csv"));
			CsvWriter dataSet = new CsvWriter(new File(outputDir, "dataset.csv"));
			CsvWriter protocol = new CsvWriter(new File(outputDir, "protocol.csv"));

			try
			{

				// DATASET PART
				dataSet.writeColNames(Arrays.asList("identifier", "name", "protocolused_identifier"));
				KeyValueTuple kvtdataSet = new KeyValueTuple();
				kvtdataSet.set("identifier", "cbm");
				kvtdataSet.set("name", "cbm");
				kvtdataSet.set("protocolused_identifier", "cbm_protocol");
				dataSet.write(kvtdataSet);
				// END DATASET PART

				// get uploaded file and do checks

				File currentXsdfile = new File(this.getClass().getResource("/schemas/CBM.xsd").getFile());
				// if no error, set file, and continue
				this.setCurrentFile(file);
				// Here the actual data is going to be imported.
				CbmXmlParser cbmXmlParser = new CbmXmlParser();

				CbmNode result = cbmXmlParser.load(currentFile, currentXsdfile);

				List<String> listOfCollectionProtocolFeatures = new ArrayList<String>();

				// List for the protocol CollectionProtocol
				List<String> listOfAllCollectionProtocolFeatures = Arrays.asList("row_identifier",
						"isCollaborationRequired", "isAvailableToOutsideInstitution",
						"isAvailableToForeignInvestigators", "isAvailableToCommercialOrganizations",
						"hasTreatmentInformation", "hasParticipantsAvailableForFollowup", "hasOutcomeInformation",
						"hasMatchedSpecimens", "hasLongitudinalSpecimens", "hasLabData",
						"hasHistopathologicInformation", "hasFamilyHistory", "hasExposureHistory",
						"hasAdditionalPatientDemographics", "emailAddress", "lastName", "firstName", "fullName",
						"streetOrThoroughfareNameAndType", "state", "zipCode", "country", "city");

				// List of features of the protocol participant protocol
				List<String> listOfParticipantFeatures = Arrays.asList("pcs", "gender", "ethnicity", "race",
						"specimenId", "specimenType", "specimenCount", "anatomicSource", "patientAgeGroupLow",
						"patientAgeGroupHigh", "diagnosis");

				// List of all features for the ObservableFeature
				List<String> listOfAllFeatures = Arrays.asList("pcs", "gender", "ethnicity", "race", "specimenId",
						"specimenType", "specimenCount", "anatomicSource", "patientAgeGroupLow", "patientAgeGroupHigh",
						"diagnosis", "row_identifier", "isCollaborationRequired", "isAvailableToOutsideInstitution",
						"isAvailableToForeignInvestigators", "isAvailableToCommercialOrganizations",
						"hasTreatmentInformation", "hasParticipantsAvailableForFollowup", "hasOutcomeInformation",
						"hasMatchedSpecimens", "hasLongitudinalSpecimens", "hasLabData",
						"hasHistopathologicInformation", "hasFamilyHistory", "hasExposureHistory",
						"hasAdditionalPatientDemographics", "emailAddress", "lastName", "firstName", "fullName",
						"streetOrThoroughfareNameAndType", "state", "zipCode", "country", "city");

				dataCBM.writeColNames(listOfAllFeatures);

				// FEATURE PART
				observableFeature.writeColNames(Arrays.asList("identifier", "name"));
				KeyValueTuple kvtFeature = new KeyValueTuple();

				for (String feature : listOfAllFeatures)
				{
					kvtFeature.set("identifier", feature);
					kvtFeature.set("name", feature);
					listOfCollectionProtocolFeatures.add(feature);
					observableFeature.write(kvtFeature);
				}
				// END FEATURE PART

				StringBuilder collectionProtFeatures = new StringBuilder();
				// make enroll features StringBuilder
				for (String feature : listOfAllCollectionProtocolFeatures)
				{
					collectionProtFeatures.append(feature + ",");
				}
				String collectionFeatures = collectionProtFeatures.substring(0, collectionProtFeatures.length() - 1);

				StringBuilder enrollsFeatures = new StringBuilder();
				// make enroll features StringBuilder
				for (String feature : listOfParticipantFeatures)
				{
					enrollsFeatures.append(feature + ",");
				}
				String ParticipantFeatures = enrollsFeatures.substring(0, enrollsFeatures.length() - 1);

				protocol.writeColNames(Arrays.asList("identifier", "name", "features_identifier",
						"subprotocols_identifier"));

				// Big protocol containing both the subprotocols
				KeyValueTuple protocolFeat_kvt = new KeyValueTuple();
				protocolFeat_kvt.set("identifier", "cbm_protocol");
				protocolFeat_kvt.set("name", "cbm_protocol");
				protocolFeat_kvt.set("subprotocols_identifier", "collection_prot,participant_prot");
				protocol.write(protocolFeat_kvt);

				// create protocol collectionProtocols , containing a list of
				// features (allFeat) and the subprotocol is pcs
				protocolFeat_kvt = new KeyValueTuple();
				protocolFeat_kvt.set("identifier", "collection_prot");
				protocolFeat_kvt.set("name", "collection_prot");
				protocolFeat_kvt.set("features_identifier", collectionFeatures);
				protocol.write(protocolFeat_kvt);
				// create protocol collectionProtocols , containing a list of
				// features (allFeat) and the subprotocol is pcs
				protocolFeat_kvt = new KeyValueTuple();
				protocolFeat_kvt.set("identifier", "participant_prot");
				protocolFeat_kvt.set("name", "participant_prot");
				protocolFeat_kvt.set("features_identifier", ParticipantFeatures);
				protocol.write(protocolFeat_kvt);

				// END PROTOCOL PART

				for (CollectionProtocol collectionProtocolFromJaxb : result.getProtocols().getCollectionProtocol())
				{

					// Create for every collectionprotocol a new dataset
					String collectionIdentifier = collectionProtocolFromJaxb.getIdentifier() != null ? collectionProtocolFromJaxb
							.getIdentifier() : collectionProtocolFromJaxb.getId().toString();

					for (ParticipantCollectionSummary pcsummary : collectionProtocolFromJaxb.getEnrolls()
							.getParticipantCollectionSummary())
					{

						KeyValueTuple kvtdataSetCBM = new KeyValueTuple();

						kvtdataSetCBM.set("pcs", "pcs_id_" + pcsummary.getId());
						kvtdataSetCBM.set("gender", pcsummary.getGender());
						kvtdataSetCBM.set("ethnicity", pcsummary.getEthnicity());

						StringBuilder raceStringBuilder = new StringBuilder();

						for (Race race : pcsummary.getIsClassifiedBy().getRace())
						{
							raceStringBuilder.append(race.getRace() + ",");

						}
						if (pcsummary.getIsClassifiedBy().getRace().size() > 1)
						{

							String raceString = raceStringBuilder.substring(0, raceStringBuilder.length() - 1);
							kvtdataSetCBM.set("race", raceString);
						}

						kvtdataSetCBM.set("enrolls_row_identifier", "enrolls_row_identifier" + collectionIdentifier);

						if (pcsummary.getProvides().getSpecimenCollectionSummary().size() > 1)
						{
							throw new IOException("More than one SpecimenCollectionSummary found for PCS "
									+ pcsummary.getId());

						}

						kvtdataSetCBM.set("specimenId", pcsummary.getProvides().getSpecimenCollectionSummary().get(0)
								.getId().toString());
						kvtdataSetCBM.set("specimenCount", pcsummary.getProvides().getSpecimenCollectionSummary()
								.get(0).getSpecimenCount().toString());
						kvtdataSetCBM.set("specimenType", pcsummary.getProvides().getSpecimenCollectionSummary().get(0)
								.getSpecimenType().toString());
						kvtdataSetCBM.set("anatomicSource",
								pcsummary.getProvides().getSpecimenCollectionSummary().get(0).getAnatomicSource()
										.toString());
						kvtdataSetCBM.set("patientAgeGroupLow", pcsummary.getProvides().getSpecimenCollectionSummary()
								.get(0).getQualifiesPatientAgeAtSpecimenCollection().getPatientAgeGroupLow());
						kvtdataSetCBM.set("patientAgeGroupHigh", pcsummary.getProvides().getSpecimenCollectionSummary()
								.get(0).getQualifiesPatientAgeAtSpecimenCollection().getPatientAgeGroupHigh());

						StringBuilder diagnosisStringBuilder = new StringBuilder();

						for (Diagnosis dia : pcsummary.getReceives().getDiagnosis())
						{
							diagnosisStringBuilder.append(dia.getDiagnosisType() + ",");

						}
						if (pcsummary.getReceives().getDiagnosis().size() >= 1)
						{

							String diagnosisString = diagnosisStringBuilder.substring(0,
									diagnosisStringBuilder.length() - 1);
							kvtdataSetCBM.set("diagnosis", diagnosisString);
						}

						kvtdataSetCBM.set("row_identifier", collectionIdentifier);

						kvtdataSetCBM.set("isCollaborationRequired", collectionProtocolFromJaxb.getIsConstrainedBy()
								.isIsCollaborationRequired().toString());

						kvtdataSetCBM.set("isAvailableToOutsideInstitution", collectionProtocolFromJaxb
								.getIsConstrainedBy().isIsAvailableToOutsideInstitution().toString());

						kvtdataSetCBM.set("isAvailableToForeignInvestigators", collectionProtocolFromJaxb
								.getIsConstrainedBy().isIsAvailableToForeignInvestigators().toString());

						kvtdataSetCBM.set("isAvailableToCommercialOrganizations", collectionProtocolFromJaxb
								.getIsConstrainedBy().isIsAvailableToCommercialOrganizations().toString());

						kvtdataSetCBM.set("hasOutcomeInformation", collectionProtocolFromJaxb.getMakesAvailable()
								.isHasOutcomeInformation().toString());

						kvtdataSetCBM.set("hasMatchedSpecimens", collectionProtocolFromJaxb.getMakesAvailable()
								.isHasMatchedSpecimens().toString());

						kvtdataSetCBM.set("hasTreatmentInformation", collectionProtocolFromJaxb.getMakesAvailable()
								.isHasTreatmentInformation().toString());

						kvtdataSetCBM.set("hasHistopathologicInformation", collectionProtocolFromJaxb
								.getMakesAvailable().isHasHistopathologicInformation().toString());

						kvtdataSetCBM.set("hasParticipantsAvailableForFollowup", collectionProtocolFromJaxb
								.getMakesAvailable().isHasParticipantsAvailableForFollowup().toString());

						kvtdataSetCBM.set("hasLongitudinalSpecimens", collectionProtocolFromJaxb.getMakesAvailable()
								.isHasLongitudinalSpecimens().toString());

						kvtdataSetCBM.set("hasLabData", collectionProtocolFromJaxb.getMakesAvailable().isHasLabData()
								.toString());

						kvtdataSetCBM.set("hasFamilyHistory", collectionProtocolFromJaxb.getMakesAvailable()
								.isHasFamilyHistory().toString());

						kvtdataSetCBM.set("hasExposureHistory", collectionProtocolFromJaxb.getMakesAvailable()
								.isHasExposureHistory().toString());

						kvtdataSetCBM.set("hasAdditionalPatientDemographics", collectionProtocolFromJaxb
								.getMakesAvailable().isHasAdditionalPatientDemographics().toString());

						kvtdataSetCBM.set("emailAddress", collectionProtocolFromJaxb.getIsAssignedTo()
								.getEmailAddress().toString());

						kvtdataSetCBM.set("lastName", collectionProtocolFromJaxb.getIsAssignedTo().getLastName()
								.toString());

						kvtdataSetCBM.set("firstName", collectionProtocolFromJaxb.getIsAssignedTo().getFirstName()
								.toString());

						kvtdataSetCBM.set("fullName", collectionProtocolFromJaxb.getIsAssignedTo().getFullName()
								.toString());

						kvtdataSetCBM.set("streetOrThoroughfareNameAndType", collectionProtocolFromJaxb
								.getIsAssignedTo().getIsLocatedAt().getStreetOrThoroughfareNameAndType().toString());

						kvtdataSetCBM.set("state", collectionProtocolFromJaxb.getIsAssignedTo().getIsLocatedAt()
								.getState().toString());

						kvtdataSetCBM.set("zipCode", collectionProtocolFromJaxb.getIsAssignedTo().getIsLocatedAt()
								.getZipCode().toString());

						kvtdataSetCBM.set("country", collectionProtocolFromJaxb.getIsAssignedTo().getIsLocatedAt()
								.getCountry().toString());

						kvtdataSetCBM.set("city", collectionProtocolFromJaxb.getIsAssignedTo().getIsLocatedAt()
								.getCity().toString());

						// write participant file
						dataCBM.write(kvtdataSetCBM);

					}

				}

			}
			finally
			{
				IOUtils.closeQuietly(dataCBM);
				IOUtils.closeQuietly(protocol);

				IOUtils.closeQuietly(dataSet);
				IOUtils.closeQuietly(observableFeature);
			}

			File zipFile = new File(outputDir, "cbm.zip");

			File[] sourceFiles =
			{ new File(outputDir, "dataset.csv"), new File(outputDir, "dataset_cbm.csv"),
					new File(outputDir, "protocol.csv"), new File(outputDir, "observablefeature.csv") };

			FileOutputStream fout = new FileOutputStream(zipFile);
			ZipOutputStream zout = new ZipOutputStream(fout);

			for (File f : sourceFiles)
			{

				System.out.println("Adding " + f.getAbsolutePath());
				FileInputStream fin = new FileInputStream(f);
				zout.putNextEntry(new ZipEntry(f.getName()));
				byte[] b = new byte[1024];
				int length;
				while ((length = fin.read(b)) > 0)
				{
					zout.write(b, 0, length);
				}
				zout.closeEntry();
				fin.close();
			}
			zout.close();
			fout.close();

			URL localURL = zipFile.toURI().toURL();
			URLConnection conn = localURL.openConnection();
			InputStream in = new BufferedInputStream(conn.getInputStream());

			String mimetype = request.getRequest().getServletContext().getMimeType(zipFile.getName());
			if (mimetype != null) request.getResponse().setContentType(mimetype);

			request.getResponse()
					.setHeader("Content-disposition", "attachment; filename=\"" + zipFile.getName() + "\"");

			if (zipFile.length() > Integer.MAX_VALUE)
			{
				throw new Exception("Zip file too big to be handled by webserver");
			}
			request.getResponse().setContentLength((int) zipFile.length());
			OutputStream out = request.getResponse().getOutputStream();

			byte[] buffer = new byte[1024];
			for (;;)
			{
				int nBytes = in.read(buffer);
				if (nBytes <= 0) break;
				out.write(buffer, 0, nBytes);
			}
			out.flush();

			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);

			logger.info("serving " + request.getRequest().getRequestURI());

			// FIXME: This is a hack, fix asap
			EasyPluginController.HTML_WAS_ALREADY_SERVED = true;
		}
	}

	private void setCurrentFile(File file)
	{
		this.currentFile = file;
	}

	@Override
	public void reload(Database db)
	{

	}

	public File getOutputDir()
	{
		return outputDir;
	}

	public List<String> getListFiles()
	{
		return listFiles;
	}
}
