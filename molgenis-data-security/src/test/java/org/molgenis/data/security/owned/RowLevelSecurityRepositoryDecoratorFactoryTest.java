package org.molgenis.data.security.owned;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class RowLevelSecurityRepositoryDecoratorFactoryTest extends AbstractMockitoTest
{
	@Mock
	private UserPermissionEvaluator userPermissionEvaluator;
	@Mock
	private MutableAclService mutableAclService;
	@Mock
	private MutableAclClassService mutableAclClassService;
	private RowLevelSecurityRepositoryDecoratorFactory rowLevelSecurityRepositoryDecoratorFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		rowLevelSecurityRepositoryDecoratorFactory = new RowLevelSecurityRepositoryDecoratorFactory(
				userPermissionEvaluator, mutableAclService, mutableAclClassService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testRowLevelSecurityRepositoryDecoratorFactory()
	{
		new RowLevelSecurityRepositoryDecoratorFactory(null, null, null);
	}

	@Test
	public void testCreateDecoratedRepositoryRowLevelSecurityEnabled()
	{
		Repository<Entity> repository = getRepositoryMock();
		when(mutableAclClassService.hasAclClass("entity-entityTypeId")).thenReturn(true);
		assertTrue(rowLevelSecurityRepositoryDecoratorFactory.createDecoratedRepository(
				repository) instanceof RowLevelSecurityRepositoryDecorator);
	}

	@Test
	public void testCreateDecoratedRepositoryRowLevelSecurityDisabled()
	{
		Repository<Entity> repository = getRepositoryMock();
		assertEquals(rowLevelSecurityRepositoryDecoratorFactory.createDecoratedRepository(repository), repository);
	}

	private Repository<Entity> getRepositoryMock()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(repository.getEntityType()).thenReturn(entityType);
		return repository;
	}
}