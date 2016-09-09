'use strict';
(function () {

    class StatusCompCtrl {
        constructor(StatusService) {
            this.players = StatusService.players;
            this.status = StatusService;
        }
    }

    angular.module('UtAdmin')
        .component('status', {
            templateUrl: '/status-template.html',
            controller: StatusCompCtrl
        });

})();