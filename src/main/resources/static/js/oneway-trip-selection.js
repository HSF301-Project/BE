document.addEventListener('DOMContentLoaded', () => {
    const tripStopEtas = window.OneWayTripData?.stopEtas || [];
    const pickupSelect = document.querySelector('select[name="pickupLocationId"]');
    const dropoffSelect = document.querySelector('select[name="dropoffLocationId"]');

    function updateDropoffOptions() {
        if (!pickupSelect || !dropoffSelect) return;

        const selectedPickupId = pickupSelect.value;
        const selectedPickupStop = tripStopEtas.find(stop => stop.stopId === selectedPickupId);
        const currentDropoffId = dropoffSelect.value;

        dropoffSelect.innerHTML = '';

        let addedCount = 0;
        if (selectedPickupStop) {
            const pickupOffset = selectedPickupStop.offsetMinutes;
            tripStopEtas.forEach(stop => {
                // Chỉ hiện điểm TRẢ (DROPOFF) có thời gian ĐẾN sau thời gian ĐÓN
                if ((stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH') && stop.offsetMinutes > pickupOffset) {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = `${stop.stopName} (${stop.etaTime})`;
                    dropoffSelect.appendChild(option);
                    addedCount++;
                }
            });
        }

        // Nếu không tìm thấy chặng hợp lệ, fallback hiện tất cả điểm trả
        if (addedCount === 0) {
            tripStopEtas.forEach(stop => {
                if (stop.pointType === 'DROPOFF' || stop.pointType === 'BOTH') {
                    const option = document.createElement('option');
                    option.value = stop.stopId;
                    option.textContent = `${stop.stopName} (${stop.etaTime})`;
                    dropoffSelect.appendChild(option);
                }
            });
        }

        // Giữ lại giá trị cũ nếu nó vẫn hợp lệ trong list mới
        if (Array.from(dropoffSelect.options).some(opt => opt.value === currentDropoffId)) {
            dropoffSelect.value = currentDropoffId;
        }
    }

    if (pickupSelect) {
        pickupSelect.addEventListener('change', updateDropoffOptions);
        updateDropoffOptions();
    }
});