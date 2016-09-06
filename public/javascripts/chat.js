UtAdmin.controller('ChatCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer) {
        $scope.updating = false;
        $scope.init = function(state){
            console.log("Init ChatModule", state);
            $scope.updated = false;
            $scope.autoUpdateInterval = 3500;
            $scope.talkbackTxt = "";
            $scope.msgs = [];
            $scope.state = state;
            $interval($scope.update, $scope.autoUpdateInterval, 0, true, true);
        };

        var OnChatLoaded = function (response) {
            console.log("got ", response.data.length, "new messages");

            if (!$scope.state.autoUpdate) {
                console.log("Not AUto Updating", response.data);
                $scope.msgs = response.data;
            } else {
                for (var i = response.data.length; i > 0; i--) {
                    var m = response.data[i - 1];
                    if (m.id > $scope.state.latestId) {
                        $scope.state.latestId = m.id
                    }
                    $scope.msgs.unshift(m);
                }
            }
            $scope.updated = true;
            $scope.updating = false;
        };

        var OnError = function () {
            console.log("Error Loading Chat");
        };


        $scope.showConv = function(id){
            $scope.state.fromMessageId = id;
            $scope.state.latestId = 0;
            $scope.state.userId = undefined;
            $scope.state.queryString = undefined;
            $scope.msgs = [];
            $scope.update();
        };

        /** Send Talkback message */
        $scope.talkback = function () {
            if ($scope.talkbackTxt != "") {
                $.ajax(jsRoutes.controllers.Rcon.say($scope.talkbackTxt));
                $scope.talkbackTxt = "";
                Materialize.toast('Sent public message!', 1500);
            }
        };

        /** Previous Chat Log Page */
        $scope.prev = function () {
            $scope.state.page -= 1;
            $scope.state.latestId = 0;
            $scope.msgs = [];
            $scope.update();
        };

        /** Next Chat Log Page */
        $scope.next = function () {
            $scope.state.page += 1;
            $scope.state.latestId = 0;
            $scope.msgs = [];
            $scope.update();
        };

        /** Clean the state and do a fresh upate */
        $scope.cleanUpdate = function () {
            $scope.state.latestId = 0;
            $scope.state.fromMessageId = undefined;
            $scope.msgs = [];
            $scope.state.page = 0;
            $scope.update();
        };


        $scope.update = function (autoCalled) {
            $scope.updating = true;
            if ($("#chat-module").is(":visible")) {
                if (autoCalled != undefined && autoCalled && !$scope.state.autoUpdate) {
                    $scope.updating = false;
                    return;
                }
                console.log($httpParamSerializer($scope.state));
                $http.get("/chat.json?" + $httpParamSerializer($scope.state)).then(OnChatLoaded, OnError);
            }
        };
    }
);