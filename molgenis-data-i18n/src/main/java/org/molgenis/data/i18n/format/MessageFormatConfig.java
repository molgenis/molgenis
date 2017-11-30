package org.molgenis.data.i18n.format;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.FormatFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MessageFormatConfig
{
	public static final String LABEL = "label";
	public static final String ID = "id";

	@Bean
	public Map<String, FormatFactory> messageFormatRegistry()
	{
		return ImmutableMap.of(LABEL, new LabelFormatFactory(), ID, new IdFormatFactory());
	}
}
