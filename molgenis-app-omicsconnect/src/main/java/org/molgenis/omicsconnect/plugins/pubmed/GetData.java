package org.molgenis.omicsconnect.plugins.pubmed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

public class GetData
{

	public String db;
	public String format;
	public String id;
	public String baseURL;

	static Logger logger = Logger.getLogger("test.GetData");
	static Map<String, String> pubMed = new HashMap<String, String>();

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

	public GetData()
	{
	}

	/**
	 * @return
	 */
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

	/**
	 * @param xmlString
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document generateDoc(String xmlString) throws UnsupportedEncodingException, SAXException,
			IOException, ParserConfigurationException
	{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true); // never forget this!
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));

		return doc;

	}

	public static String getMeshTerms(XPath xpath, String id, Document doc, String expression)
			throws XPathExpressionException
	{

		XPathExpression expr = xpath.compile(expression);

		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		return expression;

	}

	public static String getAuthors(XPath xpath, String id, Document doc, String expression)
			throws XPathExpressionException
	{

		XPathExpression expr = xpath.compile(expression);

		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		String lastname = null;
		String forename;
		String initials;
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
		pubMed.put("Authors", sb.toString());
		return null;
	}

	public static String getDetails(String expression)
	{

		return expression;

	}

	/**
	 * @param xpath
	 * @param id
	 * @param doc
	 * @param expression
	 * @return
	 * @throws XPathExpressionException
	 */

	public static String getField(XPath xpath, String id, Document doc, String expression, String key)
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
			pubMed.put(key, text);

		}

		return null;

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		GetData data = new GetData();
		data.setDb("pubmed");
		data.setFormat("xml");
		data.setId("22457343");
		data.setBaseURL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?");

		String urlString = data.generateURL();
		/* fetch the URL */
		String output = Efetch.getHttpUrl(urlString);
		System.out.println(output);

		// Parse Pubmed XML
		try
		{
			Document doc = generateDoc(output);
			XPath xpath = XPathFactory.newInstance().newXPath();

			// define the expressions for XPath search
			String abstractExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation[PMID/text()='" + data.getId()
					+ "']/Article/Abstract/AbstractText/text()";

			String authorExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/AuthorList/Author";
			String titleExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/ArticleTitle";
			String doiExpression = "/PubmedArticleSet/PubmedArticle/PubmedData/ArticleIdList";
			String jourTitle = "/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Journal/Title";
			String meshTermsExpression = "/PubmedArticleSet/PubmedArticle/MedlineCitation/MeshHeadingList";

			// Get the data
			getAuthors(xpath, data.getId(), doc, authorExpression);
			getField(xpath, data.getId(), doc, abstractExpression, "Abstract");
			getField(xpath, data.getId(), doc, jourTitle, "Journal");
			getField(xpath, data.getId(), doc, titleExpression, "Title");
			getPublicationIdentifiers(xpath, data.getId(), doc, doiExpression);
			// getMeshTerms(xpath, data.getId(), doc,meshTermsExpression);

			System.out.println(pubMed.toString());

		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// String output =
	}

	private static void getPublicationIdentifiers(XPath xpath, String id, Document doc, String expression)
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

								pubMed.put("DOI:", s);

							}

							if (theAttribute.getNodeValue().equals("pubmed"))
							{

								String p = c.getTextContent();

								System.out.println();

								pubMed.put("PUBMED", p);

							}

							if (theAttribute.getNodeValue().equals("pii"))
							{

								String pi = c.getTextContent();

								System.out.println();

								pubMed.put("PII", pi);

							}

							if (theAttribute.getNodeValue().equals("pmc"))
							{

								String pmc = c.getTextContent();

								System.out.println();

								pubMed.put("PMC", pmc);

							}

						}

					}

				}

			}

		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
