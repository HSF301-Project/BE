/* ============================================================
   staff-combobox.js — Searchable combobox logic for staff pages
   ============================================================ */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        // ── Min date = today ──
        var dateInput = document.getElementById('staff-search-date');
        if (dateInput) {
            dateInput.setAttribute('min', new Date().toISOString().split('T')[0]);
        }

        // ── Searchable Combobox Logic ──
        function initCombobox(searchId, hiddenId, dropdownId, arrowId, otherHiddenId) {
            var searchInput = document.getElementById(searchId);
            var hiddenInput = document.getElementById(hiddenId);
            var dropdown = document.getElementById(dropdownId);
            var arrow = document.getElementById(arrowId);
            var allOptions = Array.from(dropdown.querySelectorAll('.combobox-option'));

            if (hiddenInput.value) {
                searchInput.value = hiddenInput.value;
            }

            function filterAndShow() {
                var query = searchInput.value.toLowerCase();
                var otherVal = document.getElementById(otherHiddenId).value;
                var hasVisible = false;
                allOptions.forEach(function (opt) {
                    var val = opt.getAttribute('data-value');
                    var matchSearch = val.toLowerCase().includes(query);
                    var notDuplicate = val !== otherVal;
                    if (matchSearch && notDuplicate) {
                        opt.style.display = '';
                        hasVisible = true;
                    } else {
                        opt.style.display = 'none';
                    }
                });
                dropdown.classList.toggle('hidden', !hasVisible);
                arrow.style.transform = hasVisible ? 'translateY(-50%) rotate(180deg)' : '';
            }

            searchInput.addEventListener('focus', filterAndShow);
            searchInput.addEventListener('input', function () {
                hiddenInput.value = '';
                filterAndShow();
            });

            arrow.addEventListener('click', function () {
                if (dropdown.classList.contains('hidden')) {
                    searchInput.focus();
                    filterAndShow();
                } else {
                    dropdown.classList.add('hidden');
                    arrow.style.transform = '';
                }
            });

            allOptions.forEach(function (opt) {
                opt.addEventListener('click', function () {
                    var val = this.getAttribute('data-value');
                    searchInput.value = val;
                    hiddenInput.value = val;
                    dropdown.classList.add('hidden');
                    arrow.style.transform = '';
                });
            });

            document.addEventListener('click', function (e) {
                if (!searchInput.contains(e.target) && !dropdown.contains(e.target) && !arrow.contains(e.target)) {
                    dropdown.classList.add('hidden');
                    arrow.style.transform = '';
                }
            });
        }

        initCombobox('staffFromSearch', 'staffFromValue', 'staffFromDropdown', 'staffFromArrow', 'staffToValue');
        initCombobox('staffToSearch', 'staffToValue', 'staffToDropdown', 'staffToArrow', 'staffFromValue');

        // ── Validation trước submit ──
        var form = document.getElementById('staffSearchForm');
        if (form) {
            form.addEventListener('submit', function (e) {
                var from = document.getElementById('staffFromValue').value;
                var to = document.getElementById('staffToValue').value;
                if (!from || !to) {
                    e.preventDefault();
                    alert('Vui lòng chọn cả điểm đi và điểm đến.');
                }
            });
        }
    });
})();
