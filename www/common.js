/**
 * common.js - Shared UI components and logic for ScholarNet
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check Authentication and Role
    if (!checkAuth()) return;

    // Inject Global Styles
    injectGlobalStyles();

    // Inject Header
    injectHeader();

    // Inject Footer
    injectFooter();

    // Handle Authentication UI
    handleAuthUI();

    // Initialize Notification Badge
    updateNotificationBadge();

    // Initialize Password Toggles
    initPasswordToggles();

    // Initialize Lucide Icons
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

/**
 * Injects global CSS for UI/UX enhancements.
 */
function injectGlobalStyles() {
    const style = document.createElement('style');
    style.textContent = `
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

        :root {
            --glass-bg: rgba(255, 255, 255, 0.7);
            --glass-border: rgba(255, 255, 255, 0.3);
            --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
            --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            --shadow-soft: 0 4px 20px -5px rgba(0, 0, 0, 0.05);
        }

        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif !important;
            background-color: #f8fafc !important; /* Subtle slate-50 background tint */
            scroll-behavior: smooth;
        }

        /* Glassmorphism for Header */
        nav {
            backdrop-filter: blur(12px) !important;
            -webkit-backdrop-filter: blur(12px) !important;
            background-color: rgba(15, 23, 42, 0.85) !important; /* Primary color with transparency */
            position: sticky !important;
            top: 0;
            z-index: 50;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
        }

        /* 3D Visuals for major action elements */
        button.bg-primary, 
        button.bg-secondary,
        a.bg-primary, 
        a.bg-secondary,
        .btn-3d {
            position: relative !important;
            border-bottom: 4px solid rgba(0, 0, 0, 0.2) !important;
            transition: all 0.1s ease !important;
        }

        button.bg-primary:active, 
        button.bg-secondary:active,
        a.bg-primary:active, 
        a.bg-secondary:active,
        .btn-3d:active {
            transform: translateY(2px) !important;
            border-bottom-width: 0 !important;
            margin-bottom: 2px !important;
        }

        /* Adjust specific border colors to match button backgrounds */
        .bg-primary { border-bottom-color: rgba(0, 0, 0, 0.4) !important; }
        .bg-secondary { border-bottom-color: rgba(0, 0, 0, 0.1) !important; }
        
        /* 3D Inputs - being careful not to cover icons */
        input.bg-background, 
        select.bg-background, 
        textarea.bg-background { 
            border-bottom: 4px solid rgba(0, 0, 0, 0.05) !important;
            transition: all 0.1s ease !important;
        }

        /* Ensure icons stay visible when inputs are transformed on focus */
        .relative div.absolute, 
        .relative i[data-lucide], 
        .password-toggle {
            z-index: 10 !important;
        }

        input.bg-background:focus {
            transform: translateY(1px);
            border-bottom-width: 2px !important;
        }

        /* 3D Cards */
        .bg-card, .bg-white, .rounded-xl:not(.rounded-full) {
            background-color: #ffffff !important;
            border: 2px solid rgba(0, 0, 0, 0.05) !important;
            border-bottom-width: 6px !important;
            box-shadow: var(--shadow-soft) !important;
            transition: transform 0.2s ease, box-shadow 0.2s ease !important;
        }

        .bg-card:hover {
            transform: translateY(-2px);
            border-bottom-width: 8px !important;
            box-shadow: var(--shadow-xl) !important;
        }

        .bg-card:active {
            transform: translateY(4px);
            border-bottom-width: 2px !important;
        }

        /* Hero Section Animation */
        section.bg-gradient-to-br {
            position: relative;
            overflow: hidden;
        }

        section.bg-gradient-to-br::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 60%);
            animation: pulse 15s infinite linear;
            pointer-events: none;
        }

        @keyframes pulse {
            0% { transform: scale(1) translate(0, 0); }
            50% { transform: scale(1.1) translate(-5%, -5%); }
            100% { transform: scale(1) translate(0, 0); }
        }

        /* Entry Animations */
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        main > section {
            animation: fadeInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
        }

        .grid > div {
            opacity: 0;
            animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
        }

        .grid > div:nth-child(1) { animation-delay: 0.1s; }
        .grid > div:nth-child(2) { animation-delay: 0.2s; }
        .grid > div:nth-child(3) { animation-delay: 0.3s; }
        .grid > div:nth-child(4) { animation-delay: 0.4s; }

        /* Button & Link Hover Effects */
        a, button {
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
        }

        button:active, a:active {
            transform: scale(0.96);
        }

        /* Subtle Background Highlights */
        .bg-muted\\/30 {
            background-color: rgba(241, 245, 249, 0.5) !important; /* Subtle highlight */
        }

        /* Custom Scrollbar for modern look */
        ::-webkit-scrollbar {
            width: 10px;
        }
        ::-webkit-scrollbar-track {
            background: #f1f5f9;
        }
        ::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 5px;
            border: 2px solid #f1f5f9;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: #94a3b8;
        }
    `;
    document.head.appendChild(style);
}

/**
 * Initializes peek/visibility toggles for all password fields.
 * Looks for <input type="password"> and adds a toggle button if it's in a .relative container.
 */
function initPasswordToggles() {
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
        const container = input.closest('.relative');
        if (!container) return;

        // Check if toggle already exists
        if (container.querySelector('.password-toggle')) return;

        // Adjust input padding to make room for toggle
        input.classList.add('pr-12');

        const toggleBtn = document.createElement('button');
        toggleBtn.type = 'button';
        toggleBtn.className = 'password-toggle absolute inset-y-0 right-0 pr-3 flex items-center text-muted-foreground hover:text-foreground transition-colors focus:outline-none';
        toggleBtn.innerHTML = '<i data-lucide="eye" class="w-5 h-5"></i>';

        const showPassword = () => {
            input.type = 'text';
            toggleBtn.innerHTML = '<i data-lucide="eye-off" class="w-5 h-5 text-primary"></i>';
            if (window.lucide) window.lucide.createIcons();
        };

        const hidePassword = () => {
            input.type = 'password';
            toggleBtn.innerHTML = '<i data-lucide="eye" class="w-5 h-5"></i>';
            if (window.lucide) window.lucide.createIcons();
        };

        // Mouse events
        toggleBtn.addEventListener('mousedown', showPassword);
        toggleBtn.addEventListener('mouseup', hidePassword);
        toggleBtn.addEventListener('mouseleave', hidePassword);

        // Touch events
        toggleBtn.addEventListener('touchstart', (e) => {
            e.preventDefault(); // Prevent context menu
            showPassword();
        });
        toggleBtn.addEventListener('touchend', hidePassword);
        toggleBtn.addEventListener('touchcancel', hidePassword);

        container.appendChild(toggleBtn);
    });
}

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

    if (path.includes('/student/') && user.role === 'Student') {
        // Profile check disabled to prevent loops
    }

    return true;
}

/**
 * Quick check for profile completion based on local storage
 */
function isProfileComplete(user) {
    if (!user) return false;
    // Basic fields required
    // Ensure we handle 0 as a valid value for cgpa and familyIncome
    return user.qualification &&
        user.major &&
        user.cgpa !== undefined && user.cgpa !== null &&
        user.yearOfStudy &&
        user.familyIncome !== undefined && user.familyIncome !== null;
}

/**
 * Verify profile completion with the server
 */
async function checkProfileOnServer(userId) {
    try {
        const response = await fetch(`/api/users?id=${userId}`);
        if (response.ok) {
            const data = await response.json();
            const profile = data.user;

            // Update local storage with latest data
            const currentUser = JSON.parse(localStorage.getItem("user") || sessionStorage.getItem("user") || "{}");
            currentUser.id = profile.userId; // Sync IDs
            Object.assign(currentUser, profile);
            localStorage.setItem("user", JSON.stringify(currentUser));

            return isProfileComplete(profile);
        }
    } catch (e) {
        console.error("Profile check failed", e);
    }
    return true; // Default to true on error to avoid lockout loops
}


function injectHeader() {
    const headerContainer = document.getElementById('header-container');
    if (!headerContainer) return;

    const user = JSON.parse(localStorage.getItem("user") || sessionStorage.getItem("user") || "{}");
    const path = window.location.pathname;

    // Determine which header to inject
    if (user.role === 'Admin' && (path.includes('/admin/') || path.includes('profile-') || path.includes('notifications-'))) {
        injectAdminHeader(headerContainer);
    } else if (user.role === 'Student' && (path.includes('/student/') || path.includes('profile-') || path.includes('notifications-'))) {
        injectStudentHeader(headerContainer);
    } else if (user.role === 'Reviewer' && (path.includes('/reviewer/') || path.includes('profile-') || path.includes('notifications-'))) {
        injectReviewerHeader(headerContainer);
    } else if (user.role === 'Committee' && (path.includes('/committee/') || path.includes('profile-') || path.includes('notifications-'))) {
        injectCommitteeHeader(headerContainer);
    } else {
        injectPublicHeader(headerContainer);
    }
}

function injectPublicHeader(container) {
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold"><a href="/index.html">ScholarNet</a></h1>
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
        <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold"><a href="/admin/dashboard-admin.html">Admin Portal</a></h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/admin/dashboard-admin.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/admin/users-management.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('users') ? 'font-semibold' : ''}">
                        <i data-lucide="users" class="w-4 h-4"></i> Users
                    </a>
                    <a href="/admin/application-management-admin.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('application-management-admin') ? 'font-semibold' : ''}">
                        <i data-lucide="award" class="w-4 h-4"></i> Scholarships
                    </a>
                    <a href="/admin/applications-management.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('applications') ? 'font-semibold' : ''}">
                        <i data-lucide="clipboard-check" class="w-4 h-4"></i> Applications
                    </a>
                    <a href="/admin/notifications-admin.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('notifications') ? 'font-semibold' : ''}">
                        <i data-lucide="bell" class="w-4 h-4"></i> Notifications
                        <span id="notif-badge" class="flex items-center justify-center w-5 h-5 bg-muted text-muted-foreground opacity-30 text-[10px] font-bold rounded-full text-center">0</span>
                    </a>
                    <a href="/admin/inquiry-admin.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('inquiry') ? 'font-semibold' : ''}">
                        <i data-lucide="message-square" class="w-4 h-4"></i> Inquiries
                    </a>
                    <a href="/admin/profile-admin.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('profile') ? 'font-semibold' : ''}">
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

function injectStudentHeader(container) {
    const currentPath = window.location.pathname;
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold"><a href="/student/dashboard-student.html">Student Portal</a></h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/student/dashboard-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/student/scholarships-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('scholarships') ? 'font-semibold' : ''}">
                        <i data-lucide="award" class="w-4 h-4"></i> Scholarships
                    </a>
                    <a href="/student/applications-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('applications') ? 'font-semibold' : ''}">
                        <i data-lucide="file-text" class="w-4 h-4"></i> My Applications
                    </a>
                    <a href="/student/interviews-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('interviews') ? 'font-semibold' : ''}">
                        <i data-lucide="video" class="w-4 h-4"></i> Interviews
                    </a>
                    <a href="/student/notifications-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('notifications') ? 'font-semibold' : ''}">
                        <i data-lucide="bell" class="w-4 h-4"></i> Notifications
                        <span id="notif-badge" class="flex items-center justify-center w-5 h-5 bg-muted text-muted-foreground opacity-30 text-[10px] font-bold rounded-full text-center">0</span>
                    </a>
                    <a href="/student/inquiry-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('inquiry') ? 'font-semibold' : ''}">
                        <i data-lucide="help-circle" class="w-4 h-4"></i> My Inquiries
                    </a>
                    <a href="/student/profile-student.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('profile') ? 'font-semibold' : ''}">
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
        <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold"><a href="/reviewer/dashboard-reviewer.html">Reviewer Portal</a></h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/reviewer/dashboard-reviewer.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/reviewer/application-assignments.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('assignments') ? 'font-semibold' : ''}">
                        <i data-lucide="clipboard-list" class="w-4 h-4"></i> Assignments
                    </a>
                    <a href="/reviewer/notifications-reviewer.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('notifications') ? 'font-semibold' : ''}">
                        <i data-lucide="bell" class="w-4 h-4"></i> Notifications
                        <span id="notif-badge" class="flex items-center justify-center w-5 h-5 bg-muted text-muted-foreground opacity-30 text-[10px] font-bold rounded-full text-center">0</span>
                    </a>
                    <a href="/reviewer/clarification-reviewer.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('clarification') ? 'font-semibold' : ''}">
                        <i data-lucide="message-circle-question" class="w-4 h-4"></i> Clarifications
                    </a>
                    <a href="/reviewer/profile-reviewer.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('profile') ? 'font-semibold' : ''}">
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

function injectCommitteeHeader(container) {
    const currentPath = window.location.pathname;
    container.innerHTML = `
    <nav class="bg-primary text-primary-foreground border-b border-border">
        <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-6 h-6"></i>
                    <h1 class="text-xl font-bold"><a href="/committee/dashboard-committee.html">Committee Portal</a></h1>
                </div>
                <div class="flex items-center gap-6">
                    <a href="/committee/dashboard-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('dashboard') ? 'font-semibold' : ''}">
                        <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                    </a>
                    <a href="/committee/applications-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('applications') ? 'font-semibold' : ''}">
                        <i data-lucide="clipboard-list" class="w-4 h-4"></i> Applications
                    </a>
                    <a href="/committee/application-decisions.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('decisions') ? 'font-semibold' : ''}">
                        <i data-lucide="gavel" class="w-4 h-4"></i> Decisions
                    </a>
                    <a href="/committee/application-management-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('application-management-committee') ? 'font-semibold' : ''}">
                        <i data-lucide="award" class="w-4 h-4"></i> Scholarships
                    </a>
                    <a href="/committee/schedule-interviews.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('interviews') ? 'font-semibold' : ''}">
                        <i data-lucide="calendar" class="w-4 h-4"></i> Interviews
                    </a>
                    <a href="/committee/clarification-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('clarification') ? 'font-semibold' : ''}">
                        <i data-lucide="message-circle-question" class="w-4 h-4"></i> Clarifications
                    </a>
                    <a href="/committee/notifications-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-2 ${currentPath.includes('notifications') ? 'font-semibold' : ''}">
                        <i data-lucide="bell" class="w-4 h-4"></i> Notifications
                        <span id="notif-badge" class="flex items-center justify-center w-5 h-5 bg-muted text-muted-foreground opacity-30 text-[10px] font-bold rounded-full text-center">0</span>
                    </a>
                    <a href="/committee/profile-committee.html" class="hover:text-muted-foreground transition-colors flex items-center gap-1 ${currentPath.includes('profile') ? 'font-semibold' : ''}">
                        <i data-lucide="user" class="w-4 h-4"></i> Profile
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
        <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex flex-col md:flex-row justify-between items-center gap-4">
                <div class="flex items-center gap-2">
                    <i data-lucide="graduation-cap" class="w-5 h-5 text-primary"></i>
                    <span class="font-bold text-lg">ScholarNet</span>
                </div>
                <p class="text-center text-muted-foreground text-sm">&copy; 2026 ScholarNet. All rights reserved.</p>
                <div class="flex gap-6">
                    <a href="/privacy-policy.html" class="text-muted-foreground hover:text-primary transition-colors text-sm">Privacy Policy</a>
                    <a href="/terms-of-service.html" class="text-muted-foreground hover:text-primary transition-colors text-sm">Terms of Service</a>
                    <a href="/contact-us.html" class="text-muted-foreground hover:text-primary transition-colors text-sm">Contact Us</a>
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

    if (token && user.fullName) {
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

/**
 * Fetches notifications count and updates the header badge
 */
async function updateNotificationBadge() {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user") || sessionStorage.getItem("user") || "{}");
    const badge = document.getElementById('notif-badge');

    if (!token || !user.id || !badge) return;

    try {
        const response = await fetch(`/api/notifications?userId=${user.id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            const notifications = data.notifications || [];
            const unreadCount = notifications.filter(n => !n.isRead).length;

            badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
            if (unreadCount > 0) {
                badge.classList.add('bg-destructive', 'text-destructive-foreground');
                badge.classList.remove('bg-muted', 'text-muted-foreground', 'opacity-30');
            } else {
                badge.classList.remove('bg-destructive', 'text-destructive-foreground');
                badge.classList.add('bg-muted', 'text-muted-foreground', 'opacity-30');
            }
        }
    } catch (e) {
        console.error("Failed to update notification badge", e);
    }
}
