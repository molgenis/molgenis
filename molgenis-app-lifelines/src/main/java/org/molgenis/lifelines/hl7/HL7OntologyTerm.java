package org.molgenis.lifelines.hl7;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class HL7OntologyTerm
{

	private Node ontologyTermCode;
	private XPath xpath;
	String displayName;
	String code;
	String codeSystemName;
	String codeSystem;
	private static final String DISPLAYNAME = "@displayName";
	private static final String CODE = "@code";
	private static final String CODESYSTEMNAME = "@codeSystemName";
	private static final String CODESYSTEM = "@codeSystem";

	public HL7OntologyTerm(Node node, XPath xpath) throws Exception
	{
		this.xpath = xpath;
		ontologyTermCode = node;
		readDisplayName();
		readCode();
		readCodeSystem();
		readCodeSystemName();
	}

	public void readDisplayName() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(DISPLAYNAME, ontologyTermCode, XPathConstants.NODE);
		this.displayName = nameNode.getNodeValue();
	}

	public void readCode() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(CODE, ontologyTermCode, XPathConstants.NODE);
		this.code = nameNode.getNodeValue();

	}

	public void readCodeSystemName() throws XPathExpressionException
	{
		Node nameNode = (Node) xpath.evaluate(CODESYSTEMNAME, ontologyTermCode, XPathConstants.NODE);
		this.codeSystemName = nameNode.getNodeValue();
	}

	public void readCodeSystem() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(CODESYSTEM, ontologyTermCode, XPathConstants.NODE);
		if (nameNode != null)
		{
			this.codeSystem = nameNode.getNodeValue();
		}
		else
		{
			this.codeSystem = "";
		}
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getCode()
	{
		return code;
	}

	public String getCodeSystemName()
	{
		return codeSystemName;
	}

	public String getCodeSystem()
	{
		return codeSystem;
	}

}
