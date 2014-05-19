package org.molgenis.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisUserDecoratorTest
{
	private CrudRepository decoratedRepository;
	private MolgenisUserDecorator molgenisUserDecorator;
	private PasswordEncoder passwordEncoder;

	@BeforeMethod
	public void setUp()
	{
		decoratedRepository = mock(CrudRepository.class);
		molgenisUserDecorator = new MolgenisUserDecorator(decoratedRepository);
		ApplicationContext ctx = mock(ApplicationContext.class);
		passwordEncoder = mock(PasswordEncoder.class);
		when(ctx.getBean(PasswordEncoder.class)).thenReturn(passwordEncoder);
		new ApplicationContextProvider().setApplicationContext(ctx);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		Entity entity = new MapEntity();
		entity.set(MolgenisUser.PASSWORD_, password);
		molgenisUserDecorator.add(entity);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(entity);
	}
}
