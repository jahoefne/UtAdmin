UtAdmin.component('userName', {

    template: '<a ui-sref="user({b3id:{{$ctrl.b3id}}})"  class="waves-effect waves-light truncate"' +
    'ng-mouseenter="$ctrl.showIt()" ng-mousedown="$ctrl.hideIt()" ng-mouseleave="$ctrl.hideIt()">' +
    '{{$ctrl.name | limitTo: 20 }}{{$ctrl.name.length > 20 ? "..." : ""}}</a>',

    bindings: {
        b3id: '<',
        name: '<'
    },

    controller: function ($http, $interval, $timeout, $httpParamSerializer, $filter) {
        var ctrl = this;

        ctrl.name = $filter('urtstring')(ctrl.name);
        // mouseenter event

        ctrl.showIt = function () {
            timer = $timeout(function () {
                $('.alias-toast').remove()
                $http.get("user-aliases.json?" + $httpParamSerializer({id: ctrl.b3id})).success(function (result) {
                    var toast =(result.length != 0) ? "\'s most recent aliases: " + result.join(", ") + "" : " doesn't have any aliases.";
                    Materialize.toast(ctrl.name + toast, 7000,"alias-toast");
                }.bind(this));
            }, 200);
        };

        // mouseleave event
        ctrl.hideIt = function () {
            $timeout.cancel(timer);
            $('.alias-toast').remove()
        };
    }
});