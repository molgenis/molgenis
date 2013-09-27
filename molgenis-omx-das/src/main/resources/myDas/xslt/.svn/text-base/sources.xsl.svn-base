<?xml version="1.0" encoding="utf-8"?>
    <!--
    /**
     * Logic for sources.html.
     * Based on dsn.xsl on MyDas distribution (Antony Quinn) and the Proserver sources xsl (Andy Jenkinson)
     * @author Leyla Garcia
     * @version $Id$
     * @since   1.0
     */
     -->
    <xsl:stylesheet	version="2.0"
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

        <xsl:include href="stylefree.xsl"/>

        <xsl:output method="html"
                    indent="yes"
                    media-type="text/html"
                    encoding="iso-8859-1"
                    omit-xml-declaration="yes"
                    standalone="yes"/>

        <xsl:param name="template-uri" select="'sources.html'"/>

        <xsl:param name="resource-uri" select="''"/>

        <!-- Source XML document (SOURCES XML) -->
        <xsl:variable name="root" select="/"/>

        <!-- HTML template -->
        <xsl:variable name="layout" select="document($template-uri)"/>

        <!-- Start matching nodes in the HTML template -->
        <xsl:template match="/">
            <xsl:apply-templates select="$layout/html" mode="stylefree-layout"/>
        </xsl:template>

        <xsl:template match="table[@class='sortable']" mode="stylefree-layout">
            <xsl:copy>
                <!-- Table tag attributes -->
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <!-- Header row (column titles) -->
                <xsl:apply-templates select="tr[@class='head']" mode="stylefree-layout"/>
                <!-- Reference to row template - this will be copied for each feature -->
                <xsl:variable name="row-template" select="node()[@class='template']"/>
                <!-- Rare case of for-each being more readable than templates -->
                <xsl:for-each select="$root/SOURCES/SOURCE">
                    <!-- Fill in values for row template (see xsl:template match="tr[@class='template'] below) -->
                    <xsl:apply-templates select="$row-template" mode="stylefree-layout">
                        <xsl:with-param name="source" select="current()"/>
                    </xsl:apply-templates>
                </xsl:for-each>
            </xsl:copy>
        </xsl:template>

        <!-- Row template (called for each SOURCE in the source document) -->
        <xsl:template match="tr[@class='template']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <xsl:apply-templates select="node()" mode="stylefree-layout">
                    <xsl:with-param name="source" select="$source"/>
                </xsl:apply-templates>
            </xsl:copy>
        </xsl:template>

        <!-- version-uri -->
        <xsl:template match="node()[@class='version-uri']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <a href="{$source/VERSION/@uri}">
                    <xsl:value-of select="$source/VERSION/@uri"/>
                </a>
            </xsl:copy>
        </xsl:template>

        <!-- short-name -->
        <xsl:template match="node()[@class='short-name']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <xsl:value-of select="$source/@title"/>
            </xsl:copy>
        </xsl:template>

        <!-- Description -->
        <xsl:template match="node()[@class='description']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <xsl:value-of select="$source/@description"/>
            </xsl:copy>
        </xsl:template>

        <!-- doc-href -->
        <xsl:template match="node()[@class='doc-href']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <a href="{$source/@doc_href}">
                    <xsl:value-of select="$source/@doc_href"/>
                </a>
            </xsl:copy>
        </xsl:template>

        <!-- capabilities (can be more than one)-->
        <xsl:template match="node()[@class='capabilities']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <xsl:for-each select="$source/VERSION/CAPABILITY">
                    <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                    <xsl:choose>
                        <xsl:when test="contains(@type,'sequence')">
                            <a href="{@query_uri}?segment=">
                                <xsl:value-of select="@type"/>
                            </a>
                        </xsl:when>
                        <xsl:when test="contains(@type,'features')">
                            <a href="{@query_uri}?segment=">
                                <xsl:value-of select="@type"/>
                            </a>
                        </xsl:when>
                        <xsl:when test="contains(@type,'entry_points')">
                            <a href="{@query_uri}?rows=1-100">
                                <xsl:value-of select="@type"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{@query_uri}">
                                <xsl:value-of select="@type"/>
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                    <br/>
                </xsl:for-each>
            </xsl:copy>
        </xsl:template>

        <!-- coordinate (can be more than one)-->
        <xsl:template match="node()[@class='coordinate']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:for-each select="$source/VERSION/COORDINATES">
                    <!--table border="1" class="notSorting">
                        <tr>
                            <td width="40%"><a href="{@uri}"><xsl:value-of select="@uri"/></a></td>
                            <td width="20%"><xsl:value-of select="@source"/></td>
                            <td width="20%"><xsl:value-of select="@authority"/></td>
                            <td width="10%"><xsl:value-of select="@test_range"/></td>
                        </tr>
                    </table-->
                    <!--ul id="navlist">
                        <li><a href="{@uri}"><xsl:value-of select="@uri"/></a></li>
                        <li><xsl:value-of select="@source"/></li>
                        <li><xsl:value-of select="@authority"/></li>
                        <li><xsl:value-of select="@test_range"/></li>
                    </ul-->
                    <div id="container">
                        <div id="row">
                            <div class="oddColumn" style="width: 40%"><a href="{@uri}"><xsl:value-of select="@uri"/></a></div>
                            <div class="evenColumn" style="width: 20%"><xsl:value-of select="@source"/></div>
                            <div class="oddColumn" style="width: 20%"><xsl:value-of select="@authority"/></div>
                            <div class="evenColumn" style="width: 20%"><xsl:value-of select="@test_range"/></div>
                        </div>
                    </div>
                </xsl:for-each>
            </xsl:copy>
        </xsl:template>

        <!-- maintainer -->
        <xsl:template match="node()[@class='maintainer']" mode="stylefree-layout">
            <xsl:param name="source"/>
            <xsl:copy>
                <xsl:apply-templates select="@*" mode="stylefree-layout"/>
                <xsl:value-of select="$source/MAINTAINER/@email"/>
            </xsl:copy>
        </xsl:template>

        <!--XML format - div xml-->
        <xsl:template match="node()[@class='xml']" mode="stylefree-layout">
            <xsl:copy>
                <xsl:for-each select="$root">
                    <xsl:choose>
                        <xsl:when test="*">
                            <xsl:apply-templates select="@*" mode="xml-att"/>
                            <xsl:apply-templates select="*" mode="xml-main"/>
                        </xsl:when>
                        <xsl:when test="text()">
                            <span style="color:blue">&lt;<xsl:value-of select="name()"/></span><xsl:apply-templates select="@*" mode="xml-att"/><span style="color:blue">&gt;</span><xsl:apply-templates select="text()" mode="xml-text"/><span style="color:blue">&lt;/<xsl:value-of select="name()"/>&gt;</span><br/>
                        </xsl:when>
                        <xsl:otherwise>
                            <span style="color:blue">&lt;<xsl:value-of select="name()"/></span><xsl:apply-templates select="@*" mode="xml-att"/><span style="color:blue"> /&gt;</span><br/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:copy>
        </xsl:template>
        <!--XML format - internal xml-->
        <xsl:template match="*" mode="xml-main">
            <xsl:choose>
                <xsl:when test="*">
                    <span style="color:blue">&lt;<xsl:value-of select="name()"/></span>
                    <xsl:apply-templates select="@*" mode="xml-att"/>
                    <span style="color:blue">&gt;</span>
                    <div style="margin-left: 1em">
                    <xsl:apply-templates select="*" mode="xml-main"/>
                    </div>
                    <span style="color:blue">&lt;/<xsl:value-of select="name()"/>&gt;</span>
                    <br/>
                </xsl:when>
                <xsl:when test="text()">
                    <span style="color:blue">&lt;<xsl:value-of select="name()"/></span><xsl:apply-templates select="@*" mode="xml-att"/><span style="color:blue">&gt;</span><xsl:apply-templates select="text()" mode="xml-text"/><span style="color:blue">&lt;/<xsl:value-of select="name()"/>&gt;</span><br/>
                </xsl:when>
                <xsl:otherwise>
                    <span style="color:blue">&lt;<xsl:value-of select="name()"/></span><xsl:apply-templates select="@*" mode="xml-att"/><span style="color:blue"> /&gt;</span><br/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:template>
        <!--XML format - attribute-->
        <xsl:template match="@*" mode="xml-att">
            <span style="color:purple"><xsl:text>&#160;</xsl:text><xsl:value-of select="name()"/>=&quot;</span><span style="color:red"><xsl:value-of select="."/></span><span style="color:purple">&quot;</span>
        </xsl:template>
        <!--XML format - text-->
        <xsl:template match="text()" mode="xml-text">
            <div style="margin-left: 1em; color:black"><xsl:value-of select="."/></div>
        </xsl:template>

    </xsl:stylesheet>
