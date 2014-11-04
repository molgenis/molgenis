package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("hgncSymbolService")
public class HgncSymbolServiceAnnotator extends LocusAnnotator
{
	private final AnnotationService annotatorService;
	private final HgncLocationsProvider hgncLocationsProvider;

	static final String HGNC_SYMBOL = "HGNC_SYMBOL";
	private static final String NAME = "HGNC-Symbol";

	@Autowired
	public HgncSymbolServiceAnnotator(AnnotationService annotatorService, HgncLocationsProvider hgncLocationsProvider)
	{
		this.annotatorService = annotatorService;
		this.hgncLocationsProvider = hgncLocationsProvider;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	protected boolean annotationDataExists()
	{
		// TODO Check if web service is available
		return true;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		List<Entity> results = new ArrayList<Entity>();

		String chromosome = entity.getString(CHROMOSOME);
		Long position = entity.getLong(POSITION);

		Locus locus = new Locus(chromosome, position);
		String hgncSymbol = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(), locus).get(0);

		HashMap<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put(HGNC_SYMBOL, hgncSymbol);
		resultMap.put(CHROMOSOME, chromosome);
		resultMap.put(POSITION, position);

		results.add(new MapEntity(resultMap));

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HGNC_SYMBOL, FieldTypeEnum.STRING));

		return metadata;
	}
}
