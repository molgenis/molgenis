package org.molgenis.omx;

import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

//@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration
{

	// @Override
	// @Bean
	// public MethodInterceptor methodSecurityInterceptor() throws Exception
	// {
	// MethodSecurityInterceptor methodSecurityInterceptor = new MolgenisMethodSecurityInterceptor();
	// methodSecurityInterceptor.setAccessDecisionManager(accessDecisionManager());
	// methodSecurityInterceptor.setAfterInvocationManager(afterInvocationManager());
	// methodSecurityInterceptor.setAuthenticationManager(authenticationManager());
	// methodSecurityInterceptor.setSecurityMetadataSource(methodSecurityMetadataSource());
	//
	// RunAsManager runAsManager = runAsManager();
	// if (runAsManager != null)
	// {
	// methodSecurityInterceptor.setRunAsManager(runAsManager);
	// }
	//
	// return methodSecurityInterceptor;
	// }

	// private final String runAsKey = RandomStringUtils.random(10);
	//
	// @Override
	// protected RunAsManager runAsManager()
	// {
	// RunAsManagerImpl impl = new RunAsManagerImpl()
	// {
	//
	// @Override
	// public Authentication buildRunAs(Authentication authentication, Object object,
	// Collection<ConfigAttribute> attributes)
	// {
	// System.out.println("BuidRunAs authentication=" + authentication);
	// return super.buildRunAs(authentication, object, attributes);
	// }
	//
	// };
	// impl.setKey(runAsKey);
	//
	// return impl;
	// }
	//
	// @Bean
	// public RunAsImplAuthenticationProvider runAsAuthenticationProvider()
	// {
	// RunAsImplAuthenticationProvider runAs = new RunAsImplAuthenticationProvider();
	// runAs.setKey(runAsKey);
	//
	// return runAs;
	// }
	//
	// @Override
	// protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception
	// {
	// auth.authenticationProvider(runAsAuthenticationProvider());
	// }

}
