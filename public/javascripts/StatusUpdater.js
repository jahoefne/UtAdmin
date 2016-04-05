window.update = true;

var failCount = 0;

function updateStatus() {
    if (window.update) {
        $("#updateStatus").show();
        $.get("/online-plain", function (data) {
            $("#statusPanel").html(data);
            $(".dropdown-menu-container").hover(
                function () {
                    window.update = false;
                }, function () {
                    window.update = true;
                }
            );
            window.setTimeout(function () {
                $("#updateStatus").hide()
            }, 1000);
        }).fail(function () {
            failCount += 1;
            if (failCount == 5) {
                swal({
                    title: "Could not load update from server",
                    text: "Maybe your session timed out, please try reloading the page",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Reload Page!",
                    closeOnConfirm: false
                }, function () {
                    window.location.reload();
                });
            }
        });
    }
}

$(document).ready(function () {
    updateStatus();
    window.setInterval(updateStatus, 2500);
});