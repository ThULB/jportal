var jp = jp || {};

jp.util = {

    importCSS: function (cssFile) {
        if (document.createStyleSheet) {
            document.createStyleSheet(baseURL + 'css/' + cssFile);
        } else {
            let link = $('<link>').attr({
                type: 'text/css',
                rel: 'stylesheet',
                href: jp.baseURL + 'css/' + cssFile,
                'class': 'myStyle'
            });
            $('head').append(link);
        }
    },

    getJSON: function(url) {
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

    translate: function (prefix) {
        return jp.util.getJSON(jp.baseURL + "rsc/locale/translate/" + jp.lang + "/" + prefix);
    }

};
