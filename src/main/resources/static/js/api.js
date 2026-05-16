const API_BASE = "http://localhost:8080/api";

async function apiFetch(endpoint, options = {}) {
    return fetch(API_BASE + endpoint, {
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });
}

async function apiJson(endpoint, options = {}) {
    const response = await apiFetch(endpoint, options);
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Request failed");
    }

    if (data.code && data.code !== "00" && data.code !== "09") {
        throw new Error(data.message || "Backend rejected request");
    }

    return data;
}