package org.molgenis.data.vcf;

import java.io.Closeable;

import org.molgenis.vcf.VcfReader;

import com.google.common.base.Supplier;

public interface VcfReaderFactory extends Supplier<VcfReader>, Closeable
{

}
