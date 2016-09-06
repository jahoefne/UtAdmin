UtAdmin.controller('ModulesCtrl',
    function ($scope, $http, $interval, $location, $timeout, $httpParamSerializer) {
        $scope.init = function () {
            $(".module").hide();
            $scope.current = "";
            $scope.update("status")
        };

        $scope.update = function (module) {
            console.log(module);
            if ($scope.current != module) {
                $(".module").hide();
                $("#"+module + "-module").fadeIn(500);
                $scope.current = module;
            }
        };
    }
);

