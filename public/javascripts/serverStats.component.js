var colorSetChats = ['#AF0400', '#067700', '#1F0765'];


UtAdmin.component('serverStats', {
        templateUrl: '/server-stats.html',

        controller: function ($http, $filter) {
            ctrl = this;


            /** Total Kills Pie Chart */
            ctrl.pieChartOptions = {
                chart: {
                    type: 'pieChart',
                    height: 450,
                    x: function (d) {
                        return d.key;
                    },
                    y: function (d) {
                        return d.y;
                    },
                    valueFormat: function (d) {
                        return d3.format(',.0f')(d) + "&nbsp;&nbsp;&nbsp;";
                    },
                    duration: 500,
                    growOnHover: true,
                    labelType: "percent",
                    labelThreshold: 0.01,
                    labelSunbeamLayout: true,
                    color: colorSetChats,
                    legend: {
                        margin: {
                            top: 5,
                            right: 35,
                            bottom: 5,
                            left: 0
                        }
                    }
                }
            };

            ctrl.pieChartData = [
                {key: "Loading……", y: 1},
            ];

            ctrl.init = function () {

                /** Total Death Data */
                $http.get("totalDeaths.json").success(function (result) {
                    console.log("got", result);
                    ctrl.pieChartData[0] = {key: "Kills", y: result.kills};
                    console.log(result.teamKills);
                    //ctrl.pieChartData[1] = {key: "Team", y: result.teamKills};
                    ctrl.pieChartData[1] = {key: "Suicides", y: result.suicides};
                    console.log(ctrl.pieChartData);

                }.bind(this));

            };
            ctrl.init();
        }
    }
);