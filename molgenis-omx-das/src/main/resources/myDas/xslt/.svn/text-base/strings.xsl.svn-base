<?xml version="1.0" encoding="utf-8"?>
<!--
/**
 * String utilities.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
 -->
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!--
        Splits long strings to allow text wrapping by inserting non-breaking spaces after
        every n characters. For example, "ABCDEFGHIJKLMNOPQ" is converted
        to "ABCDEFGHIJ KLMNOPQ"
        @param  str String to split
        @param  max Number of characters to output before adding non-breaking space (default is 10 characters)
    -->
    <xsl:template name="string-split">
        <xsl:param name="str"/>
        <xsl:param name="max" select="10"/>
        <xsl:choose>
            <xsl:when test="string-length($str) > $max">
                <xsl:variable name="substr" select="substring($str, 1, $max)"/>
                <xsl:value-of select="substring($substr, 1, $max)"/>&#160;
                <xsl:variable name="subsubstr"
                              select="substring($str, number($max + 1), string-length($str))"/>
                <xsl:choose>
                    <xsl:when test="string-length($subsubstr) > $max">
                        <!-- Recursion -->
                        <xsl:call-template name="string-split">
                            <xsl:with-param name="str" select="$subsubstr"/>
                            <xsl:with-param name="max" select="$max"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$subsubstr"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
