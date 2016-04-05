function removePunishment(penaltyId){
    $.get("/remove-punishment?penaltyId="+penaltyId);
    var btnIcon = $("#"+penaltyId);
    btnIcon.removeClass();
    btnIcon.addClass("fa fa-spinner fa-spin");
    setTimeout(function() { location.reload(); }, 1500);
}

$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip()
});