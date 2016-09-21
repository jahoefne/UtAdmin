angular.module('UtAdmin').component('topOnlineGraph', {
    template:
        '<div align="center">All <u>players combined</u> played for a total of <b><u>{{$ctrl.totalText}}</u></b>' +
        '<br>That\' s <b>{{$ctrl.totalWage}}$</b> worth of minimum wage work in the US.</div>'+
    '<nvd3 options="$ctrl.topOnlineTimeOption" id="historicalBarChart" data="$ctrl.topOnlineTimeData"></nvd3><br>',

    controller: function ($http, $filter, $interval, StatusService, $state, $httpParamSerializer) {
        var ctrl = this;
        ctrl.totalTime = 0;
        ctrl.totalText = "";
        ctrl.totalWage = 0;
        ctrl.rawTopPlayers = {};
        ctrl.topOnlineTimeOption = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 800,
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
                tooltip: {
                    valueFormatter: function (d, i) {
                        //console.log(d, i);
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
            ctrl.interval = $interval(ctrl.updateCaption, 500, 0, true, true);

            $http.get("topTotalTime.json?" + $httpParamSerializer({count: 25})).success(function (result) {
                ctrl.rawTopPlayers = result;
                ctrl.topOnlineTimeData[0].values.length = 0;
                [].push.apply(ctrl.topOnlineTimeData[0].values,
                    result.map(function (obj) {
                        return {"label": obj.player.name, "value": obj.time};
                    }));
            }.bind(this));


            $http.get("totalTime.json").success(function (result) {
                // console.log(result);
                ctrl.totalTime = result;
                ctrl.updateCaption();

            }.bind(this));

        };

        ctrl.updateCaption = function () {
            //   console.log("update");
            ctrl.totalTime += 0.5 * StatusService.players.length;
            ctrl.totalText = moment.duration(ctrl.totalTime, "seconds").format("Y [years] M [months] D[ days] H [hours] m [minutes and] s [seconds!]");
            ctrl.totalWage = d3.format(',.2f')(ctrl.totalTime / 3600 * 8.92);
        };

        ctrl.$onDestroy = function () {
            //console.log("Destroying Chat Module");
            $interval.cancel(ctrl.interval);
        };


        ctrl.init();

    }
});
