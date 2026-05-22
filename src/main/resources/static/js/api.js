/**
 * src/main/resources/static/js/api.js
 *
 * Central fetch wrapper for all FastLane Academy API calls.
 *
 * KEY FIX — credentials: 'include'
 * ---------------------------------
 * The backend uses Spring HttpSession for auth. The session ID lives in
 * the JSESSIONID cookie that the server sets at POST /api/auth/login.
 * For every subsequent request the browser must send that cookie back,
 * or the server sees no session → SessionUtil.isRole() returns false → 403.
 *
 * When the frontend runs on a different origin from the backend
 * (e.g. VS Code Live Server on :5500 vs Spring Boot on :8080) the browser
 * will NOT send cookies unless:
 *   1. The fetch call sets  credentials: 'include'   ← this file
 *   2. The server sets      Access-Control-Allow-Credentials: true   ← WebConfig.java
 *   3. The server uses an   exact origin, NOT "*"   ← WebConfig.java
 *
 * All three must be true at the same time.
 *
 * Exports (attached to window):
 *   apiJson(path, options?)  → resolves with response.content, throws on error
 */

const API_BASE = '/api';

/**
 * Wrapper around fetch that:
 *  - Prepends /api to every path
 *  - Always sends credentials (session cookie)
 *  - Sets Content-Type: application/json by default
 *  - Throws a descriptive Error when the HTTP status is not ok OR
 *    when the response body's code is not '00' (VarList.RSP_SUCCESS)
 *
 * @param {string} path    - e.g. '/auth/login', '/lessons/createSlot'
 * @param {RequestInit} [options] - standard fetch options (method, body, headers…)
 * @returns {Promise<any>} - resolves with the full ResponseDTO body
 */
async function apiJson(path, options = {}) {
    const url = `${API_BASE}${path}`;

    const response = await fetch(url, {
        // ↓ THIS IS THE CRITICAL LINE — without it every session-protected
        //   endpoint returns 403 because the JSESSIONID cookie is not sent.
        credentials: 'include',

        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },

        ...options,
    });

    // Handle non-2xx HTTP errors (network-level failures, 404, 500, etc.)
    if (!response.ok) {
        let message = "Please fill all required fields correctly.";

        try {
            const errorData = await response.json();

            if (response.status >= 500) {
                message = "Please fill all required fields correctly.";
            } else if (errorData.message) {
                message = errorData.message;
            } else if (errorData.error && errorData.error !== "Internal Server Error") {
                message = errorData.error;
            }

        } catch (_) {
            message = "Please fill all required fields correctly.";
        }

        throw new Error(message);
    }

    const data = await response.json();

    // Application-level error check: VarList.RSP_SUCCESS === '00'
    // Throw so callers can catch with a single try/catch.
    if (data.code && data.code !== '00' && data.code !== '15' && data.code !== '09') {
        throw new Error(data.message || `API error (code ${data.code})`);
    }

    return data;
}

// Expose globally so every other script can call apiJson() without imports
window.apiJson = apiJson;