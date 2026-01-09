/**
 * common.js - Shared UI components and logic for ScholarNet
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check Authentication and Role
    if (!checkAuth()) return;

    // Inject Header
    injectHeader();

    // Inject Footer
    injectFooter();

    // Handle Authentication UI
    handleAuthUI();

    // Initialize Lucide Icons
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

/**
 * Checks if the user is authenticated and has the correct role for the current path.
 * Redirects to login if unauthorized.
 */
function checkAuth() {
    const path = window.location.pathname;

    // Public pages don't need auth checks here (handled by handleAuthUI for UI only)
    if (!path.includes('/admin/') && !path.includes('/student/') &&
        !path.includes('/reviewer/') && !path.includes('/committee/')) {
        return true;
    }

    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user") || sessionStorage.getItem("user") || "{}");

    if (!token || !user.role) {
        window.location.href = "/login.html";
        return false;
    }

    // Role-based path protection
    if (path.includes('/admin/') && user.role !== 'Admin') {
        window.location.href = "/login.html";
        return false;
    }
    if (path.includes('/student/') && user.role !== 'Student') {
        window.location.href = "/login.html";
        return false;
    }
    if (path.includes('/reviewer/') && user.role !== 'Reviewer') {
        window.location.href = "/login.html";
        return false;
    }
    if (path.includes('/committee/') && user.role !== 'Committee') {
        window.location.href = "/login.html";
        return false;
    }

    return true;
}


function injectHeader() {
    const headerContainer = document.getElementById('header-container');
    if (!headerContainer) return;

    const user = JSON.parse(localStorage.getItem("user") || "{}");
    const path = window.location.pathname;

    // Determine which header to inject
    if (path.includes('/admin/')) {
        injectAdminHeader(headerContainer);
    } else if (path.includes('/student/')) {
        injectStudentHeader(headerContainer);
    } else if (path.includes('/reviewer/')) {
        injectReviewerHeader(headerContainer);
    } else if (path.includes('/committee/')) {
        injectCommitteeHeader(headerContainer);
    } else {
        injectPublicHeader(headerContainer);
    }
}

function injectPublicHeader(container) {
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold">ScholarNet</h1>
                </div>
                <div class="flex items-center gap-8" id="navLinks">
                    <a href="/index.html" class="hover:text-muted-foreground transition-colors inline-flex items-center gap-2 whitespace-nowrap">
                        <i data-lucide="home" class="w-4 h-4"></i>
                        <span>Home</span>
                    </a>
                    <a href="/scholarships.html" class="hover:text-muted-foreground transition-colors inline-flex items-center gap-2 whitespace-nowrap">
                        <i data-lucide="book-open" class="w-4 h-4"></i>
                        <span>Scholarships</span>
                    </a>
                    <a href="/login.html" id="loginLink" class="hover:text-muted-foreground transition-colors inline-flex items-center gap-2 whitespace-nowrap">
                        <i data-lucide="log-in" class="w-4 h-4"></i>
                        <span>Login</span>
                    </a>
                    <a href="/register.html" id="registerLink" class="hover:text-muted-foreground transition-colors inline-flex items-center gap-2 whitespace-nowrap">
                        <i data-lucide="user-plus" class="w-4 h-4"></i>
                        <span>Register</span>
                    </a>
                    <a href="#" id="dashboardLink" style="display: none;" class="hover:text-muted-foreground transition-colors inline-flex items-center gap-2 whitespace-nowrap">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i>
                        <span>Dashboard</span>
                    </a>
                    <button id="logoutBtn" style="display: none;" class="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors inline-flex items-center gap-2 whitespace-nowrap">
                        <i data-lucide="log-out" class="w-4 h-4"></i>
                        <span>Logout</span>
                    </button>
                </div>
            </div>
        </div>
    </nav>
    `;
}

function injectAdminHeader(container) {
    const currentPath = window.location.pathname;
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold">ScholarNet Admin</h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/admin/dashboard-admin.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/admin/users-management.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('users') ? 'font-semibold' : ''}">
                        <i data-lucide="users" class="w-4 h-4"></i> Users
                    </a>
                    <a href="/admin/scholarships-management.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('scholarships') ? 'font-semibold' : ''}">
                        <i data-lucide="award" class="w-4 h-4"></i> Scholarships
                    </a>
                    <a href="/admin/applications.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('applications') ? 'font-semibold' : ''}">
                        <i data-lucide="clipboard-check" class="w-4 h-4"></i> Applications
                    </a>
                    <button id="logoutBtn" class="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-1">
                        <i data-lucide="log-out" class="w-4 h-4"></i> Logout
                    </button>
                </div>
            </div>
        </div>
    </nav>
    `;
}

function injectStudentHeader(container) {
    const currentPath = window.location.pathname;
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold">Student Portal</h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/student/dashboard-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/student/scholarships.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('scholarships') ? 'font-semibold' : ''}">
                        <i data-lucide="award" class="w-4 h-4"></i> Scholarships
                    </a>
                    <a href="/student/my-applications.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('applications') ? 'font-semibold' : ''}">
                        <i data-lucide="file-text" class="w-4 h-4"></i> My Applications
                    </a>
                    <a href="/student/profile.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('profile') ? 'font-semibold' : ''}">
                        <i data-lucide="user" class="w-4 h-4"></i> Profile
                    </a>
                    <button id="logoutBtn" class="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-1">
                        <i data-lucide="log-out" class="w-4 h-4"></i> Logout
                    </button>
                </div>
            </div>
        </div>
    </nav>
    `;
}

function injectReviewerHeader(container) {
    const currentPath = window.location.pathname;
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold">Reviewer Dashboard</h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/reviewer/dashboard-reviewer.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/reviewer/assignments.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('assignments') ? 'font-semibold' : ''}">
                        <i data-lucide="clipboard-list" class="w-4 h-4"></i> Assignments
                    </a>
                    <button id="logoutBtn" class="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-1">
                        <i data-lucide="log-out" class="w-4 h-4"></i> Logout
                    </button>
                </div>
            </div>
        </div>
    </nav>
    `;
}

function injectCommitteeHeader(container) {
    const currentPath = window.location.pathname;
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold">Committee Portal</h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/committee/dashboard-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/committee/applications.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('applications') ? 'font-semibold' : ''}">
                        <i data-lucide="clipboard-list" class="w-4 h-4"></i> Applications
                    </a>
                    <a href="/committee/decisions.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('decisions') ? 'font-semibold' : ''}">
                        <i data-lucide="gavel" class="w-4 h-4"></i> Decisions
                    </a>
                    <a href="/committee/interviews.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('interviews') ? 'font-semibold' : ''}">
                        <i data-lucide="calendar" class="w-4 h-4"></i> Interviews
                    </a>
                    <button id="logoutBtn" class="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
                        <i data-lucide="log-out" class="w-4 h-4"></i> Logout
                    </button>
                </div>
            </div>
        </div>
    </nav>
    `;
}

function injectFooter() {
    const footerContainer = document.getElementById('footer-container');
    if (!footerContainer) return;

    footerContainer.innerHTML = `
    <footer class="bg-muted/50 border-t border-border py-8 mt-auto">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex flex-col md:flex-row justify-between items-center gap-4">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-5 h-5 text-primary"></i>
                    <span class="font-bold text-lg">ScholarNet</span>
                </div>
                <p class="text-center text-muted-foreground text-sm">&copy; 2026 ScholarNet. All rights reserved.</p>
                <div class="flex gap-6">
                    <a href="#" class="text-muted-foreground hover:text-primary transition-colors text-sm">Privacy Policy</a>
                    <a href="#" class="text-muted-foreground hover:text-primary transition-colors text-sm">Terms of Service</a>
                    <a href="#" class="text-muted-foreground hover:text-primary transition-colors text-sm">Contact Us</a>
                </div>
            </div>
        </div>
    </footer>
    `;
}

function handleAuthUI() {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user") || sessionStorage.getItem("user") || "{}");

    const loginLink = document.getElementById("loginLink");
    const registerLink = document.getElementById("registerLink");
    const dashboardLink = document.getElementById("dashboardLink");
    const logoutBtn = document.getElementById("logoutBtn");

    // Re-bind logout buttons because they might have been re-injected
    const allLogoutBtns = document.querySelectorAll('#logoutBtn');
    allLogoutBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            localStorage.clear();
            sessionStorage.clear();
            window.location.href = "/index.html";
        });
    });

    if (token && user.username) {
        if (loginLink) loginLink.style.display = "none";
        if (registerLink) registerLink.style.display = "none";
        if (dashboardLink) {
            dashboardLink.style.display = "inline-flex";
            // Set correct dashboard link based on role
            if (user.role === 'Admin') dashboardLink.href = "/admin/dashboard-admin.html";
            else if (user.role === 'Reviewer') dashboardLink.href = "/reviewer/dashboard-reviewer.html";
            else if (user.role === 'Committee') dashboardLink.href = "/committee/dashboard-committee.html";
            else dashboardLink.href = "/student/dashboard-student.html";
        }
        if (logoutBtn) logoutBtn.style.display = "inline-flex";
    } else {
        if (loginLink) loginLink.style.display = "inline-flex";
        if (registerLink) registerLink.style.display = "inline-flex";
        if (dashboardLink) dashboardLink.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "none";
    }
}

