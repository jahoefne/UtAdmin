var usersearch = {
    send: function () {
        var tf = $(".userSearchTextfield");
        var text = tf.val();
        window.location.replace("/usersearch?name=" + text)
    },

    init: function () {
        $(".userSearchButton").click(
            function () {
                usersearch.send();
            }
        );
        $(document).on("keypress", ".userSearchTextfield", function(e) {
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