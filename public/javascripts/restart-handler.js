$(document).ready(function () {
    $("#restartServerButton").click(function () {
        swal({
                title: 'Are you sure?',
                text: 'Restarting will kick all online players!',
                type: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#FFa0a0',
                confirmButtonText: 'Restart',
                closeOnConfirm: false
            },
            function () {
                swal({
                        title: 'Really?',
                        text: 'ARE YOU SURE???',
                        type: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#FF5050',
                        confirmButtonText: 'DO IT!!',
                        closeOnConfirm: false
                    },
                    function () {
                        swal({
                                title: 'REALLY REALLY????',
                                text: 'Are you positive?',
                                type: 'warning',
                                showCancelButton: true,
                                confirmButtonColor: '#FF00FF',
                                confirmButtonText: 'YES I AM POSITIVE',
                                closeOnConfirm: true
                            },
                            function () {
                                $.get("/restart-server");
                                console.log("Killing the server!");
                            });
                    });
            });

    });
});


$(document).ready(function () {
    $("#restartB3Button").click(function () {
        swal({
                title: 'Are you sure?',
                text: 'Restarting b3 will take some time - while restarting echelon will not work as expected!',
                type: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#FFa0a0',
                confirmButtonText: 'Restart B3',
                closeOnConfirm: true
            },
            function () {
                $.get("/restart-b3");
                console.log("Killing the server!");
            });
    });
});