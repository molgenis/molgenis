package org.molgenis.app.controller;

import static org.molgenis.app.controller.RDFController.URI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.rest.RestController;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.rdf.RdfService;
import org.molgenis.rdf.fair.FairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class RDFController extends MolgenisPluginController
{
	public static final String ID = "rdf";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public RDFController()
	{
		super(URI);
	}

	@Autowired
	DataService dataService;

	@Autowired
	RdfService rdfService;

	@Autowired
	FairService fairService;

	@RequestMapping(value = "schema", method = RequestMethod.GET)
	@ResponseBody
	public Model getRDFSchema()
	{
		return rdfService.getSchema("http://localhost:8080" + RestController.BASE_URI);
	}

	@RequestMapping(value = "fair", method = RequestMethod.GET)
	@ResponseBody
	public Model getFairProfile()
	{
		return fairService.getProfile("http://localhost:8080" + RestController.BASE_URI);
	}

	@RequestMapping(value = "jsonld/{entityName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public FileSystemResource getFile(@PathVariable("entityName") String entityName, HttpServletResponse response)
	{
		response.setHeader("Content-disposition", "attachment; filename=" + entityName + ".json");
		File file = new File(entityName + ".json");

		try
		{
			Model model = ModelFactory.createDefaultModel();
			Repository repo = dataService.getRepositoryByEntityName(entityName);
			Iterator<Entity> iter = repo.iterator();

			while (iter.hasNext())
			{
				Entity entity = iter.next();
				String ns = "http://www.molgenis.org/";
				String restPrefix = org.molgenis.data.rest.RestController.BASE_URI;
				for (AttributeMetaData metaData : entity.getEntityMetaData().getAtomicAttributes())
				{
					if (metaData.isIdAtrribute())
					{
						Resource subject = model.createResource(restPrefix
								.concat("/" + entity.getEntityMetaData().getName()).concat("/")
								.concat(entity.getIdValue().toString()));
						Property predicate = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
						Resource object = model.createResource(ns.concat(entity.getEntityMetaData().getName()));

						connect(subject, predicate, object, model);
					}
					else
					{
						Resource subject = model.createResource(restPrefix
								.concat("/" + entity.getEntityMetaData().getName()).concat("/")
								.concat(entity.getIdValue().toString()));
						Property predicate = model.createProperty(metaData.getPredicateIri() != null ? metaData
								.getPredicateIri() : "");
						Resource object = model.createResource(entity.getString(metaData.getName()) != null ? entity
								.getString(metaData.getName()) : "");

						connect(subject, predicate, object, model);
					}
				}
			}

			try
			{
				FileOutputStream fop = new FileOutputStream(file);
				RDFDataMgr.write(fop, model, RDFFormat.JSONLD);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			/**
			 * InputStream is = new FileInputStream(file); org.apache.commons.io.IOUtils.copy(is,
			 * response.getOutputStream()); response.flushBuffer();
			 **/
		}
		catch (Exception ex)
		{
			throw new RuntimeException("IOError writing file to output stream");
		}
		return new FileSystemResource(file);
	}

	@RequestMapping(value = "xml/{entityName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public FileSystemResource getXMLFile(@PathVariable("entityName") String entityName, HttpServletResponse response)
	{
		response.setHeader("Content-disposition", "attachment; filename=" + entityName + ".xml");
		File file = new File(entityName + ".xml");

		try
		{
			Model model = ModelFactory.createDefaultModel();
			Repository repo = dataService.getRepositoryByEntityName(entityName);
			Iterator<Entity> iter = repo.iterator();

			while (iter.hasNext())
			{
				Entity entity = iter.next();
				String ns = "http://www.molgenis.org/";
				for (AttributeMetaData metaData : entity.getEntityMetaData().getAtomicAttributes())
				{
					if (metaData.isIdAtrribute())
					{
						Resource subject = model.createResource(ns.concat(entity.getString(metaData.getName())));
						Property predicate = model.createProperty(ns.concat("is_a"));
						Resource object = model.createResource(ns.concat(entity.getEntityMetaData().getName()));

						connect(subject, predicate, object, model);
					}
					else
					{
						Resource subject = model.createResource(entity.getString(entity.getEntityMetaData()
								.getIdAttribute().getName()));
						Property predicate = model.createProperty(ns.concat(metaData.getName()));
						Resource object = model.createResource(entity.getString(metaData.getName()) != null ? entity
								.getString(metaData.getName()) : "");

						connect(subject, predicate, object, model);
					}
				}
			}

			try
			{
				FileOutputStream fop = new FileOutputStream(file);
				RDFDataMgr.write(fop, model, RDFFormat.RDFXML_PRETTY);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			/**
			 * InputStream is = new FileInputStream(file); org.apache.commons.io.IOUtils.copy(is,
			 * response.getOutputStream()); response.flushBuffer();
			 **/
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Error writing file" + ex.getMessage());
		}
		return new FileSystemResource(file);
	}

	private static Statement connect(Resource subject, Property predicate, Resource object, Model model)
	{
		model.add(subject, predicate, object);
		return model.createStatement(subject, predicate, object);
	}
}
