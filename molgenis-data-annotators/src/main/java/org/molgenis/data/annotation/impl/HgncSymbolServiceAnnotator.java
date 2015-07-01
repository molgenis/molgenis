package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.utils.HgncLocationsUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("hgncSymbolService")
public class HgncSymbolServiceAnnotator extends LocusAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ClinicalGenomicsDatabaseServiceAnnotator.class);
	private final HgncLocationsProvider hgncLocationsProvider;

	static final String HGNC_SYMBOL = "HGNC_SYMBOL";
	private static final String NAME = "HGNC_Symbol";
	private Map<String, HGNCLocations> hgncLocations = new HashMap<>();

	@Autowired
	public HgncSymbolServiceAnnotator(HgncLocationsProvider hgncLocationsProvider)
	{
		this.hgncLocationsProvider = hgncLocationsProvider;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	protected boolean annotationDataExists()
	{
		// FIXME Check if web service is available
		return true;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		getAnnotationDataFromSources();

		String chromosome = entity.getString(VcfRepository.CHROM);
		Long position = entity.getLong(VcfRepository.POS);
		Locus locus = new Locus(chromosome, position);
		HashMap<String, Object> resultMap = new HashMap<>();

		resultMap.put(HGNC_SYMBOL, HgncLocationsUtils.locationToHgcn(hgncLocations, locus).get(0));

		List<Entity> results = new ArrayList<Entity>();
		results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));

		return results;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(new DefaultAttributeMetaData(HGNC_SYMBOL, FieldTypeEnum.STRING));

		return metadata;
	}

	private void getAnnotationDataFromSources() throws IOException
	{
		if (this.hgncLocations.isEmpty())
		{
			LOG.info("hgncLocations empty, started fetching the data");
			this.hgncLocations = hgncLocationsProvider.getHgncLocations();
			LOG.info("finished fetching the hgncLocations data");
		}
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return AnnotatorInfo.create(Status.INDEV, Type.UNUSED, "unknown",
				"This is the description for the HGNC Annotator", getOutputMetaData());
	}
}
