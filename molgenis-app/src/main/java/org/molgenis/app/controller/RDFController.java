package org.molgenis.app.controller;

import com.hp.hpl.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static org.molgenis.app.controller.RDFController.URI;

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

    @RequestMapping(value = "jsonld/{entityName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getFile(
            @PathVariable("entityName") String entityName,
            HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename="+ entityName+".json");
        File file = new File(entityName+".json");

        try {
            Model model = ModelFactory.createDefaultModel();
            Repository repo = dataService.getRepositoryByEntityName(entityName);
            Iterator<Entity> iter = repo.iterator();

            while (iter.hasNext()){
                Entity entity = iter.next();
                String ns = "http://www.molgenis.org/";
                for (AttributeMetaData metaData : entity.getEntityMetaData().getAtomicAttributes()) {
                    if (metaData.isIdAtrribute()) {
                        Resource subject = model.createResource(ns.concat(entity.getString(metaData.getName())));
                        Property predicate = model.createProperty(ns.concat("is_a"));
                        Resource object = model.createResource(ns.concat(entity.getEntityMetaData().getName()));

                        connect(subject, predicate, object, model);
                    } else {
                        Resource subject = model.createResource(entity.getString(entity.getEntityMetaData().getIdAttribute().getName()));
                        Property predicate = model.createProperty(metaData.getName());
                        Resource object = model.createResource(entity.getString(metaData.getName())!=null?entity.getString(metaData.getName()):"");

                        connect(subject, predicate, object, model);
                    }
                }
            }

            try {
                FileOutputStream fop = new FileOutputStream(file);
                RDFDataMgr.write(fop, model, RDFFormat.JSONLD) ;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /**InputStream is = new FileInputStream(file);
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer(); **/
        } catch (Exception ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
        return new FileSystemResource(file);
    }

    @RequestMapping(value = "xml/{entityName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getXMLFile(
            @PathVariable("entityName") String entityName,
            HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename="+ entityName+".xml");
        File file = new File(entityName+".xml");

        try {
            Model model = ModelFactory.createDefaultModel();
            Repository repo = dataService.getRepositoryByEntityName(entityName);
            Iterator<Entity> iter = repo.iterator();

            while (iter.hasNext()){
                Entity entity = iter.next();
                String ns = "http://www.molgenis.org/";
                for (AttributeMetaData metaData : entity.getEntityMetaData().getAtomicAttributes()) {
                    if (metaData.isIdAtrribute()) {
                        Resource subject = model.createResource(ns.concat(entity.getString(metaData.getName())));
                        Property predicate = model.createProperty(ns.concat("is_a"));
                        Resource object = model.createResource(ns.concat(entity.getEntityMetaData().getName()));

                        connect(subject, predicate, object, model);
                    } else {
                        Resource subject = model.createResource(entity.getString(entity.getEntityMetaData().getIdAttribute().getName()));
                        Property predicate = model.createProperty(ns.concat(metaData.getName()));
                        Resource object = model.createResource(entity.getString(metaData.getName())!=null?entity.getString(metaData.getName()):"");

                        connect(subject, predicate, object, model);
                    }
                }
            }

            try {
                FileOutputStream fop = new FileOutputStream(file);
                RDFDataMgr.write(fop, model, RDFFormat.RDFXML_PRETTY) ;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /**InputStream is = new FileInputStream(file);
             org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
             response.flushBuffer(); **/
        } catch (Exception ex) {
            throw new RuntimeException("Error writing file"+ex.getMessage());
        }
        return new FileSystemResource(file);
    }

    private static Statement connect(
            Resource subject,
            Property predicate,
            Resource object,
            Model model)
    {
        model.add(subject, predicate, object);
        return model.createStatement(subject, predicate, object);
    }
}
