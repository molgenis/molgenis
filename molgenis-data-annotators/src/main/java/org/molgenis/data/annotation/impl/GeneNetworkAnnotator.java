package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.impl.datastructures.JsonReader;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.context.event.ContextRefreshedEvent;
import org.json.*;

public class GeneNetworkAnnotator extends AbstractRepositoryAnnotator implements RepositoryAnnotator
{
	private static final String NAME = "GENENETWORK";

	private final AnnotationService annotatorService;

	private JSONObject geneNetworkJsonCallback;

	public final String HPO_TERMS = "HPO_TERMS";
	public final String HGNC_SYMBOL = "HGNC_SYMBOL";

	// In the future will change to a P-value
	public static final String SCORE = "SCORE";

	public GeneNetworkAnnotator(AnnotationService annotatorService)
	{
		this.annotatorService = annotatorService;
	}

	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		// Call molgenis server once
		if (geneNetworkJsonCallback == null)
		{
			String geneNetworkUrl = "http://molgenis58.target.rug.nl/api/v1/prioritization/"
					+ entity.getString(HPO_TERMS) + "?verbose&genes=" + entity.getString(HGNC_SYMBOL);
			geneNetworkJsonCallback = JsonReader.readJsonFromUrl(geneNetworkUrl);
		}

		JSONArray jsonResults = geneNetworkJsonCallback.getJSONArray("results");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (int i = 0; i < jsonResults.length(); i++)
		{
			resultMap.put(SCORE, jsonResults.getJSONObject(i).getDouble("weightedZScore"));
		}

		List<Entity> results = new ArrayList<Entity>();
		results.add(getAnnotatedEntity(entity, resultMap));
		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SCORE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_TERMS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HGNC_SYMBOL, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public boolean canAnnotate(EntityMetaData inputMetaData)
	{
		return true;
	}

	@Override
	public String getFullName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean annotationDataExists()
	{
		return true;
	}

}
