#!/bin/bash

# UON Marketplace 2FA Test Script
# Tests all 2FA endpoints with sample data

BASE_URL="http://localhost:8080/auth"
USER_ID=1  # Replace with actual user ID from your database

echo "==================================="
echo "UON Marketplace 2FA Test Suite"
echo "==================================="
echo ""

# Test 1: Check 2FA Status
echo "1️⃣  Checking 2FA status for user $USER_ID..."
curl -s -X GET "$BASE_URL/2fa/status?userId=$USER_ID" | json_pp
echo ""
echo ""

# Test 2: Setup 2FA
echo "2️⃣  Setting up 2FA for user $USER_ID..."
SETUP_RESPONSE=$(curl -s -X POST "$BASE_URL/2fa/setup" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": $USER_ID}")

echo "$SETUP_RESPONSE" | json_pp
echo ""

# Extract QR code (you'll need to view this in browser)
QR_CODE=$(echo "$SETUP_RESPONSE" | grep -o '"qrCodeUrl":"[^"]*' | cut -d'"' -f4)
echo "QR Code Data URL (paste in browser to view):"
echo "$QR_CODE" | head -c 100
echo "..."
echo ""

# Extract backup codes
echo "📋 Backup Codes (save these securely):"
echo "$SETUP_RESPONSE" | grep -o '"backupCodes":\[[^\]]*\]' | sed 's/"backupCodes":\[//;s/\]//;s/,/\n/g' | sed 's/"//g'
echo ""
echo ""

# Test 3: Verify 2FA (you need to provide actual code from authenticator app)
echo "3️⃣  To verify and enable 2FA, run:"
echo "   curl -X POST $BASE_URL/2fa/verify \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"userId\": $USER_ID, \"code\": \"YOUR_6_DIGIT_CODE\"}'"
echo ""
echo ""

# Test 4: Login with 2FA (after enabling)
echo "4️⃣  To login with 2FA enabled, run:"
echo "   # Step 1: Login with credentials"
echo "   curl -X POST $BASE_URL/login/v2 \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\": \"your@email.com\", \"password\": \"yourpassword\"}'"
echo ""
echo "   # Step 2: Login with 2FA code"
echo "   curl -X POST $BASE_URL/login/v2 \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\": \"your@email.com\", \"password\": \"yourpassword\", \"twoFactorCode\": \"123456\"}'"
echo ""
echo ""

# Test 5: Regenerate backup codes
echo "5️⃣  To regenerate backup codes:"
echo "   curl -X POST '$BASE_URL/2fa/regenerate-backup-codes?userId=$USER_ID&verificationCode=123456'"
echo ""
echo ""

# Test 6: Disable 2FA
echo "6️⃣  To disable 2FA:"
echo "   curl -X POST '$BASE_URL/2fa/disable?userId=$USER_ID&password=yourpassword'"
echo ""
echo ""

echo "==================================="
echo "📱 Next Steps:"
echo "==================================="
echo "1. Scan the QR code with Google Authenticator/Authy"
echo "2. Get the 6-digit code from your app"
echo "3. Run the verify command (step 3) with your code"
echo "4. Test login with 2FA enabled"
echo ""
echo "📚 Full Documentation: See 2FA-IMPLEMENTATION.md"
echo "🔧 Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo ""
