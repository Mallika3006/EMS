(() => {
  const key = "ems-ui-theme";
  const root = document.documentElement;
  let saved = "light";
  try { const stored = localStorage.getItem(key); if (stored === "light" || stored === "dark") saved = stored; } catch (_) {}
  function apply(theme) {
    const dark = theme === "dark";
    root.setAttribute("data-theme", dark ? "dark" : "light");
    const button = document.getElementById("emsThemeToggle");
    if (button) {
      button.setAttribute("aria-pressed", String(dark));
      button.setAttribute("aria-label", dark ? "Switch to light theme" : "Switch to dark theme");
      button.textContent = dark ? "☀ Light mode" : "☾ Dark mode";
    }
  }
  function install() {
    if (!document.body || document.getElementById("emsThemeToggle")) return;
    const button = document.createElement("button");
    button.type = "button"; button.id = "emsThemeToggle"; button.className = "theme-toggle";
    button.addEventListener("click", () => {
      const next = root.getAttribute("data-theme") === "dark" ? "light" : "dark";
      try { localStorage.setItem(key, next); } catch (_) {}
      apply(next);
    });
    document.body.appendChild(button);
    apply(root.getAttribute("data-theme") || saved);
  }
  apply(saved);
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", install, { once: true });
  else install();
})();