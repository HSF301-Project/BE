//timer.js
document.addEventListener('DOMContentLoaded', function() {
    const timerDisplay = document.getElementById('countdown-timer');
    if (!timerDisplay) return; // Dừng nếu không tìm thấy element

    const rawExpiry = timerDisplay.getAttribute('data-expiry');
    // Nếu không có expiryTimestamp, dừng script tránh lỗi
    if (!rawExpiry) return;

    const expiryTimestamp = parseInt(rawExpiry);
    if (Number.isNaN(expiryTimestamp)) return;
    const btnDesktop = document.getElementById('btn-submit-desktop');
    const btnMobile = document.getElementById('btn-submit-mobile');

    const interval = setInterval(function () {
        const now = new Date().getTime();
        const distance = expiryTimestamp - now;

        // Nếu hết thời gian
        if (distance <= 0) {
            clearInterval(interval);
            timerDisplay.innerHTML = "00:00";
            timerDisplay.classList.replace('text-secondary', 'text-red-600');

            // Vô hiệu hóa nút thanh toán
            if(btnDesktop) {
                btnDesktop.disabled = true;
                btnDesktop.classList.add('opacity-50', 'cursor-not-allowed');
            }
            if(btnMobile) {
                btnMobile.disabled = true;
                btnMobile.classList.add('opacity-50', 'cursor-not-allowed');
            }

            alert("Hết thời gian giữ chỗ (7 phút). Phiên giao dịch đã bị hủy.");
            window.location.href = "/home"; // Redirect về trang chủ hoặc trang tìm kiếm
            return;
        }

        // Tính toán phút và giây
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);

        // Định dạng hiển thị 00:00
        const displayMinutes = minutes < 10 ? "0" + minutes : minutes;
        const displaySeconds = seconds < 10 ? "0" + seconds : seconds;

        timerDisplay.innerHTML = displayMinutes + ":" + displaySeconds;
    }, 1000);
});
