package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.model.RecoveryCodeFactory;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretMetaData;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static com.google.api.client.util.Lists.newArrayList;
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
	private final UserAccountService userAccountService;

	public RecoveryServiceImpl(DataService dataService, UserAccountService userAccountService,
			RecoveryCodeFactory recoveryCodeFactory, IdGenerator idGenerator)
	{
		this.dataService = requireNonNull(dataService);
		this.userAccountService = requireNonNull(userAccountService);
		this.recoveryCodeFactory = requireNonNull(recoveryCodeFactory);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	@Transactional
	public Stream<RecoveryCode> generateRecoveryCodes()
	{
		String userId = userAccountService.getCurrentUser()
										  .getId()
										  .orElseThrow(() -> new IllegalArgumentException("User has empty id."));
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
		String userId = userAccountService.getCurrentUser()
										  .getId()
										  .orElseThrow(() -> new IllegalStateException("User has empty id."));
		RecoveryCode existingCode = runAsSystem(() -> dataService.findOne(RECOVERY_CODE,
				new QueryImpl<RecoveryCode>().eq(USER_ID, userId).and().eq(CODE, recoveryCode), RecoveryCode.class));
		if (existingCode != null)
		{
			runAsSystem(() -> dataService.delete(RECOVERY_CODE, existingCode));
			UserSecret secret = runAsSystem(() -> dataService.findOne(USER_SECRET,
					new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, userId), UserSecret.class));
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
		String userId = userAccountService.getCurrentUser()
										  .getId()
										  .orElseThrow(() -> new IllegalArgumentException("User has empty id."));
		return runAsSystem(() -> dataService.findAll(RECOVERY_CODE, new QueryImpl<RecoveryCode>().eq(USER_ID, userId),
				RecoveryCode.class));
	}

	private void deleteOldRecoveryCodes(String userId)
	{
		runAsSystem(() ->
		{
			Stream<RecoveryCode> recoveryCodes = dataService.findAll(RECOVERY_CODE,
					new QueryImpl<RecoveryCode>().eq(USER_ID, userId), RecoveryCode.class);
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
}
