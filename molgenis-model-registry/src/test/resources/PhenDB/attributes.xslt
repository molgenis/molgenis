<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gscf="gscf"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<xsl:output method="text" encoding="UTF-8" indent="no" />

<xsl:strip-space elements="*"/>
<xsl:template match="/">
<xsl:text>name	label	entity	datatype	idattribute	nillable	description
</xsl:text>
	<xsl:for-each select="distinct-values(//gscf:entity)"><xsl:text>id		"</xsl:text><xsl:value-of select="."/><xsl:text>"	int	TRUE	FALSE	"generated ID attribute"
</xsl:text>
	</xsl:for-each>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="gscf:template">
<xsl:text>id	"id"	entity</xsl:text><xsl:value-of select="count(preceding-sibling::gscf:template)"/><xsl:text>	int	TRUE	FALSE	"generated ID attribute"
</xsl:text>
<xsl:apply-templates select="gscf:templateFields">
	<xsl:with-param name="entity"><xsl:text>entity</xsl:text><xsl:value-of select="count(preceding-sibling::gscf:template)"/></xsl:with-param>
</xsl:apply-templates>
</xsl:template>

<xsl:template match="gscf:templateField">
<xsl:param name = "entity" />
<xsl:text>attr</xsl:text><xsl:value-of select="count(preceding-sibling::gscf:templateField)"/><xsl:text>	</xsl:text>
<xsl:text>"</xsl:text><xsl:value-of select="gscf:name"/><xsl:text>"	"</xsl:text>
<xsl:value-of select="$entity"/><xsl:text>"	</xsl:text>
<xsl:if test="gscf:type='STRING'">string</xsl:if>
<xsl:if test="gscf:type='DOUBLE'">decimal</xsl:if>
<xsl:if test="gscf:type='LONG'">long</xsl:if>
<xsl:if test="gscf:type='EXTENDABLESTRINGLIST'">text</xsl:if>
<xsl:if test="gscf:type='FILE'">text</xsl:if>
<xsl:if test="gscf:type='STRINGLIST'">text</xsl:if>
<xsl:if test="gscf:type='TEXT'">text</xsl:if><xsl:text>	</xsl:text>
<xsl:if test="gscf:preferredIdentifier='true'">TRUE</xsl:if><xsl:text>	</xsl:text>
<xsl:if test="gscf:required='false'">TRUE</xsl:if><xsl:text>	"</xsl:text>
<xsl:if test="gscf:comment"><xsl:value-of select="replace(gscf:comment, '&quot;', '', 'm')"/></xsl:if><xsl:text>"
</xsl:text>
</xsl:template>

</xsl:stylesheet>