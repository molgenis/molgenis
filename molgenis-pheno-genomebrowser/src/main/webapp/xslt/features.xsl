<?xml version="1.0" encoding="utf-8"?>
<!--
  ~ Copyright 2007 Antony Quinn, EMBL-European Bioinformatics Institute
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~
  ~ For further details of the mydas project, including source code,
  ~ downloads and documentation, please see:
  ~
  ~ http://code.google.com/p/mydas/
  ~
  -->        

<!--
/**
 * Logic for features.html.
 * Based on the idea of "style-free" stylesheets pioneered by Eric van der Vlist:
 * http://eric.van-der-vlist.com/blog/2368_The_influence_of_microformats_on_style-free_stylesheets.item
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
 -->
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:include href="strings.xsl"/>
    <xsl:include href="stylefree.xsl"/>

    <xsl:output method="html"
                indent="yes"
                media-type="text/html"
                encoding="iso-8859-1"
                omit-xml-declaration="yes"
                standalone="yes"/>

    <!-- HTML template -->
    <xsl:param name="template-uri" select="'features.html'"/>

    <!-- Location  for CSS, images ...etc relative to the DAS URL, eg. http://host/das/dsn-name/features -->
    <xsl:param name="resource-uri" select="'../../xslt/'"/>

    <!-- Source XML document (DAS features XML) -->
    <xsl:variable name="source" select="/"/>

    <!-- Load HTML template into variable -->
    <xsl:variable name="layout" select="document($template-uri)"/>

    <!-- Start matching nodes in the HTML template -->
    <xsl:template match="/">
        <xsl:apply-templates select="$layout/html" mode="stylefree-layout"/>
    </xsl:template>

    <!-- ======== Warnings ======== -->

    <!-- Error/unknown segment -->
    <xsl:template match="node()[@id='error-segment']" mode="stylefree-layout">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="stylefree-layout"/>
        </xsl:copy>
    </xsl:template>
   
    <!-- Display only if error or unknown segment -->
    <xsl:template match="node()[@id='error-segment']/@class" mode="stylefree-layout">
        <xsl:choose>
            <xsl:when test="$source/DASGFF/GFF/ERRORSEGMENT or $source/DASGFF/GFF/UNKNOWNSEGMENT">
                <!-- Change CSS class to "warning" -->
                <xsl:attribute name="{name()}">warning</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <!-- Echo -->
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>    

    <!-- Error segment ID -->
    <xsl:template match="node()[@class='error-segment-id']" mode="stylefree-layout">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:value-of select="$source/DASGFF/GFF/ERRORSEGMENT/@id"/>
            <xsl:value-of select="$source/DASGFF/GFF/UNKNOWNSEGMENT/@id"/>
        </xsl:copy>
    </xsl:template>

    <!-- Segment type -->
    <xsl:template match="node()[@class='segment-type']" mode="stylefree-layout">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:value-of select="name($source/DASGFF/GFF/*)"/>
        </xsl:copy>
    </xsl:template>

    <!-- ======== Sortable tables ======== -->

    <xsl:template match="table[@class='sortable']" mode="stylefree-layout">
        <xsl:variable name="table-id" select="@id"/>
        <xsl:copy>
            <!-- Table tag attributes -->
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <!-- Header row (column titles) -->
            <xsl:apply-templates select="tr[@class='head']" mode="stylefree-layout"/>
            <!-- Reference to row template - this will be copied for each feature -->
            <xsl:variable name="row-template" select="node()[@class='template']"/>
            <xsl:choose>
                <!-- Positional features -->
                <xsl:when test="$table-id='positional-features'">
                    <xsl:for-each select="$source/DASGFF/GFF/SEGMENT/FEATURE[not(START = '0') and not(END = '0')]">
                        <!-- Fill in values for row template (see xsl:template match="tr[@class='template'] below) -->
                        <xsl:apply-templates select="$row-template" mode="stylefree-layout">
                            <xsl:with-param name="feature" select="current()"/>
                        </xsl:apply-templates>
                    </xsl:for-each>
                </xsl:when>
                <!-- Non-positional features -->
                <xsl:when test="$table-id='non-positional-features'">
                    <xsl:for-each select="$source/DASGFF/GFF/SEGMENT/FEATURE[START = '0' and END = '0']">
                        <!-- Fill in values for row template (see xsl:template match="tr[@class='template'] below) -->
                        <xsl:apply-templates select="$row-template" mode="stylefree-layout">
                            <xsl:with-param name="feature" select="current()"/>
                        </xsl:apply-templates>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Summary -->
                    <xsl:for-each select="$source/DASGFF/GFF/SEGMENT">
                        <!-- Fill in values for row template (see xsl:template match="tr[@class='template'] below) -->
                        <xsl:apply-templates select="$row-template" mode="stylefree-layout">
                            <xsl:with-param name="segment" select="current()"/>
                        </xsl:apply-templates>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <!-- Row template -->
    <xsl:template match="tr[@class='template']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:param name="segment"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:apply-templates select="node()" mode="stylefree-layout">
                <xsl:with-param name="feature" select="$feature"/>
                <xsl:with-param name="segment" select="$segment"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <!-- ======== Summary ======== -->

    <!-- Only show summary section if only one segment in DAS XML -->
    <xsl:template match="node()[@id='summary']" mode="stylefree-layout">
        <xsl:if test="count($source/DASGFF/GFF/SEGMENT) &lt; 2">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()" mode="stylefree-layout"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <!-- Segment ID -->
    <xsl:template match="node()[@class='segment-id']" mode="stylefree-layout">
        <xsl:param name="segment" select="$source/DASGFF/GFF/SEGMENT"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:value-of select="$segment/@id"/>
        </xsl:copy>
    </xsl:template>

    <!-- Segment description -->
    <xsl:template match="node()[@class='segment-description']" mode="stylefree-layout">
        <xsl:param name="segment" select="$source/DASGFF/GFF/SEGMENT"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:value-of select="$segment/FEATURE[TYPE[@id='description']]/NOTE"/>
        </xsl:copy>
    </xsl:template>

    <!-- Sequence length -->
    <xsl:template match="node()[@class='sequence-length']" mode="stylefree-layout">
        <xsl:param name="segment" select="$source/DASGFF/GFF/SEGMENT"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:value-of select="$segment/@stop"/>
        </xsl:copy>
    </xsl:template>

    <!-- Sequence version -->
    <xsl:template match="node()[@class='sequence-version']" mode="stylefree-layout">
        <xsl:param name="segment" select="$source/DASGFF/GFF/SEGMENT"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:value-of select="$segment/@version"/>
        </xsl:copy>
    </xsl:template>

    <!-- ======== Features ======== -->

    <!-- Segment ID -->
    <xsl:template match="td[@class='feature-segment-id']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"              mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/../@id" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Only show segment ID columns if more than one segment in DAS XML -->
    <xsl:template match="node()[@class='feature-segment-id']/@class" mode="stylefree-layout">
        <!-- Only echo class name if less than 2 segments -->
        <xsl:if test="count($source/DASGFF/GFF/SEGMENT) &lt; 2">
            <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!-- Category -->
    <xsl:template match="node()[@class='category']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/TYPE/@category"/>
        </xsl:copy>
    </xsl:template>

    <!-- Type -->
    <xsl:template match="node()[@class='type']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"            mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/TYPE" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Method -->
    <xsl:template match="node()[@class='method']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"              mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/METHOD" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Feature -->
    <xsl:template match="node()[@class='feature']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"        mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature"  mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Start -->
    <xsl:template match="node()[@class='start']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"             mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/START" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- End -->
    <xsl:template match="node()[@class='end']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"            mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/END"  mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Score -->
    <xsl:template match="node()[@class='score']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"             mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/SCORE" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Orientation -->
    <xsl:template match="node()[@class='orientation']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"             mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/ORIENTATION" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Phase -->
    <xsl:template match="node()[@class='phase']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"             mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/PHASE" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Target -->
    <xsl:template match="node()[@class='target']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"             mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/TARGET" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Group -->
    <xsl:template match="node()[@class='group']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"             mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/GROUP" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Notes -->
    <xsl:template match="node()[@class='notes']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"            mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/NOTE" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- Links -->
    <xsl:template match="node()[@class='links']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"            mode="stylefree-layout"/>
            <xsl:apply-templates select="$feature/LINK" mode="source"/>
        </xsl:copy>
    </xsl:template>

    <!-- ======== Source ======== -->

    <!--
       The following templates match elements and attributes in the
       source document (DAS XML), hence the use of mode="source".
    -->

    <!-- Do not show @id in brackets if same as @category -->
    <xsl:template match="TYPE[@id = @category]" mode="source">
        <xsl:apply-templates select="."/>
    </xsl:template>

    <!-- Just show @id if @label is empty or same as @id -->
    <xsl:template match="FEATURE[@label='' or @label=@id]" mode="source">
        <xsl:apply-templates select="@id" mode="source"/>
    </xsl:template>

    <!-- Show label with @id in brackets, eg. "Homo sapiens (9606)"  -->
    <xsl:template match="FEATURE" mode="source">
        <xsl:apply-templates select="@label" mode="source"/>&#160;
        (<xsl:apply-templates select="@id"   mode="source"/>)
    </xsl:template>

    <!-- Wrap text if longer than 20 characters -->
    <xsl:template match="FEATURE/@id | FEATURE/@label" mode="source">
        <xsl:call-template name="string-split">
            <xsl:with-param name="str" select="."/>
            <xsl:with-param name="max" select="20"/>
        </xsl:call-template>
    </xsl:template>

    <!-- Just show @id if text is empty or same as @id -->
    <xsl:template match="TYPE[.='' or .=@id] | METHOD[.='' or .=@id]" mode="source">
        <xsl:apply-templates select="@id"/>
    </xsl:template>

    <!-- Show text with @id in brackets, eg. "Sequence variation (VARIANT)"  -->
    <xsl:template match="TYPE | METHOD" mode="source">
        <xsl:apply-templates select="."/>&#160;(<xsl:apply-templates select="@id"/>)
    </xsl:template>

    <!-- Add line-break (can be more than one) -->
    <xsl:template match="NOTE" mode="source">
        <xsl:apply-templates select="."/><br/>
    </xsl:template>

    <!-- Add line-break (can be more than one) -->
    <xsl:template match="LINK" mode="source">
        <xsl:apply-templates select="." mode="source-link"/><br/>
    </xsl:template>

    <!-- Create HTML link -->
    <xsl:template match="LINK" mode="source-link">
        <a href="{@href}">
            <!-- Wrap text if longer than 20 characters -->
            <xsl:call-template name="string-split">
                <xsl:with-param name="str" select="."/>
                <xsl:with-param name="max" select="20"/>
            </xsl:call-template>
        </a>
    </xsl:template>

    <!-- Add line-break (can be more than one) -->
    <xsl:template match="TARGET" mode="source">
        <xsl:apply-templates select="." mode="source-target"/><br/>
    </xsl:template>

    <!-- Target details -->
    <xsl:template match="TARGET" mode="source-target">
        <xsl:apply-templates select="."/>
        (<xsl:apply-templates select="@id"/>)
        <xsl:apply-templates select="@start"/> -
        <xsl:apply-templates select="@stop"/>
    </xsl:template>

    <!-- Add line-break (can be more than one) -->
    <xsl:template match="GROUP" mode="source">
        <xsl:apply-templates select="." mode="source-group"/><br/>
    </xsl:template>

    <!-- Group details -->
    <xsl:template match="GROUP" mode="source-group">
        <xsl:apply-templates select="@type"/>:
        <xsl:apply-templates select="@label"/>
        (<xsl:apply-templates select="@id"/>)
        <xsl:apply-templates select="NOTE" mode="source"/>
        <xsl:apply-templates select="LINK" mode="source"/>
        <xsl:apply-templates select="TARGET" mode="source"/>
    </xsl:template>

    <!-- ======== Graphic ======== -->

    <xsl:template match="node()[@class='graphic' or @class='graphic-padding']" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="stylefree-layout">
                <xsl:with-param name="feature" select="$feature"/>
            </xsl:apply-templates>
            <xsl:apply-templates mode="stylefree-layout">
                <xsl:with-param name="feature" select="$feature"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <!-- Calculate whitespace padding as (feature-start / protein-length) * 100 -->
    <xsl:template match="span[@class='graphic-padding']/@style" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:attribute name="{name()}">
            width:<xsl:value-of select="format-number(($feature/START div $feature/../@stop) * 100, '0.0')"/>%;
        </xsl:attribute>
    </xsl:template>

    <!-- Calculate graphic as ((feature-end - feature-start) / protein-length) * 100 -->
    <xsl:template match="span[@class='graphic']/@style" mode="stylefree-layout">
        <xsl:param name="feature"/>
        <xsl:attribute name="{name()}">
            background-color:<xsl:apply-templates select="$feature/TYPE" mode="background-color"/>;
            border-color:    <xsl:apply-templates select="$feature/TYPE" mode="border-color"/>;
            width:<xsl:value-of select="format-number((($feature/END - $feature/START) div $feature/../@stop) * 100, '0.0')"/>%;
        </xsl:attribute>
    </xsl:template>

    <!-- TODO: background-color and border-color depend on feature/@category - get from DAS stylesheet command, otherwise use defaults -->

    <!-- Lookup colours for given category -->
    <xsl:template match="TYPE" mode="background-color">
        <xsl:choose>
            <xsl:when test="@category='Molecule Processing'">
                blue
            </xsl:when>
            <xsl:when test="@category='Secondary structure'">
                red
            </xsl:when>
            <xsl:otherwise>
                purple
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="TYPE" mode="border-color">
        <xsl:choose>
            <xsl:when test="@category='Molecule Processing'">
                navy
            </xsl:when>
            <xsl:when test="@category='Secondary structure'">
                maroon
            </xsl:when>
            <xsl:otherwise>
                fuschia
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
