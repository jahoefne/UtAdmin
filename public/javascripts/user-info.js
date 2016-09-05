function setXlrStatsVisibility(b3Id, visibility) {
    $.ajax(jsRoutes.controllers.Administrator.setXlrVisibility(b3Id, visibility));
}


$(document).ready(function () {

    console.log(window.userId);
    $.get("/chatlog").done(function(data){
        $("#chatlog").html(data);
    });

    $("#xlrstats-visibility-switch").change(function () {
            var el = document.getElementById('xlrstats-visibility-switch');
            var b3Id = $(el).data("b3-id");
            if (el.checked) {
                setXlrStatsVisibility(b3Id, true);
            } else {
                setXlrStatsVisibility(b3Id, false);
            }
        }
    );

    $('.modal-trigger').leanModal();

    $('.datepicker').pickadate({
        selectMonths: true, // Creates a dropdown to control month
        selectYears: 0, // Creates a dropdown of 15 years to control year
        min: Date.now()
    });

    $(document).ready(function() {
        $('select').material_select();
    });

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
                var map = L.map('user-location-map', {
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

                var marker = L.marker([json.location.latitude, json.location.longitude]).addTo(map);


                var cityOpt = "";
                if (typeof json.city !== 'undefined') {
                    cityOpt = "<br>City: " + json.city.names.en;
                }

                marker.bindPopup(
                    "<b style='color:black;'>" +
                    "Country: "
                    + '<span class="flag-icon flag-icon-' + json.country.iso_code.toLowerCase() + '"></span> ' + json.country.names.en
                    + cityOpt +
                    "</b>").openPopup();


                $("#userCountryFlag").html('<span class="highlight">' + json.country.names.en + '</span>');
            }
        );
    }


})
;