document.addEventListener('DOMContentLoaded', () => {
    let selectedOutbound = null;
    let selectedReturn = null;

    // Element: #selected-outbound-info, #selected-return-info
    function renderTripInfo(date, time, station, name) {
        return `
            <div class="flex flex-col w-full text-white">
                <div class="flex items-baseline gap-2 mb-1.5">
                    <span class="text-xl md:text-2xl font-black leading-none font-headline">${time}</span>
                    <span class="text-[11px] font-bold text-white/80 uppercase tracking-widest">${date}</span>
                </div>
                <div class="flex items-center gap-1.5 mb-1">
                    <span class="material-symbols-outlined text-[14px] text-white/50">location_on</span>
                    <span class="text-xs md:text-sm font-semibold truncate text-white/90 leading-tight">${station}</span>
                </div>
                <div class="text-[10px] font-bold text-white/40 ml-5 uppercase tracking-widest">
                    ${name.replace('Chuyến xe ', '')}
                </div>
            </div>
        `;
    }

    // Element: #tab-departure, #tab-return, #departure-results, #return-results
    window.switchTab = function (tab) {
        const departureBtn = document.getElementById('tab-departure');
        const returnBtn = document.getElementById('tab-return');
        const departureResults = document.getElementById('departure-results');
        const returnResults = document.getElementById('return-results');

        if (tab === 'departure') {
            departureBtn.classList.add('border-primary', 'text-primary');
            departureBtn.classList.remove('border-transparent', 'text-on-surface-variant');
            returnBtn.classList.remove('border-primary', 'text-primary');
            returnBtn.classList.add('border-transparent', 'text-on-surface-variant');
            departureResults.classList.remove('hidden');
            returnResults.classList.add('hidden');
        } else {
            returnBtn.classList.add('border-primary', 'text-primary');
            returnBtn.classList.remove('border-transparent', 'text-on-surface-variant');
            departureBtn.classList.remove('border-primary', 'text-primary');
            departureBtn.classList.add('border-transparent', 'text-on-surface-variant');
            returnResults.classList.remove('hidden');
            departureResults.classList.add('hidden');
        }
    };

    // Element: .outbound-btn, .return-btn, #selected-outbound-info, #selected-return-info, #selection-summary
    function selectTrip(type, id, name, time, date, station, targetBtn) {
        if (type === 'outbound') {
            selectedOutbound = { id, name, time, date, station };
            document.getElementById('selected-outbound-info').innerHTML = renderTripInfo(date, time, station, name);

            document.querySelectorAll('.outbound-btn').forEach(btn => {
                btn.classList.remove('ring-4', 'ring-primary/30', 'scale-105');
                btn.innerText = 'Chọn chiều đi';
            });
            if (targetBtn) {
                targetBtn.classList.add('ring-4', 'ring-primary/30', 'scale-105');
                targetBtn.innerText = 'Đã chọn chiều đi';
            }

            if (!selectedReturn) {
                setTimeout(() => {
                    window.switchTab('return');
                    const summary = document.getElementById('selection-summary');
                    summary.innerText = 'CHỌN CHIỀU VỀ';
                    summary.className = 'text-xs font-black text-orange-500 uppercase tracking-widest animate-pulse';
                }, 400);
            }
        } else {
            selectedReturn = { id, name, time, date, station };
            document.getElementById('selected-return-info').innerHTML = renderTripInfo(date, time, station, name);

            document.querySelectorAll('.return-btn').forEach(btn => {
                btn.classList.remove('ring-4', 'ring-orange-500/30', 'scale-105');
                btn.innerText = 'Chọn chiều về';
            });
            if (targetBtn) {
                targetBtn.classList.add('ring-4', 'ring-orange-500/30', 'scale-105');
                targetBtn.innerText = 'Đã chọn chiều về';
            }
        }

        updateBanner();
    }

    // Element: #selection-banner, #proceed-booking-btn, #selection-summary
    function updateBanner() {
        const banner = document.getElementById('selection-banner');
        const btn = document.getElementById('proceed-booking-btn');
        const summary = document.getElementById('selection-summary');

        if (selectedOutbound || selectedReturn) {
            banner.classList.remove('translate-y-[150%]');
            banner.classList.add('translate-y-0');
        }

        if (selectedOutbound && selectedReturn) {
            btn.disabled = false;
            summary.innerText = 'ĐÃ CHỌN ĐỦ 2 CHIỀU';
            summary.className = 'text-xs font-black text-green-400 uppercase tracking-widest';
            summary.classList.remove('animate-pulse');
        } else if (selectedOutbound) {
            summary.innerText = 'CHỌN CHIỀU VỀ';
            summary.className = 'text-xs font-black text-orange-500 uppercase tracking-widest animate-pulse';
            btn.disabled = true;
        } else if (selectedReturn) {
            summary.innerText = 'CHỌN CHIỀU ĐI';
            summary.className = 'text-xs font-black text-primary uppercase tracking-widest animate-pulse';
            btn.disabled = true;
        }
    }

    // Element: window.location
    window.proceedToBooking = function () {
        if (selectedOutbound && selectedReturn) {
            window.location.href = `/booking/roundtrip?outboundId=${selectedOutbound.id}&returnId=${selectedReturn.id}`;
        }
    };

    // Element: .trip-select-btn
    document.addEventListener('click', function (e) {
        const btn = e.target.closest('.trip-select-btn');
        if (btn) {
            const tripId = btn.dataset.tripId;
            const tripName = btn.dataset.tripName;
            const tripTime = btn.dataset.tripTime;
            const tripDate = btn.dataset.tripDate;
            const tripStation = btn.dataset.tripStation;
            const tripType = btn.dataset.tripType;
            selectTrip(tripType, tripId, tripName, tripTime, tripDate, tripStation, btn);
        }
    });
});