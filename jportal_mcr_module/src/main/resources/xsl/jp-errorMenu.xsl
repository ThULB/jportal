<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="jp-errorMenu">
    <h1 style="margin-bottom: 34px;">Systemfehler Übersicht</h1>
    <div class="row">
      <div class="col-sm-6">
        <div class="panel panel-primary">
          <div class="panel-heading"><b>Suchen</b> - Unfertig, Korrupt oder nicht verlinkt</div>
          <div class="panel-body center">
            <div class="btn-group">
              <button type="button" class="btn btn-default" id="findBrokenVolumes">Bände</button>
              <button type="button" class="btn btn-default" data-toggle="popover" title="Suchbeschreibung" data-placement="right"
                      data-content="Zeigt alle Bände ohne Publikationsdatum (Ein Publikationsdatum kann auch vom Elterndokument [Zeitschrift etc.] gesetzt werden)
                      UND alle Bänder ohne Elterndokument UND alle Bände ohne journalID.">
                <i class="glyphicon glyphicon-info-sign" />
              </button>
            </div>
            <div class="btn-group">
              <button type="button" class="btn btn-default" id="findBrokenArticles">Artikel</button>
              <button type="button" class="btn btn-default" data-toggle="popover" title="Suchbeschreibung" data-placement="right"
                      data-content="Zeigt alle Artikel ohne Publikationsdatum (Ein Publikationsdatum kann auch vom Elterndokument [Zeitschrift etc.] gesetzt werden)
                      UND alle Artikel ohne Elterndokument UND alle Artikel ohne journalID.">
                <i class="glyphicon glyphicon-info-sign" />
              </button>
            </div>
            <div class="btn-group">
              <button type="button" class="btn btn-default" id="findBrokenPersons">Personen</button>
              <button type="button" class="btn btn-default" data-toggle="popover" title="Suchbeschreibung" data-placement="right"
                      data-content="Zeigt alle nicht verlinkten Personen.">
                <i class="glyphicon glyphicon-info-sign" />
              </button>
            </div>
            <div class="btn-group">
              <button type="button" class="btn btn-default" id="findBrokenInstitutions">Institutionen</button>
                          <button type="button" class="btn btn-default" data-toggle="popover" title="Suchbeschreibung" data-placement="right"
                      data-content="Zeigt alle nicht verlinkten Institutionen.">
                <i class="glyphicon glyphicon-info-sign" />
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="col-sm-6"></div>
    </div>

    <style>
      .popover {
        width: 400px;
      }
    </style>
    <script type="text/javascript">
      $(document).ready(function() {
        $(function () {
          $('[data-toggle="popover"]').popover();
        });

        $("#findBrokenVolumes").on("click", function() {
          var qry = "+objectType:jpvolume -(published:* AND parent:* AND journalID:*)";
          solrQuery(qry);
        });
        $("#findBrokenArticles").on("click", function() {
          var qry = "+objectType:jparticle -(published:* AND parent:* AND journalID:*)";
          solrQuery(qry);
        });
        $("#findBrokenPersons").on("click", function() {
          var qry = "-({!join from=link to=id}objectType:jpvolume) AND -({!join from=link to=id}objectType:jparticle) AND -({!join from=link to=id}objectType:jpjournal) +(objectType:person)";
          solrQuery(qry);
        });
        $("#findBrokenInstitutions").on("click", function() {
          var qry = "-({!join from=link to=id}objectType:jpvolume) AND -({!join from=link to=id}objectType:jparticle) AND -({!join from=link to=id}objectType:jpjournal) +(objectType:jpinst)";
          solrQuery(qry);
        });
      });
      function solrQuery(qry) {
        var url = jp.baseURL + "servlets/solr/select?q=" + qry;
        window.location = url;
      }
    </script>
  </xsl:template>

</xsl:stylesheet>