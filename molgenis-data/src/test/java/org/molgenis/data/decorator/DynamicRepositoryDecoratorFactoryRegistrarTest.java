package org.molgenis.data.decorator;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DynamicRepositoryDecoratorFactoryRegistrarTest extends AbstractMockitoTest
{
	@Mock
	DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry;
	@Mock
	DynamicRepositoryDecoratorFactory decoratorFactory1;
	@Mock
	DynamicRepositoryDecoratorFactory decoratorFactory2;
	@Mock
	ApplicationContext context;

	@Test
	public void testRegister()
	{
		Map<String, DynamicRepositoryDecoratorFactory> map = new HashMap<>();
		map.put("decoratorFactory1", decoratorFactory1);
		map.put("decoratorFactory2", decoratorFactory2);

		when(context.getBeansOfType(DynamicRepositoryDecoratorFactory.class)).thenReturn(map);

		DynamicRepositoryDecoratorFactoryRegistrar dynamicRepositoryDecoratorFactoryRegistrar = new DynamicRepositoryDecoratorFactoryRegistrar(
				repositoryDecoratorRegistry);
		dynamicRepositoryDecoratorFactoryRegistrar.register(context);

		verify(repositoryDecoratorRegistry).addFactory(decoratorFactory1);
		verify(repositoryDecoratorRegistry).addFactory(decoratorFactory2);
	}
}