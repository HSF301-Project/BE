/* ============================================================
   seat-selection.js — Interactive seat selection and
   passenger info form for the booking page
   ============================================================ */
(function () {
    'use strict';

    // ── State ─────────────────────────────────────────────────
    let selectedSeats = []; // [{ seatId, deck }]

    // ── DOM helpers ───────────────────────────────────────────
    function $(sel) { return document.querySelector(sel); }
    function $$(sel) { return document.querySelectorAll(sel); }

    // ── Seat click handler ────────────────────────────────────
    function handleSeatClick(btn) {
        const seatId   = btn.dataset.seatId;
        const deck     = btn.dataset.deck;       // "lower" | "upper"
        const status   = btn.dataset.status;     // "AVAILABLE" | "BOOKED"

        if (status === 'BOOKED') return; // booked seats are not clickable

        const idx = selectedSeats.findIndex(s => s.seatId === seatId);

        if (idx >= 0) {
            // Deselect seat
            selectedSeats = selectedSeats.filter(s => s.seatId !== seatId);
            btn.classList.remove('bg-secondary', 'text-white');
            btn.classList.add('bg-[#AACDDC]');
        } else {
            // Select seat
            selectedSeats.push({ seatId, deck });
            btn.classList.add('bg-secondary', 'text-white');
            btn.classList.remove('bg-[#AACDDC]');
        }

        refreshPassengerForms();
        refreshSummary();
    }

    function syncPassengerNameCopies(value) {
        $$('.passenger-name-copy').forEach(inp => {
            inp.value = value;
        });
    }

    // ── Build passenger form rows dynamically ─────────────────
    function refreshPassengerForms() {
        const container = $('#passenger-forms');
        if (!container) return;

        // Keep the typed name before re-render
        const existingPrimaryInput = document.getElementById('primary-passenger-name');
        const existingName = existingPrimaryInput ? existingPrimaryInput.value : '';

        container.innerHTML = '';

        if (selectedSeats.length > 0) {
            const primarySeat = selectedSeats[0];
            const primaryDeckLabel = primarySeat.deck === 'lower' ? 'Tầng dưới' : 'Tầng trên';
            const primaryBorderColor = primarySeat.deck === 'lower' ? 'border-primary' : 'border-secondary';
            const primaryBadgeColor = primarySeat.deck === 'lower' ? 'text-primary' : 'text-secondary';
            const selectedSeatLabels = selectedSeats
                .map(seat => `${seat.seatId} (${seat.deck === 'lower' ? 'Tầng dưới' : 'Tầng trên'})`)
                .join(', ');

            const wrapper = document.createElement('div');
            wrapper.className = `p-6 bg-surface-container-lowest rounded-2xl shadow-[0_12px_32px_rgba(26,27,33,0.06)] border-l-4 ${primaryBorderColor}`;
            wrapper.innerHTML = `
                <div class="flex flex-col md:flex-row md:items-center gap-6">
                    <div class="flex-shrink-0 w-12 h-12 bg-primary-fixed flex items-center justify-center rounded-full ${primaryBadgeColor} font-bold">1</div>
                    <div class="flex-grow space-y-4">
                        <div class="space-y-1">
                            <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Ghế đã chọn</label>
                            <div class="flex items-center ${primaryBadgeColor} font-bold">
                                <span class="material-symbols-outlined mr-1">event_seat</span>
                                ${selectedSeatLabels}
                            </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 items-start">
                            <div class="space-y-1">
                                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Ghế đại diện</label>
                                <div class="flex items-center ${primaryBadgeColor} font-bold">
                                    <span class="material-symbols-outlined mr-1">event_seat</span>
                                    ${primarySeat.seatId} (${primaryDeckLabel})
                                </div>
                                <input type="hidden" name="passengers[0].seatId" value="${primarySeat.seatId}"/>
                                <input type="hidden" name="passengers[0].deck" value="${primarySeat.deck}"/>
                                <input type="hidden" name="passengers[0].seatLabel" value="${primarySeat.seatId}"/>
                            </div>
                            <div class="space-y-1">
                                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest" for="primary-passenger-name">Họ và tên</label>
                                <input
                                    id="primary-passenger-name"
                                    class="passenger-name-input w-full bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-surface-tint p-3 text-on-surface placeholder:text-outline"
                                    type="text"
                                    name="passengers[0].fullName"
                                    value="${existingName}"
                                    placeholder="Ví dụ: Nguyễn Văn A"
                                    required
                                />
                                <p class="text-xs text-on-surface-variant">Tên này sẽ được áp dụng cho toàn bộ ghế đã chọn.</p>
                            </div>
                        </div>

                        <div class="hidden" id="passenger-copies">
                            ${selectedSeats.slice(1).map((seat, i) => {
                                const index = i + 1;
                                return `
                                    <input type="hidden" name="passengers[${index}].seatId" value="${seat.seatId}"/>
                                    <input type="hidden" name="passengers[${index}].deck" value="${seat.deck}"/>
                                    <input type="hidden" name="passengers[${index}].seatLabel" value="${seat.seatId}"/>
                                    <input type="hidden" class="passenger-name-copy" name="passengers[${index}].fullName" value="${existingName}"/>
                                `;
                            }).join('')}
                        </div>
                    </div>
                </div>`;
            container.appendChild(wrapper);

            const primaryInput = $('#primary-passenger-name');
            if (primaryInput) {
                primaryInput.addEventListener('input', () => syncPassengerNameCopies(primaryInput.value));
                syncPassengerNameCopies(primaryInput.value);
            }
        }

        // Show/hide empty state
        const emptyMsg = $('#passenger-empty-msg');
        if (emptyMsg) emptyMsg.style.display = selectedSeats.length === 0 ? 'block' : 'none';

        // Update seat count badge
        const countBadge = $('#selected-count');
        if (countBadge) countBadge.textContent = selectedSeats.length;
    }

    // ── Refresh order summary sidebar ─────────────────────────
    function refreshSummary() {
        const unitPriceEl = $('#unit-price-value');
        const unitPrice   = unitPriceEl ? parseInt(unitPriceEl.dataset.price || '0') : 0;
        const totalEl     = $('#summary-total');
        const seatIdsEl   = $('#summary-seat-ids');
        const calculationEl = $('#summary-calculation');

        if (seatIdsEl) {
            if (selectedSeats.length > 0) {
                seatIdsEl.textContent = selectedSeats.map(s => s.seatId).join(', ');
                seatIdsEl.classList.remove('text-on-surface-variant');
                seatIdsEl.classList.add('text-primary');
            } else {
                seatIdsEl.textContent = 'Chưa chọn ghế';
                seatIdsEl.classList.add('text-on-surface-variant');
                seatIdsEl.classList.remove('text-primary');
            }
        }

        const total = unitPrice * selectedSeats.length;
        if (totalEl) {
            totalEl.textContent = total.toLocaleString('vi-VN') + 'đ';
        }

        if (calculationEl) {
            if (selectedSeats.length > 0) {
                const formattedPrice = unitPrice.toLocaleString('vi-VN');
                calculationEl.textContent = `${formattedPrice}đ x ${selectedSeats.length}`;
            } else {
                calculationEl.textContent = '';
            }
        }

        // Enable/disable the continue button
        const continueBtn = $('#btn-continue-payment');
        if (continueBtn) {
            continueBtn.disabled = selectedSeats.length === 0;
            continueBtn.classList.toggle('opacity-50',   selectedSeats.length === 0);
            continueBtn.classList.toggle('cursor-not-allowed', selectedSeats.length === 0);
        }
    }

    // ── Init ──────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', function () {
        // Wire up seat buttons
        $$('.seat-btn').forEach(btn => {
            btn.addEventListener('click', () => handleSeatClick(btn));
        });

        // Initial state
        refreshPassengerForms();
        refreshSummary();
    });
})();
