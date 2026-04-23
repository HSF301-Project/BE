/* ============================================================
   staff-schedule.js — Trip detail modal logic for staff schedule
   ============================================================ */
(function () {
    'use strict';

    window.openTripDetail = function (trip) {
        document.getElementById('modalTripRoute').innerText = trip.origin + ' → ' + trip.destination;

        var statusEl = document.getElementById('modalTripStatus');
        statusEl.innerText = trip.status;
        statusEl.className = 'px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider ';
        if (trip.status === 'SẮP CHẠY') statusEl.classList.add('bg-blue-50', 'text-blue-600');
        else if (trip.status === 'ĐANG CHẠY') statusEl.classList.add('bg-amber-50', 'text-amber-600');
        else statusEl.classList.add('bg-emerald-50', 'text-emerald-600');

        document.getElementById('modalTripCoach').innerHTML = '<span class="text-primary">' + trip.plate + '</span> (' + trip.coachType + ')';
        document.getElementById('modalTripStaff').innerHTML = 'TX: ' + trip.driverName + '<br>PX: ' + trip.assistantName;
        document.getElementById('modalTripDeparture').innerText = trip.actualDeparture;
        document.getElementById('modalTripArrival').innerText = trip.actualArrival;
        document.getElementById('modalTripPassenger').innerText = trip.checkedInCount + ' / ' + trip.passengerCount + ' khách đã lên xe';

        document.getElementById('tripDetailModal').classList.remove('hidden');
    };

    window.closeTripModal = function () {
        document.getElementById('tripDetailModal').classList.add('hidden');
    };
})();
