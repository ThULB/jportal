$(document).ready(function () {
    $(".jp-subSelect").each(function () {
        var labelName = $(this).find(".jp-subSelect-name");

        if (labelName.find("input").filter(":first").val() != "") {
            var title = labelName.find("input").filter(":first").val();
            var href = labelName.find("input").filter(":last").val();
            labelName.find(".jp-name-display").html(title + " " + "<label>( " + href + " )</label>");
        }

        $(this).find(".jp-subSelect-button").each(function () {
            var that = $(this);
            that.click(function () {
                var type = that.data('type');
                var namebase = that.parent().prev(".jp-subSelect-name").find("input").filter(":last").attr("name");
                namebase = namebase.substr(0, namebase.length - 12);

                var conn = connector(type, namebase);
                initModal(conn, labelName);
            })
        })
    });

    function connector(type, namebase) {
        var conn = {};

        conn.getPersonSelect = function (qry, sort, start, callback) {
            $.ajax({
                url: jp.baseURL + "servlets/solr/subselect?qry=" + qry + "&sort=" + sort + "&XSL.subselect.type=" + type + "&rows=6" + "&start=" + start,
                type: "GET",
                dataType: "html",
                success: function (data) {
                    callback(data);
                },
                error: function (error) {
                    alert(error.statusText + "/n" + error.responseText);
                }
            });
        };
        conn.sendPerson = function (href, title, callback) {
            var submit = "_xed_submit_ajaxSubselect:" + namebase + ":";
            var session = $("input[name='_xed_session']").val();
            $.ajax({
                url: jp.baseURL + "servlets/XEditor?_xed_session=" + session + "&@xlink:href=" + href + "&@xlink:title=" + title + "&" + submit,
                type: "POST",
                dataType: "text",
                success: function (data) {
                    callback(data);
                },
                error: function (error) {
                    alert(error.statusText + "/n" + error.responseText);
                }
            });
        };

        return conn;
    }

    function initModal(conn, labelName) {
        conn.getPersonSelect("", "score+desc", 0, getModal);

        function getModal(bodyContent) {
            var processedContent = preProcess(bodyContent);
            $("#personSelect-modal-body").append(processedContent);
            $("#personSelect-cancel-button").html($("#personSelect-select > div > a#selectButton").next().html());
            $("#personSelect-send").html($("#personSelect-select > div > a#selectButton").html());
            $("#personSelect-modal-title").html($("#personSelect-select > div > h2").html());

            var personSelectModal = $("#personSelect-modal");
            personSelectModal.on('hidden.bs.modal', function () {
                $("#personSelect-send").attr("disabled", "");
                $("#personSelect-modal-body").children().remove();
            });
            personSelectModal.on('shown.bs.modal', function () {
                personSelectModal.find("#inputField").focus();
            });
            personSelectModal.modal("show");
        }

        function changeModal(bodyContent) {
            var processedContent = preProcess(bodyContent);
            $("#personSelect-modal-body").html(processedContent);
        }

        function preProcess(data) {
            var html = $("<div></div>");
            html.append($("<div></div>").append(data).find("#searchBar").attr("id", "personSelect-searchBar"));
            html.append($("<div></div>").append(data).find("#main").attr("id", "personSelect-select"));
            $(html).find("#selectButton").parent().hide();
            $(html).find("hr").remove();
            $(html).find("select[name='sort']").attr("onChange", "");
            return html;
        }

        var parent = $("#personSelect-modal");

        parent.parent().on("click", "#submitButton", function (event) {
            event.preventDefault();
            conn.getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), 0, changeModal);
        });

        parent.unbind().on("click", "#personSelect-send", function () {
            var entry = $("#personSelect-select .list-group-item.active");
            var title = $(entry).attr("data-submit-url").split("@xlink:title=")[1];
            var href = entry.attr("data-jp-mcrid");
            labelName.find(".jp-name-display").html(title + " " + "<label>( " + href + " )</label>");
            labelName.find("input").filter(":first").val(title);
            labelName.find("input").filter(":last").val(href);
            $("#personSelect-modal").modal("hide");
            conn.sendPerson(href, title, function (data) {
                $("input[name='_xed_session']").val(data);
            });
        });

        parent.on("change", "#personSelect-searchBar select", function () {
            conn.getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), 0, changeModal);
        });

        parent.on("click", "#personSelect-select ul.jp-pagination > li:not(.active)", function (event) {
            event.preventDefault();
            var link = $(this).find("a").attr("href").split("start=");
            if (link.length === 2) {
                conn.getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), link[1], changeModal);
            }
            else {
                conn.getPersonSelect($("#personSelect-searchBar input[name='qry']").val(), $("select[name='sort']").val().replace(" ", "+"), 0, changeModal);
            }
        });

        parent.on("click", "#personSelect-select .list-group-item ", function () {
            $("#personSelect-send").removeAttr("disabled");
        });

        parent.on("click", ".personSelect-cancel", function () {
            $("#personSelect-modal").modal("hide");
        });
    }
});