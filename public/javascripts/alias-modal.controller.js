$(document).ready(function () {
    $('.modal-trigger').leanModal();
});


UtAdmin.controller('AliasModalCtrl', ['$scope','$http', '$httpParamSerializer', function ($scope,$http, $httpParamSerializer) {

    $scope.name = "Foo";
    $scope.aliases = [];

    $scope.$on("modalForUser", function (event, args) {
        console.log("Modal for user", args);

        $scope.name = args.name;

        $http.get("user-aliases.json?" + $httpParamSerializer({id: args.b3id})).success(function (result) {
            $scope.aliases = result;
            $('#aliasModal').openModal();
        }.bind(this));
    });


    $scope.clanTags = ["<DN>",">DN<","{DN}","|DN|"];

    $scope.isClanTagName = function(name) {
        for (var i = 0; i < $scope.clanTags.length; i++) {
            if(name.toLowerCase().indexOf($scope.clanTags[i].toLowerCase()) != -1){
                return true;
            }
        }
        return false;
    };


    $scope.aliasOrder = function(alias) {

        if($scope.isClanTagName(alias.name)){
            console.log("COntains DN");
            return Number.MIN_SAFE_INTEGER;
        }
        return -alias.last;
    };

}]);
