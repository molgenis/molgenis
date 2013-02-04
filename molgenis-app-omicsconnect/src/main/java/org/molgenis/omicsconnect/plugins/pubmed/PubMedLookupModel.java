package org.molgenis.omicsconnect.plugins.pubmed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.molgenis.omicsconnect.plugins.eutils.Efetch;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;

//mac shift o 

public class PubMedLookupModel
{

	private String result, baseURL, id, format, db;
	static HashMap<String, String> pubmed = new HashMap<String, String>();

	static JsonObject json = new JsonObject();

	public JsonObject getJson()
	{
		return json;
	}

	public void setJson(JsonObject json)
	{
		PubMedLookupModel.json = json;
	}

	public HashMap<String, String> getPubmed()
	{
		return pubmed;
	}

	public void setPubmed(HashMap<String, String> pubmed)
	{
		PubMedLookupModel.pubmed = pubmed;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getFormat()
	{
		return format;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public String getDb()
	{
		return db;
	}

	public void setDb(String db)
	{
		this.db = db;
	}

	public String generateURL()
	{

		String db = this.getDb();
		String format = this.getFormat();
		String id = this.getId();
		String baseURL = this.getBaseURL();

		// construct URL
		String url = Efetch.constructURL(baseURL, db, format, id);

		return url;

	}

	public Document generateDoc(String xmlString) throws UnsupportedEncodingException, SAXException, IOException,
			ParserConfigurationException
	{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true); // never forget this!
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-16")));

		return doc;

	}

	public String getField(XPath xpath, String id, Document doc, String expression, String key)
			throws XPathExpressionException
	{
		XPathExpression expr = xpath.compile(expression);
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		// System.out.println(id);
		String text = "";
		for (int i = 0; i < list.getLength(); i++)
		{

			text = (list.item(i).getTextContent());

			// logger.info("WIGGLE" + text);

			pubmed.put(key, text);

		}

		// System.out.println(pubmed.toString());
		return null;

	}

	public HashMap<String, String> PubMedData() throws Exception
	{
		// make sure the pubmed hash is clear

		pubmed.clear();

		String urlString = this.generateURL();
		/* fetch the URL */
		String output = Efetch.getHttpUrl(urlString);
		this.setResult(output);

		System.out.println("OUT>>>>>>>>>>>>>" + output);
		// Parse Pubmed XML
		Document doc = null;

		doc = this.generateDoc(output);

		XPath xpath = XPathFactory.newInstance().newXPath();
		// define the expressions for XPath search
		String abstractExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation[PMID/text()='" + this.getId()
				+ "']/Article/Abstract/AbstractText/text()";

		String authorExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/AuthorList/Author";
		String titleExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/ArticleTitle";
		String doiExpression = "/PubmedArticleSet/PubmedArticle/PubmedData/ArticleIdList";
		String jourTitle = "/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Journal/Title";
		// String meshTermsExpression =
		// "/PubmedArticleSet/PubmedArticle/MedlineCitation/MeshHeadingList";

		try
		{
			this.getField(xpath, this.getId(), doc, abstractExpression, "abstract");
			this.getAuthors(xpath, this.getId(), doc, authorExpression);
			this.getField(xpath, this.getId(), doc, jourTitle, "journal");
			this.getField(xpath, this.getId(), doc, titleExpression, "title");
			this.getPublicationIdentifiers(xpath, this.getId(), doc, doiExpression);

		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}

		json.add(id, new JsonObject());

		return pubmed;
	}

	private void getPublicationIdentifiers(XPath xpath, String id, Document doc, String expression)
	{

		XPathExpression expr;
		try
		{
			expr = xpath.compile(expression);
			NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < list.getLength(); i++)
			{

				Node t = list.item(i);
				NodeList tc = t.getChildNodes();

				// System.out.println(t.toString());
				// logger.info(t.getNodeName());

				for (int j = 0; j < tc.getLength(); j++)
				{
					Node c = tc.item(j);

					// check we have the Article ID
					if (c.getNodeName().equals("ArticleId"))
					{

						// Get all attributes
						NamedNodeMap attributes = c.getAttributes();

						// foreach attribute check for doi, PMID PMC etc
						for (int a = 0; a < attributes.getLength(); a++)
						{
							Node theAttribute = attributes.item(a);
							// System.out.println(theAttribute.getNodeName() +
							// "=" + theAttribute.getNodeValue());

							if (theAttribute.getNodeValue().equals("doi"))
							{

								String s = c.getTextContent();

								System.out.println(s);

								pubmed.put("doi", s);

							}

							if (theAttribute.getNodeValue().equals("pubmed"))
							{

								String p = c.getTextContent();

								System.out.println();

								pubmed.put("pubmed", p);

							}

							if (theAttribute.getNodeValue().equals("pii"))
							{

								String pi = c.getTextContent();

								System.out.println();

								pubmed.put("pii", pi);

							}

							if (theAttribute.getNodeValue().equals("pmc"))
							{

								String pmc = c.getTextContent();

								System.out.println();

								pubmed.put("pmc", pmc);

							}

						}

					}

				}

			}

		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
	}

	public String getAuthors(XPath xpath, String id, Document doc, String expression) throws XPathExpressionException
	{

		XPathExpression expr = xpath.compile(expression);

		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		String lastname = null;
		String initials;
		@SuppressWarnings("unused")
		String forename;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < list.getLength(); i++)
		{

			Node t = list.item(i);
			NodeList tc = t.getChildNodes();

			// System.out.println(t.toString());
			// logger.info(t.getNodeName());

			for (int j = 0; j < tc.getLength(); j++)
			{
				Node c = tc.item(j);

				// logger.info(c.getLocalName());
				if (c.getNodeName().equals("LastName"))
				{

					// logger.info(c.getTextContent());
					lastname = c.getTextContent();

					sb.append(lastname);
					sb.append(",");

				}
				else if (c.getNodeName().equals("ForeName"))
				{
					// logger.info(c.getTextContent());
					forename = c.getTextContent();

				}
				else if (c.getNodeName().equals("Initials"))
				{
					// logger.info(c.getTextContent());
					initials = c.getTextContent();
					sb.append(initials);
					sb.append(";");

				}

			}

		}

		// logger.info(sb.toString());
		pubmed.put("authors", sb.toString());
		return null;
	}

}
