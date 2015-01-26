package org.molgenis.data.annotation.impl;

import com.hp.hpl.jena.query.*;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Component("StadiumAnnotator")
public abstract class SparqlAnnotator extends AbstractRepositoryAnnotator  implements RepositoryAnnotator,
        ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger logger = Logger.getLogger(SparqlAnnotator.class);
	private final AnnotationService annotatorService;

	private String service;

    public SparqlAnnotator(AnnotationService annotatorService, String service)
	{
		this.annotatorService = annotatorService;
        this.service = service;
	}

    @Override
    protected boolean annotationDataExists() {
        return true;
    }

	public List<Entity> annotateEntity(Entity entity, String queryString) throws IOException, InterruptedException
	{
        List<Entity> results = new ArrayList<Entity>();
        HashMap<String, Object> resultMap = new HashMap<String, Object>();

        Query query = QueryFactory.create(queryString);
        QueryExecution qExe = QueryExecutionFactory.sparqlService(service, query);
        ResultSet resultSet = qExe.execSelect();
        //ResultSetFormatter.out(System.out, resultSet, query) ;
        //FIXME: only processes the first hit now

        if(resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.next();
            for(AttributeMetaData attributeMetaData : getOutputMetaData().getAttributes()){
                resultMap.put(attributeMetaData.getName(), querySolution.get(attributeMetaData.getName()).toString());
            }
        }
        results.add(getAnnotatedEntity(entity, resultMap));

		return results;
	}
}
