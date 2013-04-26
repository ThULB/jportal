$(document).ready(function(){
	var SortedSet = function(){
		var list = [];
		
		function sortIP(a,b){
			var _aIp = a.ip.replace(/-\d{1,3}/,"").split(/\./);
			var _bIp = b.ip.replace(/-\d{1,3}/,"").split(/\./);
			for ( var i = 0; i < _aIp.length; i++) {
				var compare = _aIp[i] - _bIp[i];
				if(compare != 0){
					return compare;
				}
			}
			
			return 0;
		}
		
		return {
			remove : function(entry){
				for ( var i = 0; i < list.length; i++) {
					if(list[i].ip == entry.ip){
						list.splice(i,1);
					}
				}
			},
			add : function(entry){
				for ( var i = 0; i < list.length; i++) {
					if(list[i].ip == entry.ip){
						return false;
					}
				}
				list.push(entry);
				return true;
			},
			entries : function(){
				return list;
			},
			sort : function(){
				list = list.sort(sortIP);
			}
		}
	}
	
	var EditValueGUI = function(value, setNewValue){
		var input = $("<input type='text' value='" + value + "'/>")
		var form = $("<form class='editValue'/>").append(input);
		
		form.submit(function(event){
			setNewValue(input.val());
			return false;
		})
		return form;
	}
	
	var TableEntryGUI = function(/*{ip:"IP",abo:"ABO"}*/ entry, controller){
		var entryHtml;
		if(entry == null){
			entryHtml = $("<tr><td>leer</td><td>leer</td><td></td></tr>");
		}else{
			entryHtml = $("<tr><td class='ipAbo' type='ip'>"+entry.ip+"</td><td class='ipAbo' type='abo'>"+entry.abo+"</td><td><i class='icon-remove pull-right'/></td></tr>");
			entryHtml.on("click","i.icon-remove",function(){
				$.ajax({
					url: url,
					type: "DELETE",
					dataType: "text",
					data: entry.ip + " " + entry.abo,
					statusCode: {
						200: function() {
							controller.remove(entry);
							entryHtml.remove();
						},
						404 : function(){
							alert("Fehler! IP: " + entry.ip + " " + entry.abo + " ist nicht vorhanden.");
						},
						500: function(error) {
							alert("Server Error: " + error);
						}
					},
				});
				
			}).on("hover", "i.icon-remove", function(){
				$(this).toggleClass("hover");
			}).on("hover","td.ipAbo", function(){
				$(this).toggleClass("hover");
			}).on("click","td.ipAbo", function(){
				var elem = $(this);
				var tdIPAbo = $("td.ipAbo");
				
				elem.toggleClass("hover");
				tdIPAbo.toggleClass("ipAbo");
				
				var value = elem.text();
				
				elem.html(EditValueGUI(value, function(newValue){
					var oldIp = entry.ip + " " + entry.abo;
					var type = elem.attr("type");
					entry[type] = newValue;
					var url = window.location.pathname.replace("/start","")
					
					var newIp = entry.ip + " " + entry.abo;
					
					$.ajax({
						url: url,
						type: "PUT",
						contentType: 'application/json',
						dataType: "json",
						handleAs : "json",
						data: JSON.stringify({newIp: newIp, oldIp: oldIp}),
						statusCode: {
							201: function() {
								elem.empty();
								elem.text(newValue);
								tdIPAbo.toggleClass("ipAbo");
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
					
				}));
			});
		}
		
		return entryHtml;
	}
	
	var AlertIPExistGUI = function(entry){
		return $("<div class='alert alert-block alert-error fade in'><a class='close' data-dismiss='alert' href='#'>&times;</a>"
				+"Die eingegebene IP "
				+ "<strong>"+ entry.ip +"</strong> f&uuml;r "
				+ "<strong>"+ entry.abo +"</strong> exisiert bereits."
				+"</div>");
	}
	
	var IPAboJSON = function(str){
		var firstBlank = str.indexOf(" ");
		var ip = firstBlank != -1 ? str.substring(0,firstBlank) : str;
		var abo = firstBlank != -1 ? str.substring(firstBlank+1) : "-";
		
		return {ip:ip,abo:abo};
	}
	
	var TableController = function(/*<tbody>*/ table){
		var ipList = SortedSet();
		
		var EntryController = function(parentCtr){
			return {
				remove : function(entry){
					ipList.remove(entry);
					if(ipList.entries().length == 0){
						parentCtr.refreshGUI();
					}
				},
				replace : function(entry, newEntry){
					ipList.remove(entry);
					ipList.add(newEntry);
					if(ipList.entries().length == 0){
						parentCtr.refreshGUI();
					}
				}
			}
		}
		
		return {
			refreshGUI : function(){
				table.empty();
				ipList.sort();
				ipListEntries = ipList.entries();
				
				if(ipListEntries.length == 0){
					table.append(TableEntryGUI(null).html());
				}else{
					for ( var i = 0; i < ipListEntries.length; i++) {
						var entry = ipListEntries[i];
						var ctr = this;
						table.append(TableEntryGUI(entry, EntryController(this)));
					}
				}
			},
			addIP : function(entry){
				if(entry != null){
					var success = ipList.add(entry);
					
					if(success != true){
						$(".table").before(AlertIPExistGUI(entry));
					}
				}
			}
		};
	}
	$('#myModal').modal('show');
	var ipTableCtr = TableController($("#ipTable"));
	var url = window.location.pathname.replace("/start","")
	
	$.getJSON(url,function(ipList){
		for ( var i = 0; i < ipList.length; i++) {
			ipTableCtr.addIP(ipList[i]);
		}
		ipTableCtr.refreshGUI();
		$('#myModal').modal('hide');
	})
	
	$('#newIPForm').submit(function(event){
		var enteredIP = $.trim($('#newIPFormInput').val());
		if(enteredIP != ""){
			$.ajax({
				url: url,
				type: "POST",
				dataType: "text",
				data: enteredIP,
				statusCode: {
					201: function() {
						ipTableCtr.addIP(IPAboJSON(enteredIP));
						ipTableCtr.refreshGUI();
					},
					418 : function(){
						alert("Fehler! IP: " + enteredIP + " ist nicht korrekt.");
					},
					419: function() {
						alert("Fehler! IP: " + enteredIP + " bereits vorhanden.");
					},
					500: function(error) {
						alert("Server Error: " + error);
					}
				}
			});
		}
		$('#newIPFormInput').val("");
		return false;
	});
})