package org.molgenis.lifelines.hl7.lra;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

public class HL7ValueSetAnswerLRA
{

	private static final String CODE = "@code";
	private static final String DISPLAYNAME = "@displayName";
	private String codeValue = "";
	private String name = "";

	public HL7ValueSetAnswerLRA(Node node, XPath xpath) throws Exception
	{

		Node nameNode = (Node) xpath.compile(DISPLAYNAME).evaluate(node, XPathConstants.NODE);
		name = nameNode.getNodeValue().trim();
		Node codeNode = (Node) xpath.compile(CODE).evaluate(node, XPathConstants.NODE);
		codeValue = codeNode.getNodeValue().trim();
	}

	public String getName()
	{
		return name;
	}

	public String getCodeValue()
	{
		return codeValue;
	}

}
