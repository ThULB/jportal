$(function() {
	var logoURLBase = jp.baseURL + "rsc/proxy/logo/";
	checkIfEdit();

	$("#thumbLogoPlain, #thumbLogoText").click(function() {
		let thumbnailId = this.getAttribute("id");
		let inputTag = this.parentNode.querySelector("input");
		initModal(inputTag, thumbnailId)
	});

	function initModal(inputTag, thumbnailId) {
		let logoPath = inputTag.hasAttribute("value") ? inputTag.getAttribute("value").replace(/\/*\w+.svg/, "") : "";
		showLogos();
		selectLogo();
		
		function showLogos() {
			initBody();
			
			loadElement(logoPath, buildList);
			$("#personSelect-modal").modal("show");
		};
		
		function selectLogo() {
			if(logoPath != ""){
				setTimeout(function() {
					select("a[value='" + logoPath + "']");
				}, 100);
				
				setTimeout(function() {
					var logo = $("a[value='" + logoPath + "']");
					var container = $("div.editor-logoSelect-container");
					$(".editor-logoSelect-container").scrollTop($(container).scrollTop() + $(logo).position().top - $(container).height() / 2 + $(logo).height() / 2);
				}, 200);
			}
		}
		
		$("#personSelect-send").unbind().click(function() {
			$("#" + thumbnailId + " > img").remove();
			var picAdress = $(".list-group-item.editor-logo-active").attr("value");
			inputTag.setAttribute("value", picAdress);
			$("#" + thumbnailId).next("span").show();
			document.getElementById(thumbnailId).appendChild(newLogoImg(picAdress));

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
		$(this).prev(".thumbnail").children().filter("img").remove();
		$(this).prev(".thumbnail").children().filter("p").show();
		$(this).next("input").val("");
		$(this).hide();
	});
	
	function checkIfEdit() {
		loadPic("thumbLogoPlain", "input[name='/mycoreobject/metadata/logo/url']");
		loadPic("thumbLogoText","input[name='/mycoreobject/metadata/logo/url[2]']");
	};

	function loadPic(anchorID, inputSelector) {
		let a = document.getElementById(anchorID);
    let input = document.querySelector(inputSelector);
    let inputVal = input.getAttribute("value");
		if(inputVal != ""){
      a.appendChild(newLogoImg(inputVal))

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

    let doc = parseHTML(data);
    let anchors = doc.getElementsByTagName('a');
    let fragment = document.createDocumentFragment();

    for (i = 0; i < anchors.length; i++) {
			var currentItem = anchors.item(i);
			var href = currentItem.getAttribute('href');
			var val = currentItem.innerText;

      if(href.indexOf(".svg") > -1) {
        let logoItem = newLogoItem(subfolder, href);
        fragment.appendChild(logoItem);
      } else if(href !== "/" && val === 'Parent Directory') {
        var inputBase = '<a class="list-group-item thumbnail glyphicon glyphicon-folder-open text-center" value="" ><p>zurück</p></a>';
        $(".modal-header").append(inputBase);
      } else if(val !== 'Parent Directory' && href.charAt(href.length - 1) === "/"){
        var inputBase = '<a class="list-group-item thumbnail glyphicon glyphicon-folder-open text-center" value="' + href + '" ><p>' + href + '</p></a>';
        $(".modal-header").append(inputBase);
      }
    }

    document.querySelector("#personSelect-modal-body > .editor-logoSelect-container").appendChild(fragment)
	};

	function newLogoImg(logoAdress){
    let img = document.createElement("img");
    img.setAttribute("src", logoURLBase + logoAdress);
    // img.setAttribute("src", logoURLBase + encodeURIComponent(logoAdress));

		return img;
  }

	function newLogoItem(subfolder, href){
		let logoAdress = subfolder + href;
		let a = document.createElement("a");
		a.classList.add("list-group-item");
		a.classList.add("thumbnail");
		a.classList.add("text-center");

		a.setAttribute("value", logoAdress);

    a.appendChild(newLogoImg(logoAdress));

    let h5 = document.createElement("h5");
    h5.innerText = href
    a.appendChild(h5);

    return a;
  }

	function parseHTML(htmlStr) {
    parser = new DOMParser();
    return parser.parseFromString(htmlStr, "text/html");
	}
	
	function loadElement(path, callback){
		$.ajax({
			url: logoURLBase + path,
			type: "GET",
			success: function(data) {
				callback(data, path);
			},
			error: function(error) {
				console.log(error);
			}
		});
	};
});