package org.molgenis.lifelines.hl7.generic;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HL7ValueSetDCM
{

	private String valueSetsName = "";
	private static final String CODE = "urn:hl7-org:v3:code";
	private List<HL7ValueSetAnswerDCM> listOFAnswers = null;

	public HL7ValueSetDCM(Node node, XPath xpath) throws Exception
	{

		listOFAnswers = new ArrayList<HL7ValueSetAnswerDCM>();
		valueSetsName = node.getAttributes().getNamedItem("name").getNodeValue().trim();
		NodeList nodesFile = (NodeList) xpath.compile(CODE).evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < nodesFile.getLength(); i++)
		{
			HL7ValueSetAnswerDCM answer = new HL7ValueSetAnswerDCM(nodesFile.item(i), xpath);
			listOFAnswers.add(answer);
		}

	}

	public List<HL7ValueSetAnswerDCM> getListOFAnswers()
	{
		return listOFAnswers;
	}

	public String getValueSetsName()
	{
		return valueSetsName;
	}

}
