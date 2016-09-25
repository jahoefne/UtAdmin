UtAdmin.component('onlineActions', {
        templateUrl: "/assets/html/online-actions.html",
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