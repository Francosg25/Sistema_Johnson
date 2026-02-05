document.addEventListener('DOMContentLoaded', function() {
  var btn = document.getElementById('sidebarToggle');
  var sidebar = document.getElementById('sidebar-wrapper');
  var page = document.getElementById('page-content-wrapper');

  if (!sidebar || !page) return;

  // Ensure initial state
  if (window.innerWidth <= 992) {
    sidebar.classList.add('collapsed');
    page.classList.add('collapsed');
  }

  if (btn) {
    btn.addEventListener('click', function() {
      var isCollapsed = sidebar.classList.toggle('collapsed');
      // toggle page content class for width/margin adjustments
      if (isCollapsed) {
        page.classList.add('collapsed');
      } else {
        page.classList.remove('collapsed');
      }
    });
  }

  // handle window resize to reset classes appropriately
  window.addEventListener('resize', function() {
    if (window.innerWidth <= 992) {
      sidebar.classList.add('collapsed');
      page.classList.add('collapsed');
    } else {
      sidebar.classList.remove('collapsed');
      page.classList.remove('collapsed');
    }
  });
});
