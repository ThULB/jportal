<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="">

  <xsl:template name="jp.layout.footer">
    <div class="jp-layout-footer">
      <xsl:apply-templates select="document(concat('logo:footer:', /mycoreobject/@ID))" mode="footer"/>
    </div>
  </xsl:template>

  <xsl:template match="logos" mode="footer">
    <ul>
      <xsl:apply-templates select="entity" mode="footer"/>
    </ul>
  </xsl:template>

  <xsl:template match="entity[@url != '']" mode="footer">
    <li>
      <a href="{@url}">
        <img src="{@logoURL}" class="logo"/>
      </a>
    </li>
  </xsl:template>

  <xsl:template match="entity" mode="footer">
    <li>
      <img src="{@logoURL}" class="logo"/>
    </li>
  </xsl:template>

</xsl:stylesheet>
