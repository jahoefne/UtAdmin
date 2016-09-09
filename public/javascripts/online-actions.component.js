UtAdmin.component('onlineActions', {
        template:
        '<span class="cursor" ng-show="$ctrl.isPlayerOnline">' +
        '<span mac-tooltip="Online Now!" mac-tooltip-direction="top">' +
        '<span class="blue-text glow-blue" class="blue-text"><i class="{{$ctrl.size}} material-icons">check_circle</i></span></span>&nbsp;' +
        '<span class="cursor" ng-click="$ctrl.pm()"><i class="{{$ctrl.size}} material-icons">message</i></span>&nbsp;' +
        '<span class="cursor" ng-click="$ctrl.punishModal()"><i class="{{$ctrl.size}} material-icons">pan_tool</i></span></span>',

        bindings: {
            b3id: '<',
            name: '@',
            size: '@'
        },

        controller: function (StatusService, $interval) {
            this.pm = function () {
                StatusService.pm(this.slot, this.name, this.b3id);
            };

            this.punishModal = function () {
                StatusService.punishModal(this.slot, this.name);
            };

            this.updateOnline = function (self) {
                var online = StatusService.isOnline(self.b3id);
                self.isPlayerOnline = online.online;
                if (online.online) {
                    self.slot = online.slot;
                }
            };

            this.$onInit = function () {
                this.interval = $interval(this.updateOnline, 4000, 0, true, this);
            };

            this.$onDestroy = function () {
                $interval.cancel(this.interval);
            };

            this.updateOnline(this);
        }
    }
);