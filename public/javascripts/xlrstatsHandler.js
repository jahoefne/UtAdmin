function setXlrStatsVisibility(b3Id, visibility) {
    console.log("FO");
    $.ajax(jsRoutes.controllers.Administrator.setXlrVisibility(b3Id, visibility));
    setTimeout(function () {
        location.reload();
    }, 500);
}

$(document).ready(function () {
    $("#resetStatsBtn").click(function () {
        swal({
                title: 'Are you sure?',
                text: 'Resetting a players stats may also affect other players stats!',
                type: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#FFa0a0',
                confirmButtonText: 'DELETE IT!',
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
                                $.ajax(jsRoutes.controllers.Administrator.resetXlrstats(userId));
                                setTimeout(function () {
                                    location.reload();
                                }, 500);
                            });
                    });
            });
    });
});