document.addEventListener('DOMContentLoaded', () => {
    let outSeats = [];
    let retSeats = [];

    const priceOutElement = document.getElementById('unit-price-out');
    const priceRetElement = document.getElementById('unit-price-ret');
    const priceOut = priceOutElement ? parseInt(priceOutElement.dataset.price) : 0;
    const priceRet = priceRetElement ? parseInt(priceRetElement.dataset.price) : 0;

    const outboundStopEtas = window.PremiumTransitData?.outboundStopEtas || [];
    const returnStopEtas = window.PremiumTransitData?.returnStopEtas || [];

    // 1. Tab Switching Logic
    window.switchSeatTab = function(tab) {
        const outboundBtn = document.getElementById('tab-btn-outbound');
        const returnBtn = document.getElementById('tab-btn-return');
        const outboundSeatsTab = document.getElementById('seat-tab-outbound');
        const returnSeatsTab = document.getElementById('seat-tab-return');
        const outboundDetail = document.getElementById('trip-detail-outbound');
        const returnDetail = document.getElementById('trip-detail-return');

        if(tab === 'outbound') {
            outboundBtn.classList.add('border-primary', 'text-primary');
            outboundBtn.classList.remove('border-transparent', 'text-on-surface-variant');
            returnBtn.classList.remove('border-primary', 'text-primary');
            returnBtn.classList.add('border-transparent', 'text-on-surface-variant');
            outboundSeatsTab.classList.remove('hidden');
            returnSeatsTab.classList.add('hidden');
            outboundDetail.classList.remove('hidden');
            returnDetail.classList.add('hidden');
        } else {
            returnBtn.classList.add('border-primary', 'text-primary');
            returnBtn.classList.remove('border-transparent', 'text-on-surface-variant');
            outboundBtn.classList.remove('border-primary', 'text-primary');
            outboundBtn.classList.add('border-transparent', 'text-on-surface-variant');
            returnSeatsTab.classList.remove('hidden');
            outboundSeatsTab.classList.add('hidden');
            returnDetail.classList.remove('hidden');
            outboundDetail.classList.add('hidden');
        }
    };

    // 2. Seat Selection Logic
    document.querySelectorAll('.seat-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const seatId = this.dataset.seatId;
            const leg = this.dataset.leg;
            const deck = this.dataset.deck;
            let list = leg === 'outbound' ? outSeats : retSeats;

            const idx = list.findIndex(s => s.seatId === seatId);
            if(idx >= 0) {
                list.splice(idx, 1);
                this.classList.remove('bg-secondary', 'text-white');
                this.classList.add('bg-[#AACDDC]');
            } else {
                list.push({seatId, deck});
                this.classList.add('bg-secondary', 'text-white');
                this.classList.remove('bg-[#AACDDC]');
            }
            updateSummary();
        });
    });

    // 3. Update Summary, Formula & Combined Seats
    function updateSummary() {
        const combinedSeatsEl = document.getElementById('combined-seats');
        const formulaEl = document.getElementById('price-formula');
        const totalEl = document.getElementById('summary-total');
        const btn = document.getElementById('btn-submit-form');

        // Hiển thị danh sách ghế gộp (Outbound + Return)
        const allSeatIds = [...outSeats.map(s => s.seatId), ...retSeats.map(s => s.seatId)];
        if (combinedSeatsEl) {
            combinedSeatsEl.innerText = allSeatIds.length > 0 ? allSeatIds.join(', ') : 'Chưa chọn';
        }

        // Cập nhật text phụ ở các tab chi tiết (giữ lại để tránh lỗi null)
        const outSeatsSub = document.getElementById('outbound-seats');
        const retSeatsSub = document.getElementById('return-seats');
        if(outSeatsSub) outSeatsSub.innerText = outSeats.length ? outSeats.map(s => s.seatId).join(', ') : 'Chưa chọn';
        if(retSeatsSub) retSeatsSub.innerText = retSeats.length ? retSeats.map(s => s.seatId).join(', ') : 'Chưa chọn';

        // Tính tổng tiền
        const total = (outSeats.length * priceOut) + (retSeats.length * priceRet);
        totalEl.innerText = total.toLocaleString('vi-VN') + 'đ';

        // Hiển thị công thức tính tiền
        if (formulaEl) {
            if (outSeats.length > 0 || retSeats.length > 0) {
                if (priceOut === priceRet) {
                    // Nếu giá vé 2 chiều bằng nhau -> hiển thị gộp (VD: 250.000đ x 2)
                    const totalCount = outSeats.length + retSeats.length;
                    formulaEl.innerText = `${priceOut.toLocaleString('vi-VN')}đ x ${totalCount}`;
                } else {
                    // Nếu giá vé khác nhau -> hiển thị tách biệt (VD: 200k x 1 + 250k x 1)
                    let parts = [];
                    if (outSeats.length > 0) parts.push(`${priceOut.toLocaleString('vi-VN')}đ x ${outSeats.length}`);
                    if (retSeats.length > 0) parts.push(`${priceRet.toLocaleString('vi-VN')}đ x ${retSeats.length}`);
                    formulaEl.innerText = parts.join(' + ');
                }
            } else {
                formulaEl.innerText = '';
            }
        }

        // Trạng thái nút Submit
        if(outSeats.length > 0 && retSeats.length > 0) {
            btn.classList.remove('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
        } else {
            btn.classList.add('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
        }
    }

    // 4. Dropoff filtering based on Pickup (giữ nguyên logic cũ của bạn)
    function updateDropoffOptions(isReturn) {
        const prefix = isReturn ? 'return' : '';
        const pickupSelect = document.querySelector(`select[name="${prefix ? 'returnPickupLocationId' : 'pickupLocationId'}"]`);
        const dropoffSelect = document.querySelector(`select[name="${prefix ? 'returnDropoffLocationId' : 'dropoffLocationId'}"]`);
        const etas = isReturn ? returnStopEtas : outboundStopEtas;

        if (!pickupSelect || !dropoffSelect || etas.length === 0) return;

        const selectedPickupId = String(pickupSelect.value).trim();
        const selectedPickupStop = etas.find(stop => String(stop.stopId).trim() === selectedPickupId);
        const currentDropoffId = String(dropoffSelect.value).trim();

        dropoffSelect.innerHTML = '';
        let addedCount = 0;
        const pickupOffset = selectedPickupStop ? Number(selectedPickupStop.offsetMinutes) : -1;

        etas.forEach(stop => {
            const stopIdStr = String(stop.stopId).trim();
            const isDropType = (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH');
            const isAfterPickup = Number(stop.offsetMinutes) > pickupOffset;
            const isNotSameId = stopIdStr !== selectedPickupId;

            if (isDropType && isAfterPickup && isNotSameId) {
                const option = document.createElement('option');
                option.value = stop.stopId;
                option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                dropoffSelect.appendChild(option);
                addedCount++;
            }
        });

        if (addedCount === 0) {
            etas.forEach(stop => {
                const stopIdStr = String(stop.stopId).trim();
                const isDropType = (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH');
                if (isDropType && stopIdStr !== selectedPickupId) {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                    dropoffSelect.appendChild(option);
                }
            });
        }

        const options = Array.from(dropoffSelect.options);
        const stillValid = options.some(opt => String(opt.value).trim() === currentDropoffId);
        if (!stillValid && options.length > 0) {
            dropoffSelect.value = options[0].value;
        } else {
            dropoffSelect.value = currentDropoffId;
        }
    }

    // Init and Events for Dropoff
    updateDropoffOptions(false);
    updateDropoffOptions(true);
    document.querySelector('select[name="pickupLocationId"]')?.addEventListener('change', () => updateDropoffOptions(false));
    document.querySelector('select[name="returnPickupLocationId"]')?.addEventListener('change', () => updateDropoffOptions(true));

    // 5. Submit Form Logic
    window.submitRoundTrip = function() {
        const name = document.getElementById('p-name').value;
        const phone = document.getElementById('p-phone').value;
        const email = document.getElementById('p-email').value;

        if(!name || !phone || !email) {
            alert('Vui lòng nhập đầy đủ thông tin hành khách.');
            return;
        }

        let hiddenHtml = '';
        outSeats.forEach((s, i) => {
            hiddenHtml += `
                <input type="hidden" name="passengers[${i}].seatId" value="${s.seatId}"/>
                <input type="hidden" name="passengers[${i}].deck" value="${s.deck}"/>
                <input type="hidden" name="passengers[${i}].fullName" value="${name}"/>
                <input type="hidden" name="passengers[${i}].phoneNumber" value="${phone}"/>
                <input type="hidden" name="passengers[${i}].email" value="${email}"/>
            `;
        });

        retSeats.forEach((s, i) => {
            hiddenHtml += `
                <input type="hidden" name="returnPassengers[${i}].seatId" value="${s.seatId}"/>
                <input type="hidden" name="returnPassengers[${i}].deck" value="${s.deck}"/>
                <input type="hidden" name="returnPassengers[${i}].fullName" value="${name}"/>
                <input type="hidden" name="returnPassengers[${i}].phoneNumber" value="${phone}"/>
                <input type="hidden" name="returnPassengers[${i}].email" value="${email}"/>
            `;
        });

        document.getElementById('hidden-passengers').innerHTML = hiddenHtml;
        document.getElementById('booking-form').submit();
    };
});