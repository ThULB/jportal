var jp = jp || {};

jp.util = {

  importCSS: (cssFile) => {
    if (document.createStyleSheet) {
      document.createStyleSheet(baseURL + 'css/' + cssFile);
    } else {
      let link = $('<link>').attr({
        type: 'text/css',
        rel: 'stylesheet',
        href: jp.baseURL + 'css/' + cssFile
      });
      $('head').append(link);
    }
  },

  getJSON: (url) => {
    return new Promise((resolve, reject) => {
      let request = new XMLHttpRequest();
      request.onload = () => {
        if (this.status < 200 || this.status >= 300) {
          reject({
            status: this.status,
            statusText: xhr.statusText
          });
          return;
        }
        resolve(JSON.parse(request.responseText));
      };
      request.onerror = function () {
        reject({
          status: this.status,
          statusText: xhr.statusText
        });
      };
      request.open('GET', url);
      request.send();
    });
  },

  translate: (prefix) => {
    return jp.util.getJSON(jp.baseURL + "rsc/locale/translate/" + jp.lang + "/" + prefix);
  },

  initLeaflet: () => {
    return new Promise((resolve, reject) => {
      // check if leaflet is already loaded
      if (window.L != null) {
        resolve();
        return;
      }
      // css
      let leafletCSS = $("<link>").attr({
        type: "text/css",
        rel: "stylesheet",
        href: "https://unpkg.com/leaflet@1.3.1/dist/leaflet.css",
        crossorigin: "",
        integrity: "sha512-Rksm5RenBEKSKFjgI3a41vrjkw4EVPlJ3+OiI65vTjIdo9brlAacEuKOiQ5OFh7cOI1bkDwLqdLw3Zg0cRJAAQ=="
      });
      $("head").append(leafletCSS);
      // script
      let leafletJS = $("<script>").attr({
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
