UtAdmin.controller('UserCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer) {
        $scope.user = undefined;

        $scope.init = function() {
            console.log("Init User");
            $http.get("/user.json?id=2").then(function (response) {
                console.log(response);
                $scope.user = response.data;
            });
        };

        $scope.$on('show-for-user-page', function (event, id) {
           // TODO
        });
    }
);