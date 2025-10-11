# Token Rotation System - Quick Reference

## System Architecture

### Token Flow
```
Login → Access Token (Response Body) + Refresh Token (Cookie)
  ↓
API Requests → Authorization: Bearer {access_token}
  ↓
Token Near Expiry (5s) → Auto Refresh → New Tokens
  ↓
Logout → Invalidate Both Tokens + Clear Cookie
```

### Security Features
- ✅ Access token expires in 15 minutes (900s)
- ✅ Refresh token expires in 7 days (604800s)
- ✅ Refresh token in HTTP-only cookie (XSS protected)
- ✅ Old tokens immediately invalidated after rotation
- ✅ Both tokens invalidated on logout

## API Endpoints

### 1. Login
```http
POST /api/auth/token
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "accessToken": "eyJhbGci...",
    "authenticated": true,
    "expiresIn": 900,
    "tokenType": "Bearer"
  }
}
```

**Cookie Set:** `refreshToken` (HTTP-only, 7 days)

### 2. Refresh Token
```http
POST /api/auth/refresh
Cookie: refreshToken={refresh_token}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "accessToken": "eyJhbGci...",
    "authenticated": true,
    "expiresIn": 900,
    "tokenType": "Bearer"
  }
}
```

**Cookie Updated:** New `refreshToken` (old one invalidated)

### 3. Logout
```http
POST /api/auth/logout
Cookie: refreshToken={refresh_token}
```

**Response:**
```json
{
  "code": 1000,
  "result": null
}
```

**Cookie Cleared:** `refreshToken` removed

### 4. Introspect Token (Check Validity)
```http
POST /api/auth/introspect
Content-Type: application/json

{
  "token": "eyJhbGci..."
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "valid": true
  }
}
```

## Frontend Integration Checklist

### Setup
- [ ] Install axios: `npm install axios`
- [ ] Create `axiosInstance.js` with interceptors
- [ ] Create `authService.js` for auth operations
- [ ] Add `withCredentials: true` to axios config
- [ ] Initialize auth state on app load

### Login Component
- [ ] Call `authService.login(email, password)`
- [ ] Store access token in memory/localStorage
- [ ] Calculate and store expiration time
- [ ] Redirect to dashboard on success

### API Requests
- [ ] Use configured axios instance for all API calls
- [ ] Access token automatically added to headers
- [ ] Automatic refresh when token near expiry
- [ ] Automatic retry on 401 errors

### Logout
- [ ] Call `authService.logout()`
- [ ] Clear access token from storage
- [ ] Redirect to login page

### Protected Routes
- [ ] Check authentication before rendering
- [ ] Redirect to login if not authenticated

## Testing Scenarios

### Test 1: Successful Login
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' \
  -c cookies.txt -v
```

**Expected:**
- Status: 200
- Response contains `accessToken`
- Cookie `refreshToken` is set

### Test 2: Use Access Token
```bash
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -b cookies.txt
```

**Expected:**
- Status: 200
- Access granted to protected resource

### Test 3: Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b cookies.txt -c cookies.txt -v
```

**Expected:**
- Status: 200
- New `accessToken` in response
- New `refreshToken` cookie set
- Old refresh token invalidated

### Test 4: Use Old Refresh Token (Should Fail)
```bash
# Save old cookie, get new token, then try old cookie again
curl -X POST http://localhost:8080/api/auth/refresh \
  -b old_cookies.txt
```

**Expected:**
- Status: 401 (Unauthenticated)
- Error message about invalid token

### Test 5: Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt -v
```

**Expected:**
- Status: 200
- Cookie `refreshToken` cleared (Max-Age=0)

### Test 6: Use Token After Logout (Should Fail)
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b cookies.txt
```

**Expected:**
- Status: 401 (Unauthenticated)

## Common Issues & Solutions

### Issue: Cookies not being sent
**Solution:**
- Frontend: Add `withCredentials: true` to axios config
- Backend: Verify CORS allows credentials
- Check same-origin policy (use localhost for both if testing)

### Issue: 401 after refresh
**Cause:** Old refresh token reused after rotation
**Solution:** Always use the latest refresh token from cookie

### Issue: Token not auto-refreshing
**Check:**
- Interceptor is configured correctly
- Token expiration calculation is accurate
- Refresh threshold (5 seconds) is appropriate

### Issue: CORS errors
**Solution:**
- Backend: Verify frontend origin in CORS config
- Frontend: Ensure correct backend URL
- Both: Check credentials are allowed

## Database Cleanup

Old tokens are stored in `invalid_token` table. Consider cleanup job:

```sql
-- Delete expired invalid tokens (older than 7 days)
DELETE FROM invalid_token 
WHERE expiry_time < NOW() - INTERVAL '7 days';
```

Schedule this to run daily to prevent table bloat.

## Configuration

### Backend (application.yml)
```yaml
jwt:
  signerKey: "your-secret-key"
  valid-duration: 900        # 15 minutes
  refreshable-duration: 604800 # 7 days
```

### Frontend (axiosInstance.js)
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
const TOKEN_REFRESH_THRESHOLD = 5000; // 5 seconds
```

### Cookie Settings
```java
cookie.setHttpOnly(true);     // Prevent JavaScript access
cookie.setSecure(false);      // Set true in production (HTTPS)
cookie.setPath("/");          // Available for all paths
cookie.setMaxAge(604800);     // 7 days
```

## Production Deployment

### Before Going Live:
1. **Enable HTTPS**: Set `cookie.setSecure(true)`
2. **Update CORS**: Change allowed origins to production URLs
3. **Environment Variables**: Externalize all configuration
4. **Monitoring**: Log token operations for security auditing
5. **Rate Limiting**: Prevent brute force on refresh endpoint
6. **Database**: Set up cleanup job for invalid_token table
7. **Testing**: Full end-to-end testing in staging environment

## Security Best Practices

✅ **Never log tokens** - Avoid logging token values  
✅ **Short access token lifetime** - 15 minutes recommended  
✅ **Long refresh token lifetime** - 7 days reasonable  
✅ **Rotate on every refresh** - Prevent token replay attacks  
✅ **HTTP-only cookies** - Protect refresh token from XSS  
✅ **Secure flag in production** - HTTPS only in production  
✅ **Validate on every request** - Check token validity and blacklist  
✅ **Clear on logout** - Invalidate both tokens

## Support

For issues or questions, check:
- `FRONTEND_INTEGRATION_GUIDE.md` for detailed frontend setup
- Backend logs for authentication errors
- Browser DevTools Network tab for cookie inspection
- Database `invalid_token` table for token invalidation

