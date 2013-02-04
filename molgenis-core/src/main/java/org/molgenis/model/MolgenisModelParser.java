/**
 * File: invengine_generate/parser/XmlParser.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-12-08; 1.0.0; RA Scheltema Creation.
 * </ul>
 */
package org.molgenis.model;

// jdk
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.fieldtypes.UnknownField;
import org.molgenis.model.elements.DBSchema;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Form;
import org.molgenis.model.elements.Form.SortOrder;
import org.molgenis.model.elements.Index;
import org.molgenis.model.elements.Matrix;
import org.molgenis.model.elements.Menu;
import org.molgenis.model.elements.Method;
import org.molgenis.model.elements.MethodQuery;
import org.molgenis.model.elements.Model;
import org.molgenis.model.elements.Module;
import org.molgenis.model.elements.Parameter;
import org.molgenis.model.elements.Plugin;
import org.molgenis.model.elements.Plugin.Flavor;
import org.molgenis.model.elements.Record;
import org.molgenis.model.elements.Tree;
import org.molgenis.model.elements.UISchema;
import org.molgenis.model.elements.View;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO: refactor: spread over multiple files.
 */
public class MolgenisModelParser
{
	private static final Logger logger = Logger.getLogger(MolgenisModelParser.class);

	/**
	 * 
	 * @param model
	 * @param element
	 *            a DOM element that looks like <entity name="a"
	 *            abstract="true"><field name="a" type="....
	 * @throws MolgenisModelException
	 */
	public static Entity parseEntity(Model model, Element element) throws MolgenisModelException
	{
		// check for illegal words
		String[] keywords = new String[]
		{ "name", "label", "extends", "implements", "abstract", "description", "system", "decorator", "xref_label",
				"allocationSize" };
		List<String> key_words = new ArrayList<String>(Arrays.asList(keywords));
		for (int i = 0; i < element.getAttributes().getLength(); i++)
		{
			if (!key_words.contains(element.getAttributes().item(i).getNodeName()))
			{
				throw new MolgenisModelException("attribute '" + element.getAttributes().item(i).getNodeName()
						+ "' not allowed for <entity>");
			}
		}

		// check properties
		// NAME
		if (element.getAttribute("name").trim().isEmpty())
		{
			String message = "name is missing for entity " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		Entity entity = new Entity(element.getAttribute("name").trim(), element.getAttribute("label"),
				model.getDatabase());
		entity.setNamespace(model.getName());

		// add optional properties
		// EXTENDS exactly one
		String _extends = element.getAttribute("extends");
		if (_extends != null)
		{
			Vector<String> parents = new Vector<String>();
			StringTokenizer tokenizer = new StringTokenizer(_extends, ",");
			while (tokenizer.hasMoreTokens())
			{
				parents.add(tokenizer.nextToken().trim());
			}

			entity.setParents(parents);
		}

		// IMPLEMENTS
		String _implements = element.getAttribute("implements");
		if (_implements != null && !_implements.isEmpty())
		{
			entity.setImplements(new Vector<String>(Arrays.asList(_implements.split(","))));
		}

		// ABSTRACT
		entity.setAbstract(Boolean.parseBoolean(element.getAttribute("abstract")));

		// SYSTEM
		entity.setSystem(Boolean.parseBoolean(element.getAttribute("system")));

		// XREF_LABEL
		String xref_label = element.getAttribute("xref_label");
		if (xref_label != null && !xref_label.isEmpty())
		{
			List<String> xref_labels = new ArrayList<String>();
			xref_labels.addAll(Arrays.asList(xref_label.split(",")));
			entity.setXrefLabels(xref_labels);
		}
		else
		{
			entity.setXrefLabels(null);
		}

		// TRIGGER
		if (element.hasAttribute("decorator"))
		{
			entity.setDecorator(element.getAttribute("decorator"));
		}

		// DESCRIPTION
		NodeList elements = element.getElementsByTagName("description");
		for (int j = 0; j < elements.getLength(); j++)
		{
			// parse the contents, including markup...
			entity.setDescription(elementValueToString((Element) elements.item(j)));
		}

		// FIELD
		elements = element.getElementsByTagName("field");
		for (int j = 0; j < elements.getLength(); j++)
		{
			Element elem = (Element) elements.item(j);
			parseField(entity, elem);
		}

		// UNIQUE
		elements = element.getElementsByTagName("unique");
		for (int j = 0; j < elements.getLength(); j++)
		{
			Element elem = (Element) elements.item(j);
			Vector<String> keys = new Vector<String>();

			// keys from keyfield="a,b" attribute
			if (elem.hasAttribute("fields"))
			{
				for (String name : elem.getAttribute("fields").split(","))
				{
					// Field f = entity.getField(name);
					// if (f == null)
					// {
					// // try to get superclass field (need to copy it then)
					// f = entity.getAllField(name);
					// if (f == null) throw new
					// MolgenisModelException("Missing unique field '" + name
					// + "' in entity '" + entity.getName() + "'");
					//
					// // copy the field so it will end up in the table (as
					// // redundant copy) to enforce unique
					// // f = new Field(f);
					// // f.setEntity(entity);
					// // f.setSystem(true);
					// // entity.addField(f);
					// }

					keys.add(name);
				}

			}

			// keys from <keyfield> elements
			NodeList key_elements = elem.getElementsByTagName("keyfield");
			for (int k = 0; k < key_elements.getLength(); k++)
			{
				elem = (Element) key_elements.item(k);

				String name = elem.getAttribute("name");
				// should include superclass methods now
				// if (f == null)
				// {
				// throw new MolgenisModelException("Missing unique field: " +
				// elem.getAttribute("name"));
				// // return null;
				// }

				keys.add(name);
			}

			// description
			String key_description = null;
			if (elem.hasAttribute("description"))
			{
				key_description = elem.getAttribute("description");
			}

			// check if keys
			if (keys.size() == 0)
			{
				throw new MolgenisModelException("missing fields on unique of '" + entity.getName()
						+ "'. Expected <unique fields=\"field1[,field2,..]\" description=\"...\"/>");
			}

			try
			{
				entity.addKey(keys, elem.getAttribute("subclass").equals("true"), key_description);
				// might be duplicate key
			}
			catch (Exception e)
			{
				throw new MolgenisModelException(e.getMessage());
			}
		}

		elements = element.getElementsByTagName("indices");
		if (elements.getLength() == 1)
		{
			Element elem = (Element) elements.item(0);

			NodeList index_elements = elem.getElementsByTagName("index");
			for (int k = 0; k < index_elements.getLength(); k++)
			{
				elem = (Element) index_elements.item(k);

				Index index = new Index(elem.getAttribute("name"));

				NodeList indexfield_elements = elem.getElementsByTagName("indexfield");
				for (int l = 0; l < indexfield_elements.getLength(); l++)
				{
					elem = (Element) indexfield_elements.item(l);

					Field f = entity.getField(elem.getAttribute("name"));
					if (f == null)
					{
						// System.err.println(String.format(Error.
						// MISSING_INDEX_FIELD.msg,
						// elem.getAttribute("name")));
						throw new MolgenisModelException("Missing index field: " + elem.getAttribute("name"));
						// return null;
					}

					try
					{
						index.addField(elem.getAttribute("name"));
					}
					catch (Exception e)
					{
						throw new MolgenisModelException(e.getMessage());
					}
				}

				try
				{
					entity.addIndex(index);
				}
				catch (Exception e)
				{
				}
			}
		}
		else if (elements.getLength() > 1)
		{
			// System.err.println(Error.MULTIPLE_INDICES_ELEMENTS.msg);
			// return null;
			throw new MolgenisModelException("Multiple indices elements");
		}
		// done

		// Todo: change if(molgenisOptions.jpa_use_sequence &&
		// element.hasAttribute("allocationSize"))
		if (element.hasAttribute("allocationSize"))
		{
			int allocationSize = Integer.parseInt(element.getAttribute("allocationSize"));
			entity.setAllocationSize(allocationSize);
		}

		logger.debug("read: " + entity.getName());
		return entity;
	}

	public static void parseMatrix(Model model, Element element) throws MolgenisModelException
	{
		// check parameters
		String[] keywords = new String[]
		{ "name", "content_entity", "content", "container", "row", "col", "row_entity", "col_entity" };
		List<String> key_words = new ArrayList<String>(Arrays.asList(keywords));
		for (int i = 0; i < element.getAttributes().getLength(); i++)
		{
			if (!key_words.contains(element.getAttributes().item(i).getNodeName()))
			{
				throw new MolgenisModelException("attribute '" + element.getAttributes().item(i).getNodeName()
						+ "' unknown for <entity>");
			}
		}

		// make sure required properties are set
		if (element.getAttribute("name") != null && element.getAttribute("name").isEmpty())
		{
			String message = "name is missing for entity " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// create the matrix object and put it in the model
		Matrix matrix = new Matrix(element.getAttribute("name"), model.getDatabase());
		matrix.setCol(element.getAttribute("col"));
		matrix.setRow(element.getAttribute("row"));
		matrix.setContentEntity(element.getAttribute("content_entity"));
		matrix.setContainer(element.getAttribute("container"));
		matrix.setColEntityName(element.getAttribute("col_entity"));
		matrix.setRowEntityName(element.getAttribute("row_entity"));
		matrix.setContent(element.getAttribute("content"));

		logger.debug("read: " + matrix.toString());
	}

	public static void parseField(Entity entity, Element element) throws MolgenisModelException
	{
		// check for illegal words
		String[] keywords = new String[]
		{ "type", "name", "label", "auto", "nillable", "optional", "readonly", "default", "description", "desc",
				"unique", "hidden", "length", "index", "enum_options", "default_code", "xref", "xref_entity",
				"xref_field", "xref_label", "xref_name", "mref_name", "mref_localid", "mref_remoteid", "filter",
				"filtertype", "filterfield", "filtervalue", "xref_cascade" + "", "allocationSize", "jpaCascade" };
		List<String> key_words = new ArrayList<String>(Arrays.asList(keywords));
		for (int i = 0; i < element.getAttributes().getLength(); i++)
		{
			if (!key_words.contains(element.getAttributes().item(i).getNodeName()))
			{
				throw new MolgenisModelException("attribute '" + element.getAttributes().item(i).getNodeName()
						+ "' not allowed for <field>");
			}
		}

		// required properties
		String type = element.getAttribute("type");
		String name = element.getAttribute("name");
		String label = element.getAttribute("label");
		String auto = element.getAttribute("auto");
		String nillable = element.getAttribute("nillable");
		if (element.hasAttribute("optional"))
		{
			nillable = element.getAttribute("optional");
		}
		String readonly = element.getAttribute("readonly");
		String default_value = element.getAttribute("default");

		// other properties
		String description = element.getAttribute("description");
		if (description.isEmpty())
		{
			description = element.getAttribute("desc");
		}
		String unique = element.getAttribute("unique");
		String hidden = element.getAttribute("hidden");
		String length = element.getAttribute("length");
		String index = element.getAttribute("index");
		String enum_options = element.getAttribute("enum_options").replace('[', ' ').replace(']', ' ').trim();
		String default_code = element.getAttribute("default_code");
		// xref and mref
		String xref_entity = element.getAttribute("xref_entity");
		String xref_field = element.getAttribute("xref_field");
		String xref_label = element.getAttribute("xref_label");
		String mref_name = element.getAttribute("mref_name");
		String mref_localid = element.getAttribute("mref_localid");
		String mref_remoteid = element.getAttribute("mref_remoteid");

		if (type.isEmpty())
		{
			type = "string";
		}

		if (element.hasAttribute("xref") && !element.hasAttribute("xref_field"))
		{
			xref_field = element.getAttribute("xref");
		}

		if (!xref_field.isEmpty() && !element.hasAttribute("xref_entity"))
		{
			String[] entity_field = xref_field.split("[.]");

			if (entity_field.length == 2)
			{
				xref_entity = entity_field[0];
				xref_field = entity_field[1];
			}
		}

		// filter for xref and mref
		String filter = element.getAttribute("filter");
		String filtertype = element.getAttribute("filtertype");
		String filterfield = element.getAttribute("filterfield");
		String filtervalue = element.getAttribute("filtervalue");

		// (re)set optional properties
		if (type.equals("varchar"))
		{
			type = "string";
		}
		if (type.equals("number"))
		{
			type = "int";
		}
		if (type.equals("boolean"))
		{
			type = "bool";
		}
		if (type.equals("xref_single"))
		{
			type = "xref";
		}
		if (type.equals("xref_multiple"))
		{
			type = "mref";
		}
		if (label.isEmpty())
		{
			label = name;
		}
		if (description.isEmpty())
		{
			description = label;
		}
		if (xref_label == null || xref_label.isEmpty())
		{
			xref_label = null;
		}
		if (type.equals("autoid"))
		{
			type = "int";
			nillable = "false";
			auto = "true";
			readonly = "true";
			unique = "true";
			default_value = "";
		}

		// TODO: validate the booleans to "true" or "false" (or even better: if
		// the value is set, than it is true).

		// check exceptions
		if (type != null && type.isEmpty())
		{
			throw new MolgenisModelException("type is missing for field '" + name + "' of entity '" + entity.getName()
					+ "'");
		}
		if (MolgenisFieldTypes.getType(type) instanceof UnknownField)
		{
			throw new MolgenisModelException("type '" + type + "' unknown for field '" + name + "' of entity '"
					+ entity.getName() + "'");
		}
		if (name.isEmpty())
		{
			throw new MolgenisModelException("name is missing for a field in entity '" + entity.getName() + "'");
		}
		if (hidden.equals("true") && !nillable.equals("true") && (default_value.isEmpty() && !auto.equals("true")))
		{
			throw new MolgenisModelException("field '" + name + "' of entity '" + entity.getName()
					+ "' must have a default value. A field that is not nillable and hidden must have a default value.");
		}

		String jpaCascade = null;
		if (type.equals("mref") || type.equals("xref"))
		{
			if (element.hasAttribute("jpaCascade"))
			{
				jpaCascade = element.getAttribute("jpaCascade");
			}
		}

		// construct
		Field field = new Field(entity, MolgenisFieldTypes.getType(type), name, label, Boolean.parseBoolean(auto),
				Boolean.parseBoolean(nillable), Boolean.parseBoolean(readonly), default_value, jpaCascade);
		logger.debug("read: " + field.toString());

		// add optional properties
		if (!description.isEmpty())
		{
			field.setDescription(description.trim());
		}
		if (hidden.equals("true"))
		{
			field.setHidden(true);
		}
		if (!default_code.isEmpty())
		{
			field.setDefaultCode(default_code);
		}
		if (filter.equals("true"))
		{
			logger.warn("filter set for field '" + name + "' of entity '" + entity.getName() + "'");
			logger.warn(filterfield + " " + filtertype + " " + filtervalue);
			logger.warn(System.currentTimeMillis() + " - filter bool: '" + Boolean.parseBoolean(filter) + "'");
			if ((filtertype != null && filtertype.isEmpty()) || (filterfield != null && filterfield.isEmpty()))
			{
				throw new MolgenisModelException("field '" + name + "' of entity '" + entity.getName()
						+ "': when the filter is set to true, the filtertype, filterfield and filtervalue must be set");
			}
			if (filtervalue != null && filtervalue.isEmpty())
			{
				logger.warn("no value specified for filter in field '" + name + "' of entity '" + entity.getName()
						+ "'");
			}
			field.setFilter(Boolean.parseBoolean(filter));
			field.setFiltertype(filtertype);
			field.setFilterfield(filterfield);
			field.setFiltervalue(filtervalue);
		}

		// add type dependent properties
		if (type.equals("string"))
		{
			if (!length.isEmpty())
			{
				field.setVarCharLength(Integer.parseInt(length));
			}
			else
			{
				field.setVarCharLength(255);
			}
		}
		else if (type.equals("enum"))
		{
			Vector<String> options = new Vector<String>();
			StringTokenizer tokenizer = new StringTokenizer(enum_options, ",");
			while (tokenizer.hasMoreElements())
			{
				options.add(tokenizer.nextToken().trim());
			}
			if (options.size() < 1)
			{
				throw new MolgenisModelException("enum_options must be ',' delimited for field '" + field.getName()
						+ "' of entity '" + entity.getName() + "'");
			}

			field.setEnumOptions(options);
		}
		else if (type.equals("xref") || type.equals("mref"))
		{
			// xref must be defined unless mref_name is set
			// caveat, can be both ends!!!
			if (mref_name.isEmpty() && xref_entity.isEmpty())
			{
				throw new MolgenisModelException("xref_entity must be set for xref field '" + field.getName()
						+ "' of entity '" + entity.getName() + "'");
			}

			List<String> xref_labels = null;
			if (xref_label != null)
			{
				xref_labels = Arrays.asList(xref_label.split(","));
			}

			field.setXRefVariables(xref_entity, xref_field, xref_labels);

			// optional custom naming instead of default
			// necessary when using existing database
			if (type.equals("mref"))
			{
				if (!mref_name.isEmpty())
				{
					field.setMrefName(mref_name);
				}
				if (!mref_localid.isEmpty())
				{
					field.setMrefLocalid(mref_localid);
				}
				if (!mref_remoteid.isEmpty())
				{
					field.setMrefRemoteid(mref_remoteid);
				}
			}

			if (!element.getAttribute("xref_cascade").isEmpty())
			{
				if (element.getAttribute("xref_cascade").equalsIgnoreCase("true"))
				{
					field.setXrefCascade(true);
				}
				else
				{
					throw new MolgenisModelException("Unknown option on xref_cascade: '"
							+ element.getAttribute("xref_cascade") + "'");
				}
			}
		}

		// add the field to entity
		try
		{
			entity.addField(field);

		}
		catch (Exception e)
		{
			throw new MolgenisModelException("duplicate field '" + field.getName() + "' in entity '" + entity.getName()
					+ "'");
		}

		// check whether this field has a short-hand for index
		if (index.equals("true"))
		{
			Index i = new Index(name);
			try
			{
				i.addField(name);
			}
			catch (Exception e)
			{
				throw new MolgenisModelException("duplicate field '" + field.getName() + "' in entity '"
						+ entity.getName() + "'");
			}

			entity.addIndex(i);
		}

		// check whether this field has a short-hand for unique
		if (unique.equals("true"))
		{
			entity.addKey(field.getName(), null);
		}

		// parse annotations (used by JPA)
		if (element.getChildNodes().getLength() >= 1)
		{
			String annotations = org.apache.commons.lang.StringUtils.deleteWhitespace(
					element.getChildNodes().item(1).getTextContent()).trim();
			field.setAnnotations(annotations);
		}

	}

	public static void parseView(Model model, Element element) throws MolgenisModelException
	{
		// get the attributes
		String name = element.getAttribute("name");
		String label = element.getAttribute("label");
		String entities = element.getAttribute("entities");

		// check properties
		if (name.isEmpty())
		{
			throw new MolgenisModelException("name is missing for view " + element.toString());
		}
		if (entities.isEmpty())
		{
			throw new MolgenisModelException("entities is missing for view " + element.toString());
		}
		if (label.isEmpty())
		{
			label = name;
		}

		List<String> entityList = new ArrayList<String>(Arrays.asList(entities.split(",")));
		if (entityList.size() < 2)
		{
			throw new MolgenisModelException("a view needs at least 2 entities, define as entities=\"e1,e2\": "
					+ element.toString());
		}

		// construct the view
		View view = new View(name, label, model.getDatabase());

		// add the viewentities
		for (String viewentity : entityList)
		{
			if (view.getEntities().contains(viewentity))
			{
				throw new MolgenisModelException("view " + name + " has duplicate viewentity entries (" + viewentity
						+ ")");
			}
			view.addEntity(viewentity);
		}
	}

	// Method parser
	public static void parseMethod(Model model, Element element) throws MolgenisModelException
	{
		// NAME
		if (element.getAttribute("name") != null && element.getAttribute("name").isEmpty())
		{
			String message = "name is missing for method " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		Method method = new Method(element.getAttribute("name"), model.getMethodSchema());

		NodeList nodes = element.getChildNodes();
		for (int nodeid = 0; nodeid < nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}

			if (((Element) node).getTagName().equals("description"))
			{
				method.setDescription(((Element) node).getTextContent().trim());
			}
			else if (((Element) node).getTagName().equals("parameter"))
			{
				parseParameter(method, (Element) node);
			}
			else if (((Element) node).getTagName().equals("return"))
			{
				parseReturnType(model, method, (Element) node);
			}
			else if (((Element) node).getTagName().equals("query"))
			{
				parseQuery(model, method, (Element) node);
			}
		}
	}

	public static void parseParameter(Method method, Element element) throws MolgenisModelException
	{
		// check properties
		// NAME
		if (element.getAttribute("name") != null && element.getAttribute("name").isEmpty())
		{
			String message = "name is missing for parameter " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}
		// TYPE
		if (element.getAttribute("type") != null && element.getAttribute("type").isEmpty())
		{
			String message = "type is missing for parameter " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		Parameter parameter = new Parameter(method, Parameter.Type.getType(element.getAttribute("type")),
				element.getAttribute("name"), element.getAttribute("label"), false, element.getAttribute("default"));

		try
		{
			method.addParameter(parameter);
		}
		catch (Exception e)
		{
			throw new MolgenisModelException("duplicate parameter '" + parameter.getName() + "' in method '"
					+ method.getName() + "'");
		}
	}

	public static void parseReturnType(Model model, Method method, Element element) throws MolgenisModelException
	{
		// check properties
		// TYPE
		if (element.getAttribute("type") != null && element.getAttribute("type").isEmpty())
		{
			String message = "type is missing for returntype " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		Entity entity = model.getEntity(element.getAttribute("type"));

		try
		{
			method.setReturnType(entity);
		}
		catch (Exception e)
		{
		}
	}

	public static void parseQuery(Model model, Method method, Element element) throws MolgenisModelException
	{
		// check properties
		// TYPE
		if (element.getAttribute("entity") != null && element.getAttribute("entity").isEmpty())
		{
			String message = "type is missing for returntype " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		MethodQuery query = new MethodQuery(element.getAttribute("entity"));
		method.setQuery(query);

		// parse the rules
		NodeList nodes = element.getChildNodes();
		for (int nodeid = 0; nodeid < nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}

			if (((Element) node).getTagName().equals("rule"))
			{
				parseQueryRule(model, query, (Element) node);
			}
		}
	}

	public static void parseQueryRule(Model model, MethodQuery query, Element element) throws MolgenisModelException
	{
		// check properties
		// TYPE
		if (element.getAttribute("field") != null && element.getAttribute("field").isEmpty())
		{
			String message = "type is missing for field " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}
		if (element.getAttribute("operator") != null && element.getAttribute("operator").isEmpty())
		{
			String message = "type is missing for operator " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}
		if (element.getAttribute("parameter") != null && element.getAttribute("parameter").isEmpty())
		{
			String message = "type is missing for parameter " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		query.addRule(new MethodQuery.Rule(element.getAttribute("field"), element.getAttribute("operator"), element
				.getAttribute("parameter")));
	}

	/** Simple parse an xml string */
	public static Model parseDbSchema(String xml) throws MolgenisModelException
	{
		Model model = new Model("molgenis");

		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));

			Document document = builder.parse(is);

			parseXmlDocument(model, document);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MolgenisModelException(e.getMessage());
		}

		return model;
	}

	public static Model parseDbSchema(ArrayList<String> filenames) throws MolgenisModelException
	{
		return MolgenisModelParser.parseDbSchema(filenames, "molgenis");
	}

	// db parser
	public static Model parseDbSchema(ArrayList<String> filenames, String modelName) throws MolgenisModelException
	{
		Model model = new Model(modelName);

		// initialize the document
		for (String filename : filenames)
		{
			parseXmlDocument(model, MolgenisModelParser.parseXmlFile(filename));
		}

		return model;
	}

	private static Document parseXmlDocument(Model model, Document document) throws MolgenisModelException
	{
		// retrieve the document-root
		Element document_root = document.getDocumentElement();
		if (document_root.getAttribute("name") != null && document_root.getAttribute("name").isEmpty())
		{
			document_root.setAttribute("name", "molgenis");
		}

		String modelName = document_root.getAttribute("name");
		String modelLabel = document_root.getAttribute("label");

		model.setName(modelName);
		if (!"".equals(modelLabel))
		{
			model.setLabel(modelLabel);
		}

		// retrieve the children
		NodeList children = document_root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);

			if (child.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}

			Element element = (Element) child;
			if (element.getTagName().equals("module"))
			{
				parseModule(model, element);
			}
			else if (element.getTagName().equals("entity"))
			{
				parseEntity(model, element);
			}
			else if (element.getTagName().equals("matrix"))
			{
				parseMatrix(model, element);
			}
			else if (element.getTagName().equals("view"))
			{
				parseView(model, element);
			}
			else if (element.getTagName().equals("method"))
			{
				parseMethod(model, element);
			}
			else if (element.getTagName().equals("description"))
			{
				model.setDBDescription(model.getDBDescription() + elementValueToString(element));
			}
		}
		return document;
	}

	public static void parseModule(Model model, Element element) throws MolgenisModelException
	{
		// check for illegal words
		String[] keywords = new String[]
		{ "name", "label" };
		List<String> key_words = new ArrayList<String>(Arrays.asList(keywords));
		for (int i = 0; i < element.getAttributes().getLength(); i++)
		{
			if (!key_words.contains(element.getAttributes().item(i).getNodeName()))
			{
				throw new MolgenisModelException("attribute '" + element.getAttributes().item(i).getNodeName()
						+ "' unknown for <module " + element.getAttribute("name") + ">");
			}
		}

		// check properties
		// NAME
		if (element.getAttribute("name").trim().isEmpty())
		{
			String message = "name is missing for module " + element.toString();
			logger.error(message);
			throw new MolgenisModelException(message);
		}

		// construct
		Module module = new Module(model.getName() + "." + element.getAttribute("name").trim(), model);

		if (element.getAttribute("label") != null && !element.getAttribute("label").isEmpty())
		{
			module.setLabel(element.getAttribute("label"));
		}

		// DESCRIPTION
		NodeList elements = element.getElementsByTagName("description");
		for (int j = 0; j < elements.getLength(); j++)
		{
			// parse the contents, including markup...
			if (elements.item(j).getParentNode().equals(element))
			{
				module.setDescription(elementValueToString((Element) elements.item(j)));
			}
		}

		// ENTITY
		elements = element.getElementsByTagName("entity");
		for (int j = 0; j < elements.getLength(); j++)
		{
			Element elem = (Element) elements.item(j);
			Entity e = parseEntity(model, elem);
			e.setNamespace(module.getName());
			module.getEntities().add(e);
			e.setModule(module);
		}
	}

	private static void parseUiSchema(Model model, Element element, UISchema parent) throws MolgenisModelException
	{
		UISchema new_parent = null;

		if (element.getTagName().equals("include"))
		{
			String fileName = element.getAttribute("file");
			if (fileName == null || fileName.isEmpty())
			{
				throw new MolgenisModelException("include failed: no file attribute set");
			}
			try
			{
				Document document_root = parseXmlFile(fileName);
				// include all its elements, ignore the root node
				NodeList rootChildren = document_root.getChildNodes();
				for (int i = 0; i < rootChildren.getLength(); i++)
				{
					NodeList children = rootChildren.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++)
					{
						Node child = children.item(j);
						if (child.getNodeType() != Node.ELEMENT_NODE)
						{
							continue;
						}
						try
						{

							parseUiSchema(model, (Element) child, parent);

						}
						catch (Exception e)
						{
							throw new MolgenisModelException(e.getMessage());
						}

					}
				}

			}
			catch (Exception e)
			{
				throw new MolgenisModelException("include failed: " + e.getMessage());
			}
		}
		else if (element.getTagName().equals("description"))
		{
			logger.warn("currently the '<description>' tag is ignored in ui.xml");
		}
		else
		{

			String name = element.getAttribute("name").trim();
			String namespace = model.getName();
			String label = element.getAttribute("label");
			String group = element.getAttribute("group");
			String groupRead = element.getAttribute("groupRead");

			// check required properties
			if ((name == null || name.isEmpty()) && !element.getTagName().equals("form"))
			{
				throw new MolgenisModelException("name is missing for subform of screen '" + parent.getName() + "'");
			}
			if (label != null && label.isEmpty())
			{
				label = name;
			}

			if (group.isEmpty())
			{
				group = null; // TODO: Discuss with Erik/Morris/Robert!
			}

			if (groupRead.isEmpty())
			{
				groupRead = null; // TODO: Discuss with Erik/Morris/Robert!
			}

			// add this element to the meta-model
			if (element.getTagName().equals("menu"))
			{
				Menu menu = new Menu(name, parent);
				menu.setLabel(label);
				menu.setGroup(group);
				menu.setGroupRead(groupRead);
				menu.setNamespace(namespace);

				if (group != null && groupRead != null && group.equals(groupRead))
				{
					throw new MolgenisModelException(
							"You cannot assign both read/write and read rights on a single menu");
				}

				if (element.getAttribute("position") == null || !element.getAttribute("position").isEmpty())
				{
					menu.setPosition(Menu.Position.getPosition(element.getAttribute("position")));
				}

				new_parent = menu;
			}
			else if (element.getTagName().equals("form"))
			{
				if (name.isEmpty())
				{
					name = element.getAttribute("entity");
				}
				Form form = new Form(name, parent);
				form.setLabel(label);
				form.setGroup(group);
				form.setGroupRead(groupRead);

				if (group != null && groupRead != null && group.equals(groupRead))
				{
					throw new MolgenisModelException(
							"You cannot assign both read/write and read rights on a single form");
				}

				/** Optional custom header for the selected form screen */
				String header = element.getAttribute("header");
				if (!header.isEmpty()) form.setHeader(header);

				/** Optional description for the selected form screen */
				String description = element.getAttribute("description");
				if (!description.isEmpty()) form.setDescription(description);

				form.setNamespace(namespace);
				new_parent = form;

				// VIEWTYPE
				if (element.getAttribute("view").equals("record"))
				{
					element.setAttribute("view", "edit");
				}
				if (element.getAttribute("view").isEmpty())
				{
					if (element.getChildNodes().getLength() > 0)
					{
						element.setAttribute("view", "edit");
					}
					else
					{
						element.setAttribute("view", "list");
					}
				}
				if (Form.ViewType.parseViewType(element.getAttribute("view")) == Form.ViewType.VIEWTYPE_UNKNOWN)
				{
					throw new MolgenisModelException("view '" + element.getAttribute("view") + "' unknown for form '"
							+ form.getName() + "'");
				}
				form.setViewType(Form.ViewType.parseViewType(element.getAttribute("view")));

				// LIMIT
				form.setLimit(10);
				String limit = element.getAttribute("limit");
				if (limit != null && !limit.isEmpty())
				{
					form.setLimit(Integer.parseInt(limit));
				}

				// ACTIONS
				form.setCommands(new ArrayList<String>());
				String commands = element.getAttribute("commands");
				if (commands != null && !commands.isEmpty())
				{
					String[] commandArray = commands.split(",");
					for (String command : commandArray)
					{
						form.getCommands().add(command.trim());
					}
				}

				// SORT
				String sortby = element.getAttribute("sortby");
				if (sortby != null && !sortby.isEmpty())
				{
					// TODO ensure valid sort field
					form.setSortby(sortby);
				}
				String sortorder = element.getAttribute("sortorder");
				if (sortorder != null && !sortorder.isEmpty())
				{
					if (!sortorder.equalsIgnoreCase(Form.SortOrder.ASC.toString())
							&& !sortorder.equalsIgnoreCase(Form.SortOrder.DESC.toString()))
					{
						throw new MolgenisModelException(
								"sortorder can only be 'asc' or 'desc'. Parser found <form name=\"" + form.getName()
										+ "\" sortorder=\"" + sortorder + "\"");
					}
					else
					{

						form.setSortorder(SortOrder.parse(sortorder));
					}
				}

				// FILTER
				String filter = element.getAttribute("filter");
				if (filter != null && filter.equals("true"))
				{
					if (element.getAttribute("filterfield") != null && element.getAttribute("filterfield").isEmpty())
					{
						throw new MolgenisModelException("filterfield is missing for subform of screen '"
								+ parent.getName() + "'");
					}
					if (element.getAttribute("filtertype") != null && element.getAttribute("filtertype").isEmpty())
					{
						throw new MolgenisModelException("filtertype is missing for subform of screen '"
								+ parent.getName() + "'");
					}
					if (element.getAttribute("filtervalue") != null && element.getAttribute("filtervalue").isEmpty())
					{
						logger.warn("filtervalue is missing for subform of screen '" + parent.getName() + "'");
					}
					form.setFilter(true);
					form.setFilterfield(element.getAttribute("filterfield"));
					form.setFiltertype(element.getAttribute("filtertype"));
					form.setFiltervalue(element.getAttribute("filtervalue"));
				}

				// READONLY
				form.setReadOnly(false);
				String readonly = element.getAttribute("readonly");
				if (readonly != null)
				{
					form.setReadOnly(Boolean.parseBoolean(readonly));
				}

				// ENTITY
				// TODO: whould have expected this in the constructor!
				Entity entity = (Entity) model.getDatabase().getChild(element.getAttribute("entity"));
				if (entity == null)
				{
					throw new MolgenisModelException("Could not find the specified entity '"
							+ element.getAttribute("entity") + "' for form '" + form.getName() + "'");
				}
				form.setRecord(entity);// form.setEntity(entity);

				// HIDDEN FIELDS
				form.setHideFields(new ArrayList<String>());
				String hide_fields = element.getAttribute("hide_fields");
				if (hide_fields != null && !hide_fields.isEmpty())
				{
					String[] hiddenFieldArray = hide_fields.split(",");
					for (String field : hiddenFieldArray)
					{
						Field f = entity.getAllField(field.trim());
						if (f == null)
						{
							throw new MolgenisModelException("Could not find field '" + field
									+ "' defined in hide_fields='" + element.getAttribute("hide_fields")
									+ "' in form '" + form.getName() + "'");
						}
						// use name from 'f' to correct for case problems
						form.getHideFields().add(f.getName());
					}
				}

				// COMPACT_FIELDS
				if (!element.getAttribute("compact_view").isEmpty())
				{
					String[] fields = element.getAttribute("compact_view").split(",");
					// check if the fields are there
					List<String> compact_fields = new ArrayList<String>();
					for (String field : fields)
					{
						Field f = entity.getAllField(field);
						if (f == null)
						{
							throw new MolgenisModelException("Could not find field '" + field
									+ "' defined in compact_view='" + element.getAttribute("compact_view")
									+ "' in form '" + form.getName() + "'");
						}
						// use name from 'f' to correct for case problems

						compact_fields.add(form.getEntity().getName() + "_" + f.getName());
					}
					form.setCompactView(compact_fields);
				}
			}
			else if (element.getTagName().equals("tree"))
			{
				// check required properties
				if (element.getAttribute("parentfield") != null && element.getAttribute("parentfield").isEmpty())
				{
					throw new MolgenisModelException("parentfield is missing for tree screen '" + name + "'");
				}
				if (element.getAttribute("idfield") != null && element.getAttribute("idfield").isEmpty())
				{
					throw new MolgenisModelException("idfield is missing for tree screen '" + name + "'");
				}
				if (element.getAttribute("labelfield") != null && element.getAttribute("labelfield").isEmpty())
				{
					throw new MolgenisModelException("labelfield is missing for tree screen '" + name + "'");
				}

				Tree tree = new Tree(name, parent, element.getAttribute("parentfield"),
						element.getAttribute("idfield"), element.getAttribute("labelfield"));
				tree.setLabel(label);
				tree.setGroup(group);
				tree.setGroupRead(groupRead);

				if (group != null && groupRead != null && group.equals(groupRead))
				{
					throw new MolgenisModelException(
							"You cannot assign both read/write and read rights on a single tree");
				}

				tree.setNamespace(namespace);
				new_parent = tree;

				// READONLY
				tree.setReadOnly(true);
				String readonly = element.getAttribute("readonly");
				if (readonly != null)
				{
					tree.setReadOnly(Boolean.parseBoolean(readonly));
				}

				// ENTITY
				// TODO: whould have expected this in the constructor!
				DBSchema entity = model.getDatabase().getChild(element.getAttribute("entity"));
				if (entity == null)
				{
					throw new MolgenisModelException("Could not find the specified entity '"
							+ element.getAttribute("entity") + "'");
				}
				tree.setRecord((Record) entity);
			}
			else if (element.getTagName().equals("plugin"))
			{
				if (element.getAttribute("type") != null && element.getAttribute("type").isEmpty())
				{
					throw new MolgenisModelException("plugin has no name");
				}
				Plugin plugin = new Plugin(name, parent, element.getAttribute("type"));
				plugin.setLabel(label);
				plugin.setGroup(group);
				plugin.setGroupRead(groupRead);

				if (group != null && groupRead != null && group.equals(groupRead))
				{
					throw new MolgenisModelException(
							"You cannot assign both read/write and read rights on a single plugin");
				}

				plugin.setNamespace(namespace);
				new_parent = plugin;

				// METHOD
				String method = element.getAttribute("flavor");
				if (!"".equals(method))
				{
					plugin.setPluginMethod(Flavor.getPluginMethod(method));
				}

				// READONLY
				plugin.setReadOnly(false);
				String readonly = element.getAttribute("readonly");
				if (readonly != null)
				{
					plugin.setReadOnly(Boolean.parseBoolean(readonly));
				}
			}
			/*
			 * else { // this is the unexpected throw new Exception("Encountered
			 * unknown element: " + element.getTagName()); }
			 */

			// recurse the children
			NodeList children = element.getChildNodes();

			for (int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);

				if (child.getNodeType() != Node.ELEMENT_NODE)
				{
					continue;
				}

				parseUiSchema(model, (Element) child, new_parent);
			}
		}
	}

	private static Document parseXmlFile(String filename) throws MolgenisModelException
	{
		Document document = null;
		DocumentBuilder builder = null;
		try
		{
			// initialize the document
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(filename);
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		catch (Exception e)
		{
			try
			{
				// try to load from classpath
				document = builder.parse(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(filename.trim()));
			}
			catch (Exception e2)
			{
				logger.error("parsing of file '" + filename + "' failed.");
				e.printStackTrace();
				throw new MolgenisModelException("Parsing of DSL (ui) failed: " + e.getMessage());
			}
		}
		return document;
	}

	public static Model parseUiSchema(String filename, Model model) throws MolgenisModelException
	{
		logger.debug("parsing ui file: " + filename);
		if (filename == null || filename.isEmpty())
		{
			return model;
		}

		Document document = parseXmlFile(filename);

		// retrieve the document-root
		Element document_root = document.getDocumentElement();
		if (document_root.getAttribute("name") != null && document_root.getAttribute("name").isEmpty()
				&& model.getName().isEmpty())
		{
			document_root.setAttribute("name", "molgenis");
		}
		String modelName = document_root.getAttribute("name");
		model.setName(modelName);

		String label = document_root.getAttribute("label");
		if (!"".equals(label))
		{
			model.setLabel(label);
		}
		// FIXME should be solved by using modules
		// alternatively ui should be in predefined dir anyway...

		// set the package name for the UI
		// model.setName("ui");

		// retrieve the children
		NodeList children = document_root.getChildNodes();

		// Menu main = new Menu("main", model.getUserinterface());
		// main.setLabel("main");
		// main.setNamespace(model.getName());
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);

			if (child.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}
			try
			{
				// root must be menu
				if (child.getNodeName().equals("description") || child.getNodeName().equals("form")
						|| child.getNodeName().equals("plugin") || child.getNodeName().equals("menu")
						|| child.getNodeName().equals("include"))
				{
					parseUiSchema(model, (Element) child, model.getUserinterface());
				}
				else
				{
					throw new MolgenisModelException("Unrecognized element: " + child.getNodeName());
				}

				// }
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new MolgenisModelException(e.getMessage());
			}

		}
		return model;
	}

	private static String elementValueToString(Element element) throws MolgenisModelException
	{
		StringWriter buffer = new StringWriter();
		try
		{
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "true");
			transformer.transform(new DOMSource(element), new StreamResult(buffer));
		}
		catch (TransformerException e)
		{
			throw new MolgenisModelException(e.getMessage());
		}
		String xml = buffer.toString();

		// FIXME: attributes
		xml = xml.replace("<" + element.getTagName() + ">", "");
		xml = xml.replace("</" + element.getTagName() + ">", "");
		xml = xml.replace("<" + element.getTagName() + "/>", "");

		return xml;
	}
}
