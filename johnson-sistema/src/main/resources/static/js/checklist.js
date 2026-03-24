       
       let modalComentariosInstance = null;

        function abrirEditorComentarios(itemId, codigo) {
            const textareaReal = document.getElementById('real-comentario-' + itemId);
            const modalTextarea = document.getElementById('editor-textarea');
            const modalCodigo = document.getElementById('editor-codigo');
            const modalItemId = document.getElementById('editor-item-id');

            if(textareaReal) {
                let texto = textareaReal.value || "";
                let htmlResaltado = texto.replace(/@([a-zA-Z0-9_.-]+)/g, '<span class="mention-tag" contenteditable="false">@$1</span>');
                modalTextarea.innerHTML = htmlResaltado;
            }
            if(modalCodigo) modalCodigo.innerText = codigo;
            if(modalItemId) modalItemId.value = itemId;

            const modalElem = document.getElementById('modalEditorComentarios');
            if (!modalComentariosInstance) {
                modalComentariosInstance = new bootstrap.Modal(modalElem);
            }
            modalComentariosInstance.show();
        }

        function guardarComentarioModal() {
            try {
                const itemId = document.getElementById('editor-item-id').value;
                const modalEditor = document.getElementById('editor-textarea');
                const nuevoTexto = modalEditor.innerText; 
                
                const textareaReal = document.getElementById('real-comentario-' + itemId);
                const previewDiv = document.getElementById('preview-comentario-' + itemId);
                const btnIcon = document.getElementById('btn-icon-comentario-' + itemId);
                const btnContenedor = document.getElementById('btn-comentario-' + itemId);

                if(textareaReal) textareaReal.value = nuevoTexto;

                if (nuevoTexto.trim() === "") {
                    if(previewDiv) {
                        previewDiv.innerText = "";
                        previewDiv.classList.add('text-muted', 'fst-italic');
                        previewDiv.classList.remove('text-dark', 'fw-bold');
                    }
                    if(btnIcon) btnIcon.className = 'bi bi-pencil';
                    if(btnContenedor) btnContenedor.className = 'btn btn-sm btn-light text-secondary border-0 rounded-circle ms-1 shadow-sm flex-shrink-0';
                } else {
                    if(previewDiv) {
                        previewDiv.innerText = nuevoTexto;
                        previewDiv.classList.remove('text-muted', 'fst-italic');
                        previewDiv.classList.add('text-dark', 'fw-bold');
                    }
                    if(btnIcon) btnIcon.className = 'bi bi-chat-text-fill text-white';
                    if(btnContenedor) btnContenedor.className = 'btn btn-sm btn-primary border-0 rounded-circle ms-1 shadow-sm flex-shrink-0';
                }

                if (modalComentariosInstance) {
                    modalComentariosInstance.hide();
                }
                
                setTimeout(() => {
                    document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
                    document.body.classList.remove('modal-open');
                    document.body.style.overflow = '';
                    document.body.style.paddingRight = '';
                }, 300);

            } catch (err) {
                console.error("Error al guardar comentario:", err);
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            fetch('/api/usuarios/menciones')
                .then(response => response.json())
                .then(usuariosPlanta => {
                    const tribute = new Tribute({
                        values: usuariosPlanta,
                        selectTemplate: function (item) {
                            return '<span class="mention-tag" contenteditable="false">@' + item.original.value + '</span>&nbsp;';
                        },
                        menuItemTemplate: function (item) {
                            return '<span class="fw-bold text-primary">' + item.original.key + '</span>' +
                                   '<br><small class="text-muted">@' + item.original.value + '</small>';
                        }
                    });

                    const editorTextarea = document.getElementById('editor-textarea');
                    if(editorTextarea) {
                        tribute.attach(editorTextarea);
                    }
                })
                .catch(err => console.error("Error al cargar usuarios para menciones:", err));
        });

    
        document.addEventListener('DOMContentLoaded', function() {
            const apqpTabs = document.getElementById('apqpTabs');
            if (!apqpTabs) return;

            const tabButtons = apqpTabs.querySelectorAll('button[data-bs-toggle="tab"]');
            const lastActiveTabId = localStorage.getItem('lastActiveApqpTab');
            
            // Priorizar la fase actual del proyecto si no hay nada en localStorage
            const proyectoFase = /*[[${proyecto.faseActual}]]*/ 'APQP Program';
            let initialTab = null;
            
            if (lastActiveTabId) {
                initialTab = document.getElementById(lastActiveTabId);
            }
            
            if (!initialTab) {
                // Mapeo de fase a ID de tab
                const mapping = {
                    'APQP Program': 'tab-prog',
                    'Stage 2': 'tab-s2',
                    'Stage 3': 'tab-s3',
                    'Stage 4': 'tab-s4',
                    'Stage 5': 'tab-s5'
                };
                initialTab = document.getElementById(mapping[proyectoFase] || 'tab-prog');
            }

            let tabToActivate = initialTab || tabButtons[0];

            if (tabToActivate && apqpTabs.contains(tabToActivate)) {
                tabButtons.forEach(btn => {
                    btn.classList.remove('active', 'fw-bold', 'bg-white');
                    btn.classList.add('bg-light', 'text-muted');
                    const targetId = btn.getAttribute('data-bs-target');
                    const pane = document.querySelector(targetId);
                    if (pane) pane.classList.remove('show', 'active');
                });

                const bsTab = new bootstrap.Tab(tabToActivate);
                bsTab.show();
                
                tabToActivate.classList.add('active', 'fw-bold', 'bg-white');
                tabToActivate.classList.remove('bg-light', 'text-muted');
            }

            apqpTabs.addEventListener('click', (e) => {
                const btn = e.target.closest('button[data-bs-toggle="tab"]');
                if (btn) {
                    localStorage.setItem('lastActiveApqpTab', btn.id);
                    tabButtons.forEach(b => {
                        const isActive = b === btn;
                        b.classList.toggle('active', isActive);
                        b.classList.toggle('fw-bold', isActive);
                        b.classList.toggle('bg-white', isActive);
                        b.classList.toggle('bg-light', !isActive);
                        b.classList.toggle('text-muted', !isActive);
                    });
                }
            });
        });
    

        function applySelectStyle(select) {
            const classes = ['status-on-time', 'status-late', 'status-decision', 'status-needs-action', 'status-default'];
            select.classList.remove(...classes);
            
            let newClass = 'status-default';
            const val = select.value;
            if (select.name.startsWith('controlEntregable')) {
                if (val === 'Closed on time') newClass = 'status-on-time';
                else if (val === 'Closed late') newClass = 'status-late';
                else if (val === 'DECISION') newClass = 'status-decision';
                else if (val === 'NEEDS ACTION') newClass = 'status-needs-action';
            } else if (select.name.startsWith('score') && val === 'OK') {
                newClass = 'status-on-time';
            }
            select.classList.add(newClass);
        }

        function applyRadioStyle(radio) {
            const group = radio.closest('.btn-group');
            if (!group) return;
            group.querySelectorAll('label').forEach(l => l.classList.remove('btn-success', 'btn-danger', 'btn-secondary', 'text-white'));
            const label = group.querySelector(`label[for="${radio.id}"]`);
            if (label) {
                label.classList.remove('btn-outline-success', 'btn-outline-danger', 'btn-outline-secondary');
                if (radio.value === 'OK') label.classList.add('btn-success', 'text-white');
                else if (radio.value === 'NOK') label.classList.add('btn-danger', 'text-white');
                else if (radio.value === 'NA') label.classList.add('btn-secondary', 'text-white');
            }
        }

        document.addEventListener('change', (e) => {
            if (e.target.tagName === 'SELECT' && (e.target.name.startsWith('controlEntregable') || e.target.name.startsWith('score'))) {
                applySelectStyle(e.target);
                e.target.blur();
            } else if (e.target.type === 'radio' && e.target.name.startsWith('estado-')) {
                applyRadioStyle(e.target);
            }
        });

        document.addEventListener('DOMContentLoaded', () => {
            document.querySelectorAll('select[name^="controlEntregable-"], select[name^="score-"]').forEach(applySelectStyle);
            document.querySelectorAll('input[type="radio"][name^="estado-"]:checked').forEach(applyRadioStyle);
        });
  
        document.addEventListener('DOMContentLoaded', function() {
            const formHitos = document.getElementById('formHitosProyecto');
            if(formHitos) {
                formHitos.querySelectorAll('.update-hito').forEach(input => {
                    input.addEventListener('change', () => {
                        const formData = new FormData(formHitos);
                        const params = new URLSearchParams(formData);
                        
                        // Añadir efecto de carga visual
                        const originalBg = input.style.backgroundColor;
                        input.style.backgroundColor = '#e2e3e5';
                        input.style.color = '#6c757d';

                        fetch(formHitos.action, {
                            method: 'POST',
                            body: params,
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                        })
                        .then(response => response.json())
                        .then(data => {
                            if(data.exito) {
                                // Efecto verde de éxito
                                input.style.backgroundColor = '#d1e7dd';
                                input.style.color = '#0f5132';
                                setTimeout(() => {
                                    input.style.backgroundColor = originalBg;
                                    input.style.color = '';
                                }, 800);
                            }
                        })
                        .catch(err => {
                            console.error('Error guardando hito:', err);
                            input.style.backgroundColor = '#f8d7da';
                        });
                    });
                });
            }
        });
   
        document.addEventListener('DOMContentLoaded', function() {
            document.querySelectorAll('.form-evidencia-ajax').forEach(form => {
                form.addEventListener('submit', function(e) {
                    e.preventDefault(); 
                    
                    let url = this.getAttribute('action'); 
                    let itemId = this.getAttribute('data-item-id'); 
                    let formData = new FormData(this);     
                    let btnSubmit = this.querySelector('.btn-subir');
                    let originalText = btnSubmit.innerHTML;
                    
                    let alertaDiv = document.getElementById('alerta-' + itemId);
                    let listaUl = document.getElementById('lista-' + itemId);
                    let msgVacio = document.getElementById('msg-vacio-' + itemId);
                    
                    btnSubmit.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Uploading...';
                    btnSubmit.disabled = true;

                    fetch(url, {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => response.json())
                    .then(data => {
                        if(data.exito) {
                            alertaDiv.innerHTML = `
                                <div class="alert alert-success alert-dismissible fade show small py-2 shadow-sm" role="alert">
                                    <i class="bi bi-check-circle-fill me-2"></i> ${data.mensaje}
                                    <button type="button" class="btn-close py-2" data-bs-dismiss="alert" aria-label="Close"></button>
                                </div>
                            `;
                            
                            if(msgVacio) {
                                msgVacio.classList.remove('d-flex');
                                msgVacio.classList.add('d-none');
                            }

                            let nombreArchivo = formData.get('archivo').name;
                            let nuevoLi = document.createElement('li');
                            nuevoLi.className = 'list-group-item d-flex justify-content-between align-items-center px-0 bg-transparent border-bottom-dashed py-2';
                            nuevoLi.innerHTML = `
                                <div class="d-flex align-items-center text-truncate pe-3">
                                    <i class="bi bi-file-earmark-check-fill text-success fs-4 me-2"></i>
                                    <span class="text-truncate fw-bold text-dark">${nombreArchivo}</span>
                                </div>
                                <div class="d-flex gap-2">
                                    <a href="/evidencias/descargar/${data.adjuntoId}" class="btn btn-sm btn-light border rounded-circle shadow-sm text-primary" title="Descargar">
                                        <i class="bi bi-download"></i>
                                    </a>
                                    <button type="button" class="btn btn-sm btn-light border rounded-circle shadow-sm text-danger" title="Eliminar" 
                                            onclick="eliminarEvidenciaAjax(${data.adjuntoId}, this, ${itemId})">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </div>
                            `;
                            listaUl.appendChild(nuevoLi);
                            this.reset(); 

                            let btnContenedorEvi = document.getElementById('btn-evidencia-' + itemId);
                            let btnIconEvi = document.getElementById('btn-icon-evidencia-' + itemId);
                            
                            if(btnContenedorEvi) {
                                btnContenedorEvi.className = 'btn btn-sm btn-primary border-0 rounded-circle ms-1 shadow-sm flex-shrink-0';
                            }
                            if(btnIconEvi) {
                                btnIconEvi.className = 'bi bi-paperclip text-white'; 
                            }
                        } else {
                            alertaDiv.innerHTML = `
                                <div class="alert alert-danger alert-dismissible fade show small py-2" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${data.mensaje}
                                    <button type="button" class="btn-close py-2" data-bs-dismiss="alert" aria-label="Close"></button>
                                </div>
                            `;
                        }
                    })
                    .catch(error => {
                        alertaDiv.innerHTML = `
                            <div class="alert alert-danger alert-dismissible fade show small py-2" role="alert">
                                <i class="bi bi-wifi-off me-2"></i> Connection error.
                                <button type="button" class="btn-close py-2" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        `;
                    })
                    .finally(() => {
                        btnSubmit.innerHTML = originalText;
                        btnSubmit.disabled = false;
                        
                        setTimeout(() => {
                            let alertElement = alertaDiv.querySelector('.alert-success');
                            if(alertElement) {
                                let bsAlert = new bootstrap.Alert(alertElement);
                                bsAlert.close();
                            }
                        }, 4000);
                    });
                });
            });
        });

        let modalConfirmacionEliminar = null;

        function eliminarEvidenciaAjax(adjuntoId, botonElemento, itemId) {
            if (!modalConfirmacionEliminar) {
                modalConfirmacionEliminar = new bootstrap.Modal(document.getElementById('modalConfirmarEliminacion'));
            }

            modalConfirmacionEliminar.show();

            document.getElementById('btnConfirmarEliminar').onclick = function() {
                modalConfirmacionEliminar.hide();

                let iconoOriginal = botonElemento.innerHTML;
                botonElemento.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
                botonElemento.disabled = true;

                let formData = new FormData();
                let csrfTokenInput = document.querySelector('meta[name="_csrf"]');
                if (csrfTokenInput) {
                    formData.append('_csrf', csrfTokenInput.getAttribute('content'));
                }
                fetch('/evidencias/eliminar-ajax/' + adjuntoId, {
                    method: 'POST',
                    body: formData
                })
                .then(response => response.json())
                .then(data => {
                    if(data.exito) {
                        let liElement = botonElemento.closest('li');
                        liElement.remove();

                        let listaUl = document.getElementById('lista-' + itemId);
                        let archivosRestantes = listaUl.querySelectorAll('li:not([id^="msg-vacio-"])').length;
                        
                        if (archivosRestantes === 0) {
                            let msgVacio = document.getElementById('msg-vacio-' + itemId);
                            if (msgVacio) {
                                msgVacio.classList.remove('d-none');
                                msgVacio.classList.add('d-flex');
                            }

                            let btnContenedorEvi = document.getElementById('btn-evidencia-' + itemId);
                            let btnIconEvi = document.getElementById('btn-icon-evidencia-' + itemId);
                            
                            if(btnContenedorEvi) {
                                btnContenedorEvi.className = 'btn btn-sm btn-light text-secondary border-0 rounded-circle ms-1 shadow-sm flex-shrink-0';
                            }
                            if(btnIconEvi) {
                                btnIconEvi.className = 'bi bi-paperclip'; 
                            }
                        }
                    } else {
                        alert('Error: ' + data.mensaje);
                        botonElemento.innerHTML = iconoOriginal;
                        botonElemento.disabled = false;
                    }
                })
                .catch(error => {
                    alert('Connection error when trying to delete the file.');
                    botonElemento.innerHTML = iconoOriginal;
                    botonElemento.disabled = false;
                });
            };
        }
    
        
        function procesarReglasNegocioFrontend() {
            const hoy = new Date();
            hoy.setHours(0,0,0,0);

            // Buscamos todos los selects de controlEntregable
            document.querySelectorAll('select[name^="controlEntregable-"]').forEach(selectControl => {
                const itemId = selectControl.name.split('-')[1];
                const inputPlan = document.querySelector(`input[name="fechaPlan-${itemId}"]`);
                const inputReal = document.querySelector(`input[name="fechaReal-${itemId}"]`);
                const selectScore = document.querySelector(`select[name="score-${itemId}"]`);

                if (!inputPlan || !inputPlan.value) return;

                const fechaPlan = new Date(inputPlan.value + 'T00:00:00');
                const scoreOk = (selectScore && selectScore.value === 'OK');
                const currentVal = selectControl.value;

                if (currentVal === 'DECISION' || currentVal === 'NEEDS ACTION') return;

                if (inputReal && inputReal.value) {
                    const fechaReal = new Date(inputReal.value + 'T00:00:00');
                    if (fechaReal > fechaPlan) {
                        selectControl.value = 'Closed late';
                    } else {
                        selectControl.value = 'Closed on time';
                    }
                } else if (fechaPlan < hoy && !scoreOk) {
                    // Si no hay fecha real, pero el plan ya pasó y no está OK
                    selectControl.value = 'Closed late';
                }

                if (currentVal !== selectControl.value) {
                    selectControl.dispatchEvent(new Event('change', { bubbles: true }));
                }
            });
        }

        document.addEventListener('DOMContentLoaded', function() {
            const formChecklist = document.getElementById('formChecklist');
            const btnGuardarTodo = document.getElementById('btnGuardarTodo');

            if (formChecklist) {
                formChecklist.addEventListener('submit', function(e) {
                    e.preventDefault();

                    // Aplicar reglas automáticas (LATE / ON TIME) antes de enviar
                    procesarReglasNegocioFrontend();

                    const formData = new FormData(this);
                    const params = new URLSearchParams(formData); 
                    const url = this.getAttribute('action');
                    const originalBtnContent = btnGuardarTodo.innerHTML;

                    btnGuardarTodo.disabled = true;
                    btnGuardarTodo.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Saving...';

                    fetch(url, {
                        method: 'POST',
                        body: params,
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded'
                        }
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.exito) {
                            btnGuardarTodo.classList.remove('btn-je-primary');
                            btnGuardarTodo.classList.add('btn-success');
                            btnGuardarTodo.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Saved!';
                            
                            setTimeout(() => {
                                btnGuardarTodo.classList.remove('btn-success');
                                btnGuardarTodo.classList.add('btn-je-primary');
                                btnGuardarTodo.innerHTML = originalBtnContent;
                                btnGuardarTodo.disabled = false;
                            }, 2000);
                        } else {
                            alert('Error saving checklist: ' + data.mensaje);
                            btnGuardarTodo.innerHTML = originalBtnContent;
                            btnGuardarTodo.disabled = false;
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('Connection error while saving.');
                        btnGuardarTodo.innerHTML = originalBtnContent;
                        btnGuardarTodo.disabled = false;
                    });
                });
            }
        });
   
    window.addEventListener('load', function() {
        setTimeout(() => {
            const skeleton = document.getElementById('checklist-skeleton');
            const content = document.getElementById('checklist-content');
            
            if(skeleton && content) {
                skeleton.style.display = 'none';
                content.style.display = 'block';
            }
        }, 200);
    });

    document.addEventListener("DOMContentLoaded", function() {
       const configDiv = document.getElementById('checklist-config');
       const esHistorico = configDiv ? configDiv.getAttribute('data-es-historico') === 'true' : false;
       const esVisitante = configDiv ? configDiv.getAttribute('data-es-visitante') === 'true' : false;
        
        if (esHistorico || esVisitante) {
            console.log("Modo Lectura Activado: Bloqueando edición.");
            
            const formElements = document.querySelectorAll('input, select, textarea');
            formElements.forEach(el => {
                el.disabled = true;
                el.style.backgroundColor = '#f8fafc'; 
                el.style.cursor = 'not-allowed';
            });
            
            const actionButtons = document.querySelectorAll('.btn-upload, .btn-primary, .btn-success, .btn-danger, .btn-download, a[href*="descargar"], button[type="submit"]');
            actionButtons.forEach(btn => {
                btn.style.setProperty('display', 'none', 'important');
            });

            document.querySelectorAll('a').forEach(link => {
                if (link.href.includes('descargar')) {
                    link.addEventListener('click', (e) => e.preventDefault());
                    link.style.display = 'none';
                }
            });
        }
    });

        function mostrarNotificacion(mensaje) {
            const vieja = document.getElementById('notificacionSello');
            if(vieja) vieja.remove();

            const toast = document.createElement('div');
            toast.id = 'notificacionSello';
            toast.className = 'animate__animated animate__fadeInUp shadow-lg';
            toast.style.cssText = 'position: fixed; bottom: 30px; right: 30px; background: white; border-left: 5px solid #10b981; border-radius: 8px; padding: 15px 20px; z-index: 9999; display: flex; align-items: center; gap: 15px; color: #1e293b; font-weight: 500; min-width: 300px;';
            
            toast.innerHTML = `
                <i class="bi bi-patch-check-fill text-success" style="font-size: 1.5rem;"></i>
                <span>${mensaje}</span>
            `;
            
            document.body.appendChild(toast);
            
            setTimeout(() => {
                toast.classList.replace('animate__fadeInUp', 'animate__fadeOutDown');
                setTimeout(() => toast.remove(), 1000);
            }, 4000);
        }

        document.addEventListener('DOMContentLoaded', function() {
    let firmaData = {}; 
    const modalElement = document.getElementById('modalConfirmarFirma');
    if(!modalElement) return; 
    
    const modalFirma = new bootstrap.Modal(modalElement);
    
    // Trigger for already signed boxes
    document.querySelectorAll('.btn-firmado-trigger').forEach(box => {
        box.addEventListener('click', function() {
            const firmante = this.getAttribute('data-firmante');
            const fecha = this.getAttribute('data-fecha');
            // Translation: "Gate sealed by [Name] on [Date]"
            mostrarNotificacion(`Gate sealed by <b>${firmante}</b> on ${fecha}.`);
        });
    });

    // Trigger to open the signing modal
    document.querySelectorAll('.btn-firmar-trigger').forEach(box => {
        box.addEventListener('click', function() {
            const esVisitante = /*[[${#authorization.expression('hasRole(''VIEWER'')')}]]*/ false;
            if(esVisitante) return; 

            firmaData = {
                proyectoId: this.getAttribute('data-proyecto'),
                etapa: this.getAttribute('data-etapa'),
                rol: this.getAttribute('data-rol')
            };

            document.getElementById('modalGateText').textContent = firmaData.etapa;
            document.getElementById('modalRolText').textContent = firmaData.rol;
            
            modalFirma.show();
        });
    });

    const btnConfirmar = document.getElementById('btnConfirmarFirmaAjax');
    if(btnConfirmar) {
        btnConfirmar.addEventListener('click', function() {
            const originalHtml = this.innerHTML;
            this.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Sealing...';
            this.disabled = true;

            const params = new URLSearchParams();
            params.append('etapa', firmaData.etapa);
            params.append('rol', firmaData.rol);

            fetch(`/proyectos/checklist/firmar-ajax/${firmaData.proyectoId}`, {
                method: 'POST',
                body: params,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            })
            .then(response => {
                if(!response.ok) throw new Error("Server error");
                return response.json();
            })
            .then(data => {
                if(data.exito) {
                    const modalBody = document.querySelector('#modalConfirmarFirma .modal-body');
                    const modalFooter = document.querySelector('#modalConfirmarFirma .modal-footer');
                    
                    if(modalBody) {
                        modalBody.innerHTML = `
                            <div class="animate__animated animate__zoomIn text-center">
                                <div class="mb-3">
                                    <i class="bi bi-patch-check-fill text-success" style="font-size: 5.5rem; filter: drop-shadow(0 4px 10px rgba(25,135,84,0.3));"></i>
                                </div>
                                <h2 class="fw-bold text-dark mb-2">Gate Sealed!</h2>
                                <p class="text-muted fs-5 mb-0">The approval from <br><b class="text-primary">${firmaData.rol}</b><br> has been successfully recorded.</p>
                            </div>
                        `;
                    }
                    if(modalFooter) modalFooter.style.display = 'none';
                    
                    setTimeout(() => { window.location.reload(); }, 1800);
                } else {
                    alert("Attention: " + data.mensaje);
                    this.innerHTML = originalHtml; 
                    this.disabled = false;
                    modalFirma.hide();
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Connection error. The signing process was cancelled.");
                this.innerHTML = originalHtml; 
                this.disabled = false;
                modalFirma.hide();
            });
        });
    }
});