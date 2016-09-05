var adminAdder = {
    addAdmin: function () {
        bootbox.dialog({
                title: "Add a new admin",
                closeButton: true,
                message: '<form role="form">' +
                ' <div class="form-group">' +
                ' <label for="usernameTf">Username (lowercase only):</label>' +
                '  <input type="text" class="form-control" id="usernameTf" placeholder="Username">' +
                '<label for="b3idTf">B3Id (online integers):</label>' +
                ' <input type="text" class="form-control" id="b3idTf" placeholder="42">' +
                '  <label for="passwordTf">Password:</label>' +
                '<input type="text" class="form-control" id="passwordTf" placeholder="Password">' +
                '<label for="rankSelect">Rank:</label>' +
                ' <select class="form-control" id="rankSelect">' +
                '<option value="0">God</option>' +
                '<option value="1">Admin</option>' +
                '<option value="2">Mod</option>' +
                '</select>' +
                '</div>' +
                '</form>',
                buttons: {
                    success: {
                        label: "Add",
                        className: "btn-success",
                        callback: function () {
                            var username = $("#usernameTf");
                            var password = $("#passwordTf");
                            var b3Id = $("#b3idTf");
                            var rank = $("#rankSelect");
                            $.ajax(jsRoutes.controllers.Administrator.addUser(username.val().toLowerCase(), password.val(),rank.val(), b3Id.val())).done(
                                function () {
                                    swal({title: "Done!", text: "Added user!", type: "success"}, function () {
                                        username.val("");
                                        rank.val("");
                                        password.val("");
                                        location.reload();
                                    });
                                }
                            ).fail(
                                function (data) {
                                    swal({
                                        title: "Couldn't add user!",
                                        text: "Unknown Error" + data.responseText,
                                        type: "success"
                                    }, function () {
                                        username.val("");
                                        rank.val("");
                                        password.val("");
                                        location.reload();
                                    });
                                }
                            );
                        }
                    }
                }
            }
        );
    },

    editAdmin: function (name, rank, b3Id) {
        var god = '<option value="0">God</option>';
        var admin = '<option value="1">Admin</option>';
        var mod = '<option value="2">Mod</option>';

        if (rank == 0) {
            god = '<option value="0" selected="selected">God</option>';
        } else if (rank == 1) {
            admin = '<option value="1" selected="selected">Admin</option>';
        } else if (rank == 2) {
            mod = '<option value="2" selected="selected">Mod</option>';
        }


        bootbox.dialog({
                title: "Edit " + name,
                closeButton: true,
                message: '<form role="form">' +
                ' <div class="form-group">' +
                ' <label for="usernameTf">Username (lowercase only):</label>' +
                '  <input type="text" class="form-control" id="usernameTf" readonly value="' + name + '">' +
                '<label for="b3idTf">B3Id (online integers):</label>' +
                ' <input type="text" class="form-control" id="b3idTf" value="' + b3Id + '">' +
                '<label for="rankSelect">Rank:</label>' +
                '<select class="form-control" id="rankSelect">' +
                god + admin + mod +
                '</select>' +
                '</div>' +
                '</form>',
                buttons: {
                    success: {
                        label: "Save",
                        className: "btn-success",
                        callback: function () {
                            var username = $("#usernameTf");
                            var b3Id = $("#b3idTf");
                            var rank = $("#rankSelect");
                            $.ajax(jsRoutes.controllers.Administrator.addUser(username.val().toLowerCase(), "",rank.val(), b3Id.val())).done(
                                function () {
                                    swal({title: "Done!", text: "Edited user!", type: "success"}, function () {
                                        username.val("");
                                        rank.val("");
                                        location.reload();
                                    });
                                }
                            ).fail(
                                function (data) {
                                    swal({
                                        title: "Couldn't edit user!",
                                        text: "Unknown Error" + data.responseText,
                                        type: "success"
                                    }, function () {
                                        username.val("");
                                        rank.val("");
                                        location.reload();
                                    });
                                }
                            );
                        }
                    }
                }
            }
        );
    },

    deleteAdmin: function (user) {
        swal({
                title: 'Are you sure?',
                text: 'You will not be able to restore this user.',
                type: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#DD6B55',
                confirmButtonText: 'Delete!',
                closeOnConfirm: true
            },
            function () {
                $.get("/accountDelete?user=" + user);
                setTimeout(function () {
                    location.reload();
                }, 2000);
            });
    }
};

$(document).ready(function () {
    $("#addUserBtn").click(function () {
        adminAdder.addAdmin();
    });
});