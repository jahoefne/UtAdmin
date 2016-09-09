UtAdmin.component('users', {
    templateUrl: '/users-template.html',
    controller: function ($http, $interval, $timeout, $httpParamSerializer, StatusService) {
        var ctrl = this;

        ctrl.updating = true;

        ctrl.byName = true;
        ctrl.searchBy = "name";

        ctrl.state = {
            page: 0,
            count: 30,
            q: undefined,
            groupBits: undefined
        };

        // Previous Log Page
        ctrl.prev = function () {
            ctrl.state.page -= 1;
            ctrl.state.latestId = 0;
            ctrl.users = [];
            ctrl.update();
        };

        // Next Log Page
        ctrl.next = function () {
            ctrl.state.page += 1;
            ctrl.state.latestId = 0;
            ctrl.users = [];
            ctrl.update();
        };

        // Clean the state and do a fresh upate
        ctrl.cleanUpdate = function () {
            ctrl.users = [];
            ctrl.state.page = 0;
            console.log(ctrl.groupBits);
            ctrl.update();
        };

        ctrl.update = function () {
            ctrl.updating = true;
            ctrl.searchBy = (ctrl.byName) ? "name" : "ip";
            ctrl.state.groupBits = (ctrl.groupBits != "Any") ? ctrl.groupBits : undefined;

            console.log("Queying", "users.json/" + ctrl.searchBy + "?" + $httpParamSerializer(ctrl.state));
            $http.get("users.json/" + ctrl.searchBy + "?" + $httpParamSerializer(ctrl.state)).success(function (result) {
                console.log("Got", result);
                ctrl.users = result;
                ctrl.updating = false;
            });
        };

        ctrl.$onInit = function () {
            ctrl.update();
        };
    }
});