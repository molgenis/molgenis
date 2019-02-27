package org.molgenis.data.file.minio;

import io.minio.MinioClient;
import java.io.IOException;

interface MinioClientFactory {
  MinioClient createClient() throws IOException;
}
