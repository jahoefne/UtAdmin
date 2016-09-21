UtAdmin.component('userName', {

    template: '<a ui-sref="user({b3id:{{$ctrl.b3id}}})" ' +
    'ng-mouseenter="$ctrl.showIt()" ng-mousedown="$ctrl.hideIt()" ng-mouseleave="$ctrl.hideIt()">{{$ctrl.name | urtstring}}</a>',

    bindings: {
        b3id: '<',
        name: '<'
    },

    controller: function ($http, $interval, $timeout, $httpParamSerializer) {
        var ctrl = this;
        // mouseenter event

        ctrl.showIt = function () {
            timer = $timeout(function () {
                $('.alias-toast').remove()
                $http.get("user-aliases.json?" + $httpParamSerializer({id: ctrl.b3id})).success(function (result) {
                    var toast =(result.length != 0) ? " is also known as: " + result.join(", ") + "." : " doesn't have any aliases.";
                    Materialize.toast(ctrl.name + toast, 40000,"alias-toast");
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