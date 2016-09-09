var UtAdmin = angular.module('UtAdmin',
    ['yaru22.angular-timeago', 'ngAnimate', 'angularSpinner', 'Mac','ui.router','ui.materialize']);


$(document).ready(function(){
    $('select').material_select();
});

UtAdmin.config(["$locationProvider", function($locationProvider) {
    //$locationProvider.html5Mode(true);
}]);


/** Enter pressed directive */
UtAdmin.directive('ngEnter', function () {
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

UtAdmin.directive('disableAnimation', function ($animate) {
    return {
        restrict: 'A',
        link: function($scope, $element){
            $animate.enabled($element,false);
        }
    };
});

UtAdmin.filter('urtstring', function () {
    return function (input) {
        if (input) {
            return input.replace(/\^\d/g, '');
        }
    }
});

UtAdmin.filter('secondsToDateTime', [function() {
    return function(seconds) {
        console.log(seconds);
        return new Date(1970, 0, 1).setSeconds(seconds);
    };
}]);