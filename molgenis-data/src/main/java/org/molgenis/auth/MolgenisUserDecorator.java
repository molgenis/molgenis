package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MolgenisUserDecorator implements Repository<MolgenisUser>
{
	private final Repository<MolgenisUser> decoratedRepository;
	private final MolgenisUserFactory molgenisUserFactory;
	private final UserAuthorityFactory userAuthorityFactory;

	public MolgenisUserDecorator(Repository<MolgenisUser> decoratedRepository, MolgenisUserFactory molgenisUserFactory,
			UserAuthorityFactory userAuthorityFactory)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.molgenisUserFactory = requireNonNull(molgenisUserFactory);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
	}

	@Override
	public void add(MolgenisUser entity)
	{
		encodePassword(entity);
		decoratedRepository.add(entity);

		// id is only guaranteed to be generated at flush time
		decoratedRepository.flush();
		addSuperuserAuthority(entity);
	}

	@Override
	public void update(MolgenisUser entity)
	{
		updatePassword(entity);
		decoratedRepository.update(entity);

		// id is only guaranteed to be generated at flush time
		decoratedRepository.flush();
		updateSuperuserAuthority(entity);
	}

	@Override
	public Integer add(Stream<MolgenisUser> entities)
	{
		entities = entities.map(entity -> {
			encodePassword(entity);
			addSuperuserAuthority(entity);
			return entity;
		});
		return decoratedRepository.add(entities);
	}

	@Override
	public void update(Stream<MolgenisUser> entities)
	{
		entities = entities.map(entity -> {
			updatePassword(entity);
			return entity;
		});
		decoratedRepository.update(entities);
	}

	private void updatePassword(MolgenisUser entity)
	{
		MolgenisUser currentUser = molgenisUserFactory.create();
		currentUser.set(findOneById(entity.getIdValue()));

		String currentPassword = currentUser.getPassword();
		String password = entity.getString(MolgenisUserMetaData.PASSWORD_);

		if (!password.equals(currentPassword))
		{
			encodePassword(entity);
		}
	}

	private void encodePassword(MolgenisUser entity)
	{
		String password = entity.getString(MolgenisUserMetaData.PASSWORD_);
		String encodedPassword = getPasswordEncoder().encode(password);
		entity.set(MolgenisUserMetaData.PASSWORD_, encodedPassword);
	}

	private void addSuperuserAuthority(MolgenisUser molgenisUser)
	{
		Boolean isSuperuser = molgenisUser.isSuperuser();
		if (isSuperuser != null && isSuperuser == true)
		{
			UserAuthority userAuthority = userAuthorityFactory.create();
			userAuthority.setMolgenisUser(molgenisUser);
			userAuthority.setRole(SecurityUtils.AUTHORITY_SU);

			getUserAuthorityRepository().add(userAuthority);
		}
	}

	private void updateSuperuserAuthority(MolgenisUser entity)
	{
		MolgenisUser molgenisUser = molgenisUserFactory.create();
		molgenisUser.set(findOneById(entity.getIdValue()));

		Repository<UserAuthority> userAuthorityRepository = getUserAuthorityRepository();
		Entity suAuthorityEntity = userAuthorityRepository.findOne(
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.MOLGENIS_USER, molgenisUser).and()
						.eq(AuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_SU));

		Boolean isSuperuser = entity.getBoolean(MolgenisUserMetaData.SUPERUSER);
		if (isSuperuser != null && isSuperuser == true)
		{
			if (suAuthorityEntity == null)
			{

				UserAuthority userAuthority = userAuthorityFactory.create();
				userAuthority.setMolgenisUser(molgenisUser);
				userAuthority.setRole(SecurityUtils.AUTHORITY_SU);
				userAuthorityRepository.add(userAuthority);
			}
		}
		else
		{
			if (suAuthorityEntity != null)
			{
				userAuthorityRepository.deleteById(suAuthorityEntity.getIdValue());
			}
		}
	}

	private PasswordEncoder getPasswordEncoder()
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required application context"));
		}
		PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
		if (passwordEncoder == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required PasswordEncoder bean"));
		}
		return passwordEncoder;
	}

	private Repository<UserAuthority> getUserAuthorityRepository()
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required application context"));
		}
		DataService dataService = applicationContext.getBean(DataService.class);
		if (dataService == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required DataService bean"));
		}
		Repository<UserAuthority> userAuthorityRepository = dataService
				.getRepository(USER_AUTHORITY, UserAuthority.class);
		if (userAuthorityRepository == null)
		{
			throw new RuntimeException("missing required UserAuthority repository");
		}
		return userAuthorityRepository;
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public Iterator<MolgenisUser> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public Stream<MolgenisUser> stream(Fetch fetch)
	{
		return decoratedRepository.stream(fetch);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query<MolgenisUser> query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query<MolgenisUser> q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<MolgenisUser> findAll(Query<MolgenisUser> q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public MolgenisUser findOne(Query<MolgenisUser> q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public MolgenisUser findOneById(Object id)
	{
		return decoratedRepository.findOneById(id);
	}

	@Override
	public MolgenisUser findOneById(Object id, Fetch fetch)
	{
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<MolgenisUser> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<MolgenisUser> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public void delete(MolgenisUser entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<MolgenisUser> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decoratedRepository.getQueryOperators();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}
}
