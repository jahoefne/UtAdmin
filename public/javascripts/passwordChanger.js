$(document).ready(function () {
    $("#pwdChangeBtn").click(function () {
        swal({
                title: 'Change Password?',
                text: 'Are you sure that you want to change the password?',
                type: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#DD6B55',
                confirmButtonText: 'Change!',
                closeOnConfirm: true
            },
            function () {
                $.get("/changePassword?password=" + $("#passwordTf").val())
                setTimeout(function () {
                    location.reload();
                }, 2000);

            });
    });
});