UtAdmin.controller('ChatCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer, $rootScope) {
        $scope.updating = false;

        $scope.init = function (state) {
            console.log("Init ChatModule", state);
            $scope.autoUpdateInterval = 3500;
            $scope.talkbackTxt = "";
            $scope.msgs = [];
            $scope.state = state;
            $http.get("/chat.json?" + $httpParamSerializer($scope.state)).then(OnChatLoaded, OnError);
            $interval($scope.update, $scope.autoUpdateInterval, 0, true, true);
        };

        /** Query was executed */
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
            $scope.updating = false;
        };

        var OnError = function () {
            console.log("Error Loading Chat");
        };

        /** Show conversation based on starting message id */
        $scope.showConv = function (id) {
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
            $scope.msgs = [];
            $scope.state.fromMessageId = undefined;
            $scope.state.page = 0;
            $scope.update();
        };

        /** Load messages from server, based on the query parameters defined in state */
        $scope.update = function (autoCalled) {

            if($scope.state.autoUpdate){
                $scope.state.queryString = undefined;
                $scope.state.userId = undefined;
            }

            if ($("#chat-module").is(":visible")) {
                $scope.updating = true;
                if (autoCalled != undefined && autoCalled && !$scope.state.autoUpdate) {
                    $scope.updating = false;
                    return;
                }
                console.log($httpParamSerializer($scope.state));
                $http.get("/chat.json?" + $httpParamSerializer($scope.state)).then(OnChatLoaded, OnError);
            }else{
                console.log("Not Updating")
            }
        };

        /***
         * Events Receiving
         */

        /** Load and display the chatlog for a given user
         *
         */
        $scope.$on('show-chat-for-user', function (event, id) {
            console.log("show-chat-for-user",event, id);
            $scope.state.userId = id;
            $scope.state.latestId= undefined;
            $scope.state.page = 0;
            $scope.state.page = 0;
            $scope.state.radio = true;
            $scope.state.autoUpdate = false;
            $("#chatlog-user-id-search-label").addClass("active");

            $http.get("/chat.json?" + $httpParamSerializer($scope.state)).then(OnChatLoaded, OnError);
            $rootScope.$broadcast('show-module',"chat");
        });

    }
);