UtAdmin.component('chat', {
    templateUrl: '/chat-template.html',

    bindings: {
      autoUpdate: "<",
      small: "<"
    },

    controller: function ($http, $interval, $timeout, $httpParamSerializer, StatusService, $location) {
        ctrl = this;
        ctrl.updating = true;
        ctrl.autoUpdateInterval = 3000;

        ctrl.copyMode = false;

        ctrl.isPlayerOnline = function (id) {
            return StatusService.isPlayerOnline(id);
        };

        ctrl.OnChatLoaded = function (response) {
            if (!ctrl.state.autoUpdate) {
                ctrl.msgs = response.data;
            } else {
                for (var i = response.data.length; i > 0; i--) {
                    var m = response.data[i - 1];
                    if (m.id > ctrl.state.latestId) {
                        ctrl.state.latestId = m.id
                    }
                    ctrl.msgs.unshift(m);
                }
                ctrl.msgs.splice(-1,response.data.length);
            }
            ctrl.updating = false;
        };

        ctrl.OnError = function () {
            console.log("Error Loading Chat");
        };

        ctrl.isRadioMessage = function (input) {
            //console.log(input);
            if (input) {
                return input.indexOf("RADIO") == 0;
            }
        };

        // Show conversation based on starting message id
        ctrl.showConv = function (id) {
            ctrl.state.fromMessageId = id;
            ctrl.state.latestId = 0;
            ctrl.state.userId = undefined;
            ctrl.state.queryString = undefined;
            ctrl.msgs = [];
            ctrl.update();
        };

        //Send Talkback message
        ctrl.talkback = function () {
            if (ctrl.talkbackTxt != "") {
                $.ajax(jsRoutes.controllers.Rcon.say(ctrl.talkbackTxt));
                ctrl.talkbackTxt = "";
                Materialize.toast('Sent public message!', 1500);
            }
        };

        // Previous Chat Log Page
        ctrl.prev = function () {
            ctrl.state.page -= 1;
            ctrl.state.latestId = 0;
            ctrl.msgs = [];
            ctrl.update();
        };

        // Next Chat Log Page
        ctrl.next = function () {
            ctrl.state.page += 1;
            ctrl.state.latestId = 0;
            ctrl.msgs = [];
            ctrl.update();
        };

        // Next Chat Log Page
        ctrl.nextPage = function () {
            if (ctrl.busy || ctrl.autoUpdate) return;
            ctrl.busy = true;
            ctrl.state.page += 1;
            ctrl.state.latestId = 0;

            $http.get("/chat.json?" + $httpParamSerializer(ctrl.state)).then(function(response){
                for (var i = 0; i< response.data.length; i++) {
                    ctrl.msgs.push(response.data[i]);
                }

                ctrl.busy = false;
            }, ctrl.OnError);
        };


        // Clean the state and do a fresh upate
        ctrl.cleanUpdate = function () {
            ctrl.state.latestId = 0;
            ctrl.msgs = [];
            ctrl.busy = false;
            ctrl.state.fromMessageId = undefined;
            ctrl.state.page = 0;
            ctrl.update();
        };

        //Load messages from server, based on the query parameters defined in state
        ctrl.update = function (autoCalled) {
            if (ctrl.state.autoUpdate) {
                ctrl.state.queryString = undefined;
                ctrl.state.userId = undefined;
            }
            ctrl.updating = true;
            if (autoCalled != undefined && autoCalled && !ctrl.state.autoUpdate) {
                ctrl.updating = false;
                return;
            }
            console.log($httpParamSerializer(ctrl.state));
            $http.get("/chat.json?" + $httpParamSerializer(ctrl.state)).then(ctrl.OnChatLoaded, ctrl.OnError);
        };

        ctrl.$onInit = function () {
            /** Build up initial state from url */

            var user = undefined;
            if (isFinite($location.search().b3id)) {
                user = +$location.search().b3id;
                $("#chatlog-user-id-search").addClass("active"); // 'touch' input field
                ctrl.autoUpdate = false;
            }

            ctrl.state = {autoUpdate: ctrl.autoUpdate, count: 12, page: 0, latestId: 0, radio: false, userId: user};
            ctrl.talkbackTxt = "";
            ctrl.msgs = [];

            console.log("Init ChatModule", ctrl.state);
            $http.get("/chat.json?" + $httpParamSerializer(ctrl.state)).then(ctrl.OnChatLoaded, ctrl.OnError);
            ctrl.interval = $interval(ctrl.update, ctrl.autoUpdateInterval, 0, true, true);
        };

        ctrl.$onDestroy = function () {
            //console.log("Destroying Chat Module");
            $interval.cancel(ctrl.interval);
        }

    }
})
;
