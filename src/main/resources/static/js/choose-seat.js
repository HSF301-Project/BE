/* ============================================================
   choose-seat.js — Simple seat toggle for the choose_seat page.
   Handles visual selection state for the two-deck seat map.
   ============================================================ */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.seat-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                if (btn.classList.contains('selected')) {
                    btn.classList.remove('selected', 'bg-secondary', 'text-white', 'scale-105', 'shadow-lg');
                    btn.classList.add('bg-surface-container-highest');
                } else {
                    btn.classList.add('selected', 'bg-secondary', 'text-white', 'scale-105', 'shadow-lg');
                    btn.classList.remove('bg-surface-container-highest');
                }
            });
        });
    });
})();
