angular.module('UtAdmin')
    .component('status', {
        templateUrl: '/status-template.html',
        controller: function (StatusService) {
            this.players = StatusService.players;
            this.status = StatusService;
        }
    });