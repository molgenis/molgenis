package org.molgenis.data.decorator;

import org.springframework.context.ApplicationContext;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class DynamicRepositoryDecoratorFactoryRegistrarTest
{

	@Test
	public void testRegister()
	{
		ApplicationContext context = mock(ApplicationContext.class);
		DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry = mock(DynamicRepositoryDecoratorRegistry.class);
		DynamicRepositoryDecoratorFactory decoratorFactory1 = mock(DynamicRepositoryDecoratorFactory.class);
		DynamicRepositoryDecoratorFactory decoratorFactory2 = mock(DynamicRepositoryDecoratorFactory.class);

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