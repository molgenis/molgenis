package org.molgenis.auth;

import com.google.common.collect.Iterators;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.AuthorityMetaData.ROLE;
import static org.molgenis.auth.UserAuthorityMetaData.USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;

public class UserDecorator extends AbstractRepositoryDecorator<User>
{
	private static final int BATCH_SIZE = 1000;

	private final Repository<User> decoratedRepository;
	private final UserAuthorityFactory userAuthorityFactory;
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public UserDecorator(Repository<User> decoratedRepository,
			UserAuthorityFactory userAuthorityFactory, DataService dataService, PasswordEncoder passwordEncoder)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	protected Repository<User> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public Query<User> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public void add(User entity)
	{
		encodePassword(entity);
		decoratedRepository.add(entity);
		addSuperuserAuthority(entity);
	}

	@Override
	public void update(User entity)
	{
		updatePassword(entity);
		decoratedRepository.update(entity);
		updateSuperuserAuthority(entity);
	}

	@Override
	public Integer add(Stream<User> entities)
	{
		AtomicInteger count = new AtomicInteger();
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(users ->
		{
			users.forEach(this::encodePassword);

			Integer batchCount = decoratedRepository.add(users.stream());
			count.addAndGet(batchCount);

			users.forEach(this::addSuperuserAuthority);
		});
		return count.get();
	}

	@Override
	public void update(Stream<User> entities)
	{
		entities = entities.map(entity ->
		{
			updatePassword(entity);
			return entity;
		});
		decoratedRepository.update(entities);
	}

	private void updatePassword(User user)
	{
		User currentUser = findOneById(user.getId());

		String currentPassword = currentUser.getPassword();
		String password = user.getPassword();
		//password is updated
		if(!currentPassword.equals(password))
		{
			password = passwordEncoder.encode(user.getPassword());
		}
		user.setPassword(password);
	}

	private void encodePassword(User user)
	{
		String password = user.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);
	}

	private void addSuperuserAuthority(User user)
	{
		Boolean isSuperuser = user.isSuperuser();
		if (isSuperuser != null && isSuperuser)
		{
			UserAuthority userAuthority = userAuthorityFactory.create();
			userAuthority.setUser(user);
			userAuthority.setRole(AUTHORITY_SU);

			getUserAuthorityRepository().add(userAuthority);
		}
	}

	private void updateSuperuserAuthority(User user)
	{
		Repository<UserAuthority> userAuthorityRepo = getUserAuthorityRepository();
		UserAuthority suAuthority = userAuthorityRepo.query().eq(USER, user).and()
				.eq(ROLE, AUTHORITY_SU).findOne();

		Boolean isSuperuser = user.isSuperuser();
		if (isSuperuser != null && isSuperuser)
		{
			if (suAuthority == null)
			{
				UserAuthority userAuthority = userAuthorityFactory.create();
				userAuthority.setUser(user);
				userAuthority.setRole(AUTHORITY_SU);
				userAuthorityRepo.add(userAuthority);
			}
		}
		else
		{
			if (suAuthority != null)
			{
				userAuthorityRepo.deleteById(suAuthority.getId());
			}
		}
	}

	private Repository<UserAuthority> getUserAuthorityRepository()
	{
		return dataService.getRepository(USER_AUTHORITY, UserAuthority.class);
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	public EntityType getEntityType()
	{
		return decoratedRepository.getEntityType();
	}

	@Override
	public Iterator<User> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<User>> consumer, int batchSize)
	{
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public long count(Query<User> q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<User> findAll(Query<User> q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public User findOne(Query<User> q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public User findOneById(Object id)
	{
		return decoratedRepository.findOneById(id);
	}

	@Override
	public User findOneById(Object id, Fetch fetch)
	{
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<User> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<User> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public void delete(User entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<User> entities)
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
}
