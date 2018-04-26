/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.molgenis.core.util;

import com.google.gson.Gson;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.lang.Nullable;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Subclassed customization of {@link GsonHttpMessageConverter} that works around https://jira.spring.io/browse/SPR-16461
 */
public class MolgenisGsonHttpMessageConverter extends GsonHttpMessageConverter
{

	public MolgenisGsonHttpMessageConverter(Gson gson)
	{
		super(gson);
	}

	@Override
	protected void writeInternal(Object o, @Nullable Type type, Writer writer) throws Exception
	{
		super.writeInternal(o, null, writer);
	}

}
