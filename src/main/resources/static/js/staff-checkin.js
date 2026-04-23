/* ============================================================
   staff-checkin.js — Staff check-in seat map interaction logic
   ============================================================ */
(function () {
    'use strict';

    let currentTicketCode = '';
    let currentButton = null;

    function openCheckInModal(button) {
        currentButton = button;
        currentTicketCode = button.getAttribute('data-ticket-code');
        const passengerName = button.getAttribute('data-passenger-name');
        const seatNumber = button.getAttribute('data-seat-number');
        const isChecked = button.getAttribute('data-is-checked') === 'true';

        document.getElementById('modalSeatNumber').innerText = seatNumber;
        document.getElementById('modalPassengerName').innerText = passengerName;
        document.getElementById('modalTicketCodeInput').value = '';
        document.getElementById('modalErrorMessage').classList.add('hidden');
        
        const formGroup = document.getElementById('checkInFormGroup');
        const statusBox = document.getElementById('checkedStatusBox');
        const confirmBtn = document.getElementById('modalConfirmBtn');
        const cancelBtn = document.getElementById('modalCancelBtn');

        if (isChecked) {
            formGroup.classList.add('hidden');
            statusBox.classList.remove('hidden');
            confirmBtn.classList.add('hidden');
            cancelBtn.classList.remove('hidden');
        } else {
            formGroup.classList.remove('hidden');
            statusBox.classList.add('hidden');
            confirmBtn.classList.remove('hidden');
            cancelBtn.classList.add('hidden');
        }

        document.getElementById('checkInModal').classList.remove('hidden');
        document.getElementById('modalTicketCodeInput').focus();
    }

    window.closeModal = function () {
        document.getElementById('checkInModal').classList.add('hidden');
    };

    window.confirmCheckIn = function () {
        const inputCode = document.getElementById('modalTicketCodeInput').value.trim();
        const isChecked = currentButton.getAttribute('data-is-checked') === 'true';

        if (!isChecked && inputCode !== currentTicketCode) {
            document.getElementById('modalErrorMessage').classList.remove('hidden');
            return;
        }

        toggleCheckIn(currentTicketCode, currentButton);
        window.closeModal();
    };

    // Event delegation for seat button clicks
    document.addEventListener('click', function (e) {
        const btn = e.target.closest('.seat-button');
        if (btn) {
            openCheckInModal(btn);
        }
    });

    function toggleCheckIn(ticketCode, button) {
        if (!ticketCode || ticketCode === 'null') return;

        const config = window.StaffCheckinConfig || {};
        const tripId = config.tripId || '0';
        const url = '/staff/operations/trips/' + tripId + '/checkin/toggle?ticketCode=' + ticketCode;

        const csrfToken = config.csrfToken || '';
        const csrfHeader = config.csrfHeader || 'X-CSRF-TOKEN';

        button.classList.add('animate-pulse');

        fetch(url, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            }
        })
            .then(function (response) { return response.json(); })
            .then(function (data) {
                button.classList.remove('animate-pulse');
                if (data.success) {
                    var checkedCountEl = document.getElementById('checkedCount');
                    var count = parseInt(checkedCountEl.innerText);

                    if (data.newStatus === 'CHECKED_IN') {
                        button.classList.remove('seat-booked');
                        button.classList.add('seat-checked');
                        button.setAttribute('data-is-checked', 'true');
                        count++;
                    } else {
                        button.classList.remove('seat-checked');
                        button.classList.add('seat-booked');
                        button.setAttribute('data-is-checked', 'false');
                        count--;
                    }
                    checkedCountEl.innerText = count;
                } else {
                    alert('Lỗi: ' + data.message);
                }
            })
            .catch(function (err) {
                button.classList.remove('animate-pulse');
                console.error(err);
                alert('Đã có lỗi xảy ra!');
            });
    }
})();
