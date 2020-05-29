jp.journalList.getJournalTitles()
    .then(results => {
        let journalsNumSpan = document.getElementById("journalsNum");
        let journalsNum = results.response.numFound;

        journalsNumSpan.innerHTML = journalsNum;
    })