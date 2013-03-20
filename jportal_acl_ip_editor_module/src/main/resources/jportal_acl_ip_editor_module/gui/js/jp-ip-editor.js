var IPRuleEditor = function(objid){
	var editButton = $('<div/>',
		    {
		        id: 'jp-ip-editor-button-edit',
		        class: 'jp-ip-editor-button jp-ip-editor-button-right icon-edit icon-large ',
		        title: 'IP bearbeiten'	        	
		    });

	var deleteButton = $('<div/>',
		    {
		        id: 'jp-ip-editor-button-delete',
		        class: 'jp-ip-editor-button jp-ip-editor-button-right icon-trash icon-large',
		        title: 'IP löschen'
		    });

	var editDoneButton = $('<div/>',
		    {
		        id: 'jp-ip-editor-button-editDone',
		        class: 'jp-ip-editor-button icon-ok icon-large',
		        title: 'Änderung bestätigen'
		    });

	var cancelButton = $('<div/>',
		    {
		        id: 'jp-ip-editor-button-cancel',
		        class: 'jp-ip-editor-button icon-remove icon-large',
		        title: 'Abbrechen'
		    });

	var checkBox = $('<input/>',
			{
				class: 'jp-ip-editor-checkBox',
				type: 'checkbox'
			});

	var IpBeingEdited = false;
	
	return {
		init: function(){
			//add IP button
			$("#jp-ip-editor-button-add").click(function() {
				if (validate("#jp-ip-editor-ips")){
					var ip = $("#jp-ip-editor-ip1").val() + "." + $("#jp-ip-editor-ip2").val() + "." + $("#jp-ip-editor-ip3").val() + "." + $("#jp-ip-editor-ip4").val();
					sendIP(ip);
				}
			});

			//delete multiple IPs button
			$("#jp-ip-editor-button-deleteMulti").live("click", function() {
				if (!IpBeingEdited){
					if ($(".jp-ip-editor-checkBox:checked").length > 0){
						var json = {
								  "ips": [],
								  "ruleid": ruleId
								};
						$(".jp-ip-editor-ipList-ip").children("input:checked").each(function() {
							json.ips.push({"ip": $(this).parent().attr("ip")});
						});
						removeIPList(json);
					}
					else{
						alert("Sie müssen erst IPs makieren bevor Sie mehere löschen können.");
					}
				}
				else{
					alert("Bitte beenden Sie erst das bearbeiten einer IP, bevor Sie mehrere IPs löschen.");
				}

			});

			//refresh IP list button
			$("#jp-ip-editor-button-refresh").live("click", function() {
				getIPs();
				$('#jp-ip-editor-ipList').animate({scrollTop : 0},'fast');
				$('.jp-ip-editor-ip').val("");
				cancelSelected();
			});

			//edit IP button
			$("#jp-ip-editor-button-edit").live('click', function() {
				IpBeingEdited = true;
				$(this).parent().addClass("jp-ip-editor-selected")
				
				var ipArray = $(this).parent().attr("ip").split(".");
				var ipEdit = "";
				for (var i = 0; i < 4; i++){
					ipEdit += "<input type='text' class='jp-ip-editor-ip' maxlength='3'/>";
					if(i < 3) {
						ipEdit += ".";
					}
				}

				$(this).parent().html(ipEdit).append(editDoneButton).append(cancelButton);
				var i = 0;
				$(".jp-ip-editor-ip", $("#jp-ip-editor-button-editDone").parent()).each(function() {
					$(this).val(ipArray[i]);
					i++;
				});
			});

			//delete IP button
			$("#jp-ip-editor-button-delete").live("click", function() {
				removeIP($(this).parent().attr("ip"), $("#jp-ip-editor-button-delete").parent());
			});

			//confirm edit button
			$("#jp-ip-editor-button-editDone").live("click", function() {
				if (validate(".jp-ip-editor-selected")){
					var ipString = "";
					var oldIp = $(this).parent().attr("ip");
					var i = 0;
					$(".jp-ip-editor-ip", $(this).parent()).each(function() {
						ipString += $(this).val();
						if(i < 3) {
							ipString += ".";
						}
						i++;
					});
					if (ipString != oldIp){
						editIP(ipString, oldIp);
					}
					else{
						$(this).parent().html(oldIp).prepend(checkBox);
						cancelSelected();
					}
				}
			})

			//cancel edit button
			$("#jp-ip-editor-button-cancel").live("click", function() {
				$(this).parent().html($(this).parent().attr("ip")).prepend(checkBox.clone());
				cancelSelected();
			});

			//select all button
			$("#jp-ip-editor-button-selectAll").live("change", function() {
				if($("#jp-ip-editor-button-selectAll").prop('checked')){
					$(".jp-ip-editor-ipList-ip").children("input").prop("checked", true);
				}
				else{
					$(".jp-ip-editor-ipList-ip").children("input:checked").prop("checked", false);
				}
			});

			//mouse over a IP
			$(".jp-ip-editor-ipList-ip").live("hover", function() {
				if (!IpBeingEdited){
					$(this).toggleClass("jp-ip-editor-hover");
					if($(this).hasClass("jp-ip-editor-hover")){
				//		$(this).html($(this).html() +" <input type='button' id='jp-ip-editor-button-add' onClick='bla()' value='Bearbeiten'><input type='button' value='Löschen'>");
						$(this).append(deleteButton);
						$(this).append(editButton);
					}
				}
			});

			//mouse leave IP list
			$("#jp-ip-editor-ipList").live("mouseleave", function() {
				if (IpBeingEdited == false){
					$("#jp-ip-editor-button-edit").remove();
					$("#jp-ip-editor-button-delete").remove();
				}
			});
			
			//let user only enter numbers and *
			$(".jp-ip-editor-ip").live("keydown",function(event) {
			    // Allow: backspace, delete, tab, escape, and enter
			    if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 || 
			         // Allow: Ctrl+A
			        (event.keyCode == 65 && event.ctrlKey === true) || 
			         // Allow: home, end, left, right
			        (event.keyCode >= 35 && event.keyCode <= 39) ||
			         // Allow: Shift + "+" to get *
			    	(event.shiftKey && event.keyCode == 171)){
			             // let it happen, don't do anything
			             return;
			    }
			    else {
			        // Ensure that it is a number and stop the keypress
			        if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
			            event.preventDefault(); 
			        }   
			    }
			});
			
			getIPs();
		}
	}
	
	//ajax list
	function getIPs(){
		$.ajax({
			url: "/rsc/IPRule/"+objid,
			type: "GET",
			dataType: "text",
//			data: {ruleId: ruleId},
			success: function(data) {
						buildIPList($.parseJSON(data));
					},
			error: function(error) {
						alert(error);
					}
		});
	}

	//ajax add
	function sendIP(ip){
		$.ajax({
			url: "/rsc/IPRule/"+objid,
			type: "POST",
			dataType: "text",
			data: ip,
			statusCode: {
				200: function() {
					addIP(ip);
				},
				409: function() {
					alert("Fehler! IP: " + ip + " bereits vorhanden.");
				},
				500: function(error) {
					alert("Server Error: " + error);
				}
			}
		});
	}

	//ajax remove
	function removeIP(ip, element){
		$.ajax({
			url: "/rsc/IPRule/remove",
			type: "GET",
			dataType: "text",
			data: {ruleId: ruleId, ip: ip},
			statusCode: {
				200: function() {
					element.remove();
					cancelSelected();
				},
				409: function() {
					alert("Fehler! IP: " + ip + " konnte nicht entfernt werden.");
					cancelSelected();
				},
				500: function(error) {
					alert("Server Error: " + error);
					cancelSelected();
				}
			}
		});
	}

	//ajax removeList
	function removeIPList(ips){
		$.ajax({
			url: "/rsc/IPRule/removeList",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(ips),
			statusCode: {
				200: function(data) {
					removeIPs(data);
					cancelSelected();
				},
				500: function(error) {
					alert("Server Error: " + error);
					cancelSelected();
				}
			}
		});
	}

	//ajax edit
	function editIP(newIp, oldIp){
		$.ajax({
			url: "/rsc/IPRule/edit",
			type: "GET",
			dataType: "text",
			data: {ruleId: ruleId, newIp: newIp, oldIp: oldIp, defRule: defRule},
			statusCode: {
				200: function() {
					$(".jp-ip-editor-selected").attr("ip", newIp);
					$(".jp-ip-editor-selected").html(newIp)
					$(".jp-ip-editor-selected").prepend(checkBox.clone());
					cancelSelected();

				},
				409: function() {
					alert("Fehler! IP: " + oldIp + " konnte nicht bearbeitet werden.");
					$(".jp-ip-editor-selected").html($(this).parent().attr("ip")).prepend(checkBox.clone());
					cancelSelected();
				},
				500: function(error) {
					alert("Server Error: " + error);
					$(".jp-ip-editor-selected").html($(this).parent().attr("ip")).prepend(checkBox.clone());
					cancelSelected();
				}
			}
		});
	}

	//build the IP list
	function buildIPList(ips){
		$('#jp-ip-editor-ipList').text("");
		$.each(ips, function(i, l) {
			var ip = l.ip;
			$('#jp-ip-editor-ipList')
				.append($("<li/>")
						.attr("ip", ip)
						.text(ip)
						.addClass("jp-ip-editor-ipList-ip")
						.prepend(checkBox.clone()));
		});
	}

	//add an IP
	function addIP(ip){
		$('#jp-ip-editor-ipList')
			.append($("<li></li>")
				.attr("ip", ip)
				.text(ip)
				.addClass("jp-ip-editor-ipList-ip")
				.prepend(checkBox.clone()));
		$('#jp-ip-editor-ipList').animate({scrollTop : $("li").length * 25},'fast');
		$('#jp-ip-editor-ips').children('.jp-ip-editor-ip').val("");
	}

	//remove an multiple IPs
	function removeIPs(json) {
		$(".jp-ip-editor-ipList-ip").children("input:checked").each(function() {
			var ip = $(this).parent().attr("ip");
			var parent = $(this).parent();
			$.each(json, function(i, l) {
				var ipJson = l.ip;
				if(ip == ipJson){
					if(l.success == 1){
						parent.remove();
						return;
					}
					else{
						alert("Fehler! IP: " + ip + " konnte nicht entfernt werden.");
						return;
					}
				}
			});
		});
	}

	//remove selection from IP
	function cancelSelected() {
		IpBeingEdited = false;
		$(".jp-ip-editor-selected").removeClass("jp-ip-editor-selected");
		$(".jp-ip-editor-hover").removeClass("jp-ip-editor-hover");
	}

	//validate IP input
	function validate(parent){
		var valid = true;
		validation:
		$(parent).children(".jp-ip-editor-ip").each(function () {
			if($(this).val() == ""){
				alert("Bitte geben Sie in jedes Feld etwas ein.");
				valid = false;
				return false;
			}
			else{
				var match = $(this).val().match(/\*/g)
				if(match != null && match.length > 1){
					alert("Bitte geben Sie nur ein * pro Feld ein.");
					valid = false;
					return false;
				}
			}
		});
		return valid;
	}

	
}

$(document).ready(function() {
    var objid = $("#jportal_acl_ip_editor_module").attr("objID")
    var ipACLEditorInstance = new IPRuleEditor(objid);
    ipACLEditorInstance.init();
});


