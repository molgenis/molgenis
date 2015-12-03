package org.molgenis.security.user;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisUserDecoratorTest
{
	private Repository decoratedRepository;
	private MolgenisUserDecorator molgenisUserDecorator;
	private PasswordEncoder passwordEncoder;
	private UserAuthorityRepository userAuthorityRepository;

	@BeforeMethod
	public void setUp()
	{
		decoratedRepository = mock(Repository.class);
		molgenisUserDecorator = new MolgenisUserDecorator(decoratedRepository);
		ApplicationContext ctx = mock(ApplicationContext.class);
		passwordEncoder = mock(PasswordEncoder.class);
		when(ctx.getBean(PasswordEncoder.class)).thenReturn(passwordEncoder);
		userAuthorityRepository = mock(UserAuthorityRepository.class);
		DataService dataService = mock(DataService.class);
		when(ctx.getBean(DataService.class)).thenReturn(dataService);
		when(dataService.getRepository(UserAuthority.ENTITY_NAME)).thenReturn(userAuthorityRepository);
		new ApplicationContextProvider().setApplicationContext(ctx);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		Entity entity = new MapEntity();
		entity.set(MolgenisUser.PASSWORD_, password);
		entity.set(MolgenisUser.SUPERUSER, false);
		molgenisUserDecorator.add(entity);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(entity);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		Entity entity = new MapEntity("id");
		entity.set("id", 1);
		entity.set(MolgenisUser.PASSWORD_, password);
		entity.set(MolgenisUser.SUPERUSER, true);
		when(decoratedRepository.findOne(1)).thenReturn(entity);

		molgenisUserDecorator.add(entity);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(entity);
		verify(userAuthorityRepository, times(1)).add(any(UserAuthority.class));
	}

	@Test
	public void findAllIterableFetch()
	{
		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		molgenisUserDecorator.findAll(ids, fetch);
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		molgenisUserDecorator.findOne(id, fetch);
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}
}
