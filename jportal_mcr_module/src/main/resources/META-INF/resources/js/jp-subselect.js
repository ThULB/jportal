var jp = jp || {};

jp.subselect = {

	cache: [],

	select: function(i) {
		$(".list-group-item").removeClass("active");
		var popoverContainer = $(".jp-popover-container");
		if(popoverContainer.hasClass("hidden")) {
			popoverContainer.fadeIn({
				duration: 400,
				start: function() {
					popoverContainer.removeClass("hidden");
				}
			});
		}
		var item = $(i);
		item.addClass('active');
		var metadataContainer = $("#metadata-content");	
		var mcrid = item.attr("data-jp-mcrid");
		var url = item.attr("data-submit-url");
		jp.subselect.get(mcrid, function(html) {
			metadataContainer.empty();
			metadataContainer.append(html);
			$("#selectButton").removeAttr("disabled");
			$("#selectButton").attr("href", url);
		});
	},

	get: function(/*String*/ mcrid, /*function*/ onSuccess) {
		if(jp.subselect.cache[mcrid]) {
			onSuccess(jp.subselect.cache[mcrid]);
			return;
		}
		$.ajax({
			url: jp.baseURL + "rsc/render/object/" + mcrid,
			dataType: "html"
		}).done(function(html) {
      let modHtml = $(html);
      modHtml.find('.col-sm-9').removeClass('col-sm-9').addClass('col-sm-8');
			jp.subselect.cache[mcrid] = modHtml;
			onSuccess(modHtml);
		});
	}

};