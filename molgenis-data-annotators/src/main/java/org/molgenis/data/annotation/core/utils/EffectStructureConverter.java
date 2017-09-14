package org.molgenis.data.annotation.core.utils;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.EFFECT;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.VARIANT;

/**
 * This class is used to convert from (MOLGENIS)SnpEff annotated VCF files
 * to the entity structure expected by Effects annotators (for example GAVIN), and the other way around
 * <p>
 * An effect is an entity containing information about the combination of a variant (CHROM POS REF ALT(single allele)) in a specific GENE
 */
@Component
public class EffectStructureConverter
{
	private EntityTypeFactory entityTypeFactory;
	private AttributeFactory attributeFactory;

	public EffectStructureConverter(EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory)
	{
		this.entityTypeFactory = entityTypeFactory;
		this.attributeFactory = attributeFactory;
	}

	public Iterator<Entity> createVcfEntityStructure(Iterator<Entity> annotatedRecords)
	{
		return new Iterator<Entity>()
		{
			final PeekingIterator<Entity> effects = Iterators.peekingIterator(annotatedRecords);

			EntityType vcfVariantEntityType;
			EntityType effectEntityType;

			private void createResultEntityType(Entity effect, EntityType variantEMD)
			{
				if (vcfVariantEntityType == null || effectEntityType == null)
				{
					effectEntityType = effect.getEntityType();
					vcfVariantEntityType = EntityType.newInstance(variantEMD);
					vcfVariantEntityType.addAttribute(
							attributeFactory.create().setName(EFFECT).setDataType(MREF).setRefEntity(effectEntityType));
				}
			}

			@Override
			public boolean hasNext()
			{
				return effects.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity variant = null;
				String peekedId;
				List<Entity> effectsForVariant = Lists.newArrayList();
				while (effects.hasNext())
				{
					peekedId = effects.peek().getEntity(VARIANT).getIdValue().toString();
					if (variant == null || variant.getIdValue().toString().equals(peekedId))
					{
						Entity effect = effects.next();
						variant = effect.getEntity(VARIANT);
						effectsForVariant.add(effect);
					}
					else
					{
						return createVcfEntityStructureForSingleEntity(variant, effectsForVariant);
					}
				}
				return createVcfEntityStructureForSingleEntity(variant, effectsForVariant);
			}

			private Entity createVcfEntityStructureForSingleEntity(Entity variant, List<Entity> effectsForVariant)
			{
				createResultEntityType(effectsForVariant.get(0), variant.getEntityType());
				Entity newVariant = new DynamicEntity(vcfVariantEntityType);
				newVariant.set(variant);

				if (effectsForVariant.size() > 1)
				{
					newVariant.set(EFFECT, effectsForVariant);
				}
				else
				{
					// is this an empty effect entity?
					Entity effectForVariant = effectsForVariant.get(0);

					if (!isEmptyEffectEntity(effectForVariant)) newVariant.set(EFFECT, effectsForVariant);
				}
				return newVariant;
			}

			private boolean isEmptyEffectEntity(Entity effectEntity)
			{
				boolean isEmpty = true;
				for (Attribute effectAttribute : effectEntityType.getAtomicAttributes())
				{
					//was an empty effect entity created? this entity can be recoginized by the fact that it only has a filled Id attribute and Variant xref
					if (effectAttribute.getName().equals(effectEntityType.getIdAttribute().getName()) || effectAttribute
							.getName()
							.equals(VARIANT))
					{
					}
					else if (effectEntity.get(effectAttribute.getName()) != null)
					{
						isEmpty = false;
						break;
					}
				}
				return isEmpty;
			}
		};
	}

	public Stream<Entity> createVariantEffectStructure(String effectAttributeName, List<Attribute> annotatorAttributes,
			VcfRepository vcfRepository)
	{
		EntityType inputVcfEntityType = vcfRepository.getEntityType();
		EntityType variantEntityType = removeAttributeAndCreateEntityTypeCopy(
				vcfRepository.getEntityType().getAttribute(EFFECT), vcfRepository.getEntityType());

		Attribute effectsAttribute = inputVcfEntityType.getAttribute(effectAttributeName);
		String description = getEffectDescription(effectsAttribute);
		String[] step1 = description.split(":");
		String effectEntityName = StringUtils.deleteWhitespace(step1[0]);
		String attributesString = step1[1].replaceAll("^\\s'|'$", "");

		ArrayList<Attribute> effectFieldAttributeList = parseEffectAttributeDescription(attributesString,
				annotatorAttributes);
		EntityType effectsEntityType = createEffectsEntityType(effectFieldAttributeList, effectEntityName,
				annotatorAttributes);

		return StreamSupport.stream(vcfRepository.spliterator(), false)
							.flatMap(entity -> createVariantEffectStructureForSingleEntity(effectsAttribute,
									effectFieldAttributeList, effectsEntityType, entity, variantEntityType));
	}

	private EntityType removeAttributeAndCreateEntityTypeCopy(Attribute attributeToParse, EntityType inputEntityType)
	{
		EntityType newMeta = EntityType.newInstance(inputEntityType);
		newMeta.removeAttribute(attributeToParse);
		return newMeta;
	}

	private Stream<Entity> createVariantEffectStructureForSingleEntity(Attribute attributeToParse,
			ArrayList<Attribute> effectFieldAttributeList, EntityType effectsEntityType, Entity vcfInputEntity,
			EntityType variantEntityType)
	{
		List<Entity> results = new ArrayList<>();
		Entity variantEntity = new DynamicEntity(variantEntityType);

		for (String attr : variantEntity.getAttributeNames())
		{
			if (vcfInputEntity.getEntityType().getAttribute(attr) != null)
			{
				variantEntity.set(attr, vcfInputEntity.get(attr));
			}
		}

		List<Entity> result = createEffectsEntitiesForSingleVariant(effectsEntityType, effectFieldAttributeList,
				vcfInputEntity.getString(attributeToParse.getName()), variantEntity).collect(Collectors.toList());
		results.addAll(result);
		return results.stream();
	}

	//Get the description of the field that needs to be parsed to determine the attributes of the Entity based on the attribute
	//for example, this line:
	//##INFO=<ID=EFFECT,Number=.,Type=String,Description="EFFECT annotations: 'Alt_Allele | Gene_Name | Annotation | Putative_impact | Gene_ID | Feature_type | Feature_ID | Transcript_biotype | Rank_total | HGVS_c | HGVS_p | cDNA_position | CDS_position | Protein_position | Distance_to_feature | Errors'">
	public String getEffectDescription(Attribute effectAttributeToParse)
	{
		String description = effectAttributeToParse.getDescription();
		if (description.indexOf(':') == -1)
		{
			throw new RuntimeException("Unable to create entitystructure, missing semicolon in description of ["
					+ effectAttributeToParse.getName() + "]");
		}
		return description;
	}

	private EntityType createEffectsEntityType(ArrayList<Attribute> effectFieldAttributeList, String effectEntityName,
			List<Attribute> annotatorAttributes)
	{
		EntityType effectsEntityType = entityTypeFactory.create(effectEntityName);
		effectsEntityType.addAttribute(attributeFactory.create().setName("identifier").setAuto(true).setVisible(false),
				EntityType.AttributeRole.ROLE_ID);
		effectsEntityType.addAttributes(effectFieldAttributeList);

		addAnnotatorAttributes(annotatorAttributes, effectsEntityType);

		effectsEntityType.addAttribute(attributeFactory.create().setName(VARIANT).setDataType(XREF));

		return effectsEntityType;
	}

	//if annotator attributes not present add them
	//check if needed for annotators running on an already annotated file
	private void addAnnotatorAttributes(List<Attribute> annotatorAttributes, EntityType effectsEntityType)
	{
		for (Attribute attr : annotatorAttributes)
		{
			if (effectsEntityType.getAttribute(attr.getName()) == null)
			{
				effectsEntityType.addAttribute(attr);
			}
		}
	}

	//Create a map of attributes based on the pipe separated attribute names in the description
	private ArrayList<Attribute> parseEffectAttributeDescription(String attributesString,
			List<Attribute> annotatorAttributes)
	{
		String[] attributeStrings = attributesString.replaceAll("^\\s'|'$", "").split("\\|");
		ArrayList<Attribute> attributeList = new ArrayList<>();
		Map<String, Attribute> annotatorAttributeMap = VcfUtils.getAttributesMapFromList(annotatorAttributes);
		for (String attribute : attributeStrings)
		{
			AttributeType type = annotatorAttributeMap.containsKey(attribute) ? annotatorAttributeMap.get(attribute)
																									 .getDataType() : STRING;
			Attribute attr = attributeFactory.create()
											 .setName(StringUtils.deleteWhitespace(attribute))
											 .setDataType(type)
											 .setLabel(attribute);
			attributeList.add(attr);
		}
		return attributeList;
	}

	private static Stream<Entity> createEffectsEntitiesForSingleVariant(EntityType effectsEntityType,
			List<Attribute> effectFieldAttributeList, String descriptionFieldsString, Entity variantEntity)
	{
		List<Entity> listOfEffectsEntities = new ArrayList<>();
		if (descriptionFieldsString == null) return listOfEffectsEntities.stream();
		String[] descriptionFieldValues = descriptionFieldsString.split(",");

		for (String descriptionFieldValue : descriptionFieldValues)
		{
			String[] descriptionFieldPartValues = descriptionFieldValue.split("\\|", -1);

			DynamicEntity singleEffectsEntity = new DynamicEntity(effectsEntityType);
			int i = 0;
			for (Attribute attribute : effectFieldAttributeList)
			{
				if (i > descriptionFieldPartValues.length)
				{
					throw new RuntimeException(
							"Description of the attribute contains more values (pipe separated values) than the actual value");
				}
				singleEffectsEntity.set(attribute.getName(), descriptionFieldPartValues[i]);
				i++;
			}
			singleEffectsEntity.set(VARIANT, variantEntity);
			listOfEffectsEntities.add(singleEffectsEntity);
		}
		return listOfEffectsEntities.stream();
	}
}
