UtAdmin.component('penalties', {
        templateUrl: '/penalties-template.html',

        bindings: {
            b3id: "<"
        },

        controller: function ($http, $interval, $timeout, $httpParamSerializer, $stateParams, $location, timeAgoSettings) {
            ctrl = this;
            ctrl.updating = true;

            /** Previous Chat Log Page */
            ctrl.prev = function () {
                ctrl.state.page -= 1;
                ctrl.update();
            };

            /** Next Chat Log Page */
            ctrl.next = function () {
                ctrl.state.page += 1;
                ctrl.update();
            };

            ctrl.cleanUpdate = function () {
                ctrl.state.page = 0;
                console.log(ctrl.state);
                ctrl.update();
            };

            ctrl.update = function () {
                ctrl.updating = true;
                console.log("Update", $httpParamSerializer(ctrl.state));
                console.log("/penalties.json?" + $httpParamSerializer(ctrl.state));
                $http.get("/penalties.json?" + $httpParamSerializer(ctrl.state)).then(function (response) {
                    ctrl.penalties = response.data;
                    ctrl.updating = false;
                });
            };

            ctrl.delete = function (penaltyId) {
                vex.dialog.confirm({
                    message: 'Remove the penalty #' + penaltyId + '?',
                    callback: function (value) {
                        if (value) {
                            ctrl.updating = true;
                            $.get("/remove-punishment?penaltyId=" + penaltyId).done(function () {
                                ctrl.update();
                                Materialize.toast('Removed Penalty!', 1500);
                            });
                        }
                    }
                });
            };

            ctrl.init = function () {
                console.log("Init PenaltiesCtrl ", $stateParams, ctrl.b3id,$location.search().b3id);
                ctrl.penalties = [];

                /** Build up initial state from url */
                var user = undefined;
                if (isFinite($location.search().b3id)) {
                    user = +$location.search().b3id;
                    $("#penalties-user-id-search-label").addClass("active"); // 'touch' input field
                }

                ctrl.state = {count: 30, page: 0, userId: user, filterType: undefined, activeOnly: false};
                ctrl.update();
            };
            ctrl.init();
        }
    }
);