// Auth check and navigation update
document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token")
  const user = JSON.parse(localStorage.getItem("user") || "{}")

  const loginLink = document.getElementById("loginLink")
  const registerLink = document.getElementById("registerLink")
  const dashboardLink = document.getElementById("dashboardLink")
  const logoutBtn = document.getElementById("logoutBtn")

  if (token && user.username) {
    // User is logged in
    if (loginLink) loginLink.style.display = "none"
    if (registerLink) registerLink.style.display = "none"
    if (dashboardLink) dashboardLink.style.display = "inline-flex" // Fixed: use inline-flex
    if (logoutBtn) {
      logoutBtn.style.display = "inline-flex" // Fixed: use inline-flex
      logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        window.location.href = "/index.html"
      })
    }
  } else {
    // User is not logged in
    if (loginLink) loginLink.style.display = "inline-flex" // Fixed: use inline-flex
    if (registerLink) registerLink.style.display = "inline-flex" // Fixed: use inline-flex
    if (dashboardLink) dashboardLink.style.display = "none"
    if (logoutBtn) logoutBtn.style.display = "none"
  }
})
