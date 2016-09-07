UtAdmin.controller('OnlinePlayerCtrl',
    function ($scope, $http, $interval, $timeout, $httpParamSerializer,$rootScope, timeAgoSettings) {
        timeAgoSettings.fullDateAfterSeconds = 60 * 60 * 24;

        $scope.update = function () {
            if($("#status-module").is(":visible")) {
                console.log("Update players");
                $scope.updating = true;
                $http.get("online-players.json").then(OnOnlinePlayersLoaded, OnError);
            }
        };

        var OnOnlinePlayersLoaded = function (response) {
            console.log("got ", response.data.length, "online players");
            $scope.players = response.data;
            $scope.updating = false;
        };

        var OnError = function (response) {
            console.log("Error");
        };

        $scope.init = function(){
            $scope.autoUpdateInterval = 4000;
            $scope.players = [];
            $scope.updating = false;
            $scope.update();
            $interval($scope.update, $scope.autoUpdateInterval, 0, true, true);

        };

        $scope.pm = function (playerSlot, playerName, playerId) {
            vex.dialog.prompt({
                message: 'Send PM to '+playerName,
                placeholder: 'Message',
                buttons: [
                    $.extend({}, vex.dialog.buttons.YES, { text: 'Send!' })
                ],
                callback: function (value) {
                    if (!value) {
                        return console.log('Cancelled')
                    }
                    $.ajax(jsRoutes.controllers.Rcon.privateMessage(playerSlot, value, playerId, playerName));
                    var $toastContent = $('<span>Sent PM to ' + playerName + '</span>');
                    Materialize.toast($toastContent, 1500);
                }
            });
        };

        $scope.punish = function (action, notification) {
            $.ajax(action);
            var $toastContent = $('<span>' + notification + '</span>');
            Materialize.toast($toastContent, 1500);
            $scope.update();
        };

        /**
         * Construct and display the dynamic punish modal
         * @param id server id of the play
         * @param name name of the player (for notifications)
         */
        $scope.punishModal = function (id,name) {
            vex.dialog.open({
                message: 'Punish '+name,
                input: [
                    '<p><input name="punish" type="radio" id="slap" value="slap" checked/><label for="slap">Slap</label></p>',
                    '<p><input name="punish" type="radio" id="kick" value="kick" /><label for="kick">Kick</label></p>',
                    '<p><input name="punish" type="radio" id="nuke" value="nuke" /><label for="nuke">Nuke</label></p>',
                    '<p><input name="punish" type="radio" id="kill" value="kill" /><label for="kill">Kill</label></p>',
                    '<p><input name="punish" type="radio" id="forceRed" value="forceRed"/><label for="forceRed">Force Red</label></p>',
                    '<p><input name="punish" type="radio" id="forceBlue" value="forceBlue"/><label for="forceBlue">Force Blue</label></p>',
                    '<p><input name="punish" type="radio" id="forceSpec" value="forceSpec"/><label for="forceSpec">Force Spec</label></p>',
                    '<p><input name="punish" type="radio" id="startServerDemo" value="startServerDemo"/><label for="startServerDemo">Start Server Demo</label></p>',
                    '<p><input name="punish" type="radio" id="stopServerDemo" value="stopServerDemo"/><label for="stopServerDemo">Stop Server Demo</label></p>'
                ].join(''),
                buttons: [
                    $.extend({}, vex.dialog.buttons.YES, { text: 'Punish' })
                ],
                callback: function (data) {
                    if (!data) {
                        return console.log('Cancelled')
                    }
                    switch (data.punish) {
                        case "slap": $scope.punish(jsRoutes.controllers.Rcon.slap(id, name),'Slapped '+name );break;
                        case "kick":$scope.punish(jsRoutes.controllers.Rcon.kick(id, name),'Kicked '+name );break;
                        case "nuke": $scope.punish(jsRoutes.controllers.Rcon.nuke(id, name),'Nuked '+name );break;
                        case "kill": $scope.punish(jsRoutes.controllers.Rcon.kill(id, name),'Killed '+name );break;
                        case "forceRed": $scope.punish(jsRoutes.controllers.Rcon.forceRed(id, name),'Forced red '+name );break;
                        case "forceBlue": $scope.punish(jsRoutes.controllers.Rcon.forceBlue(id, name),'Forced blue '+name );break;
                        case "forceSpec": $scope.punish(jsRoutes.controllers.Rcon.forceSpec(id, name),'Forced spec '+name ); break;
                        case "startServerDemo": $scope.punish(jsRoutes.controllers.Rcon.startServerDemo(id, name),'Started Server Demo for '+name );break;
                        case "stopServerDemo": $scope.punish(jsRoutes.controllers.Rcon.stopServerDemo(id, name),'Stoped Server Demo for '+name ); break;
                    }
                }
            })

        };


        $scope.restartServer = function(){
            vex.dialog.confirm({
                message: 'Resart server? This will kick all online players!',
                callback: function (value) {
                    if (value) {
                        $scope.updating = true;
                        $.get("/restart-server").done(function () {
                            $scope.update();
                            Materialize.toast('Resarted Server!', 1500);
                        });
                    }
                }
            });
        };

        $scope.restartB3 = function(){
            vex.dialog.confirm({
                message: 'Resart b3? It can take up to 2 minutes before UtAdmin is completely functional afterwards.',
                callback: function (value) {
                    if (value) {
                        $scope.updating = true;
                        $.get("/restart-b3").done(function () {
                            $scope.update();
                            Materialize.toast('Resarted B3!', 1500);
                        });
                    }
                }
            });
        };

        /***
         * Events Sending
         */
        $scope.showChatForUser = function(id) {
            $rootScope.$broadcast('show-chat-for-user',id);
        };

        $scope.showPenaltiesForUser = function(id) {
            $rootScope.$broadcast('show-penalties-for-user',id);
        };
    }
);