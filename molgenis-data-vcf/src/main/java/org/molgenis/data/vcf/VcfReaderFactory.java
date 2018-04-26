package org.molgenis.data.vcf;

import com.google.common.base.Supplier;
import org.molgenis.vcf.VcfReader;

import java.io.Closeable;

public interface VcfReaderFactory extends Supplier<VcfReader>, Closeable
{

}
