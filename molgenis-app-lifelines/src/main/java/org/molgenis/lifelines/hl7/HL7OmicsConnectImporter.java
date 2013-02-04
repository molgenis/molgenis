package org.molgenis.lifelines.hl7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.lifelines.hl7.lra.HL7ObservationLRA;
import org.molgenis.lifelines.hl7.lra.HL7OrganizerLRA;
import org.molgenis.lifelines.hl7.lra.HL7ValueSetAnswerLRA;
import org.molgenis.lifelines.hl7.lra.HL7ValueSetLRA;

public class HL7OmicsConnectImporter
{

	// private Protocol protocol;

	// private ObservableFeature feature;

	/** make a dataset named 'datasetName', if it is not already existing * */
	private DataSet findDataSet(Database db, String datasetName) throws DatabaseException
	{
		DataSet dataset = new DataSet();
		if (db.find(DataSet.class, new QueryRule(DataSet.NAME, Operator.EQUALS, datasetName)).size() == 0)
		{
			dataset.setName(datasetName);
			dataset.setIdentifier(datasetName);
			db.add(dataset);
		}
		else
		{
			dataset = db.find(DataSet.class, new QueryRule(DataSet.NAME, Operator.EQUALS, datasetName)).get(0);
		}

		return dataset;
	}

	/** make a Protocol named 'protName', if it is not already existing * */
	private Protocol makeProtocol(Database db, DataSet dataSet, String protName) throws DatabaseException
	{
		Protocol protocol;
		if (db.find(Protocol.class, new QueryRule(Protocol.NAME, Operator.EQUALS, protName)).size() == 0)
		{
			protocol = new Protocol();
			protocol.setIdentifier(protName);
			protocol.setName(protName);

			dataSet.setProtocolUsed_Identifier(protName);
			db.add(protocol);
			db.update(dataSet);

		}
		else
		{
			protocol = db.find(Protocol.class, new QueryRule(Protocol.NAME, Operator.EQUALS, protName)).get(0);

		}
		return protocol;
	}

	private ObservableFeature checkFeatures(Database db, DataSet dataSet, HL7ObservationLRA meas, Protocol subP)
			throws DatabaseException
	{
		ObservableFeature obsFeature;
		if (db.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.NAME, Operator.EQUALS, meas.getMeasurementName())).size() == 1)
		{
			obsFeature = db.find(ObservableFeature.class,
					new QueryRule(ObservableFeature.NAME, Operator.EQUALS, meas.getMeasurementName())).get(0);
		}
		else
		{
			obsFeature = new ObservableFeature();
			// set the description/label
			obsFeature.setDescription(meas.getMeasurementLabel());
			obsFeature.setName(meas.getMeasurementName());
			obsFeature.setIdentifier(subP.getIdentifier() + "." + meas.getMeasurementName());
			// set the datatype
			String dataType = meas.getMeasurementDataType();
			obsFeature.setDataType(setFeatureDataType(dataType));

			db.add(obsFeature);

		}

		return obsFeature;
	}

	private Protocol makeSubProtocols(Database db, DataSet dataset, String protName) throws DatabaseException
	{
		Protocol subProtocol;
		if (db.find(Protocol.class, new QueryRule(Protocol.NAME, Operator.EQUALS, protName)).size() == 0)
		{
			subProtocol = new Protocol();
			subProtocol.setIdentifier(protName);
			subProtocol.setName(protName);
		}
		else
		{
			subProtocol = db.find(Protocol.class, new QueryRule(Protocol.NAME, Operator.EQUALS, protName)).get(0);
		}
		return subProtocol;

	}

	private void makeCategory(Database db, ObservableFeature obsFeature, String categoryName, String codeValue,
			int teller) throws DatabaseException
	{
		Category c = new Category();
		c.setName(categoryName);
		c.setIdentifier(obsFeature.getName() + "_" + categoryName + "_" + teller);
		c.setValueCode(codeValue);
		c.setDescription(categoryName);
		c.setObservableFeature(obsFeature);
		db.add(c);

	}

	/** Select which datatype to use */
	private String setFeatureDataType(String dataType)
	{

		String datatype = "";
		if (!dataType.equals("CO") && !dataType.equals("INT"))
		{
			System.out.println();
		}
		if (dataType.equals("INT"))
		{
			datatype = "int";
		}
		else if (dataType.equals("ST"))
		{
			datatype = "string";
		}
		else if (dataType.equals("CO"))
		{
			datatype = "categorical";
		}
		else if (dataType.equals("CD"))
		{
			datatype = "code";
		}
		else if (dataType.equals("PQ"))
		{
			datatype = "decimal";
		}
		else if (dataType.equals("TS"))
		{
			datatype = "datetime";
		}
		else if (dataType.equals("REAL"))
		{
			datatype = "decimal";
		}
		else if (dataType.equals("BL"))
		{
			datatype = "bool";
		}
		else
		{
			datatype = "string";
		}
		return datatype;

	}

	public void start(HL7Data ll, Database db) throws Exception
	{
		try
		{

			db.beginTx();

			String datasetName = "LifeLines";
			DataSet dataset = findDataSet(db, datasetName);

			/**
			 * make a protocol named (name is in last column), if it is not
			 * already existing *
			 */

			Protocol protocol = makeProtocol(db, dataset, "stageCatalogue");

			HashMap<String, HL7ValueSetLRA> hashValueSetLRA = ll.getHashValueSetLRA();
			int teller = 0;
			List<Protocol> uniqueProtocol = new ArrayList<Protocol>();
			List<String> uniqueListOfProtocolName = new ArrayList<String>();

			/**
			 * Every HL7Organizer object is 1 subprotocol of stageCatalogue
			 * protocol
			 * 
			 * Make a subprotocol with a list of all the measurements of that
			 * protocol and all the categories that belong to the measurement
			 */
			for (HL7OrganizerLRA organizer : ll.getHL7OrganizerLRA())
			{

				System.out.println(organizer.getHL7OrganizerNameLRA());
				String protocolName = organizer.getHL7OrganizerNameLRA().trim();

				Protocol subProtocol = makeSubProtocols(db, dataset, protocolName);

				List<String> listProtocolFeatures = new ArrayList<String>();
				List<Integer> listProtocolFeaturesId = new ArrayList<Integer>();

				/** Every HL7Observation is an ObservableFeature */

				for (HL7ObservationLRA meas : organizer.measurements)
				{

					ObservableFeature feat = checkFeatures(db, dataset, meas, subProtocol);

					listProtocolFeatures.add(feat.getIdentifier());
					listProtocolFeaturesId.add(feat.getId());

					if (hashValueSetLRA.containsKey(protocolName + "." + meas.getMeasurementName().trim()))
					{

						HL7ValueSetLRA valueSetLRA = hashValueSetLRA
								.get(protocolName + "." + meas.getMeasurementName());

						for (HL7ValueSetAnswerLRA eachAnswer : valueSetLRA.getListOFAnswers())
						{
							teller++;
							String codeValue = eachAnswer.getCodeValue();
							String categoryName = eachAnswer.getName().trim().toLowerCase();
							makeCategory(db, feat, categoryName, codeValue, teller);
						}
					}
				}

				List<String> uniqueList = new ArrayList<String>();
				for (String each : listProtocolFeatures)
				{
					if (!uniqueList.contains(each))
					{
						uniqueList.add(each);
					}
					else
					{
						System.out.println("............................>" + each);
					}
				}
				subProtocol.setFeatures_Identifier(uniqueList);

				uniqueProtocol.add(subProtocol);

				uniqueListOfProtocolName.add(protocolName);

			}

			db.add(uniqueProtocol);

			protocol.setSubprotocols_Identifier(uniqueListOfProtocolName);

			db.update(protocol);

			db.commitTx();
		}
		catch (Exception e)
		{
			db.rollbackTx();
			e.printStackTrace();
		}

	}

	/**
	 * adding OntologyTerm is a part of the adding of the GenericDCM part in the
	 * LifeLines project
	 */
	public List<Integer> addingOntologyTerm(List<HL7OntologyTerm> listOfHL7OntologyTerms, Database db) throws Exception
	{

		List<Integer> listOfOntologyTermIDs = new ArrayList<Integer>();

		for (HL7OntologyTerm t : listOfHL7OntologyTerms)
		{

			String codeSystemName = t.getCodeSystemName();

			if (t.getCodeSystemName().toLowerCase().startsWith("snomed")
					|| t.getCodeSystemName().equalsIgnoreCase("sct"))
			{
				codeSystemName = "SCT";
			}

			Ontology ot = new Ontology();

			if (db.find(Ontology.class, new QueryRule(Ontology.ONTOLOGYACCESSION, Operator.EQUALS, t.getCodeSystem()))
					.size() == 0)
			{

				ot.setName(codeSystemName);
				ot.setOntologyAccession(t.getCodeSystem());
				db.add(ot);

			}
			else
			{
				ot = db.find(Ontology.class,
						new QueryRule(Ontology.ONTOLOGYACCESSION, Operator.EQUALS, t.getCodeSystem())).get(0);
			}

			Query<OntologyTerm> q = db.query(OntologyTerm.class);
			q.addRules(new QueryRule(OntologyTerm.TERMACCESSION, Operator.EQUALS, t.getCode()));
			q.addRules(new QueryRule(OntologyTerm.NAME, Operator.EQUALS, codeSystemName));

			OntologyTerm ont = new OntologyTerm();

			if (q.find().size() == 0)
			{

				ont.setOntology_Id(ot.getId());
				ont.setName(t.getDisplayName());
				ont.setTermAccession(t.getCode());
				db.add(ont);

			}
			else
			{
				ont = q.find().get(0);
			}

			listOfOntologyTermIDs.add(ont.getId());

			System.out.println("The mapped ontology term is " + t.getDisplayName() + "\t" + t.getCode());
		}

		return listOfOntologyTermIDs;
	}

}
