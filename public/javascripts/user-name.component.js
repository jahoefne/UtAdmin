UtAdmin.component('userName', {

    template: '<a ui-sref="user({b3id:{{$ctrl.b3id}}})"  class="waves-effect waves-light truncate">' +
    '{{$ctrl.name | limitTo: 20 }}{{$ctrl.name.length > 20 ? "..." : ""}}</a>' +
    '<span class="btn btn-flat tiny" ng-click="$ctrl.showAliases()"><i class="material-icons">people</i></span>',

    bindings: {
        b3id: '<',
        name: '<'
    },

    controller: function ($http, $interval, $timeout, $httpParamSerializer, $filter, $sanitize) {
        var ctrl = this;

        ctrl.name = $sanitize($filter('urtstring')(ctrl.name));

        ctrl.showAliases = function () {

            $http.get("user-aliases.json?" + $httpParamSerializer({id: ctrl.b3id})).success(function (result) {
                var message = ctrl.name;
                message += (result.length != 0) ? "\'s most recent aliases: " : " doesn't have any aliases.";
                message = "<b>" + message + "</b><ul>"

                for (var i = 0; i < result.length; i++) {
                    message = message + "<li><b>" + $sanitize(result[i].name)+ "</b> used "+result[i].used +"x times"+"</li>";
                }
                message += "</ul>";
                vex.dialog.alert({ unsafeMessage: message });
            }.bind(this));
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