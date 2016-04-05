var talkback = {
    send: function () {
        var tf = $("#talkback");
        var text = tf.val();
        tf.val("");
        $.ajax(jsRoutes.controllers.Rcon.say(text));
    },


    init: function () {
        $("#say").click(
            function () {
                talkback.send();
            }
        );
        $(document).on("keypress", "#talkback", function(e) {
            if (e.which == 13) {
                talkback.send();
            }
        });
    }
};

function updateChatlog(){
    $("#updateChat").show();
    $.get( "/chatlog-plain?count="+count+"&offset="+offset+"&includeRadio="+$("#includeRadioCheckbox").prop("checked"), function( data ) {
        $( "#chat-div" ).html( data );
        window.setTimeout( function(){ $("#updateChat").hide()}, 1000);
    });
};

$(document).ready(
    function () {
        talkback.init();
        window.setInterval(updateChatlog, 3000);
    }
);