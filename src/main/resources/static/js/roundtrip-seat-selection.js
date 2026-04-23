document.addEventListener('DOMContentLoaded', () => {
    let outSeats = [];
    let retSeats = [];

    const priceOutElement = document.getElementById('unit-price-out');
    const priceRetElement = document.getElementById('unit-price-ret');
    const priceOut = priceOutElement ? parseInt(priceOutElement.dataset.price) : 0;
    const priceRet = priceRetElement ? parseInt(priceRetElement.dataset.price) : 0;

    const outboundStopEtas = window.PremiumTransitData?.outboundStopEtas || [];
    const returnStopEtas = window.PremiumTransitData?.returnStopEtas || [];

    // Mở ra Window để button HTML có thể gọi
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

    // Xử lý Click chọn ghế
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
        document.getElementById('outbound-seats').innerText = outSeats.length ? outSeats.map(s => s.seatId).join(', ') : 'Chưa chọn';
        document.getElementById('return-seats').innerText = retSeats.length ? retSeats.map(s => s.seatId).join(', ') : 'Chưa chọn';

        const total = (outSeats.length * priceOut) + (retSeats.length * priceRet);
        document.getElementById('summary-total').innerText = total.toLocaleString('vi-VN') + 'đ';

        const btn = document.getElementById('btn-submit-form');

        // Đã cập nhật logic: Không ép số ghế bằng nhau, chỉ cần mỗi chiều có >= 1 ghế
        if(outSeats.length > 0 && retSeats.length > 0) {
            btn.classList.remove('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
        } else {
            btn.classList.add('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
        }
    }

    // Logic xử lý Dropoff sau Pickup
    function updateDropoffOptions(isReturn) {
        const prefix = isReturn ? 'return' : '';
        const pickupSelect = document.querySelector(`select[name="${prefix ? 'returnPickupLocationId' : 'pickupLocationId'}"]`);
        const dropoffSelect = document.querySelector(`select[name="${prefix ? 'returnDropoffLocationId' : 'dropoffLocationId'}"]`);
        const etas = isReturn ? returnStopEtas : outboundStopEtas;

        if (!pickupSelect || !dropoffSelect || etas.length === 0) return;

        // CHUẨN HÓA ID: trim() để tránh các ký tự xuống dòng hoặc khoảng trắng từ HTML
        const selectedPickupId = String(pickupSelect.value).trim();
        const selectedPickupStop = etas.find(stop => String(stop.stopId).trim() === selectedPickupId);
        const currentDropoffId = String(dropoffSelect.value).trim();

        // Xóa trắng để render lại từ đầu
        dropoffSelect.innerHTML = '';

        let addedCount = 0;
        const pickupOffset = selectedPickupStop ? Number(selectedPickupStop.offsetMinutes) : -1;

        // Debug nhanh nếu vẫn lỗi (Mở F12 để xem)
        console.log(`Checking leg ${isReturn?'Return':'Out'}: Pickup ID = ${selectedPickupId}, Offset = ${pickupOffset}`);

        etas.forEach(stop => {
            const stopIdStr = String(stop.stopId).trim();
            const isDropType = (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH');

            // Điều kiện 1: Phải nằm sau điểm đón về mặt lộ trình
            const isAfterPickup = Number(stop.offsetMinutes) > pickupOffset;

            // Điều kiện 2: Tuyệt đối không trùng ID với điểm đã chọn ở Pickup
            const isNotSameId = stopIdStr !== selectedPickupId;

            if (isDropType && isAfterPickup && isNotSameId) {
                const option = document.createElement('option');
                option.value = stop.stopId;
                option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                dropoffSelect.appendChild(option);
                addedCount++;
            }
        });

        // FALLBACK: Nếu lộ trình offset bị lỗi (ví dụ tất cả đều = 0)
        // Thì hiện các điểm DROPOFF/BOTH nhưng BẮT BUỘC bỏ qua điểm đã chọn ở Pickup
        if (addedCount === 0) {
            etas.forEach(stop => {
                const stopIdStr = String(stop.stopId).trim();
                const isDropType = (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH');
                const isNotSameId = stopIdStr !== selectedPickupId;

                if (isDropType && isNotSameId) {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                    dropoffSelect.appendChild(option);
                    addedCount++;
                }
            });
        }

        // Tự động chọn phần tử đầu tiên nếu giá trị cũ không còn hợp lệ
        const options = Array.from(dropoffSelect.options);
        const stillValid = options.some(opt => String(opt.value).trim() === currentDropoffId);

        if (!stillValid && options.length > 0) {
            dropoffSelect.value = options[0].value;
        } else {
            dropoffSelect.value = currentDropoffId;
        }
    }

    updateDropoffOptions(false);
    updateDropoffOptions(true);

    document.querySelector('select[name="pickupLocationId"]')?.addEventListener('change', () => updateDropoffOptions(false));
    document.querySelector('select[name="returnPickupLocationId"]')?.addEventListener('change', () => updateDropoffOptions(true));

    // Submit Form
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
                <input type="hidden" name="passengers[${i}].seatLabel" value="${s.seatId}"/>
                <input type="hidden" name="passengers[${i}].fullName" value="${name}"/>
                <input type="hidden" name="passengers[${i}].phoneNumber" value="${phone}"/>
                <input type="hidden" name="passengers[${i}].email" value="${email}"/>
            `;
        });

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