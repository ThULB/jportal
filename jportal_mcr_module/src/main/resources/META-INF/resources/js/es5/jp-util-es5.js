'use strict';

var jp = jp || {};

jp.util = {

  importCSS: function importCSS(cssFile) {
    if (document.createStyleSheet) {
      document.createStyleSheet(baseURL + 'css/' + cssFile);
    } else {
      var link = $('<link>').attr({
        type: 'text/css',
        rel: 'stylesheet',
        href: jp.baseURL + 'css/' + cssFile
      });
      $('head').append(link);
    }
  },

  getJSON: function getJSON(url) {
    return new Promise(function (resolve, reject) {
      var request = new XMLHttpRequest();
      request.onload = function () {
        if (request.status < 200 || request.status >= 300) {
          reject({
            status: request.status,
            statusText: request.statusText
          });
          return;
        }
        resolve(JSON.parse(request.responseText));
      };
      request.onerror = function () {
        reject({
          status: request.status,
          statusText: request.statusText
        });
      };
      request.open('GET', url);
      request.send();
    });
  },

  translate: function translate(prefix) {
    return jp.util.getJSON(jp.baseURL + "rsc/locale/translate/" + jp.lang + "/" + prefix);
  },

  initLeaflet: function initLeaflet() {
    return new Promise(function (resolve, reject) {
      // check if leaflet is already loaded
      if (window.L != null) {
        resolve();
        return;
      }
      // css
      var leafletCSS = $("<link>").attr({
        type: "text/css",
        rel: "stylesheet",
        href: "https://unpkg.com/leaflet@1.3.1/dist/leaflet.css",
        crossorigin: "",
        integrity: "sha512-Rksm5RenBEKSKFjgI3a41vrjkw4EVPlJ3+OiI65vTjIdo9brlAacEuKOiQ5OFh7cOI1bkDwLqdLw3Zg0cRJAAQ=="
      });
      $("head").append(leafletCSS);
      // script
      var leafletJS = $("<script>").attr({
        src: "https://unpkg.com/leaflet@1.3.1/dist/leaflet.js",
        crossorigin: "",
        integrity: "sha512-/Nsx9X4HebavoBvEBuyp3I7od5tA0UzAxs+j83KgC8PU0kgB4XiK4Lfe4y4cgBtaRJQEIFCW+oC506aPT2L1zw=="
      });
      $("body").append(leafletJS);
      awaitLeaflet(resolve, reject, 0);
    });

    function awaitLeaflet(resolve, reject, tries) {
      if (tries > 50) {
        reject({
          statusText: "Unable to load leaflet in 5 seconds."
        });
        return;
      }
      if (window.L == null) {
        setTimeout(function () {
          awaitLeaflet(resolve, reject, tries + 1);
        }, 100);
        return;
      }
      resolve();
    }
  }

};
