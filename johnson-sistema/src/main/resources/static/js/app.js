document.addEventListener('DOMContentLoaded', function() {
  // Delegación de eventos para el botón toggle (funciona aunque el header cargue después)
  document.addEventListener('click', function(e) {
    var btn = e.target.closest('#sidebarToggle');
    if (btn) {
      e.preventDefault();
      var wrapper = document.getElementById('wrapper');
      if (wrapper) {
        wrapper.classList.toggle('toggled');
        // Guardar preferencia
        localStorage.setItem('sidebarToggled', wrapper.classList.contains('toggled'));
        window.dispatchEvent(new Event('resize'));
      }
    }
  });

  // Recuperar estado previo al cargar la página
  var wrapper = document.getElementById('wrapper');
  if (wrapper) {
    var savedState = localStorage.getItem('sidebarToggled');
    if (savedState === 'true' || (savedState === null && window.innerWidth <= 768)) {
      wrapper.classList.add('toggled');
    }
  }
});

// Ayudante Global para CSRF en peticiones Fetch/AJAX
const CSRF = {
    getToken: () => document.querySelector('meta[name="_csrf"]')?.content,
    getHeader: () => document.querySelector('meta[name="_csrf_header"]')?.content,
    getHeaders: (baseHeaders = {}) => {
        const headers = { ...baseHeaders };
        const token = CSRF.getToken();
        const header = CSRF.getHeader();
        if (token && header) {
            headers[header] = token;
        }
        return headers;
    }
};

// Reemplazo global de fetch para incluir CSRF automáticamente en peticiones POST/PUT/DELETE
const originalFetch = window.fetch;
window.fetch = function() {
    let [resource, config] = arguments;
    if (config && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(config.method?.toUpperCase())) {
        config.headers = CSRF.getHeaders(config.headers || {});
    }
    return originalFetch(resource, config);
};
