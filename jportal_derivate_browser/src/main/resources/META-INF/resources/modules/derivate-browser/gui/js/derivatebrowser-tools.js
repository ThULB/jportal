var derivateBrowserTools = (function () {

    //private Methods   
	function getPDFImg(img, deriID, path){
		$(img).siblings(".img-placeholder").attr( "src", "/images/adobe-logo.svg");
		$(img).attr( "src", "/img/pdfthumb/" + deriID + path).on("load", function() {
			$(img).siblings(".img-placeholder").addClass("hidden");
			$(img).removeClass("hidden");
		});
	}
	
	function getImg(img, deriID, path, count){
		$(img).attr( "src", "/servlets/MCRTileCombineServlet/MIN/" + deriID + path).on("load", function() {
			$(img).siblings(".img-placeholder").addClass("hidden");
			$(img).removeClass("hidden");
		});
//		if (count < 6){
//			$.ajax({
//				url: "/servlets/MCRTileCombineServlet/MIN/" + deriID + path,
//				type: "GET",
//				processData : false,
//				success: function(data, textStatus, xhr) {
//					$(img).attr( "src", "/servlets/MCRTileCombineServlet/MIN/" + deriID + path).on("load", function() {
//						$(img).siblings(".img-placeholder").addClass("hidden");
//						$(img).removeClass("hidden");
//					});
//				},
//				error: function(error) {
//		    		setTimeout(function() {
//		    			getImg(img, deriID, path, count + 1);
//					}, 10000);
//				}
//			});
//		}
	}
    
    return {
        //public   
    	setImgPath: function(img, deriID, path) {
			if (path.endsWith("pdf")){
				getPDFImg(img, deriID, path);
			}
			else{
				getImg(img, deriID, path, 0);
			}
		}
		
    };
})();

if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}