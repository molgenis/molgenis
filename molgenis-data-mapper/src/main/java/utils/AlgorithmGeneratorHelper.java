package utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public class AlgorithmGeneratorHelper
{
	private final static Pattern MAGMA_ATTRIBUTE_PATTERN = Pattern.compile("\\$\\('([^\\$\\(\\)]*)'\\)");

	public static Set<AttributeMetaData> extractSourceAttributesFromAlgorithm(String algorithm,
			EntityMetaData sourceEntityMetaData)
	{
		if (StringUtils.isNotBlank(algorithm))
		{
			Set<String> attributeNames = new HashSet<>();
			Matcher matcher = MAGMA_ATTRIBUTE_PATTERN.matcher(algorithm);
			while (matcher.find())
			{
				attributeNames.add(matcher.group(1));
			}
			return attributeNames.stream().map(attributeName -> sourceEntityMetaData.getAttribute(attributeName))
					.filter(Objects::nonNull).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}
}
