/* ============================================================
   countdown.js — MM:SS countdown timer for the payment page.
   Reads the initial time from the text content of #countdown
   (format "MM:SS") and counts down to zero.
   ============================================================ */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var el = document.getElementById('countdown');
        if (!el) return;

        var parts = el.textContent.trim().split(':');
        if (parts.length !== 2) return;

        var secs = parseInt(parts[0], 10) * 60 + parseInt(parts[1], 10);
        if (isNaN(secs)) return;

        var interval = setInterval(function () {
            if (secs <= 0) {
                clearInterval(interval);
                el.textContent = '00:00';
                return;
            }
            secs--;
            var m = String(Math.floor(secs / 60)).padStart(2, '0');
            var s = String(secs % 60).padStart(2, '0');
            el.textContent = m + ':' + s;
        }, 1000);
    });
})();
