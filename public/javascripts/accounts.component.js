UtAdmin.component('accounts', {
    templateUrl: '/accounts-template.html',


    controller: function ($http, $httpParamSerializer) {
        var ctrl = this;

        ctrl.reset = function () {
            ctrl.new = {
                user: "",
                rank: 2,
                password: "Super Secret Password (Don't use 'password')",
                b3Id: 0
            };
            ctrl.edit = false;
        };

        ctrl.addOrEdit = function (account) {
            /*   user:String, password: String , rank:Int, b3Id:Int */
            if (account != undefined) {
                ctrl.edit = true;
                ctrl.new.user = account.name;
                ctrl.new.rank = account.rank;
                ctrl.new.b3Id = account.b3Id;
                ctrl.new.password = "Can't edit password!";
            }
        };

        ctrl.confirm = function () {
            $http.post("/addUser?" + $httpParamSerializer(ctrl.new)).then(function () {
                Materialize.toast('Done!', 1500);
                ctrl.update();
            });
        };

        ctrl.update = function () {
            ctrl.reset();
            return $http({method: 'GET', url: '/accounts.json'}).then(function successCallback(response) {
                ctrl.accounts = response.data
            });
        };

        ctrl.$onInit = function () {
            ctrl.update();
        };

        ctrl.delete = function (account) {
            vex.dialog.confirm({
                    message: "Are you absolutely sure you want to delete " + account.name + "'s account?",
                    callback: function (value) {
                        if (value) {
                            $http.post("/accountDelete?" + $httpParamSerializer({user: account.name})).then(function () {
                                Materialize.toast('Deleted!', 1500);
                                ctrl.update();
                            });
                        }
                    }
                }
            );
        };

        ctrl.changeYourPassword = function(){
            vex.dialog.prompt({
                message: 'Enter your new password!',
                placeholder: 'Password',
                callback: function (value) {
                    if(value!=undefined){
                        $http.post("/changePassword?" + $httpParamSerializer({password: value})).then(function () {
                            Materialize.toast('Changed!', 1500);
                        });
                    }
                }
            })
        }
    }
})
;