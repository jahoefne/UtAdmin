var UtAdmin = angular.module('UtAdmin',
    ['yaru22.angular-timeago', 'ngAnimate',
        'Mac','ui.router','ui.materialize', 'nvd3','ngSanitize', 'ngMdIcons',
        'infinite-scroll']);

UtAdmin.controller('MainCtrl', ['$scope', function($scope) {
}]);

angular.module('infinite-scroll').value('THROTTLE_MILLISECONDS', 1000);

$(document).ready(function(){
    $('select').material_select();
    $(".button-collapse").sideNav();
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


UtAdmin.filter('radioFilter', function () {
    return function (input) {
        if (input) {
            if(input.indexOf("RADIO")==0){
                console.log("Filtering Radio");
               return input.substring(9);
            }
            return input;
        }
    }
});

UtAdmin.filter('urtstring', function () {
    return function (input) {
        if (input) {
            return input.replace(/\^\d/g, '');
        }
    }
});


UtAdmin.filter('secondsToDuration', [function() {
    return function(seconds) {
        return moment.duration(seconds, "seconds").format("D[d] H[h] m[m] s[s]");
    };
}]);