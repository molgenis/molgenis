package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.annotations.BeforeMethod;

public class RowLevelSecurityRepositoryDecoratorTest
{
	private String entityName;
	private EntityMetaData entityMetaData;
	private Repository decoratedRepository;
	private RowLevelSecurityRepositoryDecorator rowLevelSecurityRepositoryDecorator;

	@BeforeMethod
	public void setUp()
	{
		entityName = "entity";
		entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(entityName);
		decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getName()).thenReturn(entityName);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);

		DataService dataService = mock(DataService.class);
		RowLevelSecurityPermissionValidator permissionValidator = mock(RowLevelSecurityPermissionValidator.class);
		rowLevelSecurityRepositoryDecorator = new RowLevelSecurityRepositoryDecorator(decoratedRepository, dataService,
				permissionValidator);
	}

}
