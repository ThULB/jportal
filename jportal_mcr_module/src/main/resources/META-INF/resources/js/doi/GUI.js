window.onload = function (event) {
    addCssStyle();

    let doiConfig = new DOIConfig(jp.journalID);

    doiConfig.on('change', checkBoxChangeHandler);
    doiConfig.on('loadSuccess', checkBoxLoadSuccessHandler);
    doiConfig.on('loadFail', checkBoxLoadFailHandler);
    doiConfig.on('loadError', checkBoxLoadErrorHandler);
    doiConfig.on('saveFail', checkBoxSaveFailHandler);
    doiConfig.on('saveError', checkBoxSaveErrorHandler);

    doiConfig.on('change', doiButtonDOIConfChangedHandler);

    checkBoxClickHandler(doiConfig);

    doiConfig.load();
}

function checkBoxChangeHandler(value){
    document.querySelectorAll('.journalConfCheckbox')
        .forEach(checkBox => checkBox.checked = value);
}

function checkBoxLoadSuccessHandler(success){
    console.log('Load success: ' + JSON.stringify(success));
}

function checkBoxLoadFailHandler(fail){
    console.log('Load fail: ' + JSON.stringify(fail));
}

function checkBoxLoadErrorHandler(error){
    console.log('Error: ' + JSON.stringify(error));
}

function checkBoxSaveFailHandler(fail){
    console.log('Save fail: ' + JSON.stringify(fail));
    if (fail.status === 401) {
        openFailModal("Fehler beim Speichern","Bitte melden Sie sich erneut an!");
    }
}

function checkBoxSaveErrorHandler(error){
    console.log('Save fail: ' + JSON.stringify(error));
}

function checkBoxClickHandler(config){
    document.querySelectorAll('.journalConfCheckbox')
        .forEach(checkBox => checkBox.onclick = function(event){
            let checkedVal = event.target.checked;
            config.setAllowed(checkedVal);
            config.save();
        });
}

function doiButtonDOIConfChangedHandler(doiAllowed){
    let doiButton = document.querySelector('#jp-doi-button');

    doiButton.onclick = doiAllowed ? doiButtonClickHandler : null;
    doiButton.parentNode.style.display = doiAllowed ? "block" : "none";
}

function doiButtonClickHandler() {
    console.log("DOI clicked!");
    httpRequest({
        method: 'POST',
        url: jp.baseURL + 'rsc/pi/registration/service/Datacite/' + jp.objectID,
        success: function () {
            alert('OK')
        },
        fail: function (fail) {
            let errorJSON = JSON.parse(fail.responseText);

            if (errorJSON.code === "384") {
                let modalTitel = "Fehlende Felder";
                let errorMsg = "Für die DOI-Vergabe müssen folgende Felder noch ausgefüllt werden: ";
                let missingTags = getMissingTags(errorJSON.translatedAdditionalInformation);
                openFailModal(modalTitel,errorMsg + missingTags);
            }
        },
    });
}

function getMissingTags(missingTagsStr){
    let tagsArray = missingTagsStr.split(',');
    let dataciteType = {
        "titles": "Titel",
        "creators": "Autor",
        "publisher": "Herausgeber",
        "publicationYear": "Erscheinungsjahr"
    };

    let missingTags = "";
    for (var i = 0; i < tagsArray.length; i++){
        let tag = dataciteType[tagsArray[i]];
        if(tag === undefined){
            tag = tagsArray[i];
        }

        missingTags = missingTags + tag + ", ";
    }

    return missingTags;
}

function addCssStyle() {
    let linkEl = document.createElement("link");
    linkEl.setAttribute("href", "/jportal/css/jp-DOIGui.css");
    linkEl.setAttribute("rel", "stylesheet");
    linkEl.setAttribute("type", "text/css");

    document.querySelector('head').append(linkEl);
}

function openFailModal(title, text) {
    let modalDiv = document.querySelector('#failModal');

    modalDiv.style.display = 'block';

    let modalTitle = modalDiv.querySelector('.jp-modal-title');
    modalTitle.textContent = title;

    let modalText = modalDiv.querySelector('.jp-modal-body p');
    modalText.textContent = text;

    modalDiv.querySelectorAll(".jp-modal-close").forEach(el => {
        el.onclick = function () {
            modalDiv.style.display = 'none';
        }
    });
}