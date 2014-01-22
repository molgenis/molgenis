package org.molgenis.omx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.omx.OmxDataConfig;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.EntitySourceFactoriesRegistrator;
import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.permission.MolgenisPermissionServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.testng.annotations.Test;

public class OmxStandaloneTest
{

	@Test
	public void omxStandaloneTest() throws FileNotFoundException, IOException
	{
		System.setProperty("molgenis.home", "/Users/erwin/.molgenis/omx");

		User u = new User("admin", "admin", true, true, true, true, Arrays.asList(new GrantedAuthorityImpl(
				SecurityUtils.AUTHORITY_SU)));
		Authentication authentication = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.scan("org.molgenis.data.jpa", "org.molgenis.omx.core", "org.molgenis.omx.observ", "org.molgenis.omx.auth",
				"org.molgenis.omx.study", "org.molgenis.omx.workflow", "org.molgenis.omx.xgap",
				"org.molgenis.omx.patient");
		ctx.register(Config.class);
		ctx.register(DatabaseConfig.class);
		ctx.register(DataServiceImpl.class);
		ctx.register(OmxConfig.class);
		ctx.register(OmxDataConfig.class);
		ctx.register(EmbeddedElasticSearchConfig.class);
		ctx.register(EntitySourceFactoriesRegistrator.class);
		ctx.register(MolgenisPermissionServiceImpl.class);
		ctx.refresh();

		DataService dataService = ctx.getBean(DataService.class);
		for (String entity : dataService.getEntityNames())
		{
			System.out.println(entity);
		}

		for (Entity entity : dataService.findAll("celiacsprue"))
		{
			System.out.println(entity);
		}
		ctx.close();
	}

	private static class Config
	{
		@Bean
		public static PropertySourcesPlaceholderConfigurer properties()
		{
			PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
			Resource[] resources = new Resource[]
			{ new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
					new ClassPathResource("/molgenis.properties") };
			pspc.setLocations(resources);
			pspc.setFileEncoding("UTF-8");
			pspc.setIgnoreUnresolvablePlaceholders(true);
			pspc.setIgnoreResourceNotFound(true);
			pspc.setNullValue("@null");
			return pspc;
		}
	}
}
