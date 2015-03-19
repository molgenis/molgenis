<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gscf="gscf"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<xsl:output method="text" encoding="UTF-8" indent="no" />

<xsl:strip-space elements="*"/>
<xsl:template match="/">
<xsl:text>name	package	label	extends	abstract	description
</xsl:text>
	<xsl:for-each select="distinct-values(//gscf:entity)">
		<xsl:text>"</xsl:text><xsl:value-of select="."/><xsl:text>"	NMC			TRUE	""
</xsl:text>
	</xsl:for-each>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="gscf:template">
<xsl:text>entity</xsl:text><xsl:value-of select="count(preceding-sibling::gscf:template)"/><xsl:text>	NMC	"</xsl:text>
<xsl:value-of select="gscf:name"/><xsl:text>"	"</xsl:text>
<xsl:value-of select="gscf:entity"/><xsl:text>"	FALSE	"</xsl:text>
<xsl:value-of select="gscf:description"/><xsl:text>"
</xsl:text>
</xsl:template>

</xsl:stylesheet>