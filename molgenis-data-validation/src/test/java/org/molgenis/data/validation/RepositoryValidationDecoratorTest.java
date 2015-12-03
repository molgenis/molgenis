package org.molgenis.data.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class RepositoryValidationDecoratorTest
{
	private RepositoryValidationDecorator repositoryValidationDecorator;
	private Repository decoratedRepository;

	@BeforeMethod
	public void beforeMethod()
	{
		decoratedRepository = mock(Repository.class);
		repositoryValidationDecorator = new RepositoryValidationDecorator(
				new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory()), decoratedRepository,
				new EntityAttributesValidator());
	}

	@Test
	public void checkNillable()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setNillable(false)
				.setAuto(true);
		emd.addAttribute("notnull").setNillable(false);
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		Entity e1 = new MapEntity("id");
		e1.set("notnull", "notnull");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkNillable(Arrays.asList(e1));
		assertTrue(violations.isEmpty());

		Entity e2 = new MapEntity("id");
		e2.set("notnull", null);
		violations = repositoryValidationDecorator.checkNillable(Arrays.asList(e2));
		assertEquals(violations.size(), 1);

		// With defaultvalue
		emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setNillable(false)
				.setAuto(true);
		emd.addAttribute("notnull").setNillable(false).setDefaultValue("");
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);
		violations = repositoryValidationDecorator.checkNillable(Arrays.asList(e2));
		assertEquals(violations.size(), 1);

	}

	@Test
	public void checkNillableMref()
	{
		DefaultEntityMetaData refEmd = new DefaultEntityMetaData("refEntity");
		String refIdAttrName = "refId";
		refEmd.addAttribute(refIdAttrName).setIdAttribute(true);

		Entity refEntity0 = new MapEntity(refEmd);
		refEntity0.set(refIdAttrName, "0");

		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		String requiredMrefAttrName = "requiredMref";
		emd.addAttribute(requiredMrefAttrName).setDataType(MolgenisFieldTypes.MREF).setNillable(false)
				.setRefEntity(refEmd);
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		Entity entity0 = new MapEntity();
		entity0.set(requiredMrefAttrName, null);

		Entity entity1 = new MapEntity();
		entity1.set(requiredMrefAttrName, Collections.emptyList());

		Entity entity2 = new MapEntity();
		entity2.set(requiredMrefAttrName, Arrays.asList(refEntity0));

		Set<ConstraintViolation> violations = repositoryValidationDecorator
				.checkNillable(Arrays.asList(entity0, entity1, entity2));
		assertEquals(violations.size(), 2);
	}

	@Test
	public void checkReadonly()
	{
		Entity e1 = new MapEntity("id");
		e1.set("id", Integer.valueOf(1));
		e1.set("name", "e1");
		e1.set("readonly", "readonly");

		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setReadOnly(true);
		emd.addAttribute("readonly").setReadOnly(true);
		emd.setLabelAttribute("name");
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		when(repositoryValidationDecorator.findOne(Integer.valueOf(1))).thenReturn(e1);

		Entity e2 = new MapEntity("id");
		e2.set("id", Integer.valueOf(1));
		e2.set("readonly", "readonly");
		e2.set("name", "e2");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e2));
		assertTrue(violations.isEmpty());

		Entity e3 = new MapEntity("id");
		e3.set("id", Integer.valueOf(1));
		e3.set("readonly", "readonlyNEW");
		e3.set("name", "e3");
		violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e3));
		assertEquals(violations.size(), 1);
	}

	@Test
	public void checkReadonlyByUpdate_Xref()
	{
		Entity refEntity = new MapEntity("id");
		refEntity.set("id", Integer.valueOf(10));

		Entity refEntityNew = new MapEntity("idNew");
		refEntity.set("id", Integer.valueOf(11));

		Entity e1 = new MapEntity("id");
		e1.set("id", Integer.valueOf(1));
		e1.set("name", "e1");
		e1.set("readonly-xref", refEntity);

		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setReadOnly(true);
		emd.addAttribute("readonly-xref").setDataType(MolgenisFieldTypes.XREF).setReadOnly(true);
		emd.setLabelAttribute("name");
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		when(repositoryValidationDecorator.findOne(Integer.valueOf(1))).thenReturn(e1);

		Entity e2 = new MapEntity("id");
		e2.set("id", Integer.valueOf(1));
		e2.set("readonly-xref", refEntity);
		e2.set("name", "e2");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e2));
		assertTrue(violations.isEmpty());

		Entity e3 = new MapEntity("id");
		e3.set("id", Integer.valueOf(1));
		e3.set("readonly-xref", refEntityNew);
		e3.set("name", "e3");
		violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e3));
		assertEquals(violations.size(), 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void checkUniques()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setNillable(false);
		emd.addAttribute("unique").setUnique(true);
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);
		when(decoratedRepository.count()).thenReturn(1l, 1l, 1l, 1l, 1l);

		Entity existing = new MapEntity("id");
		existing.set("id", 1);
		existing.set("unique", "qwerty");

		when(decoratedRepository.iterator()).thenReturn(Arrays.asList(existing).iterator(),
				Arrays.asList(existing).iterator(), Arrays.asList(existing).iterator(),
				Arrays.asList(existing).iterator(), Arrays.asList(existing).iterator());

		// Test add, no violations
		Entity new1 = new MapEntity("id");
		new1.set("id", 2);
		new1.set("unique", "mnbv");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new1),
				emd.getAttribute("unique"), false);
		assertTrue(violations.isEmpty());

		// Test add, already exists in repo
		Entity new2 = new MapEntity("id");
		new2.set("id", 2);
		new2.set("unique", "qwerty");
		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new2), emd.getAttribute("unique"), false);
		assertEquals(violations.size(), 1);

		// Test add double in new
		Entity new3 = new MapEntity("id");
		new3.set("id", 3);
		new3.set("unique", "qwerty1");

		Entity new4 = new MapEntity("id");
		new4.set("id", 4);
		new4.set("unique", "qwerty1");

		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new3, new4), emd.getAttribute("unique"),
				false);
		assertEquals(violations.size(), 1);

		// Test update itself
		Entity new5 = new MapEntity("id");
		new5.set("id", 1);
		new5.set("unique", "qwerty");
		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new5), emd.getAttribute("unique"), true);
		assertTrue(violations.isEmpty());

		// Test update already double in new
		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new3, new4), emd.getAttribute("unique"),
				true);
		assertEquals(violations.size(), 1);
	}

	@Test
	public void findAllIterableFetch()
	{
		DataService dataService = mock(DataService.class);
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Repository decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMeta);
		EntityAttributesValidator entityAttributesValidator = mock(EntityAttributesValidator.class);

		@SuppressWarnings("resource")
		RepositoryValidationDecorator myRepositoryValidationDecorator = new RepositoryValidationDecorator(dataService,
				decoratedRepository, entityAttributesValidator);

		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Iterable<Entity> entities = Arrays.asList(entity0, entity1);
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(Arrays.asList(entity0, entity1),
				Lists.newArrayList(myRepositoryValidationDecorator.findAll(ids, fetch)));
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
	}

	@Test
	public void findOneObjectFetch()
	{
		DataService dataService = mock(DataService.class);
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Repository decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMeta);
		EntityAttributesValidator entityAttributesValidator = mock(EntityAttributesValidator.class);

		@SuppressWarnings("resource")
		RepositoryValidationDecorator myRepositoryValidationDecorator = new RepositoryValidationDecorator(dataService,
				decoratedRepository, entityAttributesValidator);

		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOne(id, fetch)).thenReturn(entity);
		assertEquals(entity, myRepositoryValidationDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}
}
