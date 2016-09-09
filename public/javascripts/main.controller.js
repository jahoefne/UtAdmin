UtAdmin.controller('MainCtrl',
    function ($scope, $http, $interval, $timeout, timeAgoSettings, StatusService) {

        $scope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
            if (toState.resolve) {
                console.log("Resolving…………");
                $scope.showSpinner=true;
            }
        });
        $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
            if (toState.resolve) {
                console.log("Resolved!!");
                $scope.showSpinner=false;
            }
        });

        /*    var statusUpdateInterval = 2000;

         $scope.init = function init(){

         console.log("Init MainCtrl");

         var update = function(){
         StatusService.update();
         console.log("Updating from comp");
         $timeout(update,statusUpdateInterval);
         };
         update();
         };
         $scope.init();
         */
    }
);
