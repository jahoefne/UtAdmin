UtAdmin.component('user', {
    templateUrl: '/user-template.html',

    bindings: {
        b3id: '<',
        user: '<'
    },

    controller: function (StatusService, $location, $http) {
        // user object is resolved by the router and passed
        ctrl = this;

        this.updateXlrVisibility = function () {
            console.log(ctrl.user.xlrVisible);
            $.ajax(jsRoutes.controllers.Administrator.setXlrVisibility(ctrl.user.b3Id, !ctrl.user.xlrVisible));
        };


        this.punishPlayer = function(){
            console.log("Punish");
            if(ctrl.ban.type=="Notice"){
                $.get("/add-punishment?userId="+ctrl.user.b3Id+"&penalty="+ctrl.ban.type+"&reason="+ctrl.ban.reason);
            }else {
                $.get("/add-punishment?userId=" + ctrl.user.b3Id + "&reason="+ ctrl.ban.reason + "&penalty=" + ctrl.ban.type + "&duration=" +ctrl.ban.duration);
            }
            Materialize.toast('Done.', 1500);
            ctrl.ban.reason = undefined;
            ctrl.ban.duration = undefined;
        };

        this.changeGroup = function(){
            console.log("Setting Bits:", ctrl.groupBits);
            if(ctrl.groupBits!="none"){
                $.ajax(jsRoutes.controllers.Administrator.changeGroupOfUser(ctrl.user.b3Id, ctrl.groupBits));
                Materialize.toast('Changed Group', 1500);
            }else{
                console.log("Got none, wtf!")
            }
        };

        this.$onInit = function () {
            $.getJSON("http://geoip.nekudo.com/api/" + ctrl.user.currentIp + "/en/full/",
                function (json) {
                    ctrl.countryCode = json.country.iso_code.toLowerCase();
                    ctrl.countryName = json.country.names.en;

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
                    console.log(json);
                    if (json.city != undefined) {
                        cityOpt = "<br>City: " + json.city.names.en;
                    }

                    marker.bindPopup(
                        "<b>" +
                        "Country: "
                        + '<span class="flag-icon flag-icon-' + json.country.iso_code.toLowerCase() + '"></span> (' + json.country.names.en + ')'
                        + cityOpt +
                        "</b>").openPopup();
                });
        };

        this.$onDestroy = function () {
            //   console.log("Destroying User Comp");
        };
    }
});