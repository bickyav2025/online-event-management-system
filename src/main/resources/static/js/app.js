/**
 * EventFlow Client-side Interactions and Chart Initializers
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Auto-dismiss flash alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // 2. Mobile Sidebar Toggle
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebar = document.getElementById('dashboardSidebar');
    if (sidebarToggleBtn && sidebar) {
        sidebarToggleBtn.addEventListener('click', function () {
            sidebar.classList.toggle('d-none');
        });
    }

    // 3. Checkout Ticket Quantity and Total Calculation
    const ticketSelect = document.getElementById('checkoutTicketType');
    const qtyInput = document.getElementById('checkoutQuantity');
    const subtotalEl = document.getElementById('checkoutSubtotal');
    const totalEl = document.getElementById('checkoutTotal');

    if (ticketSelect && qtyInput && subtotalEl && totalEl) {
        function recalculate() {
            const selectedOption = ticketSelect.options[ticketSelect.selectedIndex];
            const price = parseFloat(selectedOption.getAttribute('data-price') || 0);
            const qty = parseInt(qtyInput.value || 1, 10);
            const subtotal = price * qty;
            subtotalEl.innerText = '$' + subtotal.toFixed(2);
            totalEl.innerText = '$' + subtotal.toFixed(2);
        }

        ticketSelect.addEventListener('change', recalculate);
        qtyInput.addEventListener('input', recalculate);
        recalculate();
    }
});

// Admin Dashboard Analytics Charts
window.initAdminCharts = function (registrationTrendsData, categoryDistributionData) {
    // 1. Registration & Ticket Trends Chart
    const trendCtx = document.getElementById('registrationTrendsChart');
    if (trendCtx && registrationTrendsData) {
        const labels = registrationTrendsData.map(d => d[0]).reverse();
        const counts = registrationTrendsData.map(d => d[1]).reverse();
        const revenue = registrationTrendsData.map(d => d[2]).reverse();

        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: labels.length > 0 ? labels : ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5'],
                datasets: [
                    {
                        label: 'Registrations',
                        data: counts.length > 0 ? counts : [4, 8, 15, 22, 38],
                        borderColor: '#4f46e5',
                        backgroundColor: 'rgba(79, 70, 229, 0.1)',
                        fill: true,
                        tension: 0.35,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Revenue ($)',
                        data: revenue.length > 0 ? revenue : [240, 590, 1100, 1850, 2900],
                        borderColor: '#0ea5e9',
                        borderDash: [5, 5],
                        tension: 0.35,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                interaction: { mode: 'index', intersect: false },
                scales: {
                    y: { type: 'linear', position: 'left', beginAtZero: true },
                    y1: { type: 'linear', position: 'right', grid: { drawOnChartArea: false }, beginAtZero: true }
                }
            }
        });
    }

    // 2. Category Distribution Doughnut
    const catCtx = document.getElementById('categoryDistributionChart');
    if (catCtx && categoryDistributionData) {
        const labels = categoryDistributionData.map(d => d[0]);
        const counts = categoryDistributionData.map(d => d[1]);

        new Chart(catCtx, {
            type: 'doughnut',
            data: {
                labels: labels.length > 0 ? labels : ['Technology', 'Music', 'Business', 'Education', 'Sports'],
                datasets: [{
                    data: counts.length > 0 ? counts : [40, 25, 15, 10, 10],
                    backgroundColor: ['#4f46e5', '#8b5cf6', '#0ea5e9', '#10b981', '#f59e0b']
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }
};

// Organizer Dashboard Analytics Charts
window.initOrganizerCharts = function (organizerTrendsData) {
    const orgTrendCtx = document.getElementById('organizerTrendsChart');
    if (orgTrendCtx && organizerTrendsData) {
        const labels = organizerTrendsData.map(d => d[0]).reverse();
        const counts = organizerTrendsData.map(d => d[1]).reverse();

        new Chart(orgTrendCtx, {
            type: 'bar',
            data: {
                labels: labels.length > 0 ? labels : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Registrations',
                    data: counts.length > 0 ? counts : [12, 19, 8, 15, 22, 30, 25],
                    backgroundColor: '#6366f1',
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                scales: { y: { beginAtZero: true } }
            }
        });
    }
};
