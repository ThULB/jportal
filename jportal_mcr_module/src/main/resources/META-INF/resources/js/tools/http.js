function URLException(msg) {
    this.message = msg;
    this.name = "URLException";
}

function httpRequest(params) {
    if (!params.url) {
        throw mew
        URLException('Missing URL param!');
    }

    let defaultParams = {
        method: 'GET',
        async: true,
        responseType: 'text'
    }

    for (let prop in defaultParams) {
        if (!params[prop]) {
            params[prop] = defaultParams[prop]
        }
    }

    let request = new XMLHttpRequest();
    request.open(params.method, params.url, params.async);

    if(params.headers){
        for (let name in params.headers){
            request.setRequestHeader(name, params.headers[name]);
        }
    }

    request.responseType = params.responseType;

    request.onload = function () {
        if (this.status >= 200 && this.status < 400) {
            if (params.success) {
                params.success(this.response)
            }
        } else {
            // We reached our target server, but it returned an error
            if (params.fail) {
                params.fail(this)
            }
        }
    };

    request.onerror = function () {
        // There was a connection error of some sort
        if (params.error) {
            params.error(this)
        }
    };

    if (params.data) {
        request.send(params.data);
    } else {
        request.send();
    }
}