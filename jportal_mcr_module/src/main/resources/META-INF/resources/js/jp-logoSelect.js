$(function() {
	var logoURLBase = "";
	checkIfEdit();
	getBaseURL(function(url){
		logoURLBase = url;
	});
	
	$("#thumbLogoPlain, #thumbLogoText").click(function() {
		var thumbnailId = $(this).attr("id");
		var logoIn = $(this).next().next("input");
		initModal(logoIn, thumbnailId)
	});
	
	function initModal(logoIn, thumbnailId) {
		showLogos();
		selectLogo();
		
		function showLogos() {
			initBody();
			
			var jumpTo = "";
			
			if($(logoIn).val() != ""){
				jumpTo = $(logoIn).val().substring(41);
				var pieces = jumpTo.split("/");
				jumpTo = jumpTo.replace(pieces[pieces.length-1], "");
			}
			
			loadElement(jumpTo, buildList);
			$("#personSelect-modal").modal("show");
		};
		
		function selectLogo() {
			if($(logoIn).val() != ""){
				setTimeout(function() {
					select("a[value='" + $(logoIn).val() + "']");
				}, 100);
				
				setTimeout(function() {
					var logo = $("a[value='" + $(logoIn).val() + "']");
					var container = $("div.editor-logoSelect-container");
					$(".editor-logoSelect-container").scrollTop($(container).scrollTop() + $(logo).position().top - $(container).height() / 2 + $(logo).height() / 2);
				}, 200);
			}
		}
		
		$("#personSelect-send").unbind().click(function() {
			$("#" + thumbnailId + " > svg").remove();
			var picAdress = $(".list-group-item.editor-logo-active").attr("value");
			$(logoIn).val(picAdress);
			$("#" + thumbnailId).next("span").show();
			loadElement(encodeURIComponent(picAdress.substring(41)), function(data){
				$("#" + thumbnailId).prepend($(data).find("svg"));
  		});
			$("#" + thumbnailId + " > p").hide();
			$("#personSelect-modal").modal("hide");
		});
		
		$(".personSelect-cancel").click(function() {
			$("#personSelect-modal").modal("hide");
		});
		
		$("#personSelect-modal").on('hidden.bs.modal', function() {
			$("#personSelect-send").attr("disabled", "");
			$("#personSelect-modal").unbind();
			$("#personSelect-modal-body > .editor-logoSelect-container").remove();
		});
		
		$("#personSelect-modal").on("click", ".modal-header > a.glyphicon-folder-open", function() {
			$("#personSelect-send").attr("disabled", "");
			loadElement($(this).attr("value"), buildList);
		});
		
		$("#personSelect-modal").on("click", ".editor-logoSelect-container > a.list-group-item", function() {
			select(this);
		});
		
		function initBody() {
			var rawDiv = "<div class='raw editor-logoSelect-container'></div>";
			$("#personSelect-modal-body").append(rawDiv);
			$("#personSelect-cancel-button").text("Abbrechen");
			$("#personSelect-send").text("Auswählen");
		};
	}
	
	$("#delLogoPlain, #delLogoText").click(function() {
		$(this).prev(".thumbnail").children().filter("svg").remove();
		$(this).prev(".thumbnail").children().filter("p").show();
		$(this).next("input").val("");
		$(this).hide();
	});
	
	function checkIfEdit() {
		var name = "input[name='/mycoreobject/metadata/logo/url']";
		loadPic(name);
		
		name = "input[name='/mycoreobject/metadata/logo/url[2]']";
		loadPic(name);
	};
	
	function loadPic(input) {
		if($(input).val() != ""){
			picAdress = $(input).val();
			loadElement(encodeURIComponent(picAdress.substring(41)), function(data){
				$(input).prev().prev(".thumbnail").prepend($(data).find("svg"));
			});
			$(input).prev().prev(".thumbnail").children().filter("p").hide();
			$(input).prev("span").show();
		}
	}
	
	function select(item) {
		$(".list-group-item").removeClass("editor-logo-active");
		$(item).addClass('editor-logo-active');
		$("#personSelect-send").removeAttr("disabled");
	};

	function buildList(data, subfolder) {
		$("#personSelect-modal-title").text("Logo Auswahl aus Ordner: /logos/" + subfolder);
		$("#personSelect-modal-body > .editor-logoSelect-container").empty();
		$(".modal-header > a").remove();
		var list = $(data).find("a");
		$(list).each(function() {
			var href = $(this).attr("href");
			var logoAdress = logoURLBase + "/" + subfolder + href;
			if(href.indexOf(".svg") > -1) {
				var inputBase = '<a class="list-group-item thumbnail text-center" value="' + logoAdress + '" ><h5>' + decodeURIComponent(href) + '</h5></a>';
				$("#personSelect-modal-body > .editor-logoSelect-container").append(inputBase);
				loadElement(encodeURIComponent(logoAdress.substring(41)), function(data){
					$("a[value='" + logoAdress + "']").prepend($(data).find("svg"));
	  		});
			} else {
				if(href.charAt(href.length - 1) == "/" && href != "/") {
					if(href == "/logos/") {
						var inputBase = '<a class="list-group-item thumbnail glyphicon glyphicon-folder-open text-center" value="" ><p>zurück</p></a>';
						$(".modal-header").append(inputBase);
					} else {
						var inputBase = '<a class="list-group-item thumbnail glyphicon glyphicon-folder-open text-center" value="' + href + '" ><p>' + href + '</p></a>';
						$(".modal-header").append(inputBase);
					}
				}
			}
		});
	};
	
	function loadElement(path, callback){
		$.ajax({
			url: jp.baseURL + "rsc/logo/get/" + path,
			type: "GET",
			success: function(data) {
				callback(data, path);
			},
			error: function(error) {
				alert(error);
			}
		});
	};

	function getBaseURL(callback) {
		$.ajax({
			url: jp.baseURL + "rsc/logo/getLogoURLBase",
			type: "GET",
			success: function(data) {
				callback(data);
			},
			error: function(error) {
				alert(error);
			}
		});
	};
});