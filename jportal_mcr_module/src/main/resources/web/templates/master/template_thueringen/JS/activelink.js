window.onload=highlightCurrentPageLink;

function highlightCurrentPageLink() {
	var anzHeads = document.getElementsByTagName("th").length;
	var checker = true;

	for ( var i = 0; i <= anzHeads - 1; i++) {
		var anzHrefs = document.getElementsByTagName("th")[i].getElementsByTagName("a").length;

		for ( var k = 0; k <= anzHrefs - 1; k++) {
			if (document.getElementsByTagName("th")[i].getElementsByTagName("a")[k].href == document.location.href) {
				document.getElementsByTagName("th")[i].getElementsByTagName("a")[k].setAttribute("id", "navi_main_active");
				checker = false;
			}
		}
	}
}