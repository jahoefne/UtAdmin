$(document).ready(function() {
    $("#PunishButton").click(function () {
       console.log("Punish!");
       var penalty = $('#penaltyTypeSelect').val();
       var reason = $("#reasonTf").val();
       if(penalty=="Notice" || penalty == "Ban"){
           $.get("/add-punishment?userId="+userId+"&penalty="+penalty+"&reason="+reason);
       }else {
           var duration = $('#durationSelect').val();
           $.get("/add-punishment?userId=" + userId + "&reason="+ reason + "&penalty=" + penalty + "&duration=" +duration);
       }

        $("#PunishButton").text("");
        $("#PunishButton").addClass("fa fa-spinner fa-spin");
        setTimeout(function() { location.reload(); }, 2000);
    });

    $('#penaltyTypeSelect').change(function () {
        var val = $('#penaltyTypeSelect').val();
        console.log(val);
        if(val == "Notice" || val == "Ban"){
            $("#durationSelect").hide();
        }else{
            $("#durationSelect").show();
}
});
});