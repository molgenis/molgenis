package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.HGNCLoc;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("CgdService")
public class ClinicalGenomicsDatabaseServiceAnnotator extends LocusAnnotator
{
	private static final String REFERENCES = "REFERENCES";
	private static final String INTERVENTION_RATIONALE = "INTERVENTION / RATIONALE";
	private static final String COMMENTS = "COMMENTS";
	private static final String INTERVENTION_CATEGORIES = "INTERVENTION CATEGORIES";
	private static final String MANIFESTATION_CATEGORIES = "MANIFESTATION CATEGORIES";
	private static final String ALLELIC_CONDITIONS = "ALLELIC CONDITIONS";
	private static final String AGE_GROUP = "AGE GROUP";
	private static final String INHERITANCE = "INHERITANCE";
	private static final String CONDITION = "CONDITION";
	private static final String ENTREZ_GENE_ID = "ENTREZ GENE ID";
	private static final String GENE = "Gene";
	private static final String NAME = "Clinical Genomic Database";

	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";

	private static final String CGD_FILE = "/Users/mdehaan/Downloads/CGD.txt";

	@Autowired
	AnnotationService annotatorService;

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
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		List<Entity> results = new ArrayList<Entity>();

		try
		{
			HashMap<String, HGNCLoc> hgncLocs = OmimHpoAnnotator.getHgncLocs();

			while (source.hasNext())
			{
				Entity entity = source.next();

				Long position = entity.getLong(POSITION);
				String chromosome = entity.getString(CHROMOSOME);

				List<Locus> locus = new ArrayList<Locus>(Arrays.asList(new Locus(chromosome, position)));
				List<String> geneSymbols = OmimHpoAnnotator.locationToHGNC(hgncLocs, locus);

				FileReader fileReader = new FileReader(CGD_FILE);
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				while (bufferedReader.ready())
				{
					String line = bufferedReader.readLine();
					if (!line.startsWith("#"))
					{
						String[] split = line.split("\t");
						for (String gene : geneSymbols)
						{
							// This gene (so variant) matches this line (cool)
							if(gene.equals(split[0])){
								HashMap<String, Object> resultMap = new HashMap<String, Object>();
								resultMap.put(GENE, split[0]);
								resultMap.put(ENTREZ_GENE_ID, split[1]);
								resultMap.put(CONDITION, split[2]);
								resultMap.put(INHERITANCE, split[3]);
								resultMap.put(AGE_GROUP, split[4]);
								resultMap.put(ALLELIC_CONDITIONS, split[5]);
								resultMap.put(MANIFESTATION_CATEGORIES, split[6]);
								resultMap.put(INTERVENTION_CATEGORIES, split[7]);
								resultMap.put(COMMENTS, split[8]);
								resultMap.put(INTERVENTION_RATIONALE, split[9]);
								resultMap.put(REFERENCES, split[10]);
								resultMap.put(CHROMOSOME, chromosome);
								resultMap.put(POSITION, position);
								
								results.add(new MapEntity(resultMap));
							}
						}
					}
				}

				bufferedReader.close();

			}

		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return results.iterator();
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENTREZ_GENE_ID, MolgenisFieldTypes.FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CONDITION, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AGE_GROUP, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALLELIC_CONDITIONS,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MANIFESTATION_CATEGORIES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERVENTION_CATEGORIES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(COMMENTS, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERVENTION_RATIONALE,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REFERENCES, MolgenisFieldTypes.FieldTypeEnum.STRING));

		return metadata;
	}
}
