function initDashboard(tendenciaData, eventosCalendario) {
    initializeCharts(tendenciaData);
    initializeCalendar(eventosCalendario);
    
    const pills = document.querySelectorAll('.champion-pill');
    const rows = document.querySelectorAll('.task-row');
    const header = document.getElementById('col-champion-header');
    const cols = document.querySelectorAll('.champ-col');

    pills.forEach(pill => {
        pill.addEventListener('click', function() {
            const champ = this.dataset.champion;
            pills.forEach(p => p.classList.remove('active'));
            this.classList.add('active');
            
            if(header) header.style.display = (champ === 'ALL' ? '' : 'none');
            cols.forEach(c => c.style.display = (champ === 'ALL' ? '' : 'none'));
            
            rows.forEach(r => {
                r.style.display = (champ === 'ALL' || r.dataset.champion === champ ? '' : 'none');
            });
        });
    });

    const trendCard = document.getElementById('trendCard');
    if (trendCard) {
        trendCard.addEventListener('click', () => {
            const modalEl = document.getElementById('modalFullTrend');
            if (modalEl) {
                const m = new bootstrap.Modal(modalEl);
                m.show();
            }
        });
    }

    // Manejador para el modal de eliminación
    const deleteButtons = document.querySelectorAll('.btn-delete-project');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const nombre = this.getAttribute('data-nombre');
            const url = this.getAttribute('href');
            
            const nombreEliminarEl = document.getElementById('nombreProyectoEliminar');
            const btnConfirmarEl = document.getElementById('btnConfirmarEliminar');
            
            if (nombreEliminarEl) nombreEliminarEl.textContent = nombre;
            if (btnConfirmarEl) btnConfirmarEl.setAttribute('href', url);
            
            const modalEl = document.getElementById('modalEliminarProyecto');
            if (modalEl) {
                const modal = new bootstrap.Modal(modalEl);
                modal.show();
            }
        });
    });

    // Manejador para el modal de edición
    const editButtons = document.querySelectorAll('.btn-edit-project');
    editButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            fetch('/proyectos/api/' + id)
                .then(response => response.json())
                .then(data => {
                    const editId = document.getElementById('edit-id');
                    const editNombre = document.getElementById('edit-nombre');
                    const editNumParte = document.getElementById('edit-numeroParte');
                    const editCliente = document.getElementById('edit-cliente');
                    const editSop = document.getElementById('edit-sop');

                    if (editId) editId.value = data.id;
                    if (editNombre) editNombre.value = data.nombre;
                    if (editNumParte) editNumParte.value = data.numeroParte;
                    if (editCliente) editCliente.value = data.cliente;
                    
                    if (data.sop && editSop) {
                        let dateStr = "";
                        if (Array.isArray(data.sop)) {
                            const y = data.sop[0];
                            const m = String(data.sop[1]).padStart(2, '0');
                            const d = String(data.sop[2]).padStart(2, '0');
                            dateStr = `${y}-${m}-${d}`;
                        } else {
                            dateStr = data.sop.split('T')[0];
                        }
                        editSop.value = dateStr;
                    }
                    
                    const modalEl = document.getElementById('modalEditarProyecto');
                    if (modalEl) {
                        const modal = new bootstrap.Modal(modalEl);
                        modal.show();
                    }
                })
                .catch(error => console.error('Error fetching project data:', error));
        });
    });
}

function initializeCharts(data) {
    const miniChartEl = document.getElementById('miniTrendChart');
    if (!miniChartEl) return;

    const formatMonthYear = (date) => {
        return String(date.getMonth() + 1).padStart(2, '0') + "/" + date.getFullYear();
    };

    const getChartValue = (date) => {
        const formatWithZero = formatMonthYear(date);
        const formatWithoutZero = (date.getMonth() + 1) + "/" + date.getFullYear();
        return data[formatWithZero] || data[formatWithoutZero] || 0;
    };

    const now = new Date();
    const prevMesDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const nextMesDate = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    
    const miniLabels = [formatMonthYear(prevMesDate), formatMonthYear(now), formatMonthYear(nextMesDate)];
    const miniValues = [getChartValue(prevMesDate), getChartValue(now), getChartValue(nextMesDate)];

    const ctxMini = miniChartEl.getContext('2d');
    const gradientMini = ctxMini.createLinearGradient(0, 0, 0, 250);
    gradientMini.addColorStop(0, 'rgba(245, 130, 31, 0.4)'); 
    gradientMini.addColorStop(1, 'rgba(245, 130, 31, 0.0)');

    const chartOptions = {
        responsive: true, 
        maintainAspectRatio: false, 
        plugins: { 
            legend: { display: false },
            tooltip: {
                backgroundColor: '#1e293b',
                padding: 12,
                titleFont: { size: 13, family: 'Inter' },
                bodyFont: { size: 14, family: 'Inter', weight: 'bold' },
                displayColors: false,
                callbacks: {
                    label: function(context) { return context.parsed.y + ' Deliverables Approved'; }
                }
            }
        }, 
        scales: { 
            y: { 
                beginAtZero: true,
                border: { display: false },
                grid: { color: 'rgba(0,0,0,0.04)', drawTicks: false },
                ticks: { stepSize: 1 }
            }, 
            x: { 
                border: { display: false },
                grid: { display: false } 
            } 
        }
    };

    new Chart(ctxMini, { 
        type: 'line', 
        data: { 
            labels: miniLabels, 
            datasets: [{ 
                label: 'Approved', 
                data: miniValues, 
                borderColor: '#F5821F', 
                backgroundColor: gradientMini, 
                borderWidth: 3,
                pointBackgroundColor: '#FFFFFF',
                pointBorderColor: '#F5821F',
                pointBorderWidth: 2,
                pointRadius: 5,
                pointHoverRadius: 7,
                fill: true,
                tension: 0.4 
            }] 
        }, 
        options: chartOptions
    });

    const fullChartEl = document.getElementById('fullTrendChart');
    if (fullChartEl) {
        const allKeysRaw = Object.keys(data);
        const allKeys = allKeysRaw.sort((a,b) => {
            const [mA, yA] = a.split('/').map(Number);
            const [mB, yB] = b.split('/').map(Number);
            return yA !== yB ? yA - yB : mA - mB;
        });

        const ctxFull = fullChartEl.getContext('2d');
        const gradientFull = ctxFull.createLinearGradient(0, 0, 0, 500);
        gradientFull.addColorStop(0, 'rgba(245, 130, 31, 0.3)');
        gradientFull.addColorStop(1, 'rgba(245, 130, 31, 0.0)');

        new Chart(ctxFull, { 
            type: 'line', 
            data: { 
                labels: allKeys, 
                datasets: [{ 
                    label: 'Annual Trend', 
                    data: allKeys.map(k => data[k]), 
                    borderColor: '#F5821F', 
                    backgroundColor: gradientFull, 
                    borderWidth: 3,
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderColor: '#F5821F',
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 7,
                    fill: true, 
                    tension: 0.4 
                }] 
            }, 
            options: chartOptions 
        });
    }
}

        document.addEventListener('DOMContentLoaded', function() {
            const pills = document.querySelectorAll('.champion-pill');
            
            pills.forEach(pill => {
                pill.addEventListener('click', function() {
                    pills.forEach(p => p.classList.remove('active'));
                    this.classList.add('active');
                    
                    const selectedChamp = this.getAttribute('data-champion');
                    
                    document.querySelectorAll('.accordion-item').forEach(accordion => {
                        let visibleRows = 0;
                        
                        accordion.querySelectorAll('.task-row').forEach(row => {
                            const rowChamp = row.getAttribute('data-champion');
                            if (selectedChamp === 'ALL' || rowChamp === selectedChamp) {
                                row.style.display = '';
                                visibleRows++;
                            } else {
                                row.style.display = 'none';
                            }
                        });
                        
                        const badge = accordion.querySelector('.project-task-count');
                        if (badge) badge.textContent = visibleRows;
                        
                        accordion.style.display = visibleRows > 0 ? '' : 'none';
                    });
                });
            });
        });


function initializeCalendar(events) {
    const el = document.getElementById('calendar');
    if (!el) return;
    
    const calendar = new FullCalendar.Calendar(el, { 
        initialView: 'dayGridMonth', 
        headerToolbar: { 
            left: 'prev,next today', 
            center: 'title', 
            right: 'dayGridMonth,listMonth' 
        }, 
        events: events, 
        height: 'auto', 
        dayMaxEvents: true 
    });
    calendar.render();
}
