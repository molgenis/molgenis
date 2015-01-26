package org.molgenis.data.annotation.impl;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.*;
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

import com.hp.hpl.jena.query.*;


@Component("StadiumAnnotator")
public class StadiumAnnotator extends AbstractRepositoryAnnotator  implements RepositoryAnnotator,
        ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger logger = Logger.getLogger(StadiumAnnotator.class);
	private final AnnotationService annotatorService;

	private static final String NAME = "StadiumAnnotator";

	@Autowired
	public StadiumAnnotator(AnnotationService annotatorService)
	{
		this.annotatorService = annotatorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

    @Override
    protected boolean annotationDataExists() {
        return true;
    }

    @Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
        List<Entity> results = new ArrayList<Entity>();
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        String queryString = "PREFIX  g:    <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" +
                "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX  onto: <http://dbpedia.org/ontology/>\n" +
                "\n" +
                "SELECT  ?subject ?stadium ?lat ?long\n" +
                "WHERE\n" +
                "  { ?subject g:lat ?lat .\n" +
                "    ?subject g:long ?long .\n" +
                "    ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> onto:Stadium .\n" +
                "    ?subject rdfs:label ?stadium\n" +
                "    FILTER ( ( ( ( ( ?lat >= "+(entity.getDouble("lat")-0.05)+" ) && ( ?lat <=  "+(entity.getDouble("lat")+0.05)+"  ) ) && ( ?long >=  "+(entity.getDouble("long")-0.05)+"  ) ) && ( ?long <=  "+(entity.getDouble("long")+0.05)+"  ) ) && ( lang(?stadium) = \"en\" ) )\n" +
                "  }\n" +
                "LIMIT   5";

        Query query = QueryFactory.create(queryString);
        QueryExecution qExe = QueryExecutionFactory.sparqlService( "http://dbpedia.org/sparql", query );
        ResultSet resultSet = qExe.execSelect();
        //ResultSetFormatter.out(System.out, resultSet, query) ;
        //FIXME: only processes the first hit now
        if(resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.next();
            resultMap.put("subject", querySolution.get("subject").toString());
            resultMap.put("stadium", querySolution.get("stadium").toString());
        }
        results.add(getAnnotatedEntity(entity, resultMap));

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData("subject", FieldTypeEnum.STRING));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData("stadium", FieldTypeEnum.STRING));

		return metadata;
	}

    @Override
    public EntityMetaData getInputMetaData()
    {
        DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

        metadata.addAttributeMetaData(new DefaultAttributeMetaData("long", MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData("lat", MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData("country", MolgenisFieldTypes.FieldTypeEnum.STRING));

        return metadata;
    }

}
