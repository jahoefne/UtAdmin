UtAdmin.component('users', {
    templateUrl: '/users-template.html',
    controller: function ($http, $interval, $timeout, $httpParamSerializer, StatusService) {
        var ctrl = this;

        ctrl.updating = true;
        ctrl.byName = true;
        ctrl.searchBy = "name";
        ctrl.users = [];
        ctrl.infScroll = true;

        ctrl.state = {
            page: -1,
            count: 30,
            q: undefined,
            groupBits: undefined
        };

        // Next Log Page
        ctrl.nextPage = function () {
            if (ctrl.busy || !ctrl.infScroll) {
                return;
            }

            ctrl.state.page += 1;
            ctrl.state.latestId = 0;
            ctrl.busy = true;
            $http.get("users.json/" + ctrl.searchBy + "?" + $httpParamSerializer(ctrl.state)).success(function (result) {
                if (result.length == 0) {
                    ctrl.infScroll = false;
                    ctrl.busy = false;
                    Materialize.toast('No more users matching query!', 1500);
                    return;
                }
                for (var i = 0; i < result.length; i++) {
                    ctrl.users.push(result[i]);
                }
                ctrl.busy = false;
            });
        };

        // Clean the state and do a fresh upate
        ctrl.cleanUpdate = function () {
            ctrl.users = [];
            ctrl.state.page = 0;
            ctrl.busy=false;
            ctrl.infScroll = true;
            ctrl.update();
        };

        ctrl.update = function () {
            ctrl.updating = true;
            ctrl.searchBy = (ctrl.byName) ? "name" : "ip";
            ctrl.state.groupBits = (ctrl.groupBits != "Any") ? ctrl.groupBits : undefined;

            $http.get("users.json/" + ctrl.searchBy + "?" + $httpParamSerializer(ctrl.state)).success(function (result) {
                ctrl.users = result;
                ctrl.updating = false;
            });
        };
    }
});