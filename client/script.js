const API_BASE = 'http://localhost:5000';

// ==================== Auth Functions ====================
function toggleForms() {
    document.getElementById('loginForm').classList.toggle('hidden');
    document.getElementById('signupForm').classList.toggle('hidden');
    clearMessages();
}

async function handleSignup(event) {
    event.preventDefault();
    const messageEl = document.getElementById('signupMessage');
    const username = document.getElementById('signupUsername').value;
    const email = document.getElementById('signupEmail').value;
    const password = document.getElementById('signupPassword').value;

    try {
        const response = await fetch(`${API_BASE}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('access_token', data.access_token);
            localStorage.setItem('userEmail', email);
            showMessage(messageEl, data.message || 'Signup successful!', 'success');
            setTimeout(() => showDashboard(), 1500);
        } else {
            showMessage(messageEl, data.message || 'Signup failed', 'error');
        }
    } catch (error) {
        showMessage(messageEl, 'Error: ' + error.message, 'error');
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const messageEl = document.getElementById('loginMessage');
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('access_token', data.access_token);
            localStorage.setItem('userEmail', email);
            showMessage(messageEl, 'Login successful!', 'success');
            setTimeout(() => showDashboard(), 1500);
        } else {
            showMessage(messageEl, data.message || 'Login failed', 'error');
        }
    } catch (error) {
        showMessage(messageEl, 'Error: ' + error.message, 'error');
    }
}

function handleLogout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('userEmail');
    showAuthPage();
}

// ==================== URL Shortening Functions ====================
async function handleShortenUrl(event) {
    event.preventDefault();
    const messageEl = document.getElementById('shortenMessage');
    const originalUrl = document.getElementById('originalUrl').value;
    const token = localStorage.getItem('access_token');

    try {
        const response = await fetch(`${API_BASE}/url/shorten`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ original_url: originalUrl })
        });

        const data = await response.json();

        if (response.ok) {
            showMessage(messageEl, 'URL shortened successfully!', 'success');
            document.getElementById('originalUrl').value = '';
            setTimeout(() => loadUserUrls(), 1000);
        } else {
            showMessage(messageEl, data.message || 'Failed to shorten URL', 'error');
        }
    } catch (error) {
        showMessage(messageEl, 'Error: ' + error.message, 'error');
    }
}

async function loadUserUrls() {
    const token = localStorage.getItem('access_token');
    const urlsList = document.getElementById('urlsList');

    try {
        urlsList.innerHTML = '<div class="loading"><div class="spinner"></div><p>Loading URLs...</p></div>';

        const response = await fetch(`${API_BASE}/url/list`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const data = await response.json();

        console.log(data);
        if (response.ok && data.urls && data.urls.length > 0) {
            urlsList.innerHTML = data.urls.map(url => `
                <div class="url-item">
                    <div class="url-item-header">
                        <div class="url-item-title">${url.short_code}</div>
                        <button class="copy-btn" onclick="copyToClipboard('${API_BASE}/url/${url.short_code}', this)">Copy</button>
                    </div>
                    <div class="url-item-original">
                        <strong>Original URL:</strong>
                        <a href="${url.original_url}" target="_blank" style="color: #667eea; text-decoration: none;">${truncateUrl(url.original_url)}</a>
                    </div>
                    <div class="url-item-details">
                        <span>📊 Clicks: <strong>${url.click_count}</strong></span>
                        <span>📅 Created: <strong>${formatDate(url.created_at)}</strong></span>
                        <button class="btn-danger" onclick="deleteUrl('${url.id}')">Delete</button>
                    </div>
                </div>
            `).join('');
        } else {
            urlsList.innerHTML = `
                <div class="empty-state">
                    <p>No URLs shortened yet</p>
                    <p>Create your first shortened URL above!</p>
                </div>
            `;
        }
    } catch (error) {
        urlsList.innerHTML = `<div class="empty-state"><p>Error loading URLs: ${error.message}</p></div>`;
    }
}

async function deleteUrl(urlId) {
    if (!confirm('Are you sure you want to delete this URL?')) return;

    const token = localStorage.getItem('access_token');

    try {
        const response = await fetch(`${API_BASE}/url/${urlId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            loadUserUrls();
        } else {
            alert('Failed to delete URL');
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// ==================== Utility Functions ====================
function copyToClipboard(text, button) {
    navigator.clipboard.writeText(text).then(() => {
        const originalText = button.textContent;
        button.textContent = 'Copied!';
        button.classList.add('copied');
        setTimeout(() => {
            button.textContent = originalText;
            button.classList.remove('copied');
        }, 2000);
    });
}

function truncateUrl(url, maxLength = 50) {
    return url.length > maxLength ? url.substring(0, maxLength) + '...' : url;
}

function formatDate(dateString) {
    const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

function showMessage(element, message, type) {
    element.textContent = message;
    element.className = `message ${type} show`;
    setTimeout(() => {
        element.classList.remove('show');
    }, 4000);
}

function clearMessages() {
    document.querySelectorAll('.message').forEach(msg => {
        msg.classList.remove('show');
    });
}

function showAuthPage() {
    document.getElementById('authContainer').classList.remove('hidden');
    document.getElementById('dashboardContainer').classList.add('hidden');
    document.getElementById('loginForm').classList.remove('hidden');
    document.getElementById('signupForm').classList.add('hidden');
}

function showDashboard() {
    document.getElementById('authContainer').classList.add('hidden');
    document.getElementById('dashboardContainer').classList.remove('hidden');
    document.getElementById('userEmail').textContent = localStorage.getItem('userEmail');
    loadUserUrls();
}

// ==================== Initialize ====================
window.addEventListener('load', () => {
    const token = localStorage.getItem('access_token');
    if (token) {
        showDashboard();
    } else {
        showAuthPage();
    }
});

window.addEventListener('scroll', () => {
    const header = document.querySelector('header');
    if (window.scrollY > 50) {
        document.body.classList.add('scrolled');
    } else {
        document.body.classList.remove('scrolled');
    }
});
