function initTimeline(rawGroups, rawItems) {
    const container = document.getElementById('visualization');
    if (!container) return;

    const groups = new vis.DataSet(rawGroups || []);
    const items = new vis.DataSet(rawItems || []);

    const view = new vis.DataView(items, {
        filter: function (item) {
            const chkMilestones = document.getElementById('chkMilestones');
            const chkMainEvents = document.getElementById('chkMainEvents');
            const showMilestones = chkMilestones ? chkMilestones.checked : true;
            const showMainEvents = chkMainEvents ? chkMainEvents.checked : true;
            return (item.className.includes('hito-') && showMilestones) || 
                   (item.className.includes('vis-event') && showMainEvents);
        }
    });

    const timeline = new vis.Timeline(container, view, groups, {
        groupOrder: 'id', 
        margin: {item: {horizontal: 10, vertical: 20}, axis: 40}, 
        orientation: 'top',
        timeAxis: {scale: 'month', step: 1}, 
        horizontalScroll: true, 
        zoomKey: 'ctrlKey', 
        stack: true, 
        showCurrentTime: true
    });

    document.querySelectorAll('.filter-checkbox').forEach(cb => {
        cb.addEventListener('change', () => view.refresh());
    });
    
    const selProyecto = document.getElementById('selProyecto');
    if (selProyecto) {
        selProyecto.addEventListener('change', () => { 
            cargarHitos(selProyecto.value); 
            cargarElementosParaMain(selProyecto.value); 
        });
    }

    timeline.on('select', (p) => {
        if (p.items.length > 0) {
            const item = items.get(p.items[0]);
            const temp = document.createElement('div'); 
            temp.innerHTML = item.content;
            
            const detalleNombreEl = document.getElementById('detalle-nombre');
            const detalleFechaEl = document.getElementById('detalle-fecha');
            const modalEl = document.getElementById('modalDetalleTimeline');

            if (detalleNombreEl) {
                detalleNombreEl.innerText = (temp.querySelector('.event-content')?.getAttribute('data-realname') || temp.innerText).trim();
            }
            if (detalleFechaEl) {
                detalleFechaEl.innerText = new Date(item.start).toLocaleDateString('en-US', {
                    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
                });
            }
            if (modalEl) {
                bootstrap.Modal.getOrCreateInstance(modalEl).show();
            }
        }
    });
}

function cargarHitos(pid) {
    if (!pid) return;
    fetch(`/api/hitos/proyecto/${pid}`)
        .then(r => r.json())
        .then(hitos => {
            const tbody = document.getElementById('listaHitos');
            if (tbody) {
                tbody.innerHTML = hitos.map(h => `
                    <tr>
                        <td class="ps-4 fw-bold">${h.etapaAsociada}</td>
                        <td class="text-primary fw-bold">${h.fecha}</td>
                        <td><span class="badge bg-primary">${h.porcentajeObjetivo}%</span></td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-danger border-0" onclick="eliminarHito(${h.id})">
                                <i class="bi bi-trash-fill"></i>
                            </button>
                        </td>
                    </tr>`).join('');
            }
        });
}

function cargarElementosParaMain(pid) {
    if (!pid) return;
    fetch(`/api/hitos/proyecto/${pid}/elementos`)
        .then(r => r.json())
        .then(elementos => {
            window.allElementosData = elementos;
            renderizarTablaMainEvents(elementos);
        });
}

function renderizarTablaMainEvents(elementos) {
    const tbody = document.getElementById('listaElementosMain');
    if (!tbody) return;

    elementos = elementos.filter(el => el.fase.includes('Program'));
    tbody.innerHTML = elementos.map(el => `
        <tr>
            <td class="small fw-bold">${el.nombre}</td>
            <td class="small text-muted">${el.etapa}</td>
            <td class="text-center">
                <button class="btn btn-sm ${el.esMainEvent ? 'btn-warning' : 'btn-outline-secondary'} border-0" 
                        onclick="toggleMainEvent(${el.id}, this)">
                    <i class="bi ${el.esMainEvent ? 'bi-star-fill text-warning' : 'bi-star'}"></i>
                </button>
            </td>
        </tr>`).join('') || '<tr><td colspan="3" class="text-center py-5 text-muted fst-italic">No deliverables found for Program</td></tr>';
}

window.guardarHito = function() {
    const selProyecto = document.getElementById('selProyecto');
    const pid = selProyecto ? selProyecto.value : null;
    const etapa = document.getElementById('hitoEtapa').value;
    const fecha = document.getElementById('hitoFecha').value;
    const porc = document.getElementById('hitoPorcentaje').value;

    if (!pid || !fecha) return alert("Select project and date");

    fetch('/api/hitos/guardar', { 
        method: 'POST', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify({proyectoId: pid, nombre: etapa, fecha: fecha, etapa: etapa, porcentaje: porc}) 
    })
    .then(r => { 
        if(r.ok) { 
            cargarHitos(pid); 
            document.getElementById('hitoFecha').value = ''; 
        } 
    });
};

window.toggleMainEvent = function(id, btn) {
    fetch(`/api/hitos/toggle-main-event/${id}`, { method: 'POST' })
        .then(r => { 
            const selProyecto = document.getElementById('selProyecto');
            if(r.ok && selProyecto) cargarElementosParaMain(selProyecto.value); 
        });
};

window.eliminarHito = function(id) {
    if (confirm('Delete?')) {
        fetch(`/api/hitos/${id}`, { method: 'DELETE' })
            .then(r => { 
                const selProyecto = document.getElementById('selProyecto');
                if(r.ok && selProyecto) cargarHitos(selProyecto.value); 
            });
    }
};

window.toggleMainEvent = function(id, btn) {
                const icono = btn.querySelector('i');
                const esMainEvent = btn.classList.contains('btn-warning');

                if (esMainEvent) {
                    btn.classList.remove('btn-warning');
                    btn.classList.add('btn-outline-secondary');
                    icono.classList.remove('bi-star-fill', 'text-warning');
                    icono.classList.add('bi-star');
                } else {
                    btn.classList.remove('btn-outline-secondary');
                    btn.classList.add('btn-warning');
                    icono.classList.remove('bi-star');
                    icono.classList.add('bi-star-fill', 'text-warning');
                }

                fetch(`/api/hitos/toggle-main-event/${id}`, { method: 'POST' })
                .then(r => { 
                    if(!r.ok) {
                        cargarElementosParaMain(); 
                    }
                })
                .catch(err => {
                    console.error("Error guardando el main event", err);
                    cargarElementosParaMain();
                });
            };