const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/v1';

/**
 * Helper function to handle API requests and JWT token attachment.
 */
async function fetchWithAuth(endpoint, options = {}) {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token && token !== 'undefined' && token !== 'null') {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'Something went wrong');
  }

  return data.data; // Our backend wraps responses in an 'ApiResponse' with a 'data' field
}

export const api = {
  // --- Auth ---
  login: (credentials) => 
    fetchWithAuth('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
    
  register: (userData) => 
    fetchWithAuth('/auth/register', { method: 'POST', body: JSON.stringify(userData) }),

  // --- URLs ---
  getUserUrls: (page = 0, size = 10) => 
    fetchWithAuth(`/urls?page=${page}&size=${size}`),
    
  createShortUrl: (urlData) => 
    fetchWithAuth('/urls', { method: 'POST', body: JSON.stringify(urlData) }),
};
