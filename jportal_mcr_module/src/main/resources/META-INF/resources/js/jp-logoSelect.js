var plainLogoIn = "input[name='/mycoreobject/metadata/logo/url']";
var logoPlusText = "input[name='/mycoreobject/metadata/logo/url[2]']";
var logoAdressStart = "http://wrackdm17.thulb.uni-jena.de/logos/";

$(function() {
	checkIfEdit();
	
	var thumbnailId = null;
	
	$("#thumbLogoPlain").click(function() {
		thumbnailId = "thumbLogoPlain";
		showLogos();
//		select("a[value='" + $(plainLogoIn).val() + "']");
	});
	
	$("#thumbLogoText").click(function() {
		thumbnailId = "thumbLogoText";
		showLogos();
//		select("a[value='" + $(logoPlusText).val() + "']");
	});
  
  $("#personSelect-send").click(function() {
  	$("#" + thumbnailId + " > svg").remove();
  	var picAdress = $(".list-group-item.editor-logo-active").attr("value");
  	if(thumbnailId == 'thumbLogoPlain') {
  		$(plainLogoIn).val(picAdress);
  		$("#delLogoPlain").show();
  	} else {
  		$(logoPlusText).val(picAdress);
  		$("#delLogoText").show();
  	}
  	loadPic("#" + thumbnailId, picAdress);
  	$("#" + thumbnailId + " > p").hide();
  	$("#personSelect-modal").modal("hide");
  });
  
  $(".personSelect-cancel").click(function() {
  	$("#personSelect-modal").modal("hide");
  });
  
	$("#personSelect-modal").on('hidden.bs.modal', function() {
		$("#personSelect-send").attr("disabled", "");
		$("#personSelect-modal-body > .editor-logoSelect-container").remove();
	});
	
	$("#delLogoPlain").click(function() {
		$("#thumbLogoPlain > svg").remove();
		$(plainLogoIn).val("");
		$("#thumbLogoPlain > p").show();
		$("#delLogoPlain").hide();
	});
	
	$("#delLogoText").click(function() {
		$("#thumbLogoText > svg").remove();
		$(logoPlusText).val("");
		$("#thumbLogoText > p").show();
		$("#delLogoText").hide();
	});
});

function showLogos() {
	initBody();
	loadList("");
  $("#personSelect-modal").modal("show");
};

function initBody() {
	var rawDiv = "<div class='raw editor-logoSelect-container'></div>";
	$("#personSelect-modal-body").append(rawDiv);
	$("#personSelect-modal-title").text("Logo Auswahl");
	$("#personSelect-cancel-button").text("Abbrechen");
	$("#personSelect-send").text("Auswählen");
};

function select(item) {
	$(".list-group-item").removeClass("editor-logo-active");
	$(item).addClass('editor-logo-active');
	$("#personSelect-send").removeAttr("disabled");
};

function checkIfEdit() {
	if($("input[name='/mycoreobject/metadata/logo/url']").val() != ""){
		var picAdress = $(plainLogoIn).val();
		loadPic("#thumbLogoPlain", picAdress);
  	$("#thumbLogoPlain > p").hide();
  	$("#delLogoPlain").show();
	}
	
	if($("input[name='/mycoreobject/metadata/logo/url[2]']").val() != ""){
		var picAdress = $(logoPlusText).val();
		loadPic("#thumbLogoText", picAdress);
  	$("#thumbLogoText > p").hide();
  	$("#delLogoText").show();
	}
};

function loadList(list){
//	alert(list);
	$.ajax({
		url: jp.baseURL + "rsc/logoImporter/getList/" + list,
		type: "GET",
		success: function(data) {
					buildList(data, list);
				},
		error: function(error) {
					alert(error);
				}
	});
};

function buildList(data, subfolder) {
	$("#personSelect-modal-body > .editor-logoSelect-container").empty();
	$(".modal-header > a").remove();
	var list = $(data).find("a");
	$(list).each(function() {
		var href = $(this).attr("href");
		if(href.contains(".svg")) {
			var logoAdress = logoAdressStart + subfolder + href;
			var inputBase = '<a class="list-group-item thumbnail text-center" onclick="select(this)" value="' + logoAdress + '" ><h5>' + href + '</h5></a>';
			$("#personSelect-modal-body > .editor-logoSelect-container").append(inputBase);
			loadPic("a[value='" + logoAdress + "']", logoAdress);
		} else {
			if(href.charAt(href.length - 1) == "/" && href != "/") {
				if(href == "/logos/") {
					var inputBase = '<a class="list-group-item thumbnail glyphicon glyphicon-folder-open text-center" onclick="loadHelper(this)" value="" ><p>zurück</p></a>';
					$(".modal-header").append(inputBase);
				} else {
					var inputBase = '<a class="list-group-item thumbnail glyphicon glyphicon-folder-open text-center" onclick="loadHelper(this)" value="' + href + '" ><p>' + href + '</p></a>';
					$(".modal-header").append(inputBase);
				}
			}
		}
	});
};

function loadPic(element, targeturl) {
	$.ajax({
		url: jp.baseURL + "rsc/logoImporter/getList/" + encodeURIComponent(targeturl.substring(41)),
		type: "GET",
		success: function(data) {
					$(element).prepend($(data).find("svg"));
				},
		error: function(error) {
					alert(error);
				}
	});
};

function loadHelper(element) {
	loadList($(element).attr("value"));
};