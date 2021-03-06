$(function() {
  let main = $("#main");

  main.parent().on("click", "button[name*='_xed_submit_insert']", function(event) {
    event.preventDefault();
    sendEditor($(this).attr("name"));
  });

  main.parent().on("click", "button[name*='_xed_submit_remove']", function(event) {
    event.preventDefault();
    sendEditor($(this).attr("name"));
  });

  main.parent().on("click", "button[name*='_xed_submit_up']", function(event) {
    event.preventDefault();
    sendEditor($(this).attr("name"));
  });

  main.parent().on("click", "button[name*='_xed_submit_down']", function(event) {
    event.preventDefault();
    sendEditor($(this).attr("name"));
  });

  function sendEditor(name) {
    // disable submit buttons "to fix" MCR-1296
    $("form")
      .filter(function() {
        return $(this).attr("xmlns:xed") === "http://www.mycore.de/xeditor";
      })
      .find('button[type=submit]')
      .prop('disabled', true);

    // send request
    $.ajax({
      url: jp.baseURL + "servlets/XEditor",
      type: "POST",
      dataType: "text",
      data: $("#main").find("form").first().serialize() + "&" + encodeURIComponent(name),
      success: function(data) {
        let html = $("<div></div>").append(data);
        $(html).find("script:not('.jp-db-reload')").remove();
        main.html($(html).find("#main").html());
        main.trigger("changed");
      },
      error: function(error) {
        alert(error);
      }
    });
  }
});