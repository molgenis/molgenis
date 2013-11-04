<?xml version="1.0" encoding="utf-8"?>
<!--
/**
 * Standard templates for style-free stylesheets.
 * Requires global resource-uri param or variable in including template, for example:
 * <xsl:param name="resource-uri" select="'../web/'"/>
 *
 * @author  Antony Quinn
 * @version $Id:$
 * @since   1.0
 */
 -->
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!--
        Returns URL based on database name and query by looking up URL from $urls and
        appending or embeding query as appropriate.
    -->
    <xsl:template match="@*|node()" mode="stylefree-layout">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="stylefree-layout"/>
        </xsl:copy>
    </xsl:template>

    <!--
        Prefixes URLs in <link>, <img> and <script> tags with $resource-uri variable
        declared in including template. For example, <img src="images/hello.png"/> is converted to
        <img src="web/images/images/hello.png"/> if $resource-uri="web/"
    -->
    <xsl:template match="link/@href | img/@src | script/@src" mode="stylefree-layout">
        <xsl:choose>
            <xsl:when test="not($resource-uri = '') and not(contains(., '://'))">
                <xsl:attribute name="{name()}">
                    <xsl:value-of select="concat($resource-uri, .)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="{name()}">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Set image path for sortable table -->
    <xsl:template match="script[not(@src)]" mode="stylefree-layout">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            var image_path = "<xsl:value-of select="$resource-uri"/>images/";
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
