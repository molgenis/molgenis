package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.model.RecoveryCodeFactory;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretMetaData;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.populate.IdGenerator.Strategy.LONG_SECURE_RANDOM;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.twofactor.model.RecoveryCodeMetadata.*;
import static org.molgenis.security.twofactor.model.UserSecretMetaData.USER_SECRET;

@Service
public class RecoveryServiceImpl implements RecoveryService
{
	private static final int RECOVERY_CODE_COUNT = 10;

	private final DataService dataService;
	private final RecoveryCodeFactory recoveryCodeFactory;
	private final IdGenerator idGenerator;
	private final UserService userService;

	public RecoveryServiceImpl(DataService dataService, UserService userService,
			RecoveryCodeFactory recoveryCodeFactory, IdGenerator idGenerator)
	{
		this.dataService = requireNonNull(dataService);
		this.userService = requireNonNull(userService);
		this.recoveryCodeFactory = requireNonNull(recoveryCodeFactory);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	@Transactional
	public Stream<RecoveryCode> generateRecoveryCodes()
	{
		String userId = getUser().getId();
		deleteOldRecoveryCodes(userId);
		List<RecoveryCode> newRecoveryCodes = generateRecoveryCodes(userId);
		//noinspection RedundantCast
		runAsSystem((Runnable) () -> dataService.add(RECOVERY_CODE, newRecoveryCodes.stream()));
		return newRecoveryCodes.stream();
	}

	@Override
	@Transactional
	public void useRecoveryCode(String recoveryCode)
	{
		String userId = getUser().getId();
		RecoveryCode existingCode = runAsSystem(() -> dataService.query(RECOVERY_CODE, RecoveryCode.class)
																 .eq(USER_ID, userId)
																 .and()
																 .eq(CODE, recoveryCode)
																 .findOne());
		if (existingCode != null)
		{
			runAsSystem(() -> dataService.delete(RECOVERY_CODE, existingCode));
			UserSecret secret = runAsSystem(() -> dataService.query(USER_SECRET, UserSecret.class)
															 .eq(UserSecretMetaData.USER_ID, userId)
															 .findOne());
			secret.setFailedLoginAttempts(0);
			runAsSystem(() -> dataService.update(USER_SECRET, secret));
		}
		else
		{
			throw new BadCredentialsException("Invalid recovery code or code already used");
		}
	}

	@Override
	public Stream<RecoveryCode> getRecoveryCodes()
	{
		String userId = getUser().getId();
		return runAsSystem(() -> dataService.query(RECOVERY_CODE, RecoveryCode.class).eq(USER_ID, userId).findAll());
	}

	private void deleteOldRecoveryCodes(String userId)
	{
		runAsSystem(() ->
		{
			Stream<RecoveryCode> recoveryCodes = dataService.query(RECOVERY_CODE, RecoveryCode.class)
															.eq(USER_ID, userId)
															.findAll();
			dataService.delete(RECOVERY_CODE, recoveryCodes);
		});
	}

	private List<RecoveryCode> generateRecoveryCodes(String userId)
	{
		List<RecoveryCode> recoveryCodes = newArrayList();
		for (int i = 0; i < RECOVERY_CODE_COUNT; i++)
		{
			RecoveryCode recoveryCode = recoveryCodeFactory.create();
			recoveryCode.setUserId(userId);
			recoveryCode.setCode(idGenerator.generateId(LONG_SECURE_RANDOM));
			recoveryCodes.add(recoveryCode);
		}
		return recoveryCodes;
	}

	private User getUser()
	{
		User user = userService.getUser(SecurityUtils.getCurrentUsername());

		if (user != null)
		{
			return user;
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + SecurityUtils.getCurrentUsername() + "]");
		}
	}
}
