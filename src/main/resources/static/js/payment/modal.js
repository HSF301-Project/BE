document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('cancel-modal');
    const content = document.getElementById('cancel-modal-content');

    if (!modal || !content) return; // Bảo vệ nếu DOM chưa load

    // Hàm mở Modal
    const showModal = () => {
        modal.classList.remove('hidden');
        modal.classList.add('flex');

        setTimeout(() => {
            content.classList.remove('scale-95', 'opacity-0');
            content.classList.add('scale-100', 'opacity-100');
        }, 10);
    };

    // Hàm đóng Modal
    const hideModal = () => {
        content.classList.remove('scale-100', 'opacity-100');
        content.classList.add('scale-95', 'opacity-0');

        setTimeout(() => {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
        }, 300);
    };

    // Event Delegation: Lắng nghe click trên toàn bộ trang
    document.addEventListener('click', (e) => {
        // Nếu bấm vào phần tử có class mở modal
        if (e.target.closest('.btn-show-cancel-modal')) {
            showModal();
        }

        // Nếu bấm vào phần tử có class đóng modal (hoặc màn đen)
        if (e.target.closest('.btn-hide-cancel-modal')) {
            hideModal();
        }
    });

    // Đóng Modal khi ấn phím Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !modal.classList.contains('hidden')) {
            hideModal();
        }
    });
});