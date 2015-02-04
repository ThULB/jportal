$(function () {
	var personSelectSessionID = "";
	var submit = "";
    var type = "";
    
    $("#main").parent().on("click",".jp-personSelect-person", function(event) {
		event.preventDefault();
        type = "person";
        $(this).addClass("personSelect-use");
        setSubmit($(this));
		getPersonSelect("","score+desc", 0, getModal);
	});
	
    $("#main").parent().on("click",".jp-personSelect-inst", function(event) {
		event.preventDefault();
        type = "jpinst";
    	setSubmit($(this));
		getPersonSelect("","score+desc", 0, getModal);
	});

    $("#main").parent().on("click", "#submitButton", function(event) {
		event.preventDefault();
		getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), 0, changeModal);
	});
	
    $("#main").parent().on("click", "#personSelect-send", function() {
		var entry = $("#personSelect-select .list-group-item.active");
		var title = $(entry).attr("data-submit-url").split("@xlink:title=")[1];
		sendPerson(entry.attr("data-jp-mcrid"), title, personSelectSessionID);
	});

    $("#main").parent().on("change", "#personSelect-searchBar select", function() {
		getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), 0, changeModal);
	});
	
    $("#main").parent().on("click", "#personSelect-select ul.pagination > li:not(.active)", function(event) {
		event.preventDefault();
		var link = $(this).find("a").attr("href").split("start=");
		if (link.length == 2){
			getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), link[1], changeModal);
		}
		else{
			getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), 0, changeModal);
		}
	});
	
    $("#main").parent().on("click", "#personSelect-select .list-group-item ", function() {
		$("#personSelect-send").removeAttr("disabled");
	});
	
    $("#main").parent().on("click", ".personSelect-cancel", function() {
        $("#personSelect-modal").modal("hide");
	});

	function getPersonSelect(qry, sort, start, callback){
		$.ajax({
			url: jp.baseURL + "servlets/solr/subselect?qry=" + qry + "&sort=" + sort + "&XSL.subselect.type=" + type + "&start=" + start,
			type: "GET",
			dataType: "html",
			success: function(data) {
						callback(preProcess(data));
					},
			error: function(error) {
						alert(error);
					}
		});
	}
	
	function sendPerson(href, title, session){
		$.ajax({
			url: jp.baseURL + "servlets/XEditor?_xed_session=" + session + "&@xlink:href=" + href + "&@xlink:title=" + title + "&" + submit,
			type: "POST",
			dataType: "text",
			success: function(data) {
						$("input[name='_xed_session']").val(data);
						$(".personSelect-use").parent().prev(".jp-personSelect-name").html(title + " " + "<label>( " + href + " )</label>");
						$(this).removeClass("personSelect-use");
						$("#personSelect-modal").modal("hide");
					},
			error: function(error) {
						alert(error);
					}
		});
	}

	function getModal(bodyContent) {
		$("#personSelect-modal-body").append(bodyContent);
        $("#personSelect-cancel-button").html($("#personSelect-select > div > a#selectButton").next().html());
		$("#personSelect-send").html($("#personSelect-select > div > a#selectButton").html());
		$("#personSelect-modal-title").html($("#personSelect-select > div > h2").html());
		$("#personSelect-modal").on('hidden.bs.modal', function() {
			$("#personSelect-modal-body").html("");
			$("#personSelect-send").attr("disabled", "");
		});
        $("#personSelect-modal").modal("show");
	}
	
	function changeModal(bodyContent) {
		$("#personSelect-modal-body").html(bodyContent);
	}
	
	function preProcess(data) {
		var html =  $("<div></div>");
		html.append($("<div></div>").append(data).find("#searchBar").attr("id", "personSelect-searchBar"));
		html.append($("<div></div>").append(data).find("#main").attr("id", "personSelect-select"));
		$(html).find("#selectButton").parent().hide();
		$(html).find("hr").remove();
		$(html).find("select[name='sort']").attr("onChange", "");
		return html;
	}

    function setSubmit(elm) {
        var pos = $(elm).attr("name").indexOf(":");
        submit = "_xed_submit_ajaxSubselect" + $(elm).attr("name").substring(pos);
        personSelectSessionID = $("input[name='_xed_session']").val();
    }
  });