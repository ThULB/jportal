window.onload = function(event) {
    document.querySelectorAll('.journalConfCheckbox')
        .forEach(initCheckBox);

    addCssStyle();
}

function addCssStyle(){
    let linkEl = document.createElement("link");
    linkEl.setAttribute("href", "/jportal/css/jp-journalConf.css");
    linkEl.setAttribute("rel", "stylesheet");
    linkEl.setAttribute("type", "text/css");

    document.querySelector('head').append(linkEl);
}

function initCheckBox(element){
    let values = element.value.split(".");
    let configType = values[0];
    let key = values[1];
    let mcrID = values[2];
    loadConfig({
        mcrID: mcrID,
        configType: configType,
        success: function(conf){
            console.log('Success: ' + JSON.stringify(conf));
            let checked = false;

            if(conf.properties.entry.length > 0){
                let entry = conf.properties.entry[0];
                if(entry.key === key && entry.value){
                    checked = (entry.value === 'true');
                }
            }else {
                conf.properties.entry[0] = {
                    key: key,
                    value: false
                }
            }

            element.checked = checked;
            element.json = conf;
        },
        fail: function(status){
            console.log('Fail: ' + status);
        },
        error: function(error){
            console.log('Error: ' + error);
        }
    });

    element.onclick = function(event){
        let element = event.target;
        let values = element.value.split(".");
        let configType = values[0];
        let key = values[1];
        let mcrID = values[2];

        let checked = element.checked;
        element.json.properties.entry[0].value = checked;

        saveConfig({
            mcrID: mcrID,
            configType: configType,
            json: element.json,
            success: function(resp) {
                console.log('Success: ' + JSON.stringify(resp));
            },
            fail: function(status){
                console.log('Fail: ' + status);
                if(status === 401){
                    openFailModal("Bitte melden Sie sich erneut an!");
                }
            },
            error: function(error){
                console.log('Error: ' + error);
            }
        })
    }
}

function openFailModal(text){
    let modalDiv = document.querySelector('#failModal');

    modalDiv.style.display = 'block';

    let modalText = modalDiv.querySelector('#failModalText');
    modalText.textContent = text;

    modalDiv.querySelectorAll(".fail-close").forEach(el => {
        el.onclick = function(){
            modalDiv.style.display = 'none';
        }
    });
}

function loadConfig(paramObj){
    let request = new XMLHttpRequest();
    request.open('GET', '/jportal/rsc/objConf/' + paramObj.mcrID +'/' + paramObj.configType, true);

    request.onload = function() {
        if (this.status >= 200 && this.status < 400) {
            if(paramObj.success){
                paramObj.success(JSON.parse(this.response))
            }
        } else {
            // We reached our target server, but it returned an error
            if(paramObj.fail){
                paramObj.fail(this.status)
            }
        }
    };

    request.onerror = function() {
        // There was a connection error of some sort
        if(paramObj.error){
            paramObj.error(this)
        }
    };

    request.send();
}

function saveConfig(conf){
    let request = new XMLHttpRequest();
    request.open('PUT', '/jportal/rsc/objConf/' + conf.mcrID +'/' + conf.configType, true);
    request.setRequestHeader('Content-Type', 'application/json');

    request.onload = function() {
        if (this.status >= 200 && this.status < 400) {
            if(conf.success){
                conf.success(this.response)
            }
        } else {
            // We reached our target server, but it returned an error
            if(conf.fail){
                conf.fail(this.status)
            }
        }
    };

    request.onerror = function() {
        // There was a connection error of some sort
        if(conf.error){
            conf.error(this)
        }
    };

    request.send(JSON.stringify(conf.json));
}

