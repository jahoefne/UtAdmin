UtAdmin.controller('PenaltiesCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer, $rootScope) {
        $scope.init = function (state) {
            console.log("Init PenaltiesCtrl");
            $scope.penalties = [];
            $scope.state = state;
            $scope.updating = false;
            $scope.update();
        };

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

        $scope.cleanUpdate = function () {
            $scope.state.page = 0;
            $scope.update();
        };

        $scope.update = function () {
            $scope.updating = true;
            console.log("/penalties.json?" + $httpParamSerializer($scope.state));
            $http.get("/penalties.json?" + $httpParamSerializer($scope.state)).then(OnLoaded, OnError);
        };

        $scope.delete = function (penaltyId) {
            vex.dialog.confirm({
                message: 'Remove the penalty #' + penaltyId + '?',
                callback: function (value) {
                    if (value) {
                        $scope.updating = true;
                        $.get("/remove-punishment?penaltyId=" + penaltyId).done(function () {
                            $scope.update();
                            Materialize.toast('Removed Penalty!', 1500);
                        });
                    }
                }
            });
        };


        /**
         * Event Listeners
         */
        $scope.$on('show-penalties-for-user', function (event, id) {
            console.log("show-penalties-for-user", event, id);
            $scope.state.page = 0;
            $scope.state.userId = id;
            $scope.state.queryString = undefined;
            $scope.state.filterType = undefined;
            $scope.penalties = [];
            $scope.update();
            $("#penalties-user-id-search-label").addClass("active");
            $rootScope.$broadcast('show-module', "penalties");
        });


    }
);