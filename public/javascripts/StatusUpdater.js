window.update = true;

var failCount = 0;
function updateStatus() {
    if (!window.update) return;
    $("#updateStatus").show();
    $.get("/online-plain", function (data) {
        $("#statusPanel").html(data);
        jQuery("time.timeago").timeago();

        $('.dropdown-button').dropdown({
                inDuration: 300,
                outDuration: 225,
                constrain_width: false, // Does not change width of dropdown to that of the activator
                hover: true, // Activate on hover
                gutter: 0, // Spacing from edge
                belowOrigin: false, // Displays dropdown below the button
                alignment: 'left' // Displays dropdown with edge aligned to the left of button
            }
        );

        $(".dropdown-content").hover(
            function () {
                window.update = false;
            },
            function () {
                window.update = true;
            }
        );

        window.setTimeout(function () {
            $("#updateStatus").hide()
        }, 1000);
        failCount = 0;
    }).fail(function () {
        failCount += 1;
        if (failCount == 5) {
            console.log("Update failed #" + failCount)
        }
    });
}

$(document).ready(function () {
    updateStatus();
    window.setInterval(updateStatus, 5000);
});
