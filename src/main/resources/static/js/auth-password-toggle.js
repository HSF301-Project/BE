document.addEventListener("DOMContentLoaded", function () {
    var toggles = document.querySelectorAll("[data-password-toggle]");

    toggles.forEach(function (toggleButton) {
        var targetId = toggleButton.getAttribute("data-target");
        if (!targetId) {
            return;
        }

        var targetInput = document.getElementById(targetId);
        if (!targetInput) {
            return;
        }

        var icon = toggleButton.querySelector(".material-symbols-outlined");

        toggleButton.addEventListener("click", function () {
            var isHidden = targetInput.type === "password";
            targetInput.type = isHidden ? "text" : "password";

            if (icon) {
                icon.textContent = isHidden ? "visibility_off" : "visibility";
            }

            toggleButton.setAttribute("aria-label", isHidden ? "An mat khau" : "Hien mat khau");
            toggleButton.setAttribute("title", isHidden ? "An mat khau" : "Hien mat khau");
        });
    });
});

