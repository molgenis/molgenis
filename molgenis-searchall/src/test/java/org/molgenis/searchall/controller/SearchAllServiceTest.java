package org.molgenis.searchall.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.searchall.model.AttributeResult;
import org.molgenis.searchall.model.EntityTypeResult;
import org.molgenis.searchall.model.PackageResult;
import org.molgenis.searchall.model.Result;
import org.molgenis.searchall.service.SearchAllService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.assertEquals;

public class SearchAllServiceTest
{
	private DataService dataService;
	private SearchAllService searchAllService;
	private EntityType entity1;
	private EntityType entity2;
	private EntityType entity3;
	private EntityType entity4;
	private EntityType abstractEntity;

	private Package pack1;
	private Package pack2;
	private Package pack3;
	private Package pack_sys;

	private Attribute attr1;
	private Attribute attr2;
	private Attribute attr3;
	private Attribute attr4;
	private Attribute attr5;

	@BeforeClass
	public void setUp()
	{
		dataService = mock(DataService.class);
		searchAllService = new SearchAllService(dataService);

		LocaleContextHolder.setLocale(Locale.ENGLISH);

		pack1 = mock(Package.class);
		when(pack1.getLabel()).thenReturn("package test nr 1");
		when(pack1.getId()).thenReturn("package id 1");
		when(pack1.getDescription()).thenReturn("package description 1");

		pack2 = mock(Package.class);
		when(pack2.getLabel()).thenReturn("package nr 2");
		when(pack2.getId()).thenReturn("package id 2");
		when(pack2.getDescription()).thenReturn("package description 2");

		pack_sys = mock(Package.class);
		when(pack_sys.getLabel()).thenReturn("package sys");
		when(pack_sys.getId()).thenReturn(PACKAGE_SYSTEM);
		when(pack_sys.getDescription()).thenReturn("package test description");

		pack3 = mock(Package.class);
		when(pack3.getLabel()).thenReturn("package nr 3");
		when(pack3.getId()).thenReturn("package id 3");
		when(pack3.getDescription()).thenReturn(null);

		// matches label
		attr1 = mock(Attribute.class);
		when(attr1.getLabel("en")).thenReturn("attr test nr 1");
		when(attr1.getName()).thenReturn("attr id 1");
		when(attr1.getDescription("en")).thenReturn("attr description 1");
		when(attr1.getDataType()).thenReturn(BOOL);

		// no match
		attr2 = mock(Attribute.class);
		when(attr2.getLabel("en")).thenReturn("attr nr 2");
		when(attr2.getName()).thenReturn("attr id 2");
		when(attr2.getDescription("en")).thenReturn(null);
		when(attr2.getDataType()).thenReturn(BOOL);

		// no match
		attr3 = mock(Attribute.class);
		when(attr3.getLabel("en")).thenReturn("attr nr 3");
		when(attr3.getName()).thenReturn("attr id 3");
		when(attr3.getDescription("en")).thenReturn("attr description 3");
		when(attr3.getDataType()).thenReturn(BOOL);

		// no match
		attr4 = mock(Attribute.class);
		when(attr4.getLabel("en")).thenReturn("attr nr 4");
		when(attr4.getName()).thenReturn("attr id 4");
		when(attr4.getDescription("en")).thenReturn("attr description 4");
		when(attr4.getDataType()).thenReturn(BOOL);

		// no match
		attr5 = mock(Attribute.class);
		when(attr5.getLabel("en")).thenReturn("attr nr 5");
		when(attr5.getName()).thenReturn("attr id 5");
		when(attr5.getDescription("en")).thenReturn("attr description 5");
		when(attr5.getDataType()).thenReturn(BOOL);

		entity1 = mock(EntityType.class);
		when(entity1.getLabel("en")).thenReturn("entity nr 1");
		when(entity1.getId()).thenReturn("entity id 1");
		when(entity1.getDescription("en")).thenReturn("entity description 1");
		when(entity1.getAllAttributes()).thenReturn(singletonList(attr1));
		when(entity1.getPackage()).thenReturn(pack2);

		entity2 = mock(EntityType.class);
		when(entity2.getLabel("en")).thenReturn("entity nr 2");
		when(entity2.getId()).thenReturn("entity id 2");
		when(entity2.getDescription("en")).thenReturn("entity description 2");
		when(entity2.getAllAttributes()).thenReturn(Arrays.asList(attr2, attr5));
		when(entity2.getPackage()).thenReturn(pack2);

		entity3 = mock(EntityType.class);
		when(entity3.getLabel("en")).thenReturn("entity test nr 3");
		when(entity3.getId()).thenReturn("entity id 3");
		when(entity3.getDescription("en")).thenReturn("entity description 3");
		when(entity3.getAllAttributes()).thenReturn(singletonList(attr3));
		when(entity3.getPackage()).thenReturn(pack3);

		entity4 = mock(EntityType.class);
		when(entity4.getLabel("en")).thenReturn("entity nr 4");
		when(entity4.getId()).thenReturn("entity id 4");
		when(entity4.getDescription("en")).thenReturn("entity test description 4");
		when(entity4.getAllAttributes()).thenReturn(singletonList(attr4));
		when(entity4.getPackage()).thenReturn(null);

		abstractEntity = mock(EntityType.class);
		when(abstractEntity.getLabel("en")).thenReturn("abstract");
		when(abstractEntity.getId()).thenReturn("abstract");
		when(abstractEntity.getDescription("en")).thenReturn("abstract");
		when(abstractEntity.getAllAttributes()).thenReturn(singletonList(attr1));
		when(abstractEntity.isAbstract()).thenReturn(true);
		when(abstractEntity.getPackage()).thenReturn(pack1);
	}

	@Test
	public void testFindAll() throws Exception
	{
		when(dataService.findAll(PackageMetadata.PACKAGE, Package.class)).thenReturn(
				Stream.of(pack1, pack2, pack3, pack_sys));
		when(dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(
				Stream.of(entity1, entity2, entity3, entity4, abstractEntity));
		when(dataService.count("entity id 1", new QueryImpl<>().search("test"))).thenReturn(2L);
		when(dataService.count("entity id 3", new QueryImpl<>().search("test"))).thenReturn(6L);
		when(dataService.count("entity id 4", new QueryImpl<>().search("test"))).thenReturn(11L);

		PackageResult packageResult = PackageResult.create("package id 1", "package test nr 1",
				"package description 1");
		AttributeResult attributeResult = AttributeResult.create("attr test nr 1", "attr description 1", "BOOL");

		EntityTypeResult entityTypeResult1 = EntityTypeResult.builder()
															 .setId("entity id 1")
															 .setLabel("entity nr 1")
															 .setDescription("entity description 1")
															 .setPackageId("package id 2")
															 .setLabelMatch(false)
															 .setDescriptionMatch(false)
															 .setAttributes(singletonList(attributeResult))
															 .setNrOfMatchingEntities(2)
															 .build();
		EntityTypeResult entityTypeResult2 = EntityTypeResult.builder()
															 .setId("entity id 3")
															 .setLabel("entity test nr 3")
															 .setDescription("entity description 3")
															 .setPackageId("package id 3")
															 .setLabelMatch(true)
															 .setDescriptionMatch(false)
															 .setAttributes(Collections.emptyList())
															 .setNrOfMatchingEntities(6)
															 .build();
		EntityTypeResult entityTypeResult3 = EntityTypeResult.builder()
															 .setId("entity id 4")
															 .setLabel("entity nr 4")
															 .setDescription("entity test description 4")
															 .setPackageId(null)
															 .setLabelMatch(false)
															 .setDescriptionMatch(true)
															 .setAttributes(Collections.emptyList())
															 .setNrOfMatchingEntities(11)
															 .build();
		Result result = Result.builder()
							  .setEntityTypes(Arrays.asList(entityTypeResult1, entityTypeResult2, entityTypeResult3))
							  .setPackages(singletonList(packageResult))
							  .build();

		assertEquals(searchAllService.searchAll("test"), result);
	}

}