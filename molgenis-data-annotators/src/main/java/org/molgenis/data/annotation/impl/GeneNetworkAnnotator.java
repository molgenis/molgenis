package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.mini.AnnotatorInfo;
import org.molgenis.data.annotation.mini.AnnotatorInfo.Status;
import org.molgenis.data.annotation.mini.AnnotatorInfo.Type;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.utils.JsonReader;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.context.event.ContextRefreshedEvent;

public class GeneNetworkAnnotator extends LocusAnnotator
{
	private static final String NAME = "GENENETWORK";
	// FIXME hardcopy url
	public static final String GENE_NETWORK_API_URL = "http://molgenis58.target.rug.nl/api/v1/prioritization/";

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

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// FIXME: disabled for now
		// annotatorService.addAnnotator(this);
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		// Call molgenis server once
		if (geneNetworkJsonCallback == null)
		{
			String geneNetworkUrl = GENE_NETWORK_API_URL + entity.getString(HPO_TERMS) + "?verbose&genes="
					+ entity.getString(HGNC_SYMBOL);
			geneNetworkJsonCallback = JsonReader.readJsonFromUrl(geneNetworkUrl);
		}

		JSONArray jsonResults = geneNetworkJsonCallback.getJSONArray("results");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (int i = 0; i < jsonResults.length(); i++)
		{
			resultMap.put(SCORE, jsonResults.getJSONObject(i).getDouble("weightedZScore"));
		}

		List<Entity> results = new ArrayList<Entity>();
		results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
		return results;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(new DefaultAttributeMetaData(SCORE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public List<AttributeMetaData> getInputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(new DefaultAttributeMetaData(HPO_TERMS, FieldTypeEnum.STRING));
		metadata.add(new DefaultAttributeMetaData(HGNC_SYMBOL, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
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

	@Override
	public AnnotatorInfo getInfo()
	{
		return AnnotatorInfo.create(Status.INDEV, Type.UNUSED, "unknown", "no description", getOutputMetaData());
	}

}
