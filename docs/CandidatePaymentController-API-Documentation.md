# Candidate Payment Controller API Documentation

## Overview
Controller manages payment functionalities for candidates, including creating payment URLs via VNPay payment gateway and handling payment return/callback from VNPay.

**Base URL:** `/api/candidate-payment`

**Tag:** Candidate - Payment

---

## API Endpoints

### 1. Create Payment URL

#### Endpoint
```
POST /api/candidate-payment
```

#### Purpose
Create a VNPay payment URL for a candidate to purchase a package. The system first checks if the candidate has an active order, then generates a secure payment URL that redirects to VNPay payment gateway.

#### Request

**Method:** `POST`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/x-www-form-urlencoded
```

**Query Parameters:**
- **packageName** (String, required): Name of the package to purchase (e.g., "BASIC", "PREMIUM")

**Request Body:** None

**Example Request:**
```bash
POST /api/candidate-payment?packageName=PREMIUM
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_Command=pay&vnp_CreateDate=20251126143025&vnp_CurrCode=VND&vnp_ExpireDate=20251126144525&vnp_IpAddr=192.168.1.100&vnp_Locale=vn&vnp_OrderInfo=packageName%3DPREMIUM%26email%3Dcandidate%40example.com&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fcandidate-payment%2Freturn&vnp_TmnCode=ABCD1234&vnp_TxnRef=A1B2C3D4&vnp_Version=2.1.0&vnp_SecureHash=abc123..."
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (String): VNPay payment URL for redirection

#### Error Response

**400 Bad Request - Cannot Pay for Free Package:**
```json
{
  "code": 400,
  "message": "CAN_NOT_PAY_FOR_FREE_PACKAGE",
  "result": null
}
```

**400 Bad Request - Has Active Package:**
```json
{
  "code": 400,
  "message": "HAS_ACTIVE_PACKAGE",
  "result": null
}
```

**404 Not Found - Package Not Found:**
```json
{
  "code": 404,
  "message": "PACKAGE_NOT_FOUND",
  "result": null
}
```

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate's email from JWT token via `coachUtil.getCurrentCandidate()`
2. Convert package name to uppercase for consistency
3. **Validation checks:**
   - If package name is "FREE" → Throw `CAN_NOT_PAY_FOR_FREE_PACKAGE` exception
   - If candidate already has an active package → Throw `HAS_ACTIVE_PACKAGE` exception
4. Retrieve package details (price, duration) from database
5. Generate payment parameters:
   - **vnp_Version:** "2.1.0" (VNPay API version)
   - **vnp_Command:** "pay" (payment command)
   - **vnp_Amount:** Package price * 100 (VNPay requires amount in smallest currency unit)
   - **vnp_TxnRef:** Random 8-character transaction reference
   - **vnp_IpAddr:** Client IP address
   - **vnp_BankCode:** "NCB" (default bank code)
   - **vnp_Locale:** "vn" (Vietnamese language)
   - **vnp_ReturnUrl:** Backend callback URL for payment verification
   - **vnp_OrderInfo:** Encoded string containing packageName and email
   - **vnp_CreateDate:** Current timestamp in format yyyyMMddHHmmss
   - **vnp_ExpireDate:** Payment expiry time (15 minutes from creation)
6. Build hash data from parameters (sorted alphabetically)
7. Generate secure hash using HMAC SHA512 algorithm with secret key
8. Construct final payment URL with all parameters and secure hash
9. Return payment URL to frontend for redirection

#### Notes
- Required role: `ROLE_CANDIDATE`
- Package name is case-insensitive (automatically converted to uppercase)
- Cannot purchase FREE package (it's assigned by default)
- Cannot purchase new package if candidate already has an active package
- Payment URL expires after 15 minutes
- Uses VNPay payment gateway (Vietnam)
- Amount is multiplied by 100 because VNPay uses smallest currency unit (1 VND = 100 xu)
- Frontend should redirect user to the returned payment URL
- User will complete payment on VNPay's website

---

### 2. Payment Return/Callback

#### Endpoint
```
GET /api/candidate-payment/return
```

#### Purpose
Handle payment return callback from VNPay payment gateway. This endpoint verifies the payment result, updates the candidate's invoice/order, and redirects the user to the frontend with payment status.

#### Request

**Method:** `GET`

**Headers:** None required (called by VNPay)

**Query Parameters:** (All parameters are provided by VNPay)
- **vnp_Amount** (String, required): Payment amount in smallest unit (price * 100)
- **vnp_BankCode** (String, optional): Bank code used for payment
- **vnp_BankTranNo** (String, optional): Bank transaction number
- **vnp_CardType** (String, optional): Card type used for payment
- **vnp_OrderInfo** (String, required): Order information containing packageName and email
- **vnp_PayDate** (String, required): Payment date in format yyyyMMddHHmmss
- **vnp_ResponseCode** (String, required): VNPay response code ("00" = success)
- **vnp_TmnCode** (String, required): Terminal code (merchant code)
- **vnp_TransactionNo** (String, required): VNPay transaction number
- **vnp_TransactionStatus** (String, required): Transaction status
- **vnp_TxnRef** (String, required): Transaction reference from original payment request
- **vnp_SecureHash** (String, required): Secure hash to verify data integrity

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-payment/return?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_OrderInfo=packageName%3DPREMIUM%26email%3Dcandidate%40example.com&vnp_PayDate=20251126143525&vnp_ResponseCode=00&vnp_TmnCode=ABCD1234&vnp_TransactionNo=14234567&vnp_TxnRef=A1B2C3D4&vnp_SecureHash=xyz789...
```

#### Response

**Success Response (302 Redirect):**
Redirects to frontend with payment result parameters:
```
HTTP 302 Found
Location: http://localhost:3000/payment/return?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_OrderInfo=packageName%3DPREMIUM%26email%3Dcandidate%40example.com&vnp_PayDate=20251126143525&vnp_ResponseCode=00&vnp_TmnCode=ABCD1234&vnp_TransactionNo=14234567&vnp_TxnRef=A1B2C3D4&serverVerified=true&serverStatus=SUCCESS
```

**Redirect URL Query Parameters:**
- All original VNPay parameters (except vnp_SecureHash)
- **serverVerified** (boolean): Whether the server successfully verified the payment hash
- **serverStatus** (String): Server-side payment status
  - `"SUCCESS"` = Payment successful and verified
  - `"INVALID_HASH"` = Hash verification failed (possible tampering)
  - `"failed_<code>"` = Payment failed with specific VNPay error code

#### Error Response

**400 Bad Request - Payment Failed:**
```json
{
  "code": 400,
  "message": "PAYMENT_FAILED",
  "result": null
}
```

**404 Not Found - User Not Found:**
```json
{
  "code": 404,
  "message": "USER_NOT_EXISTED",
  "result": null
}
```

**500 Internal Server Error:**
```json
{
  "code": 500,
  "message": "UNCATEGORIZED_EXCEPTION",
  "result": null
}
```

#### Main Logic

1. **Extract and collect all VNPay parameters** from request query string
2. **Separate vnp_SecureHash** from other parameters
3. **Verify payment integrity:**
   - Build hash data from all parameters (sorted alphabetically, excluding vnp_SecureHash)
   - Compute HMAC SHA512 checksum using secret key
   - Compare computed checksum with vnp_SecureHash from VNPay
4. **Determine server status:**
   - If hash verification fails → Status = "INVALID_HASH"
   - If vnp_ResponseCode = "00" → Status = "SUCCESS"
   - Otherwise → Status = "failed_<response_code>"
5. **If status is not SUCCESS** → Throw `PAYMENT_FAILED` exception
6. **Parse order information:**
   - Extract email from vnp_OrderInfo parameter
   - Extract packageName from vnp_OrderInfo parameter
   - Parse payment amount (divide by 100 to get actual VND amount)
   - Parse payment date from yyyyMMddHHmmss format
7. **Find candidate by email:**
   - Query account by email
   - Query candidate by account ID
   - If not found → Throw `USER_NOT_EXISTED` exception
8. **Update or create candidate invoice:**
   - If candidate has no existing invoice (first purchase) → Create new invoice with package details
   - If candidate has existing invoice → Update invoice with new package and reset expiry date
9. **Build redirect URL:**
   - Base URL: `http://localhost:3000/payment/return`
   - Include all original VNPay parameters (except vnp_SecureHash)
   - Add serverVerified flag (true/false)
   - Add serverStatus (SUCCESS/INVALID_HASH/failed_XX)
10. **Redirect user to frontend** with payment result parameters

#### Notes
- This endpoint is called by VNPay payment gateway (webhook/callback)
- Does not require authentication (VNPay cannot send JWT token)
- Security is ensured through HMAC SHA512 hash verification
- Transaction is atomic (wrapped in @Transactional)
- If any step fails, transaction is rolled back
- Frontend URL is hardcoded: `http://localhost:3000/payment/return`
- Payment status is verified server-side before updating database
- All parameters are URL-encoded for safe redirection
- Frontend should parse query parameters to display payment result to user

---

## VNPay Response Codes

Common VNPay response codes:
- **00:** Transaction successful
- **07:** Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)
- **09:** Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng
- **10:** Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần
- **11:** Giao dịch không thành công do: Đã hết hạn chờ thanh toán
- **12:** Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa
- **13:** Giao dịch không thành công do: Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)
- **24:** Giao dịch không thành công do: Khách hàng hủy giao dịch
- **51:** Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch
- **65:** Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày
- **75:** Ngân hàng thanh toán đang bảo trì
- **79:** Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định
- **99:** Các lỗi khác

## Payment Flow Diagram

```
1. Candidate clicks "Purchase Package" on frontend
   ↓
2. Frontend calls POST /api/candidate-payment?packageName=PREMIUM
   ↓
3. Backend validates (no active package, not FREE package)
   ↓
4. Backend generates VNPay payment URL with secure hash
   ↓
5. Backend returns payment URL to frontend
   ↓
6. Frontend redirects user to VNPay payment page
   ↓
7. User completes payment on VNPay
   ↓
8. VNPay redirects user back to GET /api/candidate-payment/return?vnp_params...
   ↓
9. Backend verifies payment hash and status
   ↓
10. Backend creates/updates candidate invoice
    ↓
11. Backend redirects user to frontend with payment result
    ↓
12. Frontend displays payment success/failure message
```

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | CAN_NOT_PAY_FOR_FREE_PACKAGE | Cannot create payment for FREE package |
| 400 | HAS_ACTIVE_PACKAGE | Candidate already has an active package |
| 400 | PAYMENT_FAILED | Payment verification failed or was not successful |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not a CANDIDATE) |
| 404 | PACKAGE_NOT_FOUND | Requested package does not exist |
| 404 | USER_NOT_EXISTED | User account not found (in payment callback) |
| 500 | UNCATEGORIZED_EXCEPTION | Server error during payment processing |

## Security

- Endpoint `/` (POST) requires authentication and role `CANDIDATE`
- Endpoint `/return` (GET) does not require authentication (called by VNPay)
- Payment integrity verified using HMAC SHA512 with secret key
- All payment parameters are hashed and verified before processing
- Prevents tampering with payment amount or order information
- Uses secure hash (vnp_SecureHash) to ensure data integrity
- Transaction reference (vnp_TxnRef) is randomly generated to prevent replay attacks

## Configuration Dependencies

**VNPay Configuration (from PaymentConfig):**
- **vnp_TmnCode:** Merchant/Terminal code provided by VNPay
- **secretKey:** Secret key for HMAC SHA512 hash generation
- **vnp_PayUrl:** VNPay payment gateway URL
- **vnp_ReturnUrl:** Backend callback URL (must be registered with VNPay)

**Example configuration:**
```yaml
vnpay:
  vnp_TmnCode: "ABCD1234"
  secretKey: "YOUR_SECRET_KEY_HERE"
  vnp_PayUrl: "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
  vnp_ReturnUrl: "http://localhost:8080/api/candidate-payment/return"
```

## Database Dependencies

- **CandidatePackage entity:** Package information (name, price, duration)
- **CandidateInvoice entity:** Invoice/order information for candidates
- **Candidate entity:** Candidate profile
- **Account entity:** User account with email

## Related Services

- `CandidatePaymentImp`: Business logic service for payment processing
- `CandidateInvoiceImp`: Service for managing candidate invoices/orders
- `PaymentUtil`: Utility for VNPay parameter handling and hash generation
- `PaymentConfig`: Configuration for VNPay integration
- `CoachUtil`: Utility to get current user information

## Testing Notes

### For Development/Testing:
1. Use VNPay sandbox environment
2. Test card numbers provided by VNPay documentation
3. Frontend redirect URL should match environment (localhost/staging/production)

### Sample Test Flow:
```bash
# 1. Create payment URL
curl -X POST "http://localhost:8080/api/candidate-payment?packageName=PREMIUM" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. Open returned URL in browser
# 3. Complete payment on VNPay sandbox
# 4. Verify redirect back to frontend with payment result
# 5. Check database for updated candidate invoice
```

## Important Notes

- Payment amount must be multiplied by 100 for VNPay (smallest currency unit)
- Payment URL expires after 15 minutes
- Only one active package per candidate at a time
- Cannot purchase FREE package (it's default/assigned automatically)
- Frontend redirect URL is hardcoded - update for production environment
- All timestamps use format: `yyyyMMddHHmmss`
- VNPay sandbox and production use different URLs and credentials
- Transaction is atomic - if any step fails, invoice is not created/updated

