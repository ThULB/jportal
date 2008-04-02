var simpleEncoding = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';          

function simpleEncode(values,maxValue) {          

var chartData = ['s:'];
  for (var i = 0; i < values.length; i++) {
    var currentValue = values[i];
    if (!isNaN(currentValue) && currentValue >= 0) {
    chartData.push(simpleEncoding.charAt(Math.round((simpleEncoding.length-1) * currentValue / maxValue)));
    }
      else {
      chartData.push('_');
      }
  }
return chartData.join('');
}          

  var chartBaseUrl = "http://chart.apis.google.com/chart?";
  var chartParams = "chs=350x150&cht=p3";          

  var color = "&chco=44ca20,ca2020,f96820"	

  var values = "&chd=";
  var labels = "&chl=";
  var valueArray = new Array();
  var max = 1;          

  var tbl = document.getElementById("GraphThisTable");
  var rows = tbl.getElementsByTagName("tr");             

  var headings = rows[0].getElementsByTagName("td");
  var graphedPer = headings[0].innerHTML;
  var graphLabel = headings[2].innerHTML;
  
  document.write("<p>" + graphLabel + " of " + graphedPer + "</p>");          

  for(i = 2; i < rows.length; i++){
    var cols = rows[i].getElementsByTagName("td");

    if (i == 2) {
      labels = labels + cols[0].innerHTML;
    } else {
      labels = labels + "|" + cols[0].innerHTML;
    }
    
    valueArray[i - 2] = cols[1].innerHTML;
    if (cols[1].innerHTML > max)
      max = cols[1].innerHTML;
  }
  values = values + simpleEncode(valueArray,max);
  document.write ("<p><img src=" + chartBaseUrl + chartParams + labels + values + color +"></p>");