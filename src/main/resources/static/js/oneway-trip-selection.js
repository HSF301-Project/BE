document.addEventListener('DOMContentLoaded', () => {
    const tripStopEtas = window.OneWayTripData?.stopEtas || [];
    const pickupSelect = document.querySelector('select[name="pickupLocationId"]');
    const dropoffSelect = document.querySelector('select[name="dropoffLocationId"]');

    function updateDropoffOptions() {
        if (!pickupSelect || !dropoffSelect || tripStopEtas.length === 0) return;

        const selectedPickupId = pickupSelect.value;
        // FIX: Dùng == (loose equality) hoặc String() để ép kiểu vì ID trong HTML là chuỗi, ID trong JSON có thể là số
        const selectedPickupStop = tripStopEtas.find(stop => String(stop.stopId) === String(selectedPickupId));
        const currentDropoffId = dropoffSelect.value;

        dropoffSelect.innerHTML = '';

        let addedCount = 0;
        if (selectedPickupStop) {
            const pickupOffset = selectedPickupStop.offsetMinutes;
            tripStopEtas.forEach(stop => {
                if ((stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH') && stop.offsetMinutes > pickupOffset) {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                    dropoffSelect.appendChild(option);
                    addedCount++;
                }
            });
        }

        // Fallback: nếu rỗng (lỗi offset hoặc không có điểm sau đó) thì hiện tất cả điểm trả
        if (addedCount === 0) {
            tripStopEtas.forEach(stop => {
                if (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH') {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = stop.stopName + ' (' + stop.etaTime + ')';
                    dropoffSelect.appendChild(option);
                }
            });
        }

        if (Array.from(dropoffSelect.options).some(opt => opt.value === String(currentDropoffId))) {
            dropoffSelect.value = currentDropoffId;
        } else if (dropoffSelect.options.length > 0) {
            dropoffSelect.value = dropoffSelect.options[0].value;
        }
    }

    // Khởi tạo ngay khi load
    updateDropoffOptions();

    if (pickupSelect) {
        pickupSelect.addEventListener('change', updateDropoffOptions);
    }
});