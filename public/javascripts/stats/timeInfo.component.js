angular.module('UtAdmin').component('timeInfo', {
    templateUrl: "/timeInfoTemplate.html",

    controller: function ($http, $filter, $interval, StatusService, $state, $httpParamSerializer) {
        var ctrl = this;
        ctrl.totalTime = 0;
        ctrl.totalText = "";
        ctrl.totalWage = 0;
        ctrl.rawTopPlayers = {};

        ctrl.topOnlineTimeOption = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 400,
                x: function (d) {
                    return d.label;
                },
                y: function (d) {
                    return d.value;
                },
                showControls: false,
                showValues: false,
                duration: 500,
                showLegend: false,
                noData: "Loading Data…",
                multibar: {
                    dispatch: {
                        elementClick: function (t, u) {
                            $state.go('user', {b3id: ctrl.rawTopPlayers[t.index].player.id});
                        }
                    }
                },
                title: {
                    enable: true,
                    text: "Most Online Time",
                    className: "h4"
                },
                tooltip: {
                    valueFormatter: function (d, i) {
                        return $filter("secondsToDuration")(d) + "&nbsp;&nbsp;&nbsp;";
                    },
                    keyFormatter: function (d, i) {
                        return "";
                    },
                    headerEnabled: false

                },
                yAxis: {
                    axisLabel: '',
                    tickFormat: function (d) {
                        return "";
                    }
                }
            }
        };

        ctrl.topOnlineTimeData = [
            {
                key: "Players",
                color: colorSetChats[0],
                values: []
            }
        ];

        ctrl.init = function () {
            console.log("Init");
            ctrl.interval = $interval(ctrl.updateCaption, 500, 0, true, true);
            $http.get("timeInfo.json?" + $httpParamSerializer({count: 10})).success(function (result) {
                console.log("Got Result", result);
                ctrl.rawTopPlayers = result.topTotalTime;
                ctrl.topOnlineTimeData[0].values.length = 0;
                [].push.apply(ctrl.topOnlineTimeData[0].values,
                    result.topTotalTime.map(function (obj) {
                        return {"label": obj.player.name, "value": obj.time};
                    }));
                ctrl.totalTime = result.totalTime;
                ctrl.updateCaption();
            }.bind(this));
        };

        ctrl.updateCaption = function () {
            ctrl.totalTime += 0.5 * StatusService.players.length;
            ctrl.totalText = moment.duration(ctrl.totalTime, "seconds").format("Y [years] M [months] D[ days] H [hours] m [minutes and] s [seconds!]");
            ctrl.totalWage = d3.format(',.2f')(ctrl.totalTime / 3600 * 8.92);
        };

        ctrl.$onDestroy = function () {
            $interval.cancel(ctrl.interval);
        };


        ctrl.init();

    }
});
