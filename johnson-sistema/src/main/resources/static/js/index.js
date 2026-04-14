function initDashboard(tendenciaData, eventosCalendario) {
    initializeCharts(tendenciaData);
    initializeCalendar(eventosCalendario);
        

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
                    const editSopFin = document.getElementById('edit-sopFin');
                    const editBu = document.getElementById('edit-bu');
                    const editPlanta = document.getElementById('edit-planta');
                    const editLaunchEngineer = document.getElementById('edit-launchEngineer');
                    const editFechaLineArrival = document.getElementById('edit-fechaLineArrival');
                    const editFechaLineArrivalFin = document.getElementById('edit-fechaLineArrivalFin');
                    const editFechaPvBuild = document.getElementById('edit-fechaPvBuild');
                    const editFechaPvBuildFin = document.getElementById('edit-fechaPvBuildFin');
                    const editFechaPpap = document.getElementById('edit-fechaPpap');
                    const editFechaPpapFin = document.getElementById('edit-fechaPpapFin');
                    const editScope = document.getElementById('edit-scope');
                    const editProgramManager = document.getElementById('edit-programManager');

                    if (editId) editId.value = data.id || '';
                    if (editNombre) editNombre.value = data.nombre || '';
                    if (editNumParte) editNumParte.value = data.numeroParte || '';
                    if (editCliente) editCliente.value = data.cliente || '';
                    if (editBu) editBu.value = data.bu || '';
                    if (editPlanta) editPlanta.value = data.planta || '';
                    if (editLaunchEngineer) editLaunchEngineer.value = data.launchEngineer || '';
                    if (editScope) editScope.value = data.scope || '';
                    if (editProgramManager) editProgramManager.value = data.programManager || '';
                    
                    const fillDateInput = (input, dateVal) => {
                        if (!input || !dateVal) return;
                        let dateStr = "";
                        if (Array.isArray(dateVal)) {
                            const y = dateVal[0];
                            const m = String(dateVal[1]).padStart(2, '0');
                            const d = String(dateVal[2]).padStart(2, '0');
                            dateStr = `${y}-${m}-${d}`;
                        } else {
                            dateStr = dateVal.split('T')[0];
                        }
                        input.value = dateStr;
                    };

                    fillDateInput(editSop, data.sop);
                    fillDateInput(editSopFin, data.sopFin);
                    fillDateInput(editFechaLineArrival, data.fechaLineArrival);
                    fillDateInput(editFechaLineArrivalFin, data.fechaLineArrivalFin);
                    fillDateInput(editFechaPvBuild, data.fechaPvBuild);
                    fillDateInput(editFechaPvBuildFin, data.fechaPvBuildFin);
                    fillDateInput(editFechaPpap, data.fechaPpap);
                    fillDateInput(editFechaPpapFin, data.fechaPpapFin);
                    
                    const modalEl = document.getElementById('modalEditarProyecto');
                    if (modalEl) {
                        const modal = new bootstrap.Modal(modalEl);
                        modal.show();
                    }
                })
                .catch(error => console.error('Error fetching project data:', error));
        });
    });

    // LÓGICA PARA BOTÓN "FULL MONTH"
    document.querySelectorAll('.btn-full-month').forEach(btn => {
        btn.addEventListener('click', function() {
            const container = this.closest('.col-12');
            const inputs = container.querySelectorAll('input[type="date"]');
            
            if (inputs.length >= 2) {
                const startInput = inputs[0];
                const endInput = inputs[1];
                
                if (startInput.value) {
                    // Si hay fecha de inicio, calculamos el fin de ese mes
                    const parts = startInput.value.split('-');
                    const y = parseInt(parts[0]);
                    const m = parseInt(parts[1]);
                    // Creamos el último día del mes: día 0 del siguiente mes
                    const lastDayDate = new Date(y, m, 0);
                    
                    const lastD = String(lastDayDate.getDate()).padStart(2, '0');
                    const monthStr = String(m).padStart(2, '0');
                    
                    endInput.value = `${y}-${monthStr}-${lastD}`;
                } else {
                    // Si no hay fecha de inicio, ponemos el mes actual completo por defecto
                    const now = new Date();
                    const y = now.getFullYear();
                    const m = now.getMonth() + 1;
                    const lastDayDate = new Date(y, m, 0);
                    
                    const monthStr = String(m).padStart(2, '0');
                    const lastD = String(lastDayDate.getDate()).padStart(2, '0');
                    
                    startInput.value = `${y}-${monthStr}-01`;
                    endInput.value = `${y}-${monthStr}-${lastD}`;
                }
            }
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
    const currentYear = now.getFullYear();
    const miniLabels = [];
    const miniValues = [];

    for (let month = 0; month < 12; month++) {
        const monthDate = new Date(currentYear, month, 1);
        miniLabels.push(formatMonthYear(monthDate));
        miniValues.push(getChartValue(monthDate));
    }

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
                            if (selectedChamp === 'SHOW_ALL' || rowChamp === selectedChamp) {
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
            right: '' // Handled by our custom dropdown
        }, 
        events: events, 
        height: 'auto', 
        dayMaxEvents: true,
        eventDisplay: 'block',
        displayEventTime: false,
        themeSystem: 'bootstrap5',
        eventClick: function(info) {
            if (info.event.url) {
                window.location.href = info.event.url;
                info.jsEvent.preventDefault();
            }
        },
        eventDidMount: function(info) {
            // Personalizar el estilo del evento si es necesario
            if (info.el) {
                info.el.style.borderRadius = '6px';
                info.el.style.border = 'none';
                info.el.style.padding = '2px 5px';
                info.el.style.fontSize = '0.75rem';
                info.el.style.fontWeight = '700';
                
                // Add tooltip if needed
                info.el.title = info.event.title;
            }
        }
    });
    calendar.render();

    // Handle View Switching from our custom dropdown
    const filterLinks = document.querySelectorAll('#calendarFilter + .dropdown-menu .dropdown-item');
    filterLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const view = this.getAttribute('data-view');
            
            // Update active state in UI
            filterLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // Switch FullCalendar View
            calendar.changeView(view);
            
            // Update dropdown text
            const btn = document.getElementById('calendarFilter');
            btn.innerHTML = `<i class="bi bi-funnel me-1"></i> ${this.textContent}`;
        });
    });

    // Función para llenar dinámicamente el modal de Edición de Proyecto
function llenarModalEditar(btn) {
    // Tomamos los datos que vienen ocultos en el botón (data-atributos)
    // y los inyectamos en los inputs del modal buscando por su ID
    document.getElementById('edit-id').value = btn.getAttribute('data-id') || '';
    document.getElementById('edit-nombre').value = btn.getAttribute('data-nombre') || '';
    document.getElementById('edit-numeroParte').value = btn.getAttribute('data-numeroparte') || '';
    document.getElementById('edit-cliente').value = btn.getAttribute('data-cliente') || '';
    document.getElementById('edit-bu').value = btn.getAttribute('data-bu') || '';
    document.getElementById('edit-planta').value = btn.getAttribute('data-planta') || '';
    document.getElementById('edit-launchEngineer').value = btn.getAttribute('data-launchengineer') || '';
    
    // Fechas
    document.getElementById('edit-fechaLineArrival').value = btn.getAttribute('data-fechalinearrival') || '';
    document.getElementById('edit-fechaLineArrivalFin').value = btn.getAttribute('data-fechalinearrivalfin') || '';
    document.getElementById('edit-fechaPvBuild').value = btn.getAttribute('data-fechapvbuild') || '';
    document.getElementById('edit-fechaPvBuildFin').value = btn.getAttribute('data-fechapvbuildfin') || '';
    document.getElementById('edit-sop').value = btn.getAttribute('data-sop') || '';
    document.getElementById('edit-fechaPpap').value = btn.getAttribute('data-fechappap') || '';
    
    // Textareas y otros
    document.getElementById('edit-scope').value = btn.getAttribute('data-scope') || '';
    document.getElementById('edit-programManager').value = btn.getAttribute('data-programmanager') || '';
}

}
