<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" />
  <xsl:param name="subselect.varpath" />
  <xsl:param name="subselect.webpage" />

  <xsl:template match="jpsubselect">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="$subselect.type = 'person'">
          <xsl:value-of select="'Person'" />
        </xsl:when>
        <xsl:when test="$subselect.type = 'institution'">
          <xsl:value-of select="'Institution'" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:value-of select="concat('Bitte benutzen Sie das Suchfeld, um eine ', $type, ' auszuwählen.')"></xsl:value-of>
    <!-- http://localhost:18101/servlets/XMLEditor?
    _action=end.subselect
    &subselect.session=9xvpggvoyi
    &subselect.varpath=/mycoreobject/metadata/participants/participant
    &subselect.webpage=editor_form_commit-jpjournal.xml%3Ftype%3Djpjournal%26step%3Dcommit%26cancelUrl%3Dhttp%253A%252F%252Flocalhost%253A18101%252Freceive%252Fjportal_jpjournal_00000761%26sourceUri%3DxslStyle%253Amycoreobject-editor%253Amcrobject%253Ajportal_jpjournal_00000761%26mcrid%3Djportal_jpjournal_00000761%26
    &mode=prefix
    &_var_@xlink:href=jportal_person_00061171
    &_var_@xlink:title=Gleichen-Ru%C3%9Fwurm,%20Alexander%20von%20(1865-11-06%20-%201947-10-25,%20Schriftsteller;%20Herausgeber;%20%C3%9Cbersetzer;%20Kulturphilosoph)%20%20%20%20%20%20%20%20%20%20%20%20%20
    &_var_@field=participants_art
    &_var_@operator==
    &_var_@value=jportal_person_00061171 -->
  </xsl:template>
</xsl:stylesheet>