UtAdmin.component('userName', {

    templateUrl:"/assets/html/user-name.html",

    bindings: {
        b3id: '<',
        name: '<',
        aliasCount: '<'
    },

    controller: function ($http, $interval, $timeout, $httpParamSerializer, $filter, $sanitize, $rootScope) {
        var ctrl = this;

        console.log("aliasCount",ctrl.aliasCount);
        ctrl.name = $filter('urtstring')(ctrl.name);

        ctrl.showAliases = function () {
            $rootScope.$broadcast("modalForUser", { name: ctrl.name, b3id: ctrl.b3id });
        };

        ctrl.showIt = function () {
            timer = $timeout(function () {
                $('.alias-toast').remove();
                $http.get("user-aliases.json?" + $httpParamSerializer({id: ctrl.b3id})).success(function (result) {
                    var toast = (result.length != 0) ? "\'s most recent aliases: " + result.join(", ") + "" : " doesn't have any aliases.";
                    Materialize.toast(ctrl.name + toast, 7000, "alias-toast");
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