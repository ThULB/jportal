<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="gbv-editMenu">
    <a id="editMenuDropDown" class="editMenuDropDown icon-cogs" />
    <menu id="editMenu" class="editMenu hidden">
      <xsl:apply-templates mode="menuItem" select="$menu/item" />
    </menu>

    <script type="text/javascript">
      $("#editMenuDropDown").on("click", function(event) {
        $("#editMenu").toggleClass('hidden');
        $("#editMenuDropDown").toggleClass('selected');
      });
    </script>
  </xsl:template>

</xsl:stylesheet>
