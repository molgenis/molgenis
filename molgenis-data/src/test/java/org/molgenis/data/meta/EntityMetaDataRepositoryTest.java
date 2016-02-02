package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class EntityMetaDataRepositoryTest
{
	private PackageRepository packageRepository;
	private EntityMetaDataRepository entityMetaDataRepository;
	private AttributeMetaDataRepository attributeRepository;
	private Repository entityMetaRepo;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		ManageableRepositoryCollection collection = mock(ManageableRepositoryCollection.class);
		entityMetaRepo = mock(Repository.class);
		when(collection.addEntityMeta(EntityMetaDataRepository.META_DATA)).thenReturn(entityMetaRepo);
		packageRepository = mock(PackageRepository.class);
		attributeRepository = mock(AttributeMetaDataRepository.class);
		LanguageService languageService = mock(LanguageService.class);
		entityMetaDataRepository = new EntityMetaDataRepository(collection, packageRepository, attributeRepository,
				languageService);
	}

	@Test
	public void add()
	{
		AttributeMetaData idAttr = mock(AttributeMetaData.class);
		String idAttrName = "idAttr";
		when(idAttr.getName()).thenReturn(idAttrName);
		AttributeMetaData labelAttr = mock(AttributeMetaData.class);
		String labelAttrName = "labelAttr";
		when(labelAttr.getName()).thenReturn(labelAttrName);
		AttributeMetaData lookupAttr0 = mock(AttributeMetaData.class);
		String lookupAttr0Name = "lookupAttr0";
		when(lookupAttr0.getName()).thenReturn(lookupAttr0Name);
		AttributeMetaData lookupAttr1 = mock(AttributeMetaData.class);
		String lookupAttr1Name = "lookupAttr1";
		when(lookupAttr1.getName()).thenReturn(lookupAttr1Name);

		String packageName = "package";
		// must mock PackageImpl instead of Package due to case in EntityMetaDataRepository
		PackageImpl package_ = mock(PackageImpl.class);
		when(package_.getName()).thenReturn(packageName);

		String entityName = "entity";
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getPackage()).thenReturn(package_);
		when(entityMeta.getSimpleName()).thenReturn(entityName);
		when(entityMeta.getName()).thenReturn(packageName + '_' + entityName);
		when(entityMeta.getAttributes()).thenReturn(Arrays.asList(idAttr, labelAttr, lookupAttr0, lookupAttr1));
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getOwnIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getLabelAttribute()).thenReturn(labelAttr);
		when(entityMeta.getOwnLabelAttribute()).thenReturn(labelAttr);
		when(entityMeta.getLookupAttributes()).thenReturn(Arrays.asList(lookupAttr0, lookupAttr1));
		when(entityMeta.getOwnLookupAttributes()).thenReturn(Arrays.asList(lookupAttr0, lookupAttr1));
		when(entityMeta.getAttributes()).thenReturn(Arrays.asList(idAttr, labelAttr, lookupAttr0, lookupAttr1));
		when(entityMeta.getOwnAttributes()).thenReturn(Arrays.asList(idAttr, labelAttr, lookupAttr0, lookupAttr1));

		Entity idAttrEntity = mock(Entity.class);
		when(idAttrEntity.getString(AttributeMetaDataMetaData.NAME)).thenReturn(idAttrName);
		Entity labelAttrEntity = mock(Entity.class);
		when(labelAttrEntity.getString(AttributeMetaDataMetaData.NAME)).thenReturn(labelAttrName);
		Entity lookupAttr0Entity = mock(Entity.class);
		when(lookupAttr0Entity.getString(AttributeMetaDataMetaData.NAME)).thenReturn(lookupAttr0Name);
		Entity lookupAttr1Entity = mock(Entity.class);
		when(lookupAttr1Entity.getString(AttributeMetaDataMetaData.NAME)).thenReturn(lookupAttr1Name);
		when(packageRepository.getPackage(packageName)).thenReturn(package_);
		when(attributeRepository.add(Arrays.asList(idAttr, labelAttr, lookupAttr0, lookupAttr1)))
				.thenReturn(Arrays.asList(idAttrEntity, labelAttrEntity, lookupAttr0Entity, lookupAttr1Entity));

		entityMetaDataRepository.add(entityMeta);

		verify(attributeRepository, times(1)).add(Arrays.asList(idAttr, labelAttr, lookupAttr0, lookupAttr1));

		ArgumentCaptor<EntityMetaData> entityMetaCaptor = ArgumentCaptor.forClass(EntityMetaData.class);
		verify(package_).addEntity(entityMetaCaptor.capture());
		EntityMetaData capturedEntityMeta = entityMetaCaptor.getValue();
		assertEquals(capturedEntityMeta.getName(), packageName + '_' + entityName);

		ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
		verify(entityMetaRepo).add(entityCaptor.capture());

		Entity capturedEntity = entityCaptor.getValue();
		assertEquals(capturedEntity.getEntity(EntityMetaDataMetaData.ID_ATTRIBUTE), idAttrEntity);
		assertEquals(capturedEntity.getEntity(EntityMetaDataMetaData.LABEL_ATTRIBUTE), labelAttrEntity);
		assertEquals(Lists.newArrayList(capturedEntity.getEntities(EntityMetaDataMetaData.LOOKUP_ATTRIBUTES)),
				Arrays.asList(lookupAttr0Entity, lookupAttr1Entity));
	}

	@Test
	public void fillEntityMetaDataCache()
	{
		String packageName = "package";
		// must mock PackageImpl instead of Package due to case in EntityMetaDataRepository
		PackageImpl package_ = mock(PackageImpl.class);
		when(package_.getName()).thenReturn(packageName);
		Entity packageEntity = mock(Entity.class);
		when(packageEntity.getString(PackageMetaData.FULL_NAME)).thenReturn(packageName);
		when(packageRepository.getPackage(packageName)).thenReturn(package_);

		Entity attrEntityId = mock(Entity.class);
		String idAttrName = "idAttr";
		when(attrEntityId.getString(AttributeMetaDataMetaData.NAME)).thenReturn(idAttrName);
		DefaultAttributeMetaData idAttr = mock(DefaultAttributeMetaData.class);
		when(idAttr.getName()).thenReturn(idAttrName);
		when(idAttr.getDataType()).thenReturn(STRING);
		Entity attrEntityLabel = mock(Entity.class);
		String labelAttrName = "labelAttr";
		when(attrEntityLabel.getString(AttributeMetaDataMetaData.NAME)).thenReturn(labelAttrName);
		DefaultAttributeMetaData labelAttr = mock(DefaultAttributeMetaData.class);
		when(labelAttr.getName()).thenReturn(labelAttrName);
		when(labelAttr.getDataType()).thenReturn(STRING);
		Entity attrEntityLookup0 = mock(Entity.class);
		String lookup0AttrName = "lookup0Attr";
		when(attrEntityLookup0.getString(AttributeMetaDataMetaData.NAME)).thenReturn(lookup0AttrName);
		DefaultAttributeMetaData attrLookup0 = mock(DefaultAttributeMetaData.class);
		when(attrLookup0.getName()).thenReturn(lookup0AttrName);
		when(attrLookup0.getDataType()).thenReturn(STRING);
		Entity attrEntityLookup1 = mock(Entity.class);
		String lookup1AttrName = "lookup1Attr";
		when(attrEntityLookup1.getString(AttributeMetaDataMetaData.NAME)).thenReturn(lookup1AttrName);
		DefaultAttributeMetaData attrLookup1 = mock(DefaultAttributeMetaData.class);
		when(attrLookup1.getName()).thenReturn(lookup1AttrName);
		when(attrLookup1.getDataType()).thenReturn(STRING);

		when(attributeRepository.toAttributeMetaData(attrEntityId)).thenReturn(idAttr);
		when(attributeRepository.toAttributeMetaData(attrEntityLabel)).thenReturn(labelAttr);
		when(attributeRepository.toAttributeMetaData(attrEntityLookup0)).thenReturn(attrLookup0);
		when(attributeRepository.toAttributeMetaData(attrEntityLookup1)).thenReturn(attrLookup1);

		Entity entity = mock(Entity.class);
		String simpleEntityName = "entity";
		String entityName = "package_entity";
		when(entity.getString(EntityMetaDataMetaData.SIMPLE_NAME)).thenReturn(simpleEntityName);
		when(entity.getString(EntityMetaDataMetaData.BACKEND)).thenReturn("backend");
		when(entity.getString(EntityMetaDataMetaData.FULL_NAME)).thenReturn(entityName);
		when(entity.getEntity(EntityMetaDataMetaData.ID_ATTRIBUTE)).thenReturn(attrEntityId);
		when(entity.getEntity(EntityMetaDataMetaData.LABEL_ATTRIBUTE)).thenReturn(attrEntityLabel);
		when(entity.getEntities(EntityMetaDataMetaData.LOOKUP_ATTRIBUTES))
				.thenReturn(Arrays.asList(attrEntityLookup0, attrEntityLookup1));
		when(entity.getBoolean(EntityMetaDataMetaData.ABSTRACT)).thenReturn(Boolean.FALSE);
		when(entity.getString(EntityMetaDataMetaData.LABEL)).thenReturn("label");
		when(entity.getEntity(EntityMetaDataMetaData.EXTENDS)).thenReturn(null);
		when(entity.getString(EntityMetaDataMetaData.DESCRIPTION)).thenReturn("description");
		when(entity.getEntity(EntityMetaDataMetaData.PACKAGE)).thenReturn(packageEntity);
		when(entity.getEntities(EntityMetaDataMetaData.ATTRIBUTES))
				.thenReturn(Arrays.asList(attrEntityId, attrEntityLabel, attrEntityLookup0, attrEntityLookup1));

		when(entityMetaRepo.iterator()).thenReturn(Arrays.asList(entity).iterator());
		entityMetaDataRepository.fillEntityMetaDataCache();

		DefaultEntityMetaData entityMeta = entityMetaDataRepository.get(entityName);
		assertEquals(entityMeta.getSimpleName(), simpleEntityName);
		assertEquals(entityMeta.getName(), entityName);
		assertEquals(entityMeta.getIdAttribute(), idAttr);
		assertEquals(entityMeta.getLabelAttribute(), labelAttr);
		assertEquals(Lists.newArrayList(entityMeta.getLookupAttributes()), Arrays.asList(attrLookup0, attrLookup1));
	}
}
