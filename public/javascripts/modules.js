UtAdmin.controller('ModulesCtrl',
    function ($scope) {
        $scope.init = function () {
            $(".module").hide();
            $scope.current = "";
            $scope.update("status");
        };

        $scope.update = function (module) {
            console.log(module);
            if ($scope.current != module) {
                $(".module").hide();
                $("#"+module + "-module").fadeIn(500);
                $scope.current = module;
            }
        };

        $scope.$on('show-module', function (event, module) {
            console.log("show-module",module);
            $scope.update(module);
        });
    }
);

