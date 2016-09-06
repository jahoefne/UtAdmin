var PenaltiesModule = angular.module('PenaltiesModule', ['Mac']);

PenaltiesModule.controller('PenaltiesModuleCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer, initialState) {
        $scope.state = initialState;
        $scope.updating = false;

        $scope.penalties = [];
        var OnLoaded = function (response) {
            $scope.penalties = response.data;
            $scope.updating = false;
        };

        var OnError = function () {
            console.log("Error");
        };


        /** Previous Chat Log Page */
        $scope.prev = function () {
            $scope.state.page -= 1;
            $scope.update();
        };

        /** Next Chat Log Page */
        $scope.next = function () {
            $scope.state.page += 1;
            $scope.update();
        };


        $scope.update = function () {
            $scope.updating = true;
            if ($("#penalties-module").is(":visible")) {
                console.log($httpParamSerializer($scope.state));
                $http.get("/penalties.json?" + $httpParamSerializer($scope.state)).then(OnLoaded, OnError);
            }
        };
        $scope.update();
    }
);