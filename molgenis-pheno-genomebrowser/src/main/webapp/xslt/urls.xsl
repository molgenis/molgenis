<?xml version="1.0" encoding="utf-8"?>
<!--
/**
 * Templates for building URLs to bioinformatics databases such as UniProt and ChEBI.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @see     urls.xml
 *
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Document containing set of URLs (this can be passed in as a nodeset) -->
    <xsl:param name="urls" select="document('urls.xml')/urls"/>

    <!--
        Convert comma-separated list of queries to HTML links based on database name.
        @param  database    Name of database, for example "UniProt"
        @param  query       Query terms, for example "P38398,P35823,P98005"
        @param  prefix      Prefix to display in link, for example "CHEBI:"
        @param  type        Type of URL to look up, for example "text"
        @param  title       Tool tip to use in anchor element
        @param  delimiter   String used to delimit query list, for example ","
    -->
    <xsl:template name="urls-create-links">
        <xsl:param name="database"/>
        <xsl:param name="query"/>
        <xsl:param name="prefix"    select="''"/>
        <xsl:param name="type"      select="''"/>
        <xsl:param name="title"     select="''"/>
        <xsl:param name="delimiter" select="','"/>
        <xsl:variable name="nextQuery" select="substring-before($query, $delimiter)"/>
        <xsl:choose>
            <xsl:when test="contains($query, $delimiter)">
                <xsl:call-template name="urls-create-link">
                    <xsl:with-param name="database" select="$database"/>
                    <xsl:with-param name="query"    select="$nextQuery"/>
                    <xsl:with-param name="prefix"   select="$prefix"/>
                    <xsl:with-param name="type"     select="$type"/>
                    <xsl:with-param name="title"    select="$title"/>
                </xsl:call-template>&#160;
                <!-- Call recursively until the last delimiter found -->
                <xsl:call-template name="urls-create-links">
                    <xsl:with-param name="database"  select="$database"/>
                    <xsl:with-param name="query"     select="substring-after($query, $delimiter)"/>
                    <xsl:with-param name="prefix"    select="$prefix"/>
                    <xsl:with-param name="type"      select="$type"/>
                    <xsl:with-param name="title"     select="$title"/>
                    <xsl:with-param name="delimiter" select="$delimiter"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="urls-create-link">
                    <xsl:with-param name="database" select="$database"/>
                    <xsl:with-param name="query"    select="$query"/>
                    <xsl:with-param name="prefix"   select="$prefix"/>
                    <xsl:with-param name="type"     select="$type"/>
                    <xsl:with-param name="title"    select="$title"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        Make HTML link if able to find URL matching database name.
        @param  database Name of database, for example "UniProt"
        @param  query    Query term, for example "P38398"
        @param  prefix   Prefix to display in link, for example "CHEBI:"
        @param  type     Type of URL to look up, for example "text"
        @param  title    Tool tip to use in anchor element
    -->
    <xsl:template name="urls-create-link">
        <xsl:param name="database"/>
        <xsl:param name="query"/>
        <xsl:param name="prefix" select="''"/>
        <xsl:param name="type"   select="''"/>
        <xsl:param name="title"  select="''"/>
        <xsl:variable name="href">
            <xsl:call-template name="urls-get-href">
                <xsl:with-param name="database" select="$database"/>
                <xsl:with-param name="query"    select="$query"/>
                <xsl:with-param name="type"     select="$type"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not(string-length($href) = 0)">
                <a href="{$href}" title="{$title}">
                    <xsl:value-of select="concat($prefix, $query)"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($prefix, $query)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        Returns URL based on database name and query by looking up URL from $urls and
        appending or embeding query as appropriate.
        @param  database Name of database, for example "UniProt"
        @param  query    Query term, for example "P38398"
        @param  type     Type of URL to look up, for example "text"
    -->
    <xsl:template name="urls-get-href">
        <xsl:param name="database"/>
        <xsl:param name="query"/>
        <xsl:param name="type" select="''"/>
        <!-- Find URL entry in $urls corresponding to start of $database (convert upper-case characters to lower-case so can do case-insensitive string comparison) -->
        <xsl:variable name="url"
                      select="$urls/url[starts-with(translate(@database, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),
                                                    translate($database, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))
                                        and @type=$type]"/>
        <xsl:if test="$url">
            <xsl:variable name="href" select="normalize-space($url/text())"/>
            <!-- ID version separator (eg. "." in IPI00187967.1) -->
            <xsl:variable name="version-sep" select="$urls/@version-separator"/>
            <xsl:variable name="id">
                <xsl:choose>
                    <xsl:when test="$url/@remove-version = 'true' and contains($query, $version-sep)">
                        <!-- Remove version from query -->
                        <xsl:value-of select="substring-before($query, $version-sep)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$query"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$url/@accession = 'false'">
                    <!-- Do not append id to URL -->
                    <xsl:value-of select="$href"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$url/@embedded = 'true'">
                            <!-- id is embedded in URL -->
                            <xsl:variable name="idvar" select="$urls/@idvar"/>
                            <xsl:value-of select="substring-before($href, $idvar)"/>
                            <xsl:value-of select="$id"/>
                            <xsl:value-of select="substring-after($href, $idvar)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- Append id to URL -->
                            <xsl:value-of select="concat($href, $id)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
