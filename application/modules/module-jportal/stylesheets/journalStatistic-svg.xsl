<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:svg="http://www.w3.org/2000/svg">

    <!-- ======================================================================================================================== -->

    <xsl:template match="journalStatistic">

        <svg:svg  width="1000" height="600" viewBox="0 0 5 5">
            <svg:rect id="black_stripe" fill="#000" width="5" height="1" />
            <svg:rect id="gray_i" fill="#444" width="5" height="1" y="1" />
            <svg:rect id="gray_ii" fill="#888" width="5" height="1" y="2" />
            <svg:rect id="gray_iii" fill="#ccc" width="5" height="1" y="3" />
            <svg:rect id="white" fill="#fff" width="5" height="1" y="4" />
        </svg:svg>

    </xsl:template>

    <!-- ======================================================================================================================== -->

</xsl:stylesheet>