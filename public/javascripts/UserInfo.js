var count = 15;
var offset = 0;

$.getJSON("/user-history-json?id=" + userId, function (data) {
    window.historydata = data
});
function reloadChatlog() {
    $('#chatlogTable').load(jsRoutes.controllers.Application.chatLogPlain(count, offset, userId, false, true).absoluteURL())
}

function nextPageChatlog() {
    offset += count;
    reloadChatlog();
}
function prevPageChatlog() {
    offset -= count;
    reloadChatlog();
}

$(document).ready(function () {

    reloadChatlog();

    getIpLocationJson(userIp);

    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        window.historydata.eventColor = '#07256A';
        window.historydata.displayEventEnd = true;
        $('#calendar').fullCalendar(window.historydata);
    });

    function getIpLocationJson(ip) {
        ip = ip.substring(1, ip.length - 1);
        $.getJSON("http://geoip.nekudo.com/api/" + ip + "/en/full/",
            function (json) {
                console.log(json);
                var map = L.map('userLocationMap', {
                    zoomControl: true,
                    scrollWheelZoom: false
                }).setView([json.location.latitude, json.location.longitude], 4);

                L.tileLayer('http://{s}.tile.stamen.com/toner/{z}/{x}/{y}.{ext}', {
                    attribution: 'Tiles:<a href="http://stamen.com">Stamen</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Data &copy; <a href="http://www.openstreetmap.org/copyright">OSM</a>',
                    subdomains: 'abcd',
                    minZoom: 0,
                    maxZoom: 20,
                    ext: 'png'
                }).addTo(map);

                var marker = L.marker([json.location.latitude, json.location.longitude],
                    {
                        icon: L.AwesomeMarkers.icon(
                            {
                                icon: 'crosshairs',
                                markerColor: 'blue',
                                prefix: 'fa',
                                iconColor: 'white',
                                // spin: true
                            }
                        )
                    }
                ).addTo(map);

                var cityOpt = ""
                if (typeof json.city !== 'undefined') {
                    cityOpt = "<br>City: " + json.city.names.en;
                }

                marker.bindPopup(
                    "<b style='color:black;'>Name:"
                    + userName + "<br>Country: "
                    + json.country.names.en
                    + cityOpt +
                    "</b>").openPopup();


                $("#userCountryFlag").attr("src", "/assets/javascripts/deps/flag-icon-css/flags/4x3/"+ json.country.iso_code.toLowerCase()+".svg");
               console.log(json.country.names.en);
                $("#userCountryName").text(json.country.names.en);
            }
        );
    }


})
;