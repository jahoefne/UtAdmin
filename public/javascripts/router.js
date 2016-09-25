/**
 * Main Router for the app
 */
$.fn.extend({
    animateCss: function (animationName) {
        var animationEnd = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
        this.addClass('animated ' + animationName).one(animationEnd, function () {
            $(this).removeClass('animated ' + animationName);
        });
    }
});

UtAdmin.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('status', {
        url: '/',
        component: 'status',
        resolve: {
            players: function (StatusService) {
                StatusService.update();
                return StatusService.players;
            }
        }
    });

    $stateProvider.state('chat', {
        url: '/chat',
        component: 'chat',
        resolve:{
            autoUpdate: function(){
                return false;
            },
            small: function(){
                return true;
            }
        }
    });

    $stateProvider.state('chat.user', {
        url: '?user=:b3id',
        component: 'chat',
        resolve:{
            autoUpdate: function(){
                return false;
            },
            small: function(){
                return true;
            }
        }
    });

    $stateProvider.state('penalties', {
        url: '/penalties',
        component: 'penalties',
    });

    $stateProvider.state('penalties.user', {
        url: '?user=:b3id',
        component: 'penalties'
    });


    $stateProvider.state('users', {
        url: '/users',
        component: 'users',
    });

    $stateProvider.state('accounts', {
        url: '/accounts',
        component: 'accounts'
    });

    $stateProvider.state('serverStats', {
        url: '/serverStats',
        component: 'serverStats'
    });

    $stateProvider.state('user', {
        url: '/user/:b3id',
        component: 'user',
        resolve: {
            user: function ($http, $stateParams) {
                return $http({method: 'GET', url: '/user.json?id=' + $stateParams.b3id}).then(function successCallback(response) {
                    return response.data;
                });
            }

        }
    });

    $urlRouterProvider.otherwise('/')
});