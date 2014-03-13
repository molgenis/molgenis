package org.molgenis.data.annotation.impl.datastructures;

/**
 * Created by jvelde on 2/13/14.
 */
public class Locus {

    String chrom;
    Long pos;

    public Locus(String chrom, Long pos) {
        this.chrom = chrom;
        this.pos = pos;
    }

    public String getChrom() {
        return chrom;
    }

    public Long getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return "Locus{" +
                "chrom='" + chrom + '\'' +
                ", pos=" + pos +
                '}';
    }
}
