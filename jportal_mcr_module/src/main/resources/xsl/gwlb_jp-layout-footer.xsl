<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:frontend="xalan://org.mycore.frontend.MCRFrontendUtil"

                exclude-result-prefixes="xlink mcrxml">

    <xsl:param name="JP.Site.Footer.Logo.url" />
    <xsl:param name="JP.Site.Footer.Logo.default" />
    <xsl:param name="JP.Site.Footer.Logo.small" />

    <xsl:template name="gwlb_jp.layout.footer">
        <div class="jp-layout-footer">
            <xsl:apply-templates select="." mode="footer" />
        </div>
    </xsl:template>

    <xsl:template match="*" mode="footer">
        <xsl:call-template name="jp.footer.print.default" />
    </xsl:template>

    <xsl:template name="jp.footer.print.default">
        <div id="footer">
        <div class="mainWrapperFooter">
            <a class="VZG" href="{$JP.Site.Footer.Logo.url}">
                <img src="{concat(frontend:getBaseURL(), 'images/gwlb/VZG.png')}" class="logo" />
            </a>

            <a class="DFG" href="http://www.dfg.de">
                <strong style="line-height: 4; display: block;">Gefördert durch:</strong>
                <img src="{concat(frontend:getBaseURL(), 'images/gwlb/DFG_Logo.png')}" class="logo" />
            </a>
        </div>
        </div>
    </xsl:template>

    <xsl:template name="jp.footer.print.small">
        <a href="{$JP.Site.Footer.Logo.url}">
            <img src="{concat(frontend:getBaseURL(), $JP.Site.Footer.Logo.small)}" class="logo" />
        </a>

        <a class="DFG" href="http://www.dfg.de">
            <strong style="line-height: 4; display: block;">Gefördert durch:</strong>
            <img src="{concat(frontend:getBaseURL(), 'images/gwlb/DFG_Logo.png')}" class="logo" />
        </a>
    </xsl:template>

    <xsl:template match="/mycoreobject[contains(@ID, 'jpjournal') or contains(@ID, 'jpvolume') or contains(@ID, 'jparticle')]" mode="footer">
        <xsl:if test="$journalID != ''">
            <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />
            <xsl:choose>
                <xsl:when test="$journal/metadata/participants/participant[@type='partner']">
                    <xsl:call-template name="jp.footer.print.partner">
                        <xsl:with-param name="journal" select="$journal" />
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="jp.footer.print.default" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template name="jp.footer.print.partner">
        <xsl:param name="journal" />
        <ul>
            <xsl:apply-templates select="$journal/metadata/participants/participant[@type='partner']" mode="footer" />
            <li>
                <xsl:call-template name="jp.footer.print.small" />
            </li>
        </ul>
    </xsl:template>

    <xsl:template match="participant" mode="footer">
        <xsl:variable name="participant" select="document(concat('mcrobject:', @xlink:href))/mycoreobject" />
        <xsl:if test="$participant/metadata/logo/url[@type='logoPlain']">
            <li>
                <xsl:choose>
                    <xsl:when test="$participant/metadata/urls/url">
                        <a href="{$participant/metadata/urls/url/@xlink:href}">
                            <img src="{$participant/metadata/logo/url[@type='logoPlain']}" alt="{@xlink:title}" class="logo" />
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img src="{$participant/metadata/logo/url[@type='logoPlain']}" alt="{@xlink:title}" class="logo" />
                    </xsl:otherwise>
                </xsl:choose>
            </li>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
