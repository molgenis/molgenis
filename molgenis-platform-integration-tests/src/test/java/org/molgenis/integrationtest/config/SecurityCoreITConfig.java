package org.molgenis.integrationtest.config;

import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ RunAsUserTokenFactory.class, MolgenisUserDetailsChecker.class })
public class SecurityCoreITConfig
{
}
