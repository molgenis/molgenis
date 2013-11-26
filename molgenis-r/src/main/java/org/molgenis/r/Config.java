package org.molgenis.r;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config
{
	@Value("${r_script_executable:/usr/bin/Rscript}")
	private String rScriptExecutable;

	@Bean
	public RScriptExecutor rScriptExecutor()
	{
		return new RScriptExecutor(rScriptExecutable);
	}

}
