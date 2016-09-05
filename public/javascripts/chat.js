var ChatModule = angular.module('ChatModule', ['yaru22.angular-timeago', 'ngAnimate', 'angularSpinner']);

ChatModule.controller('ChatModuleCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer, timeAgoSettings, initialState) {

        $scope.autoUpdateInterval = 3500;
        $scope.minUpdateDiff = 2000;
        $scope.updating = false;

        $scope.talkbackTxt = "";
        $scope.msgs = [];
        $scope.dirty = false;
        timeAgoSettings.fullDateAfterSeconds = 60 * 60 * 24; // display full date if it's more than 24 hours in the past

        $scope.state = initialState;
        $scope.lastUpdate = 0;

        $scope.lastSeen = 0;

        var OnChatLoaded = function (response) {
            console.log("got ", response.data.length, "new messages");

            if (response.data[0] != undefined) {
                if (response.data[0].id > $scope.lastSeen) {
                    $scope.lastSeen = response.data[0].id;
                    $scope.state.latestId= $scope.lastSeen;
                }
            }

            if (!$scope.state.autoUpdate) {
                console.log("Not AUto Updating");
                $scope.state.latestId= 0;
                $scope.msgs = response.data;
            } else {
                for (var i = response.data.length; i > 0; i--) {
                    $scope.msgs.unshift(response.data[i - 1]);
                }
            }
            $scope.updating = false;
        };

        var OnError = function () {
            console.log("Error");
        };

        $scope.talkback = function () {
            if ($scope.talkbackTxt != "") {
                $.ajax(jsRoutes.controllers.Rcon.say($scope.talkbackTxt));
                $scope.talkbackTxt = "";
                Materialize.toast('Sent public message!', 1500);
            }
        };

        $scope.prev = function () {
            $scope.state.page -= 1;
            $scope.update();
        };

        $scope.next = function () {
            $scope.state.page += 1;
            $scope.update();
        };

        $scope.update = function (autoCalled) {
            if ($("#chat-module").is(":visible")) {
                if (autoCalled != undefined && autoCalled && !$scope.state.autoUpdate && !$scope.dirty)
                    return;

                if ((Date.now() - $scope.lastUpdate) > $scope.minUpdateDiff) {
                    $scope.updating = true;
                    $http.get("/chat.json?" + $httpParamSerializer($scope.state)).then(OnChatLoaded, OnError);
                    $scope.lastUpdate = Date.now();
                    $scope.dirty = false;
                } else {
                    $scope.dirty = true;
                    $timeout($scope.update, $scope.autoUpdateInterval, true, true);
                }
            } else {
                $http.get("/chat.json/latestId").then(
                    function successCallback(response) {
                        console.log("New messges = #" + response.data - $scope.lastSeen);
                    },
                    function errorCallback(response) {
                        console.log("Coulnd't load latest message id!")
                    }
                );
            }
        };

        $scope.update();
        $interval($scope.update, $scope.autoUpdateInterval, 0, true, true);
    }
);

/** Talkback Enter pressed */
ChatModule.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if (event.which === 13) {
                scope.$apply(function () {
                    scope.$eval(attrs.ngEnter);
                });

                event.preventDefault();
            }
        });
    };
});