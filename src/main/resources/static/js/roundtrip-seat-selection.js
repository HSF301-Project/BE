document.addEventListener('DOMContentLoaded', () => {
    let outSeats = [];
    let retSeats = [];

    const priceOutElement = document.getElementById('unit-price-out');
    const priceRetElement = document.getElementById('unit-price-ret');
    const priceOut = priceOutElement ? parseInt(priceOutElement.dataset.price) : 0;
    const priceRet = priceRetElement ? parseInt(priceRetElement.dataset.price) : 0;

    const outboundStopEtas = window.PremiumTransitData?.outboundStopEtas || [];
    const returnStopEtas = window.PremiumTransitData?.returnStopEtas || [];

    window.switchSeatTab = function(tab) {
        const outboundBtn = document.getElementById('tab-btn-outbound');
        const returnBtn = document.getElementById('tab-btn-return');
        const outboundSeats = document.getElementById('seat-tab-outbound');
        const returnSeats = document.getElementById('seat-tab-return');
        const outboundDetail = document.getElementById('trip-detail-outbound');
        const returnDetail = document.getElementById('trip-detail-return');

        if(tab === 'outbound') {
            outboundBtn.classList.add('border-primary', 'text-primary');
            outboundBtn.classList.remove('border-transparent', 'text-on-surface-variant');
            returnBtn.classList.remove('border-primary', 'text-primary');
            returnBtn.classList.add('border-transparent', 'text-on-surface-variant');
            outboundSeats.classList.remove('hidden');
            returnSeats.classList.add('hidden');
            outboundDetail.classList.remove('hidden');
            returnDetail.classList.add('hidden');
        } else {
            returnBtn.classList.add('border-primary', 'text-primary');
            returnBtn.classList.remove('border-transparent', 'text-on-surface-variant');
            outboundBtn.classList.remove('border-primary', 'text-primary');
            outboundBtn.classList.add('border-transparent', 'text-on-surface-variant');
            returnSeats.classList.remove('hidden');
            outboundSeats.classList.add('hidden');
            returnDetail.classList.remove('hidden');
            outboundDetail.classList.add('hidden');
        }
    };

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

    function updateSummary() {
        // Cập nhật text hiển thị ghế đã chọn
        document.getElementById('outbound-seats').innerText = outSeats.length ? outSeats.map(s => s.seatId).join(', ') : 'Chưa chọn';
        document.getElementById('return-seats').innerText = retSeats.length ? retSeats.map(s => s.seatId).join(', ') : 'Chưa chọn';

        // Tính tổng tiền dựa trên số lượng ghế riêng biệt của từng chiều
        const total = (outSeats.length * priceOut) + (retSeats.length * priceRet);
        document.getElementById('summary-total').innerText = total.toLocaleString('vi-VN') + 'đ';

        const btn = document.getElementById('btn-submit-form');

        // FIX: Chỉ cần mỗi chiều có ít nhất 1 ghế là cho phép đặt vé
        // Không bắt buộc outSeats.length === retSeats.length nữa
        if(outSeats.length > 0 && retSeats.length > 0) {
            btn.classList.remove('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
        } else {
            btn.classList.add('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
        }
    }

    function updateDropoffOptions(isReturn) {
        const prefix = isReturn ? 'return' : '';
        const pickupSelect = document.querySelector(`select[name="${prefix ? 'returnPickupLocationId' : 'pickupLocationId'}"]`);
        const dropoffSelect = document.querySelector(`select[name="${prefix ? 'returnDropoffLocationId' : 'dropoffLocationId'}"]`);
        const etas = isReturn ? returnStopEtas : outboundStopEtas;

        if (!pickupSelect || !dropoffSelect) return;

        const selectedPickupId = pickupSelect.value;
        const selectedPickupStop = etas.find(stop => stop.stopId === selectedPickupId);
        const currentDropoffId = dropoffSelect.value;

        dropoffSelect.innerHTML = '';

        let addedCount = 0;
        if (selectedPickupStop) {
            const pickupOffset = selectedPickupStop.offsetMinutes;
            etas.forEach(stop => {
                if ((stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH') && stop.offsetMinutes > pickupOffset) {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                    dropoffSelect.appendChild(option);
                    addedCount++;
                }
            });
        }

        if (addedCount === 0) {
            etas.forEach(stop => {
                if (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH') {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                    dropoffSelect.appendChild(option);
                }
            });
        }

        if (Array.from(dropoffSelect.options).some(opt => opt.value === currentDropoffId)) {
            dropoffSelect.value = currentDropoffId;
        } else if (dropoffSelect.options.length > 0) {
            dropoffSelect.value = dropoffSelect.options[0].value;
        }
    }

    updateDropoffOptions(false);
    updateDropoffOptions(true);

    document.querySelector('select[name="pickupLocationId"]')?.addEventListener('change', () => updateDropoffOptions(false));
    document.querySelector('select[name="returnPickupLocationId"]')?.addEventListener('change', () => updateDropoffOptions(true));

    window.submitRoundTrip = function() {
        const name = document.getElementById('p-name').value;
        const phone = document.getElementById('p-phone').value;
        const email = document.getElementById('p-email').value;

        if(!name || !phone || !email) {
            alert('Vui lòng nhập đầy đủ thông tin hành khách.');
            return;
        }

        let hiddenHtml = '';
        // Chiều đi
        outSeats.forEach((s, i) => {
            hiddenHtml += `
                <input type="hidden" name="passengers[${i}].seatId" value="${s.seatId}"/>
                <input type="hidden" name="passengers[${i}].deck" value="${s.deck}"/>
                <input type="hidden" name="passengers[${i}].seatLabel" value="${s.seatId}"/>
                <input type="hidden" name="passengers[${i}].fullName" value="${name}"/>
                <input type="hidden" name="passengers[${i}].phoneNumber" value="${phone}"/>
                <input type="hidden" name="passengers[${i}].email" value="${email}"/>
            `;
        });

        // Chiều về
        retSeats.forEach((s, i) => {
            hiddenHtml += `
                <input type="hidden" name="returnPassengers[${i}].seatId" value="${s.seatId}"/>
                <input type="hidden" name="returnPassengers[${i}].deck" value="${s.deck}"/>
                <input type="hidden" name="returnPassengers[${i}].seatLabel" value="${s.seatId}"/>
                <input type="hidden" name="returnPassengers[${i}].fullName" value="${name}"/>
                <input type="hidden" name="returnPassengers[${i}].phoneNumber" value="${phone}"/>
                <input type="hidden" name="returnPassengers[${i}].email" value="${email}"/>
            `;
        });

        document.getElementById('hidden-passengers').innerHTML = hiddenHtml;
        document.getElementById('booking-form').submit();
    };
});