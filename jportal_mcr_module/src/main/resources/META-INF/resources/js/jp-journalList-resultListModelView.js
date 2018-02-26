/**
 * Created by chi on 10.10.17.
 */
function journalsToModel(searchResult) {
    return model => Object.assign({}, model, {
        journals: searchResult.response.docs
    });
}

function renderJournalTitle(journal) {
    var titleLink = document.createElement("a");
    var title = document.createElement("h3");

    title.classList.add("journal-title");
    title.appendChild(titleLink);

    titleLink.textContent = journal.maintitle;
    titleLink.setAttribute("href", baseURL + 'receive/' + journal.id);

    return title;
}

function renderJournalPublished(journal) {
    var publishedStr = 'Erscheinungsverlauf: ';
    var published = document.createElement("div");
    published.classList.add("journal-published");

    if (journal["date.published"]) {
        publishedStr += journal["date.published"];
    } else if (journal["date.published_from"]) {
        publishedStr += journal["date.published_from"] + ' - ';
        if (journal["date.published_until"]) {
            publishedStr = publishedStr + journal["date.published_until"];
        }
    }

    published.textContent = publishedStr;

    return published;
}

function renderJournalPublisher(journal, solrKey, caption) {
    var div = document.createElement("div");
    div.classList.add("publisher");

    var publisherList = journal[solrKey];
    if (publisherList) {
        var pusblisherStr = caption + ': ';
        for (var i = 0; i < publisherList.length; i++) {
            var publisher = publisherList[i];
            var indexOfHash = publisher.indexOf('#');
            if (indexOfHash === -1) {
                console.log("Invalid publisher format for '" + publisher + "'.");
                continue;
            }
            var publisherID = publisher.substring(0, indexOfHash);
            var publisherText = publisher.substring(indexOfHash + 1);
            var publisherLink = "<a href='" + baseURL + 'receive/' + publisherID + "'>" + publisherText + "</a>";
            pusblisherStr += publisherLink;
            if (i + 1 < publisherList.length) {
                pusblisherStr += "; ";
            }
        }
        div.insertAdjacentHTML("afterbegin", pusblisherStr);
    }

    return div;
}

function renderResultListEntry(journal) {
    return Rx.Observable.of(
        renderJournalTitle(journal),
        renderJournalPublished(journal),
        renderJournalPublisher(journal, "participant.mainPublisher", "Herausgeber"),
        renderJournalPublisher(journal, "participant.author", "Autor")
    ).reduce((li, journalInfo) => {
        li.appendChild(journalInfo);
        return li;
    }, document.createElement("li"))
}

function renderResultList(model, container) {
    clear(container);

    if (model.journals.length > 0) {
        Rx.Observable.from(model.journals)
            .flatMap(renderResultListEntry)
            .reduce((ul, li) => {
                ul.appendChild(li);
                return ul;
            }, document.createElement("ul"))
            .subscribe(ul => container.appendChild(ul));
    } else {
        container.insertAdjacentHTML('afterbegin', '<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>');
    }
}
