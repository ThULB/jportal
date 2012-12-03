<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                  xmlns:xlink="http://www.w3.org/1999/xlink">

  <!-- default template: just copy -->
  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <!-- only one hidden journal id allowed -->
  <xsl:template match="hidden_jpjournalsID/hidden_jpjournalID[position() &gt; 1]">
  </xsl:template>

  <!-- only one date of the same type -->
  <xsl:template match="dates/date[(@inherited = preceding-sibling::date/@inherited) and (@type = preceding-sibling::date/@type)]">
  </xsl:template>

  <!-- only one main title -->
  <xsl:template match="maintitles/maintitle[@inherited = preceding-sibling::maintitle/@inherited]">
  </xsl:template>

  <!-- fix xlink:title in participants -->
  <xsl:variable name="typesOfParticipants">
    <xsl:value-of select="'partner operator mainauthor subauthor mainPublisher subPublisher translator fotographer illustrator commentator composer painter moderator editor sponsor organiser reviewer authorOfReviewer contibutorOfReviewer mentionedInst mentionedPerson publisherOfOriginal misc author interviewer interviewee publisher contributor director assumedAuthor assumedMentionedPerson editorOfReviewedWork translatorOfReviewedWork widmungsempf Angeblicher_Autor Drucker pictured_person Begruender Bearbeiter Kartograph publisherPrinter'" />
  </xsl:variable>

  <xsl:template match="participants/participant[contains(@xlink:href, '_person_') and contains($typesOfParticipants, @xlink:title)]">
    <xsl:variable name="person" select="xalan:nodeset(document(concat('mcrobject:', @xlink:href))/mycoreobject)" />
    <xsl:variable name="lastName" select="$person/metadata/def.heading/heading/lastName" />
    <xsl:variable name="firstName" select="$person/metadata/def.heading/heading/firstName" />
    <xsl:variable name="title">
      <xsl:choose>
        <xsl:when test="$lastName != '' and $firstName != ''">
          <xsl:value-of select="concat($lastName, ', ', $firstName)" />
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="concat($lastName, $firstName)" />        
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates select="." mode="copyParticipant">
      <xsl:with-param name="title" select="$title" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="participants/participant[contains(@xlink:href, '_jpinst_') and contains($typesOfParticipants, @xlink:title)]">
    <xsl:variable name="jpinst" select="xalan:nodeset(document(concat('mcrobject:', @xlink:href))/mycoreobject)" />
    <xsl:apply-templates select="." mode="copyParticipant">
      <xsl:with-param name="title" select="$jpinst/metadata/names/name/fullname" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="participant" mode="copyParticipant">
    <xsl:param name="title" />
    <xsl:element name="participant">
      <xsl:attribute name="type"><xsl:value-of select="@xlink:title" /></xsl:attribute>
      <xsl:attribute name="inherited"><xsl:value-of select="@inherited" /></xsl:attribute>
      <xsl:attribute name="xlink:type"><xsl:value-of select="@xlink:type" /></xsl:attribute>
      <xsl:attribute name="xlink:href"><xsl:value-of select="@xlink:href" /></xsl:attribute>
      <xsl:attribute name="xlink:title"><xsl:value-of select="$title" /></xsl:attribute>
    </xsl:element>    
  </xsl:template>
</xsl:stylesheet>
