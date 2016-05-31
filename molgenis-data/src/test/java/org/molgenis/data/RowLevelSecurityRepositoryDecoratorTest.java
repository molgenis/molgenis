package org.molgenis.data;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;
import static org.testng.Assert.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.security.core.Permission;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RowLevelSecurityRepositoryDecoratorTest
{
	private String entityName;
	private EntityMetaData entityMetaData;
	private EntityMetaData completeEntityMetaData; // with row level security column(s)
	private Repository decoratedRepository;
	private RowLevelSecurityRepositoryDecorator repositoryDecorator;
	private RowLevelSecurityPermissionValidator permissionValidator;
	private Entity entity;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		entityName = "entity";

		entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(entityName);
		when(entityMetaData.isRowLevelSecured()).thenReturn(true);
		when(entityMetaData.getAttribute(UPDATE_ATTRIBUTE)).thenReturn(null);

		completeEntityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(entityName);
		when(entityMetaData.isRowLevelSecured()).thenReturn(true);
		when(entityMetaData.getAttribute(UPDATE_ATTRIBUTE)).thenReturn(mock(AttributeMetaData.class));

		decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getName()).thenReturn(entityName);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);

		entity = mock(Entity.class);
		when(entity.getEntityMetaData()).thenReturn(entityMetaData);

		permissionValidator = mock(RowLevelSecurityPermissionValidator.class);
		repositoryDecorator = new RowLevelSecurityRepositoryDecorator(decoratedRepository, permissionValidator);

		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Answer<Void> streamConsumer = new Answer<Void>()
		{
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				entities.collect(Collectors.toList());
				return null;
			}
		};

		doAnswer(streamConsumer).when(decoratedRepository).update(any(Stream.class));
	}

	@Test
	public void stream()
	{
		// TODO
	}

	@Test
	public void getEntityMetaData()
	{
		assertTrue(repositoryDecorator
				.getEntityMetaData() instanceof RowLevelSecurityRepositoryDecorator.RowLevelSecurityEntityMetaDataDecorator);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateEntityNoPermission()
	{
		when(permissionValidator.validatePermission(entity, Permission.UPDATE))
				.thenThrow(MolgenisDataAccessException.class);

		try
		{
			repositoryDecorator.update(entity);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verifyNoMoreInteractions(decoratedRepository);
			throw e;
		}
	}

	@Test
	public void updateEntity()
	{
		repositoryDecorator.update(entity);
		verify(decoratedRepository, times(1)).update(entity);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateStreamNoPermission()
	{
		Entity entity = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity);

		when(permissionValidator.validatePermission(entity, Permission.UPDATE))
				.thenThrow(MolgenisDataAccessException.class);

		try
		{
			repositoryDecorator.update(entities);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verify(decoratedRepository, times(1)).update(any(Stream.class));
			verifyNoMoreInteractions(decoratedRepository);
			throw e;
		}
	}

	@Test
	public void updateStream()
	{
		Entity entity = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity);

		repositoryDecorator.update(entities);
		verify(decoratedRepository, times(1)).getEntityMetaData();
		verify(decoratedRepository, times(1)).update(any(Stream.class));
	}

}
