package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.*;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.core.entity.impl.framework.AnnotatorImpl;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.filter.FirstResultFilter;
import org.molgenis.data.annotation.core.query.AttributeEqualsQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.GeneCsvRepository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.annotation.core.entity.impl.CGDAnnotator.CGDAttributeName.*;
import static org.molgenis.data.annotation.core.entity.impl.CGDAnnotator.GeneralizedInheritance.*;
import static org.molgenis.data.annotation.web.settings.CGDAnnotatorSettings.Meta.CGD_LOCATION;

/**
 * Annotator that can add HGNC_ID and ENTREZ_GENE_ID and other attributes to an entityMetaData that has a attribute named 'GENE'
 * that must be the HGNC gene dataType.
 * <p>
 * It reads this info from a tab separated CGD file. The location of this file is defined by a RuntimeProperty named
 * 'cgd_location'
 */
@Configuration
public class CGDAnnotator implements AnnotatorConfig
{
	public static final String NAME = "CGD";

	private static String CGD_RESOURCE = "CGDResource";
	private static final char SEPARATOR = '\t';

	// Output attribute labels
	public static final String CONDITION_LABEL = "CGDCOND";
	public static final String AGE_GROUP_LABEL = "CGDAGE";
	public static final String INHERITANCE_LABEL = "CGDINH";
	public static final String GENERALIZED_INHERITANCE_LABEL = "CGDGIN";

	@Autowired
	private Entity CGDAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	private RepositoryAnnotatorImpl annotator;

	public enum GeneralizedInheritance
	{
		DOM_OR_REC, DOMINANT, RECESSIVE, XLINKED, OTHER
	}

	public enum CGDAttributeName
	{
		GENE("#GENE", EffectsMetaData.GENE_NAME), REFERENCES("REFERENCES", "REFS"), INTERVENTION_RATIONALE(
			"INTERVENTION/RATIONALE", "INTERVENTION_RATIONALE"), COMMENTS("COMMENTS",
			"COMMENTS"), INTERVENTION_CATEGORIES("INTERVENTION CATEGORIES",
			"INTERVENTION_CATEGORIES"), MANIFESTATION_CATEGORIES("MANIFESTATION CATEGORIES",
			"MANIFESTATION_CATEGORIES"), ALLELIC_CONDITIONS("ALLELIC CONDITIONS", "ALLELIC_CONDITIONS"), ENTREZ_GENE_ID(
			"ENTREZ GENE ID", "ENTREZ_GENE_ID"), HGNC_ID("HGNC ID", "HGNC_ID"), CONDITION("CONDITION",
			CONDITION_LABEL), AGE_GROUP("AGE GROUP", AGE_GROUP_LABEL), INHERITANCE("INHERITANCE",
			INHERITANCE_LABEL), GENERALIZED_INHERITANCE("", GENERALIZED_INHERITANCE_LABEL);

		private final String cgdName;// Column dataType as defined in CGD file
		private final String attributeName;// Output attribute dataType

		// Mapping from attribute dataType to cgd dataType
		private static Map<String, String> mappings = new HashMap<String, String>();

		CGDAttributeName(String cgdName, String attributeName)
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
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		AnnotatorInfo info = getAnnotatorInfo();
		QueryCreator queryCreator = new AttributeEqualsQueryCreator(
				attributeMetaDataFactory.create().setName(GENE.getAttributeName()));
		ResultFilter resultFilter = new FirstResultFilter();

		EntityAnnotator entityAnnotator = new CGDEntityAnnotator(CGD_RESOURCE, info, queryCreator, resultFilter,
				dataService, resources);
		annotator.init(entityAnnotator);
	}

	@Bean
	public Resource cgdResource()
	{
		return new ResourceImpl(CGD_RESOURCE, new SingleResourceConfig(CGD_LOCATION, CGDAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new RepositoryFactory()
				{
					@Override
					public Repository<Entity> createRepository(File file) throws IOException
					{
						return new GeneCsvRepository(file, GENE.getCgdName(), GENE.getAttributeName(),
								entityMetaDataFactory, attributeMetaDataFactory, SEPARATOR);
					}
				};
			}
		};

	}

	private AnnotatorInfo getAnnotatorInfo()
	{
		return AnnotatorInfo.create(Status.READY, Type.PHENOTYPE_ASSOCIATION, "CGD", "Clinical Genomics Database",
				getOutputAttributes());
	}

	private List<AttributeMetaData> getOutputAttributes()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();

		attributes.add(attributeMetaDataFactory.create().setName(HGNC_ID.getAttributeName()).setDataType(STRING));
		attributes.add(attributeMetaDataFactory.create().setName(ENTREZ_GENE_ID.getAttributeName()).setDataType(TEXT));
		attributes.add(attributeMetaDataFactory.create().setName(CONDITION.getAttributeName()).setDataType(TEXT)
				.setLabel(CONDITION_LABEL));
		attributes.add(attributeMetaDataFactory.create().setName(INHERITANCE.getAttributeName()).setDataType(TEXT)
				.setLabel(INHERITANCE_LABEL));
		attributes.add(attributeMetaDataFactory.create().setName(GENERALIZED_INHERITANCE.getAttributeName())
				.setDataType(TEXT).setLabel(GENERALIZED_INHERITANCE_LABEL));
		attributes.add(attributeMetaDataFactory.create().setName(AGE_GROUP.getAttributeName()).setDataType(TEXT)
				.setLabel(AGE_GROUP_LABEL));
		attributes.add(attributeMetaDataFactory.create().setName(ALLELIC_CONDITIONS.getAttributeName())
				.setDataType(TEXT));
		attributes.add(attributeMetaDataFactory.create().setName(MANIFESTATION_CATEGORIES.getAttributeName())
				.setDataType(TEXT));
		attributes.add(attributeMetaDataFactory.create().setName(INTERVENTION_CATEGORIES.getAttributeName())
				.setDataType(TEXT));
		attributes.add(attributeMetaDataFactory.create().setName(COMMENTS.getAttributeName()).setDataType(TEXT));
		attributes.add(attributeMetaDataFactory.create().setName(INTERVENTION_RATIONALE.getAttributeName())
				.setDataType(TEXT));
		attributes.add(attributeMetaDataFactory.create().setName(REFERENCES.getAttributeName()).setDataType(TEXT));

		return attributes;
	}

	private class CGDEntityAnnotator extends AnnotatorImpl
	{
		public CGDEntityAnnotator(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
				ResultFilter resultFilter, DataService dataService, Resources resources)
		{
			super(sourceRepositoryName, info, queryCreator, resultFilter, dataService, resources,
					new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(CGD_LOCATION, CGDAnnotatorSettings));
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

		private String getGeneralizedInheritance(Entity sourceEntity)
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

			return inherMode.toString();
		}
	}

}
