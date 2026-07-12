export const API_BASE='http://localhost:8080';

export function loadStoredAuth() {
    return {
        token: localStorage.getItem('token') || '',
        userName: localStorage.getItem('userName') || '',
        userEmail: localStorage.getItem('userEmail') || '',
    };
}

export function saveStoredAuth(authResponse) {
    localStorage.serItem('token', authResponse.token);
    localStorage.setItem('userName', authResponse.name);
    localStorage.setItem('userEmail', authResponse.email);
}

export function clearStoredAuth() {
    localStorage.clear();
}

export function buildAuthHeaders(token, extraHeaders = {}) {
    return {
        'content-Type': 'application/json',
        ...(token ? {Authorization: `Bearer ${token}` } : {}),
        ...extraHeaders,
    };
}

export async function apiFetch(endpoint, token, option = {}) {

}