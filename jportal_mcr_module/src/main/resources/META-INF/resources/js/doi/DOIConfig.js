let NotSupportedTypeException = function (message) {
    this.message = message;
    this.name = "NotSupportedTypeException";
}

let NoFunctionException = function (message) {
    this.message = message;
    this.name = "NoFunctionException";
}

let NotSupportedConfigFormat = function (message) {
    this.message = message;
    this.name = "NotSupportedConfigFormat";
}

let DOIConfig = function (mcrID) {
    this.handlerType = [
        'change',
        'loadSuccess',
        'loadFail',
        'loadError',
        'saveSuccess',
        'saveFail',
        'saveError'
    ]

    this.mcrID = mcrID;
    this.allowed = false;
    this.handlers = {};
}


DOIConfig.prototype.unmarshallConf = function (/*object*/ conf) {
    if (conf.properties.entry.length > 0) {
        let entry = conf.properties.entry[0];
        if (entry.key === 'allow' && entry.value) {
            return entry.value === 'true';
        }
    }
/*
    let errMsg = "Unsupported config format: \n" + JSON.stringify(conf);
    throw new NotSupportedConfigFormat(errMsg)*/
}

DOIConfig.prototype.marshallConf = function (/*boolean*/ value) {
    let conf = {
        "properties":{
            "entry":[{"key":"allow","value":JSON.stringify(value)}]
        }
    };
    return JSON.stringify(conf);
}

DOIConfig.prototype.setAllowed = function (/*boolean*/ value) {
    this.allowed = value;
    this.handlersOf('change', handler => {
        handler(value);
    });
}

DOIConfig.prototype.getAllowed = function () {
    return this.allowed;
}

DOIConfig.prototype.once = function (/*String*/ type, /*function*/ handler) {
    this.on(type, handler);
}

DOIConfig.prototype.on = function (/*String*/ type, /*function*/ handler) {
    if (typeof handler !== 'function') {
        new NoFunctionException("This is not a function: \n" + JSON.stringify(handler));
        return;
    }

    if (!this.handlerType.includes(type)) {
        throw new NotSupportedTypeException("Handler type " + type + " is not supported!");
        return;
    }

    if (this.handlers[type] === undefined) {
        this.handlers[type] = [];
    }

    this.handlers[type].push(handler);
}

DOIConfig.prototype.handlersOf = function (/*String*/ type, /*function*/ fn) {
    if (typeof fn !== 'function') {
        new NoFunctionException("This is not a function: \n" + JSON.stringify(fn));
        return;
    }

    if (!this.handlerType.includes(type)) {
        throw new NotSupportedTypeException("Handler type " + type + " is not supported!");
        return;
    }

    let handlers = this.handlers[type];
    if (handlers !== undefined) {
        handlers.forEach(fn);
    }
}

DOIConfig.prototype.load = function () {
    let mcrID = this.mcrID;
    let that = this;

    httpRequest({
        url: jp.baseURL + 'rsc/objConf/' + mcrID + '/DOI',
        responseType: 'json',
        success: function (conf) {
            let value = that.unmarshallConf(conf);
            that.setAllowed(value);
            that.handlersOf('loadSuccess', handler => handler(conf));
            console.log('DOIConfig loaded: ' + JSON.stringify(conf));
        },
        fail: function (fail) {
            that.handlersOf('loadFail', handler => handler(fail));
            console.log('DOIConfig Fail: ' + JSON.stringify(fail));
        },
        error: function (error) {
            that.handlersOf('loadError', handler => handler(error));
            console.log('DOIConfig Error: ' + JSON.stringify(error));
        }
    });
}

DOIConfig.prototype.save = function () {
    let mcrID = this.mcrID;
    let that = this;

    httpRequest({
        method: 'PUT',
        url: jp.baseURL + 'rsc/objConf/' + mcrID + '/DOI',
        headers: {
            'Content-Type': 'application/json'
        },

        // save the Json and make some transformation for element.json !!!
        data: that.marshallConf(that.allowed),
        success: function (resp) {
            that.handlersOf('saveSuccess', handler => handler(resp));
            console.log('Success: ' + JSON.stringify(resp));
        },
        fail: function (fail) {
            that.handlersOf('saveFail', handler => handler(fail));
            console.log('Fail: ' + JSON.stringify(fail));
        },
        error: function (error) {
            that.handlersOf('saveError', handler => handler(error));
            console.log('Error: ' + JSON.stringify(error));
        }
    });
}