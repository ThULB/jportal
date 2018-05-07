$(document).ready(function () {

    let results = [];

    let selectedItemIndex = null;

    let map = null;

    let marker = null;

    let documentOnKeyDownFunction = null;

    const modal = $("#geo-select-modal");

    updateDisplay();

    function updateDisplay() {
        $(".jp-geo-input").each((index, e) => {
            const geo = $(e);
            const display = geo.find(".jp-geo-coordinates-display");
            const dataInput = geo.find(".jp-geo-input-data");
            const data = dataInput.val();
            if(data !== "") {
                display.html("<b>" + data + "</b>");
                const formGroup = geo.next(".form-group");
                let rmButton = formGroup.find(".jp-geo-remove");
                if(rmButton.length === 0) {
                    rmButton = $("<button />", {
                        text: "Koordinaten entfernen",
                        class: "btn btn-danger jp-geo-remove",
                        click: () => {
                            dataInput.val("");
                            rmButton.remove();
                            updateDisplay();
                        }
                    });
                    formGroup.append(rmButton);
                }
            } else {
                display.html("");
            }
        });
    }

    $(".jp-coordinates-select").click((e) => {
        const geoInputDiv = $(e.target.parentElement).siblings(".jp-geo-input");
        const dataInput = geoInputDiv.find(".jp-geo-input-data");
        const data = dataInput.val();
        initModal(dataInput);

        const dataSplit = data.split(",");
        const lat = dataSplit.length > 0 ? data.split(",")[0] : "";
        const lng = dataSplit.length > 0 ? data.split(",")[1] : "";
        if (map == null) {
            jp.util.initLeaflet().then(() => {
              startOSM(modal, lat, lng);
            });
        } else if(lat !== "" && lng !== "") {
            map.setView([parseFloat(lat), parseFloat(lng)], 17);
            setMarker(lat, lng);
        }
    });

    function initModal(dataInput) {
        const searchInput = modal.find(".geo-select-modal-search");
        searchInput.removeAttr("name");

        modal.find(".modal-title").html("Geographische Referenzierung");

        modal.find(".geo-select-modal-send").unbind().click(() => {
            if (selectedItemIndex == null) {
                return;
            }
            const item = results[selectedItemIndex];
            dataInput.val(item.lat + "," + item.lon);
            modal.modal("hide");
        });

        modal.unbind().on("click", ".geo-select-modal-search-button", () => {
            const q = searchInput.val();
            const url = "https://nominatim.openstreetmap.org/search" +
                "?q=" + q +
                "&format=json&accept-language=" + jp.lang +
                "&limit=7";
            $.getJSON(url)
                .done((data) => {
                    console.log(data);
                    results = data;
                    updateResults();
                })
                .fail((jqxhr, textStatus, error) => {
                    BootstrapDialog.alert('Unable to get Open Street Map search result: ' + textStatus);
                    console.log(jqxhr);
                })
        });

        modal.on("hidden.bs.modal", () => {
            document.onkeydown = documentOnKeyDownFunction;
            updateDisplay();
        });

        showModal();
    }

    function showModal() {
        modal.modal("show");
        documentOnKeyDownFunction = document.onkeydown;
        document.onkeydown = function (e) {
            e = e || window.event;
            switch (e.which || e.keyCode) {
                case 13:
                    modal.find(".geo-select-modal-search-button").click();
                    return false;
            }
        }
    }

    function startOSM(modal, lat, lng) {
        const mapContainer = modal.find(".geo-select-modal-map-container")[0];
        const isLatLngGiven = lat !== "" && lng !== "";
        const fLat = isLatLngGiven ? parseFloat(lat) : 50.92878;
        const fLng = isLatLngGiven ? parseFloat(lng) : 11.5899;
        const zoom = isLatLngGiven ? 17 : 6;
        map = L.map(mapContainer).setView([fLat, fLng], zoom);
        L.tileLayer('https://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png', {
            attribution: 'Daten von <a href="http://www.openstreetmap.org/">OpenStreetMap</a> -' +
            ' Veröffentlicht unter <a href="http://opendatacommons.org/licenses/odbl/">ODbL</a>',
            maxZoom: 18
        }).addTo(map);

        if(isLatLngGiven) {
            setMarker(lat, lng);
        }
    }

    function updateResults() {
        const resultDiv = modal.find(".geo-select-modal-search-results");
        if (results.length === 0) {
            resultDiv.html("Keine Treffer.");
            return;
        } else {
            resultDiv.fadeIn(100);
            resultDiv.show();
        }
        let html = "<ul class='list-group'>";
        for (let i = 0; i < results.length; i++) {
            html += "<li class='list-group-item' data-index='" + i + "'>" + results[i].display_name + "</li>";
        }
        html += "</ul>";
        resultDiv.html(html);

        // add onclick event for result list items
        resultDiv.find(".list-group-item").on("click", (e) => {
            selectResultItem(resultDiv, $(e.target));
        });
    }

    function selectResultItem(resultDiv, targetItem) {
        modal.find(".geo-select-modal-send").removeAttr("disabled");
        // remove active
        resultDiv.find(".list-group-item").removeClass("active");

        // add active and zoom
        targetItem.addClass("active");
        selectedItemIndex = parseInt(targetItem.data("index"));
        const item = results[selectedItemIndex];
        map.fitBounds(getBoundingBox(item));

        // marker
        setMarker(item.lat, item.lon);
    }

    function setMarker(lat, lng) {
        if (marker != null) {
            map.removeLayer(marker);
        }
        marker = L.marker([parseFloat(lat), parseFloat(lng)]).addTo(map);
    }

    function getBoundingBox(item) {
        let bb = item.boundingbox;
        let c1 = L.latLng(parseFloat(bb[0]), parseFloat(bb[2]));
        let c2 = L.latLng(parseFloat(bb[1]), parseFloat(bb[3]));
        return L.latLngBounds(c1, c2);
    }

});
