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
