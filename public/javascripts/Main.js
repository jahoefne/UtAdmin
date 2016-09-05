/**
 * This is the main controller - responsible for switching between the different
 * angular apps. By default all angular apps are running
 */
var currentModule = "";

function handleHashChange() {
    var hash = window.location.hash;
    if (hash != currentModule) {
        $(".module").hide();
        $(window.location.hash + "-module").fadeIn(500);
    }
}

$(document).ready(function () {

    /** Display the corresponding module if the initial url already has a hash */
    var initialHash = window.location.hash;
    if (initialHash != undefined && initialHash != "") {
        handleHashChange();
    } else {
        window.location.hash = "#status";
        $("#status-module").fadeIn(500);
    }

    /** If user clicks a navbar item - fade out old module and fade in new module */
    $(window).on('hashchange', function (e) {
        handleHashChange();
    });
});