package org.molgenis.searchall.controller;

import com.google.gson.Gson;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SearchAllControllerTest
{
	DataService dataService;
	SearchAllController searchAllController;

	@BeforeClass
	public void setUp()
	{
		dataService = mock(DataService.class);
		searchAllController = new SearchAllController(dataService);
	}

	@Test
	public void testFindAll() throws Exception
	{
		Package pack1 = mock(Package.class);
		when(pack1.getLabel()).thenReturn("package test nr 1");
		when(pack1.getLabelValue()).thenReturn("package test nr 1");
		when(pack1.getId()).thenReturn("package id 1");
		when(pack1.getDescription()).thenReturn("package description 1");

		Package pack2 = mock(Package.class);
		when(pack2.getLabel()).thenReturn("package nr 2");
		when(pack2.getLabelValue()).thenReturn("package nr 2");
		when(pack2.getId()).thenReturn("package id 2");
		when(pack2.getDescription()).thenReturn("package description 2");

		Package pack3 = mock(Package.class);
		when(pack3.getLabel()).thenReturn("package nr 3");
		when(pack3.getLabelValue()).thenReturn("package nr 3");
		when(pack3.getId()).thenReturn("package id 3");
		when(pack3.getDescription()).thenReturn("package test description 3");

		Attribute attr1 = mock(Attribute.class);
		when(attr1.getLabel()).thenReturn("attr test nr 1");
		when(attr1.getLabelValue()).thenReturn("attr test nr 1");
		when(attr1.getName()).thenReturn("attr id 1");
		when(attr1.getDescription()).thenReturn("attr description 1");
		when(attr1.getDataType()).thenReturn(AttributeType.BOOL);

		Attribute attr2 = mock(Attribute.class);
		when(attr2.getLabel()).thenReturn("attr nr 2");
		when(attr2.getLabelValue()).thenReturn("attr nr 2");
		when(attr2.getName()).thenReturn("attr id 2");
		when(attr2.getDescription()).thenReturn("attr test description 2");
		when(attr2.getDataType()).thenReturn(AttributeType.BOOL);

		Attribute attr3 = mock(Attribute.class);
		when(attr3.getLabel()).thenReturn("attr nr 3");
		when(attr3.getLabelValue()).thenReturn("attr nr 3");
		when(attr3.getName()).thenReturn("attr id 3");
		when(attr3.getDescription()).thenReturn("attr description 3");
		when(attr3.getDataType()).thenReturn(AttributeType.BOOL);

		Attribute attr4 = mock(Attribute.class);
		when(attr4.getLabel()).thenReturn("attr nr 4");
		when(attr4.getLabelValue()).thenReturn("attr nr 4");
		when(attr4.getName()).thenReturn("attr id 4");
		when(attr4.getDescription()).thenReturn("attr description 4");
		when(attr4.getDataType()).thenReturn(AttributeType.BOOL);

		Attribute attr5 = mock(Attribute.class);
		when(attr5.getLabel()).thenReturn("attr nr 5");
		when(attr5.getLabelValue()).thenReturn("attr nr 5");
		when(attr5.getName()).thenReturn("attr id 5");
		when(attr5.getDescription()).thenReturn("attr description 5");
		when(attr5.getDataType()).thenReturn(AttributeType.BOOL);

		EntityType entity1 = mock(EntityType.class);
		when(entity1.getLabel()).thenReturn("entity nr 1");
		when(entity1.getLabelValue()).thenReturn("entity nr 1");
		when(entity1.getId()).thenReturn("entity id 1");
		when(entity1.getDescription()).thenReturn("entity description 1");
		when(entity1.getAllAttributes()).thenReturn(Arrays.asList(attr1));
		when(entity1.getPackage()).thenReturn(pack2);

		EntityType entity2 = mock(EntityType.class);
		when(entity2.getLabel()).thenReturn("entity nr 2");
		when(entity2.getLabelValue()).thenReturn("entity nr 2");
		when(entity2.getId()).thenReturn("entity id 2");
		when(entity2.getDescription()).thenReturn("entity description 2");
		when(entity2.getAllAttributes()).thenReturn(Arrays.asList(attr2, attr5));
		when(entity2.getPackage()).thenReturn(pack2);

		EntityType entity3 = mock(EntityType.class);
		when(entity3.getLabel()).thenReturn("entity test nr 3");
		when(entity3.getLabelValue()).thenReturn("entity test nr 3");
		when(entity3.getId()).thenReturn("entity id 3");
		when(entity3.getDescription()).thenReturn("entity description 3");
		when(entity3.getAllAttributes()).thenReturn(Arrays.asList(attr3));
		when(entity3.getPackage()).thenReturn(pack3);

		EntityType entity4 = mock(EntityType.class);
		when(entity4.getLabel()).thenReturn("entity nr 4");
		when(entity4.getLabelValue()).thenReturn("entity nr 4");
		when(entity4.getId()).thenReturn("entity id 4");
		when(entity4.getDescription()).thenReturn("entity test description 4");
		when(entity4.getAllAttributes()).thenReturn(Arrays.asList(attr4));
		when(entity4.getPackage()).thenReturn(pack3);

		when(dataService.findAll(PackageMetadata.PACKAGE, Package.class)).thenReturn(
				Arrays.asList(pack1, pack2, pack3).stream());
		when(dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(
				Arrays.asList(entity1, entity2, entity3, entity4).stream());
		when(dataService.count("entity id 1", new QueryImpl<>().search("test"))).thenReturn(2L);
		when(dataService.count("entity id 3", new QueryImpl<>().search("test"))).thenReturn(6L);
		when(dataService.count("entity id 4", new QueryImpl<>().search("test"))).thenReturn(11L);

		assertEquals(new Gson().toJson(searchAllController.findAll("test")).toString(),
				"{\"entityTypes\":[{\"getId\":\"entity id 1\",\"getLabel\":\"entity nr 1\",\"getDescription\":\"entity description 1\",\"getPackageId\":\"package id 2\",\"isLabelMatch\":false,\"isDescriptionMatch\":false,\"getAttributes\":[{\"label\":\"attr test nr 1\",\"description\":\"attr description 1\",\"dataType\":\"BOOL\"}],\"nrOfMatchingEntities\":2},{\"getId\":\"entity id 3\",\"getLabel\":\"entity test nr 3\",\"getDescription\":\"entity description 3\",\"getPackageId\":\"package id 3\",\"isLabelMatch\":true,\"isDescriptionMatch\":false,\"getAttributes\":[],\"nrOfMatchingEntities\":6},{\"getId\":\"entity id 4\",\"getLabel\":\"entity nr 4\",\"getDescription\":\"entity test description 4\",\"getPackageId\":\"package id 3\",\"isLabelMatch\":false,\"isDescriptionMatch\":false,\"getAttributes\":[],\"nrOfMatchingEntities\":11}],\"packages\":[{\"label\":\"package test nr 1\",\"description\":\"package description 1\"}]}");
	}

}