package org.molgenis.data.annotation.impl;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


@Component("StadiumAnnotator2")
public class StadiumAnnotator2 extends SparqlAnnotator
{
	private static final Logger logger = Logger.getLogger(StadiumAnnotator2.class);
	private final AnnotationService annotatorService;

	private static final String NAME = "StadiumAnnotator2";
    private static final String URL = "http://dbpedia.org/sparql";

	@Autowired
	public StadiumAnnotator2(AnnotationService annotatorService)
	{
        super(annotatorService, URL);
		this.annotatorService = annotatorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		//annotatorService.addAnnotator(this);
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
        return annotateEntity(entity, getQuery(entity));
	}

    public String getQuery(Entity entity) {
        return "PREFIX  g:    <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" +
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

        metadata.addAttributeMetaData(new DefaultAttributeMetaData("long", FieldTypeEnum.DECIMAL));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData("lat", FieldTypeEnum.DECIMAL));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData("country", FieldTypeEnum.STRING));

        return metadata;
    }

}
