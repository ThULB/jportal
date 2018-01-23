/**
 * Created by chi on 10.10.17.
 */
function getActiveTabs(searchResult) {
    return Rx.Observable.from(searchResult.response.docs)
        .map(doc => doc.maintitle)
        .filter(title => title != null && title.length > 0)
        .map(title => title[0].toUpperCase())
        .map(char => {
            return (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char
        })
        .distinct()
        .reduce((set, c) => {
            set.add(c);
            return set;
        }, new Set())
        .map(set => model => Object.assign({}, model, {activeTabs: set}));
}

function renderTabNav(model, container, /*function*/ eventHandler) {
    Rx.Observable.range(65, 26)
        .map(i => String.fromCharCode(i))
        .startWith("#")
        .map(c => {
            var li = document.createElement("li");
            li.textContent = c;
            if (model.activeTabs.has(c)) {
                li.classList.add("active");
            }

            if (c === model.selectedTab) {
                li.classList.add("selected-tab");
            }

            eventHandler(li);
            return li;
        })
        .reduce((fragment, li) => {
            fragment.appendChild(li);
            return fragment;
        }, document.createDocumentFragment())
        .subscribe(fragment => {
            clear(container)
            container.appendChild(fragment)
        });
}
