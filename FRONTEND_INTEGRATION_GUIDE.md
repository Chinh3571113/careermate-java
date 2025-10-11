# Frontend Integration Guide - Token Rotation System

## Overview
This system implements automatic token rotation with refresh tokens stored in HTTP-only cookies. When the access token expires (or has 5 seconds left), the system automatically refreshes it using the refresh token from the cookie.

## Key Features
- ✅ Access token sent to frontend for storage
- ✅ Refresh token stored in HTTP-only cookie (protected from XSS)
- ✅ Automatic token rotation when access token expires
- ✅ Old tokens immediately invalidated after rotation
- ✅ Proper logout handling with token cleanup

## Backend Configuration

### Token Lifetimes (application.yml)
```yaml
jwt:
  valid-duration: 900        # Access token: 15 minutes (900 seconds)
  refreshable-duration: 604800 # Refresh token: 7 days (604800 seconds)
```

### CORS Configuration
The backend is configured to accept credentials from `http://localhost:3000`. Update `SecurityConfig.java` if your frontend runs on a different port.

## Frontend Implementation

### 1. Axios Configuration with Automatic Token Rotation

Create `src/services/axiosInstance.js`:

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';
const TOKEN_REFRESH_THRESHOLD = 5000; // 5 seconds before expiry

// Create axios instance
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Important: enables cookies
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token state
let accessToken = null;
let tokenExpiresAt = null;
let isRefreshing = false;
let refreshSubscribers = [];

// Subscribe to token refresh
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback);
};

// Notify all subscribers when token is refreshed
const onTokenRefreshed = (token) => {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
};

// Check if token needs refresh (5 seconds before expiry)
const shouldRefreshToken = () => {
  if (!tokenExpiresAt) return false;
  const now = Date.now();
  return tokenExpiresAt - now <= TOKEN_REFRESH_THRESHOLD;
};

// Refresh token function
const refreshAccessToken = async () => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/auth/refresh`,
      {},
      { withCredentials: true }
    );
    
    const { accessToken: newToken, expiresIn } = response.data.result;
    
    // Update token state
    accessToken = newToken;
    tokenExpiresAt = Date.now() + expiresIn * 1000;
    
    // Save to localStorage (optional)
    localStorage.setItem('accessToken', newToken);
    localStorage.setItem('tokenExpiresAt', tokenExpiresAt.toString());
    
    return newToken;
  } catch (error) {
    console.error('Token refresh failed:', error);
    // Clear tokens and redirect to login
    clearTokens();
    window.location.href = '/login';
    throw error;
  }
};

// Request interceptor
axiosInstance.interceptors.request.use(
  async (config) => {
    // Skip token check for auth endpoints
    if (config.url.includes('/auth/token') || config.url.includes('/auth/refresh')) {
      return config;
    }

    // Check if token needs refresh
    if (accessToken && shouldRefreshToken() && !isRefreshing) {
      isRefreshing = true;
      try {
        const newToken = await refreshAccessToken();
        onTokenRefreshed(newToken);
      } catch (error) {
        return Promise.reject(error);
      } finally {
        isRefreshing = false;
      }
    }

    // If token is being refreshed, wait for it
    if (isRefreshing) {
      return new Promise((resolve) => {
        subscribeTokenRefresh((token) => {
          config.headers.Authorization = `Bearer ${token}`;
          resolve(config);
        });
      });
    }

    // Add access token to request
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If 401 and not already retried, try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(axiosInstance(originalRequest));
          });
        });
      }

      isRefreshing = true;

      try {
        const newToken = await refreshAccessToken();
        onTokenRefreshed(newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Initialize tokens from localStorage on app start
export const initializeAuth = () => {
  const storedToken = localStorage.getItem('accessToken');
  const storedExpiry = localStorage.getItem('tokenExpiresAt');
  
  if (storedToken && storedExpiry) {
    const expiryTime = parseInt(storedExpiry);
    if (expiryTime > Date.now()) {
      accessToken = storedToken;
      tokenExpiresAt = expiryTime;
    } else {
      clearTokens();
    }
  }
};

// Clear tokens
export const clearTokens = () => {
  accessToken = null;
  tokenExpiresAt = null;
  localStorage.removeItem('accessToken');
  localStorage.removeItem('tokenExpiresAt');
};

// Set tokens (called after login)
export const setTokens = (token, expiresIn) => {
  accessToken = token;
  tokenExpiresAt = Date.now() + expiresIn * 1000;
  localStorage.setItem('accessToken', token);
  localStorage.setItem('tokenExpiresAt', tokenExpiresAt.toString());
};

// Get current access token
export const getAccessToken = () => accessToken;

export default axiosInstance;
```

### 2. Authentication Service

Create `src/services/authService.js`:

```javascript
import axiosInstance, { setTokens, clearTokens } from './axiosInstance';

const authService = {
  // Login
  async login(email, password) {
    try {
      const response = await axiosInstance.post('/auth/token', {
        email,
        password,
      });

      const { accessToken, expiresIn } = response.data.result;
      
      // Save access token (refresh token is in cookie)
      setTokens(accessToken, expiresIn);
      
      return response.data;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  },

  // Logout
  async logout() {
    try {
      await axiosInstance.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear tokens regardless of API call result
      clearTokens();
    }
  },

  // Check if user is authenticated
  isAuthenticated() {
    const token = localStorage.getItem('accessToken');
    const expiresAt = localStorage.getItem('tokenExpiresAt');
    
    if (!token || !expiresAt) return false;
    
    return parseInt(expiresAt) > Date.now();
  },
};

export default authService;
```

### 3. React App Integration

Update `src/App.js`:

```javascript
import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { initializeAuth } from './services/axiosInstance';
import authService from './services/authService';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  useEffect(() => {
    // Initialize authentication state on app load
    initializeAuth();
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

### 4. Login Component Example

Create `src/pages/Login.js`:

```javascript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
    </div>
  );
};

export default Login;
```

### 5. Logout Example

```javascript
import authService from '../services/authService';

const LogoutButton = () => {
  const handleLogout = async () => {
    await authService.logout();
    window.location.href = '/login';
  };

  return <button onClick={handleLogout}>Logout</button>;
};
```

## How It Works

### Login Flow
1. User submits credentials
2. Backend validates and returns access token in response body
3. Backend sets refresh token in HTTP-only cookie
4. Frontend stores access token in memory and localStorage
5. Frontend calculates expiration time

### Automatic Token Rotation
1. Before each API request, interceptor checks token expiration
2. If token expires in ≤5 seconds, automatically call `/auth/refresh`
3. Backend validates refresh token from cookie
4. Backend invalidates old refresh token
5. Backend returns new access token and sets new refresh token in cookie
6. Frontend updates stored access token

### API Request Flow
1. Interceptor adds access token to Authorization header
2. If 401 response, automatically try token refresh
3. Retry original request with new token
4. If refresh fails, redirect to login

### Logout Flow
1. Call `/auth/logout` endpoint
2. Backend invalidates refresh token in database
3. Backend clears refresh token cookie
4. Frontend clears access token from localStorage
5. Redirect to login page

## Security Features

✅ **Refresh token in HTTP-only cookie** - Protected from XSS attacks  
✅ **Access token in memory** - Cleared on page refresh for additional security  
✅ **Token rotation** - New tokens on every refresh, old tokens invalidated  
✅ **Automatic expiration** - Tokens automatically expire after defined duration  
✅ **CSRF protection** - Cookies with SameSite policy  
✅ **Credentials required** - CORS configured with credentials support

## Testing

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  -c cookies.txt
```

### Test Refresh (with cookies)
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b cookies.txt \
  -c cookies.txt
```

### Test Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

## Production Considerations

1. **Enable HTTPS** - Set `cookie.setSecure(true)` in production
2. **Update CORS origins** - Add production frontend URL to CORS configuration
3. **Environment variables** - Use environment variables for API URLs
4. **Token expiration** - Adjust token lifetimes based on security requirements
5. **Monitoring** - Log token refresh attempts and failures
6. **Rate limiting** - Implement rate limiting on refresh endpoint

## Troubleshooting

### Cookies not being set
- Ensure `withCredentials: true` in axios configuration
- Check CORS configuration includes `allowCredentials: true`
- Verify frontend and backend are on allowed origins

### 401 errors after refresh
- Old refresh token was already used (tokens rotate on each refresh)
- Refresh token expired (7 days default)
- Token was invalidated during logout

### Token not automatically refreshing
- Check `TOKEN_REFRESH_THRESHOLD` is appropriate (5 seconds default)
- Verify `shouldRefreshToken()` logic is correct
- Check axios interceptors are properly configured

