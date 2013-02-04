package org.molgenis.lifelines.hl7;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.DatabaseFactory;

public class XMLReader
{

	Node node = null;
	Database db = null;

	public static void main(String args[])
	{
		XMLReader test = new XMLReader();
		try
		{
			test.run();
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	public Node nodeFunction(String xmlCode, String molgenisCode)
	{
		if (node.getNodeName().equals(xmlCode))
		{
			if (!xmlCode.equals(""))
			{
				System.out.println(molgenisCode + "" + node.getAttributes().getNamedItem(xmlCode).getNodeValue());
			}
			return node;
		}
		return node;
	}

	public void run() throws DatabaseException
	{

		try
		{

			// Get a fresh new database object
			this.db = DatabaseFactory.create();

			// Begin transaction if anything goes wrong, can always roll back
			// and the database won`t be screwed up
			db.beginTx();

			// Load the XML file in the memory
			String path = "/Users/pc_iverson/Desktop/Input/";
			File file = new File(path + "StageCatalog.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			Document doc = documentBuilder.parse(file);
			doc.getDocumentElement().normalize();

			// Loop through the "organizer" to collect information on the
			// protocolName and
			// ObservableFeatureName and its fields!

			NodeList nodeLst = doc.getElementsByTagName("organizer");
			for (int i = 0; i < nodeLst.getLength(); i++)
			{

				// Get each "organizer" entity
				Node eachNode = nodeLst.item(i);

				// Probably the List size is 1 cos there is only one code entity
				// in the direct child of the organizer entity
				List<String> protocolNameNode = getAttributeFromEntity(eachNode, "code", "code", 1);

				List<Node> measurementNode = getChildNodes(eachNode, "observation", 2);

				transformToObservableFeature(protocolNameNode.get(0), measurementNode);

			}

			// commit all the changes in the database
			db.commitTx();

		}
		catch (Exception e)
		{

			// if anything goes wrong, roll back to the previous state
			db.rollbackTx();
			e.printStackTrace();
		}

	}

	/**
	 * This method is to create the Protocol and its corresponding
	 * ObservableFeatures and add them to the db.
	 * 
	 * @param protocolName
	 * @param measurementNode
	 * @throws DatabaseException
	 */
	private void transformToObservableFeature(String protocolName, List<Node> measurementNode) throws DatabaseException
	{

		List<String> listOfObservableFeatureName = new ArrayList<String>();

		Protocol p = new Protocol();

		p.setName(protocolName);

		// p.setInvestigation(inv); // TODO can we delete this?

		List<ObservableFeature> listOfObservableFeatures = new ArrayList<ObservableFeature>();

		for (Node eachNode : measurementNode)
		{

			List<String> nameOfObservableFeature = getAttributeFromEntity(eachNode, "code", "code", 1);

			List<String> description = getAttributeFromEntity(eachNode, "originalText", "", 2);

			List<String> dataType = getAttributeFromEntity(eachNode, "value", "xsi:type", 1);

			ObservableFeature m = new ObservableFeature();

			m.setName(nameOfObservableFeature.get(0));

			m.setDescription(description.get(0));

			if (dataType.get(0).equals("INT"))
			{
				m.setDataType("int");
			}
			else if (dataType.get(0).equals("ST"))
			{
				m.setDataType("string");
			}
			else if (dataType.get(0).equals("TS"))
			{
				m.setDataType("datetime");
			}

			System.out.println(dataType.get(0));

			// m.setInvestigation(inv); // TODO can we delete this?

			listOfObservableFeatures.add(m);

			listOfObservableFeatureName.add(m.getName());

		}

		db.update(listOfObservableFeatures, DatabaseAction.ADD_IGNORE_EXISTING, ObservableFeature.NAME);

		listOfObservableFeatures = db.find(ObservableFeature.class, new QueryRule(ObservableFeature.NAME, Operator.IN,
				listOfObservableFeatureName));

		List<Integer> listOfObservableFeatureId = new ArrayList<Integer>();

		for (ObservableFeature m : listOfObservableFeatures)
		{
			listOfObservableFeatureId.add(m.getId());
		}

		p.setFeatures_Id(listOfObservableFeatureId);

		if (db.find(Protocol.class, new QueryRule(Protocol.NAME, Operator.EQUALS, protocolName)).size() == 0)
		{

			db.add(p);

		}
		else
		{
			db.update(p);
		}
	}

	/**
	 * This method is to get the specified entities inside the current node. We
	 * can specify how many levels down we want to go to.
	 * 
	 * @param currentEntity
	 * @param subNodeName
	 * @param level
	 * @return
	 */
	public List<Node> getChildNodes(Node currentEntity, String subNodeName, int level)
	{

		List<Node> listOfSubNodes = new ArrayList<Node>();

		for (int x = 0; x < currentEntity.getChildNodes().getLength(); x++)
		{

			Node subNode = currentEntity.getChildNodes().item(x);

			if (level == 1)
			{

				if (subNode.getNodeName().equals(subNodeName))
				{

					listOfSubNodes.add(subNode);
					// System.out.println(subNode.getAttributes().getNamedItem(attributeName).getNodeValue());
					// System.out.println(subNode.getTextContent());
				}

			}
			else
			{
				int nextLevel = level - 1;
				List<Node> temp = getChildNodes(subNode, subNodeName, nextLevel);
				listOfSubNodes.addAll(temp);
			}
		}

		return listOfSubNodes;
	}

	/**
	 * This method is to get the specified attribute from the specified
	 * entities. We can specify how many levels down we want to go to.
	 * 
	 * @param currentEntity
	 * @param subNodeName
	 * @param attributeName
	 * @param level
	 * @return
	 */
	public List<String> getAttributeFromEntity(Node currentEntity, String subNodeName, String attributeName, int level)
	{

		List<String> listOfSubNodes = new ArrayList<String>();

		for (int x = 0; x < currentEntity.getChildNodes().getLength(); x++)
		{

			Node subNode = currentEntity.getChildNodes().item(x);

			if (level == 1)
			{

				if (subNode.getNodeType() == Node.ELEMENT_NODE)
				{

					Element element = (Element) subNode;

					if (element.getNodeName().equals(subNodeName))
					{

						if (attributeName.equals(""))
						{

							listOfSubNodes.add(element.getTextContent());

						}
						else if (element.hasAttribute(attributeName))
						{

							listOfSubNodes.add(element.getAttribute(attributeName));

							// System.out.println("The attribute is " +
							// element.getAttribute(attributeName));

						}
					}
				}

			}
			else
			{
				int nextLevel = level - 1;
				List<String> temp = getAttributeFromEntity(subNode, subNodeName, attributeName, nextLevel);
				listOfSubNodes.addAll(temp);
			}
		}

		return listOfSubNodes;
	}
}
