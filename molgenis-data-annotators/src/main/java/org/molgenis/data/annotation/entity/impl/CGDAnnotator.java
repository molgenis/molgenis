package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.LONG;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.AGE_GROUP;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.ALLELIC_CONDITIONS;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.COMMENTS;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.CONDITION;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.ENTREZ_GENE_ID;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.GENE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.GENERALIZED_INHERITANCE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.HGNC_ID;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.INHERITANCE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.INTERVENTION_CATEGORIES;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.INTERVENTION_RATIONALE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.MANIFESTATION_CATEGORIES;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.REFERENCES;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.GeneralizedInheritance.DOMINANT;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.GeneralizedInheritance.DOM_OR_REC;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.GeneralizedInheritance.OTHER;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.GeneralizedInheritance.RECESSIVE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.GeneralizedInheritance.XLINKED;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.filter.FirstResultFilter;
import org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.query.AttributeEqualsQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Annotator that can add HGNC_ID and ENTREZ_GENE_ID and other attributes to an entity that has a attribute named 'GENE'
 * that must be the HGNC gene name.
 * 
 * It reads this info from a tab separated CGD file. The location of this file is defined by a RuntimeProperty named
 * 'cgd_location'
 */
@Configuration
public class CGDAnnotator
{
	public static final String CGD_FILE_LOCATION_PROPERTY = "cgd_location";
	private static String CGD_RESOURCE = "CGDResource";
	private static final char SEPARATOR = '\t';

	// Output attribute labels
	private static final String CONDITION_LABEL = "CGDCOND";
	private static final String AGE_GROUP_LABEL = "CGDAGE";
	private static final String INHERITANCE_LABEL = "CGDINH";
	private static final String GENERALIZED_INHERITANCE_LABEL = "CGDGIN";

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	public static enum GeneralizedInheritance
	{
		DOM_OR_REC, DOMINANT, RECESSIVE, XLINKED, OTHER
	}

	public enum CGDAttributeName
	{
		GENE("#GENE", SnpEffAnnotator.GENE_NAME), REFERENCES("REFERENCES", "REFS"), INTERVENTION_RATIONALE(
				"INTERVENTION/RATIONALE", "INTERVENTION_RATIONALE"), COMMENTS("COMMENTS", "COMMENTS"), INTERVENTION_CATEGORIES(
				"INTERVENTION CATEGORIES", "INTERVENTION_CATEGORIES"), MANIFESTATION_CATEGORIES(
				"MANIFESTATION CATEGORIES", "MANIFESTATION_CATEGORIES"), ALLELIC_CONDITIONS("ALLELIC CONDITIONS",
				"ALLELIC_CONDITIONS"), ENTREZ_GENE_ID("ENTREZ GENE ID", "ENTREZ_GENE_ID"), HGNC_ID("HGNC ID", "HGNC_ID"), CONDITION(
				"CONDITION", CONDITION_LABEL), AGE_GROUP("AGE GROUP", AGE_GROUP_LABEL), INHERITANCE("INHERITANCE", INHERITANCE_LABEL),
				GENERALIZED_INHERITANCE("", GENERALIZED_INHERITANCE_LABEL);

		private final String cgdName;// Column name as defined in CGD file
		private final String attributeName;// Output attribute name

		// Mapping from attribute name to cgd name
		private static Map<String, String> mappings = new HashMap<String, String>();

		private CGDAttributeName(String cgdName, String attributeName)
		{
			this.cgdName = cgdName;
			this.attributeName = attributeName;
		}

		public static String getCgdName(String attributeName)
		{
			if (mappings.isEmpty())
			{
				for (CGDAttributeName enumValue : CGDAttributeName.values())
				{
					mappings.put(enumValue.getAttributeName(), enumValue.getCgdName());
				}
			}

			return mappings.get(attributeName);
		}

		public String getCgdName()
		{
			return cgdName;
		}

		public String getAttributeName()
		{
			return attributeName;
		}
	}

	@Bean
	public RepositoryAnnotator cgd()
	{
		AnnotatorInfo info = getAnnotatorInfo();
		QueryCreator queryCreator = new AttributeEqualsQueryCreator(new DefaultAttributeMetaData(
				GENE.getAttributeName()));
		ResultFilter resultFilter = new FirstResultFilter();

		EntityAnnotator entityAnnotator = new CGDEntityAnnotator(CGD_RESOURCE, info, queryCreator, resultFilter,
				dataService, resources);

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	public Resource cgdResource()
	{
		return new ResourceImpl(CGD_RESOURCE, new SingleResourceConfig(CGD_FILE_LOCATION_PROPERTY, molgenisSettings),
				new RepositoryFactory()
				{
					@Override
					public Repository createRepository(File file) throws IOException
					{
						return new GeneCsvRepository(file, GENE.getCgdName(), GENE.getAttributeName(), SEPARATOR);
					}
				});
	}

	private AnnotatorInfo getAnnotatorInfo()
	{
		return AnnotatorInfo.create(Status.BETA, Type.PHENOTYPE_ASSOCIATION, "CGD", "Clinical Genomics Database",
				getOutputAttributes());
	}

	private List<AttributeMetaData> getOutputAttributes()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();

		attributes.add(new DefaultAttributeMetaData(HGNC_ID.getAttributeName(), LONG));
		attributes.add(new DefaultAttributeMetaData(ENTREZ_GENE_ID.getAttributeName(), TEXT));
		attributes.add(new DefaultAttributeMetaData(CONDITION.getAttributeName(), TEXT).setLabel(CONDITION_LABEL));
		attributes.add(new DefaultAttributeMetaData(INHERITANCE.getAttributeName(), TEXT).setLabel(INHERITANCE_LABEL));
		attributes.add(new DefaultAttributeMetaData(GENERALIZED_INHERITANCE.getAttributeName(), TEXT)
				.setLabel(GENERALIZED_INHERITANCE_LABEL));
		attributes.add(new DefaultAttributeMetaData(AGE_GROUP.getAttributeName(), TEXT).setLabel(AGE_GROUP_LABEL));
		attributes.add(new DefaultAttributeMetaData(ALLELIC_CONDITIONS.getAttributeName(), TEXT));
		attributes.add(new DefaultAttributeMetaData(MANIFESTATION_CATEGORIES.getAttributeName(), TEXT));
		attributes.add(new DefaultAttributeMetaData(INTERVENTION_CATEGORIES.getAttributeName(), TEXT));
		attributes.add(new DefaultAttributeMetaData(COMMENTS.getAttributeName(), TEXT));
		attributes.add(new DefaultAttributeMetaData(INTERVENTION_RATIONALE.getAttributeName(), TEXT));
		attributes.add(new DefaultAttributeMetaData(REFERENCES.getAttributeName(), TEXT));

		return attributes;
	}

	private class CGDEntityAnnotator extends AnnotatorImpl
	{
		public CGDEntityAnnotator(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
				ResultFilter resultFilter, DataService dataService, Resources resources)
		{
			super(sourceRepositoryName, info, queryCreator, resultFilter, dataService, resources,
					new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(CGD_FILE_LOCATION_PROPERTY,
							molgenisSettings));
		}

		@Override
		protected Object getResourceAttributeValue(AttributeMetaData attr, Entity sourceEntity)
		{
			if (attr.getName().equals(GENERALIZED_INHERITANCE.getAttributeName()))
			{
				return getGeneralizedInheritance(sourceEntity);
			}

			String sourceName = CGDAttributeName.getCgdName(attr.getName());
			if (sourceName == null) throw new MolgenisDataException("Unknown attribute [" + attr.getName() + "]");

			return sourceEntity.get(sourceName);
		}

		private GeneralizedInheritance getGeneralizedInheritance(Entity sourceEntity)
		{
			GeneralizedInheritance inherMode = OTHER;
			String value = sourceEntity.getString(INHERITANCE.getCgdName());
			if (value != null)
			{
				if (value.contains("AD") && value.contains("AR"))
				{
					inherMode = DOM_OR_REC;
				}
				else if (value.contains("AR"))
				{
					inherMode = RECESSIVE;
				}
				else if (value.contains("AD"))
				{
					inherMode = DOMINANT;
				}
				else if (value.contains("XL"))
				{
					inherMode = XLINKED;
				}
			}

			return inherMode;
		}
	}

}
