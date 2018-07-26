<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY html-output SYSTEM "xsl/xsl-output-html.fragment">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:jerseyUtil="xalan://org.mycore.frontend.jersey.MCRJerseyUtil" exclude-result-prefixes="xalan xlink jerseyUtil">
  &html-output;

  <xsl:variable name="httpError" select="jerseyUtil:fromStatusCode(number(/jp-error/@HttpError))" />

  <xsl:variable name="MainTitle" select="concat(/jp-error/@HttpError, ' ', $httpError)" />

  <xsl:variable name="PageTitle" select="concat(/jp-error/@HttpError, ' ',$httpError)" />

  <xsl:template match="/jp-error">
    <xsl:apply-templates select="." mode="jp.error" />
  </xsl:template>

  <!-- Access denied -->
  <xsl:template match="jp-error[@HttpError = '401']" mode="jp.error">
    <xsl:call-template name="jp.error.jumbotron">
      <xsl:with-param name="errorCode" select="@HttpError" />
      <xsl:with-param name="header" select="'Access denied'" />
      <xsl:with-param name="text" select="'You are not allowed to see this page.'" />
    </xsl:call-template>
  </xsl:template>

  <!-- Not Found -->
  <xsl:template match="jp-error[@HttpError = '404']" mode="jp.error">
    <xsl:call-template name="jp.error.jumbotron">
      <xsl:with-param name="errorCode" select="@HttpError" />
      <xsl:with-param name="header" select="'Page not found'" />
      <xsl:with-param name="text" select="'The page you were looking for either doesn’t exist or some terrible, terrible error has occurred.'" />
    </xsl:call-template>
    <div>
      <img class="img-responsive" src="{$WebApplicationBaseURL}images/not-found.jpg" alt="not found" />
      <small class="pull-right">
        <a href="https://creativecommons.org/licenses/by/2.0/">photo by Matthew Henry</a>
      </small>
    </div>
  </xsl:template>

  <!-- All other errors -->
  <xsl:template match="jp-error" mode="jp.error">
    <xsl:call-template name="jp.error.jumbotron">
      <xsl:with-param name="errorCode" select="@HttpError" />
      <xsl:with-param name="header" select="jerseyUtil:fromStatusCode(number(@HttpError))" />
      <xsl:with-param name="text" select="text()" />
    </xsl:call-template>
  </xsl:template>

  <!-- jumbotron template -->
  <xsl:template name="jp.error.jumbotron">
    <xsl:param name="errorCode" />
    <xsl:param name="header" />
    <xsl:param name="text" />
    <div class="jumbotron" style="margin-top: 20px;">
      <h1>
        <small>
          <xsl:value-of select="$errorCode" />
        </small>
        <xsl:value-of select="concat(' ', $header)" />
      </h1>
      <p>
        <xsl:value-of select="$text" />
      </p>
      <p>
        <a class="btn btn-primary btn-lg" role="button" href="javascript:history.back()">Back to previous page</a>
      </p>
    </div>
  </xsl:template>

  <xsl:include href="MyCoReLayout.xsl" />

</xsl:stylesheet>
