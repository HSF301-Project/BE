document.addEventListener('DOMContentLoaded', () => {
    // 1. Khởi tạo các DOM Element
    const modal = document.getElementById('cancel-modal');
    const content = document.getElementById('cancel-modal-content');
    const form = document.getElementById('cancel-form');
    const title = document.getElementById('cancel-modal-title');
    const message = document.getElementById('cancel-modal-message');
    const btnConfirm = document.getElementById('btn-confirm-cancel');
    const detailsBox = document.getElementById('cancel-trip-details');

    if (!modal) return; // Bảo vệ lỗi nếu DOM chưa sẵn sàng

    // 2. Định nghĩa hàm Đóng Modal
    const hideModal = () => {
        content.classList.remove('scale-100', 'opacity-100');
        content.classList.add('scale-95', 'opacity-0');

        setTimeout(() => {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
        }, 300);
    };

    // 3. Sử dụng Event Delegation để lắng nghe tất cả click trên toàn trang
    document.addEventListener('click', (e) => {

        // --- NẾU BẤM NÚT ĐÓNG / BACKDROP ---
        const closeBtn = e.target.closest('.btn-hide-cancel-modal');
        if (closeBtn) {
            hideModal();
            return; // Dừng xử lý
        }

        // --- NẾU BẤM NÚT MỞ MODAL HỦY CHUYẾN ---
        const openBtn = e.target.closest('.btn-show-cancel-modal');
        if (openBtn) {
            // Lấy toàn bộ dữ liệu từ data attributes một cách gọn gàng
            const dataset = openBtn.dataset;
            const bookingId = dataset.id;
            const isCancellable = dataset.cancellable === 'true';

            // Đổ dữ liệu vào Modal
            document.getElementById('modal-trip-from').innerText = dataset.from;
            document.getElementById('modal-pickup-station').innerText = dataset.pickupStation;
            document.getElementById('modal-trip-to').innerText = dataset.to;
            document.getElementById('modal-dropoff-station').innerText = dataset.dropoffStation;
            document.getElementById('modal-trip-time').innerText = dataset.time;

            // Cập nhật action cho Form
            form.action = `/booking/${bookingId}/cancel`;

            // Xử lý Logic hiển thị theo isCancellable
            if (!isCancellable) {
                title.innerText = "Không thể hủy chuyến";
                message.innerHTML = "Chuyến đi này không thể hủy do đã quá hạn thời gian cho phép (trước 2 giờ khởi hành). <br/><span class='text-sm italic mt-2 block'>Vui lòng liên hệ hotline để được hỗ trợ.</span>";
                btnConfirm.classList.add('hidden');
                detailsBox.classList.add('opacity-50');
            } else {
                title.innerText = "Xác nhận hủy chuyến";
                message.innerHTML = "Bạn có chắc chắn muốn hủy chuyến đi này không? <br/><span class='font-bold text-error/80 text-sm italic mt-2 block'>Hành động này không thể hoàn tác.</span>";
                btnConfirm.classList.remove('hidden');
                detailsBox.classList.remove('opacity-50');
            }

            // Hiển thị Modal với hiệu ứng Animation
            modal.classList.remove('hidden');
            modal.classList.add('flex');

            setTimeout(() => {
                content.classList.remove('scale-95', 'opacity-0');
                content.classList.add('scale-100', 'opacity-100');
            }, 10);
        }
    });

    // 4. Lắng nghe sự kiện phím ESC để đóng Modal
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !modal.classList.contains('hidden')) {
            hideModal();
        }
    });
});