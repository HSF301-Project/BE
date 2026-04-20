document.addEventListener("DOMContentLoaded", function () {
    var bookingForm = document.querySelector("[data-staff-booking-form]");
    if (!bookingForm) {
        return;
    }

    var unitPriceRaw = Number(bookingForm.getAttribute("data-unit-price") || 0);
    var summarySelectedSeats = document.getElementById("summary-selected-seats");
    var summaryUnitPrice = document.getElementById("summary-unit-price");
    var summaryTotalPrice = document.getElementById("summary-total-price");

    var formatter = new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        maximumFractionDigits: 0
    });

    function formatCurrency(amount) {
        return formatter.format(amount || 0);
    }

    function updateSummary() {
        var selectedSeatInputs = bookingForm.querySelectorAll('input[name="selectedSeats"]:checked');
        var selectedSeats = Array.prototype.map.call(selectedSeatInputs, function (input) {
            return input.value;
        });

        if (summarySelectedSeats) {
            summarySelectedSeats.textContent = selectedSeats.length > 0
                ? selectedSeats.join(", ")
                : "Chưa chọn";
        }

        if (summaryUnitPrice) {
            summaryUnitPrice.textContent = formatCurrency(unitPriceRaw);
        }

        if (summaryTotalPrice) {
            summaryTotalPrice.textContent = formatCurrency(unitPriceRaw * selectedSeats.length);
        }
    }

    bookingForm.addEventListener("change", function (event) {
        if (event.target && event.target.name === "selectedSeats") {
            updateSummary();
        }
    });

    updateSummary();
});

