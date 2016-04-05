var usersearch = {
    send: function () {
        var tf = $("#usersearchTextfield");
        var text = tf.val();
        window.location.replace("/usersearch?name=" + text)
    },

    init: function () {
        $("#usersearchButton").click(
            function () {
                usersearch.send();
            }
        );
        $(document).on("keypress", "#usersearchTextfield", function(e) {
            if (e.which == 13) {
                usersearch.send();
            }
        });
    }
};

$(document).ready(
    function () {
        usersearch.init();
    }
);