package org.molgenis.core.ui.style;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.style.BootstrapVersion.BOOTSTRAP_VERSION_3;
import static org.molgenis.core.ui.style.BootstrapVersion.BOOTSTRAP_VERSION_4;

@Component
public class ThemeFingerprintRegistry
{
	private final Map<String, String> styleFingerprints;

	private StyleService styleService;

	public ThemeFingerprintRegistry(StyleService styleService)
	{
		styleFingerprints = new ConcurrentHashMap<>();
		this.styleService = requireNonNull(styleService);
	}

	public String getFingerprint(String themeUri) throws IOException, MolgenisStyleException
	{
		String fileFingerprint = styleFingerprints.get(themeUri);
		if (fileFingerprint == null)
		{
			fileFingerprint = createFingerprint(themeUri);
			styleFingerprints.put(themeUri, fileFingerprint);
		}
		return fileFingerprint;
	}

	private String createFingerprint(String themeUri) throws IOException, MolgenisStyleException
	{
		String fileName = extractThemeNameFromThemeUri(themeUri);
		BootstrapVersion version = extractBootstrapVersionFromPath(themeUri);
		FileSystemResource styleData = styleService.getThemeData(fileName, version);
		byte[] bytes = IOUtils.toByteArray(styleData.getInputStream());
		HashCode crc32 = Hashing.crc32().hashBytes(bytes);
		return BaseEncoding.base64Url().omitPadding().encode(crc32.asBytes());
	}

	private String extractThemeNameFromThemeUri(String themeUri)
	{
		return themeUri.substring(themeUri.lastIndexOf("/") + 1);
	}

	private BootstrapVersion extractBootstrapVersionFromPath(String themeUri)
	{
		String path = themeUri.substring(0, themeUri.lastIndexOf("/"));
		return path.contains("bootstrap-4") ? BOOTSTRAP_VERSION_4 : BOOTSTRAP_VERSION_3;
	}
}
