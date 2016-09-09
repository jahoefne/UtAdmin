/**
 * Main Router for the app
 */
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
        component: 'chat'
    });

    $stateProvider.state('chat.user', {
        url: '?user=:b3id',
        component: 'chat'
    });

    $stateProvider.state('penalties', {
        url: '/penalties',
        component: 'penalties'
    });

    $stateProvider.state('penalties.user', {
        url: '?user=:b3id',
        component: 'penalties'
    });


    $stateProvider.state('users', {
        url: '/users',
        component: 'users'
    });

    $stateProvider.state('user', {
        url: '/user?user=:b3id',
        component: 'user',
        resolve: {
            user:  function($http, $stateParams){
                return $http({method: 'GET', url: '/user.json?id='+ $stateParams.b3id}).then(function successCallback(response) {
                    return response.data;
                });
            }

        }
    });


    $urlRouterProvider.otherwise('/')
});