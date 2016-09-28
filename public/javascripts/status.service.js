'use strict';

angular.module('UtAdmin').factory('StatusService', function ($http, $interval) {

    var ctrl = this;
    this.players = [];
    var updateInterval = 4500;

    this.update = function () {
        // console.log("Updating Status Service");
        $http.get("online-players.json").success(function (result) {
            this.players.length = 0;
            [].push.apply(this.players, result);
            console.log(this.players.length);
        }.bind(this));

    }.bind(this);

    $interval(this.update, updateInterval);

    // TODO solve with promises and hashmap
    this.isOnline = function (id) {
        if (this.players == undefined) {
            return {online: false};
        }
        for (var i = 0; i < this.players.length; i++) {
            if (this.players[i].id == id) {
                return {online: true, slot: this.players[i].serverId}
            }
        }
        return {online: false};
    };

    this.pm = function (playerSlot, playerName, playerId) {
        vex.dialog.prompt({
            message: 'Send PM to ' + playerName,
            placeholder: 'Message',
            buttons: [
                $.extend({}, vex.dialog.buttons.YES, {text: 'Send!'})
            ],
            callback: function (value) {
                if (value) {
                    $.ajax(jsRoutes.controllers.Rcon.privateMessage(playerSlot, value, playerId, playerName));
                    var $toastContent = $('<span>Sent PM to ' + playerName + '</span>');
                    Materialize.toast($toastContent, 1500);
                }
            }
        });
    };

    this.punish = function (action, notification) {
        $.ajax(action);
        var $toastContent = $('<span>' + notification + '</span>');
        Materialize.toast($toastContent, 1500);
        this.update();
    };

    this.punishModal = function (id, name) {
        vex.dialog.open({
            message: 'Punish ' + name,
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
                $.extend({}, vex.dialog.buttons.YES, {text: 'Punish'})
            ],
            callback: function (data) {
                if (!data) {
                    return console.log('Cancelled')
                }
                switch (data.punish) {
                    case "slap":
                        ctrl.punish(jsRoutes.controllers.Rcon.slap(id, name), 'Slapped ' + name);
                        break;
                    case "kick":
                        ctrl.punish(jsRoutes.controllers.Rcon.kick(id, name), 'Kicked ' + name);
                        break;
                    case "nuke":
                        ctrl.punish(jsRoutes.controllers.Rcon.nuke(id, name), 'Nuked ' + name);
                        break;
                    case "kill":
                        ctrl.punish(jsRoutes.controllers.Rcon.kill(id, name), 'Killed ' + name);
                        break;
                    case "forceRed":
                        ctrl.punish(jsRoutes.controllers.Rcon.forceRed(id, name), 'Forced red ' + name);
                        break;
                    case "forceBlue":
                        ctrl.punish(jsRoutes.controllers.Rcon.forceBlue(id, name), 'Forced blue ' + name);
                        break;
                    case "forceSpec":
                        ctrl.punish(jsRoutes.controllers.Rcon.forceSpec(id, name), 'Forced spec ' + name);
                        break;
                    case "startServerDemo":
                        ctrl.punish(jsRoutes.controllers.Rcon.startServerDemo(id, name), 'Started Server Demo for ' + name);
                        break;
                    case "stopServerDemo":
                        ctrl.punish(jsRoutes.controllers.Rcon.stopServerDemo(id, name), 'Stoped Server Demo for ' + name);
                        break;
                }
            }
        })

    };

    this.restartServer = function () {
        vex.dialog.confirm({
            message: 'Restart server? This will kick all online players!',
            callback: function (value) {
                if (value) {
                    $.get("/restart-server").done(function () {
                        Materialize.toast('Restarted Server!', 1500);
                    });
                }
            }
        });
    };

    this.restartB3 = function () {
        vex.dialog.confirm({
            message: 'Restart b3? It can take up to 2 minutes before UtAdmin is completely functional afterwards.',
            callback: function (value) {
                if (value) {
                    $.get("/restart-b3").done(function () {
                        Materialize.toast('Restarted B3!', 1500);
                    });
                }
            }
        });
    };
    return this;
});