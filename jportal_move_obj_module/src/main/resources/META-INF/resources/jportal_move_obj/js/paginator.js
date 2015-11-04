function Paginator(q, response, elm) {
    var query = q;
    var element = elm;
    var numPerPage = 10;
    var maxPaginFields = 7;
    var pageCount = Math.ceil(response.numFound / numPerPage);
    var actPage = Math.ceil(response.start / numPerPage) + 1;
    var paginFields = (maxPaginFields>=pageCount) ? pageCount : maxPaginFields;

    var addPagesToPaginator = function(s,e) {
        addPageToPaginator(0, 1, "");
        if(e>0){
            for(var i = 0 ; i < paginFields-2; i++){
                var b = s+i+1;
                addPageToPaginator((b - 1) * numPerPage, b, "");
            }
            addPageToPaginator((pageCount - 1) * numPerPage, pageCount, "");
        }
    };

    var addPageToPaginator = function(start, name, state) {
        var pageButton = "";
        if (state == ""){
            pageButton = $('<a href="#" onclick="return false;">' + name + '</a>');
            pageButton.data("query", query);
            pageButton.data("start", start);
        }
        else{
            pageButton = $('<span>' + name + '</span>');
        }
        $("<li></li>").append(pageButton).addClass(state).appendTo($(element).find(".pagination"));
    };

    var disablePage = function(i) {
        $(element).find("ul.pagination > li:nth-child(" + i + ")").addClass("disabled");
        $(element).find("ul.pagination > li:nth-child(" + i + ")").html("<span>...</span>");
    };

    var activePage = function(i) {
        var activeElm = $(element).find("ul.pagination > li > a").filter(function() {
            return ($(this).data("start") == ((i - 1) * numPerPage));
        }).parent();
        activeElm.addClass("active");
        activeElm.html("<span>" + i + "</span>");
    };

    this.buildPaginator = function() {
        $(element).find(".pagination").html("");
        if(pageCount<=maxPaginFields){
            addPagesToPaginator(1, pageCount - 1);
        }
        else {
            var evenOddDiv, s, e;
            if (actPage > parseInt(maxPaginFields / 2 + 1)) {
                //last pages
                evenOddDiv = (maxPaginFields % 2 === 0) ? 1 : 0;
                if (actPage >= pageCount - (parseInt(maxPaginFields / 2 - evenOddDiv))) {
                    s = pageCount-(parseInt(maxPaginFields/2))*2+evenOddDiv;
                    e = paginFields;
                    addPagesToPaginator(s,e);
                    disablePage(2);
                }
                //middle pages
                else {
                    evenOddDiv = (maxPaginFields % 2 === 0) ? 0 : 1;
                    s = actPage - (maxPaginFields - parseInt(maxPaginFields / 2 + evenOddDiv));
                    e = actPage + (maxPaginFields - parseInt(maxPaginFields / 2 + 1));
                    addPagesToPaginator(s, e);
                    disablePage(2);
                    disablePage(maxPaginFields - 1);
                }
            }
            //first pages
            else {
                s = 1;
                e = maxPaginFields - 1;
                addPagesToPaginator(s, e);
                disablePage(maxPaginFields - 1);
            }
        }
        activePage(actPage);
    };
}