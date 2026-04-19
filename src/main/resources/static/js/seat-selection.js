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
            // Deselect
            selectedSeats.splice(idx, 1);
            btn.classList.remove('bg-secondary', 'text-white', 'scale-105', 'shadow-lg');
            btn.classList.add('bg-surface-container-highest', 'hover:bg-surface-bright');
        } else {
            // Select
            selectedSeats.push({ seatId, deck });
            btn.classList.add('bg-secondary', 'text-white', 'scale-105', 'shadow-lg');
            btn.classList.remove('bg-surface-container-highest', 'hover:bg-surface-bright');
        }

        refreshPassengerForms();
        refreshSummary();
    }

    // ── Build passenger form rows dynamically ─────────────────
    function refreshPassengerForms() {
        const container = $('#passenger-forms');
        if (!container) return;

        // Keep existing name values before re-render
        const existingNames = {};
        $$('.passenger-name-input').forEach(inp => {
            existingNames[inp.dataset.seatId] = inp.value;
        });

        container.innerHTML = '';

        selectedSeats.forEach((seat, i) => {
            const deckLabel = seat.deck === 'lower' ? 'Tầng dưới' : 'Tầng trên';
            const borderColor = seat.deck === 'lower' ? 'border-primary' : 'border-secondary';
            const badgeColor  = seat.deck === 'lower' ? 'text-primary' : 'text-secondary';

            const div = document.createElement('div');
            div.className = `p-6 bg-surface-container-lowest rounded-2xl shadow-[0_12px_32px_rgba(26,27,33,0.06)] border-l-4 ${borderColor}`;
            div.innerHTML = `
                <div class="flex flex-col md:flex-row md:items-center gap-6">
                    <div class="flex-shrink-0 w-12 h-12 bg-primary-fixed flex items-center justify-center rounded-full ${badgeColor} font-bold">${i + 1}</div>
                    <div class="flex-grow grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div class="space-y-1">
                            <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Ghế đã chọn</label>
                            <div class="flex items-center ${badgeColor} font-bold">
                                <span class="material-symbols-outlined mr-1">event_seat</span>
                                ${seat.seatId} (${deckLabel})
                            </div>
                            <input type="hidden" name="passengers[${i}].seatId"    value="${seat.seatId}"/>
                            <input type="hidden" name="passengers[${i}].deck"      value="${seat.deck}"/>
                            <input type="hidden" name="passengers[${i}].seatLabel" value="${seat.seatId}"/>
                        </div>
                        <div class="space-y-1">
                            <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest" for="pax-name-${i}">Họ và tên</label>
                            <input
                                id="pax-name-${i}"
                                class="passenger-name-input w-full bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-surface-tint p-3 text-on-surface placeholder:text-outline"
                                type="text"
                                name="passengers[${i}].fullName"
                                data-seat-id="${seat.seatId}"
                                value="${existingNames[seat.seatId] || ''}"
                                placeholder="Ví dụ: Nguyễn Văn A"
                                required
                            />
                        </div>
                    </div>
                </div>`;
            container.appendChild(div);
        });

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
        const countEl     = $('#summary-seat-count');

        if (countEl) countEl.textContent = `x${selectedSeats.length}`;

        const total = unitPrice * selectedSeats.length;
        if (totalEl) {
            totalEl.textContent = total.toLocaleString('vi-VN') + 'đ';
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
