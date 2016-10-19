package org.molgenis.fair.controller;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.converter.RDFMediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.StringWriter;
import java.util.Iterator;

import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(BASE_URI)
public class FairController
{
	static final String BASE_URI = "/fdp";

	private final DataService dataService;
	private final MetaDataService metaDataService;
	@Autowired
	public FairController(DataService dataService,MetaDataService metaDataService)
	{
		this.dataService = dataService;
		this.metaDataService = metaDataService;
	}

	@RequestMapping(method = GET, produces = RDFMediaType.TEXT_TURTLE_VALUE)
	@ResponseBody
	@RunAsSystem
	public Entity getMetadata()
	{
		return dataService.findOne("fdp_Metadata", new QueryImpl<>());
	}

	@RequestMapping(value = "/test", method = GET)
	@ResponseBody
	public String test()
	{
		return getRDF("sys_sec_User");
	}

	public String getRDF(String entityName)
	{
		StringWriter stringWriter = new StringWriter();

		try
		{
			Model model = ModelFactory.createDefaultModel();
			Repository repo = metaDataService.getRepository(entityName);
			Iterator<Entity> iter = repo.iterator();
			while (iter.hasNext())
			{
				Entity entity = iter.next();
				String ns = "http://www.molgenis.org/";
				for (Attribute metaData : entity.getEntityType().getAtomicAttributes())
				{
						Resource subject = model.createResource("/" + entity.getEntityType().getName().concat("/")
								.concat(entity.getIdValue().toString()));
						Property predicate = model
								.createProperty("");
						Resource object = model.createResource(entity.get(metaData.getName()) != null ? entity
								.get(metaData.getName()).toString() : "");

						connect(subject, predicate, object, model);

					RDFDataMgr.write(stringWriter, model, RDFFormat.TRIG_PRETTY);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return stringWriter.toString();
	}

	private static Statement connect(Resource subject, Property predicate, Resource object, Model model)
	{
		model.add(subject, predicate, object);
		return model.createStatement(subject, predicate, object);
	}
}
