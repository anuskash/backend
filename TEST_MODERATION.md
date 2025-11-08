# Testing Reporting & Moderation System

## Prerequisites
- Application running on `http://localhost:8080`
- At least 2 user accounts (one regular user, one admin)
- Sample products in the database

---

## Test 1: Automatic Keyword Rejection (High Severity)

### Test creating a product with prohibited keyword that gets auto-rejected

```bash
curl -X POST http://localhost:8080/users/product \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "productName": "Special herbs",
    "productDescription": "Selling cocaine and other stuff",
    "category": "Other",
    "condition": "New",
    "price": 50.00,
    "imageUrls": ["http://example.com/image.jpg"]
  }'
```

**Expected Result:**
```json
{
  "success": false,
  "error": "Product rejected: Contains prohibited keyword 'cocaine' (Category: drugs, Severity: high)"
}
```

---

## Test 2: Automatic Flagging (Medium Severity)

### Test creating a product that gets flagged for review

```bash
curl -X POST http://localhost:8080/users/product \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "productName": "PayPal transfer needed",
    "productDescription": "Send money via wire transfer only, quick cash",
    "category": "Electronics",
    "condition": "New",
    "price": 500.00,
    "imageUrls": ["http://example.com/image.jpg"]
  }'
```

**Expected Result:**
- Product is created BUT flagged = true
- flagReason will contain: "Contains prohibited keyword: wire transfer"
- Product appears in admin moderation queue

**Response:**
```json
{
  "productId": 15,
  "productName": "PayPal transfer needed",
  "flagged": true,
  "flagReason": "Contains prohibited keyword: 'wire transfer' (Category: scam_indicators, Severity: medium)",
  "status": "Available",
  ...
}
```

---

## Test 3: Profanity Filter

### Test creating a product with profanity

```bash
curl -X POST http://localhost:8080/users/product \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "productName": "Damn good laptop",
    "productDescription": "This shit is awesome, fuck yeah!",
    "category": "Electronics",
    "condition": "Used",
    "price": 300.00,
    "imageUrls": ["http://example.com/image.jpg"]
  }'
```

**Expected Result:**
- Product is flagged for profanity
- Admin can review

---

## Test 4: User Reports a Product

### Step 1: Create a normal product first
```bash
curl -X POST http://localhost:8080/users/product \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "productName": "iPhone 13",
    "productDescription": "Great condition",
    "category": "Electronics",
    "condition": "Like New",
    "price": 800.00,
    "imageUrls": ["http://example.com/iphone.jpg"]
  }'
```

**Note the productId from response (e.g., 16)**

### Step 2: Report the product
```bash
curl -X POST http://localhost:8080/api/reports/product \
  -H "Content-Type: application/json" \
  -H "userId: 2" \
  -d '{
    "productId": 16,
    "reportReason": "counterfeit",
    "reportDetails": "This looks like a fake iPhone, suspected counterfeit"
  }'
```

**Expected Result:**
```json
{
  "success": true,
  "message": "Product reported successfully",
  "reportId": 1
}
```

---

## Test 5: View My Reports (User)

```bash
curl -X GET http://localhost:8080/api/reports/my-reports \
  -H "userId: 2"
```

**Expected Result:**
```json
[
  {
    "reportId": 1,
    "productId": 16,
    "productName": "iPhone 13",
    "reportReason": "counterfeit",
    "reportDetails": "This looks like a fake iPhone, suspected counterfeit",
    "status": "pending",
    "reportDate": "2025-11-07T22:50:00"
  }
]
```

---

## Test 6: Admin Views Pending Reports

```bash
curl -X GET http://localhost:8080/admin/reports/pending
```

**Expected Result:**
```json
[
  {
    "reportId": 1,
    "productId": 16,
    "productName": "iPhone 13",
    "reporterId": 2,
    "reporterName": "John Doe",
    "reportReason": "counterfeit",
    "reportDetails": "This looks like a fake iPhone",
    "status": "pending",
    "reportCount": 1
  }
]
```

---

## Test 7: Admin Reviews Report (Approve)

```bash
curl -X POST "http://localhost:8080/admin/reports/1/review?action=approved&adminNotes=Valid report, product flagged" \
  -H "userId: 1"
```

**Expected Result:**
- Report status changes to "resolved"
- Product gets flagged if not already

---

## Test 8: Admin Reviews Report (Remove Product)

```bash
curl -X POST "http://localhost:8080/admin/reports/1/review?action=remove_product&adminNotes=Confirmed counterfeit, removed" \
  -H "userId: 1"
```

**Expected Result:**
- Product is deleted from marketplace
- Report status changes to "resolved"

---

## Test 9: Admin Views Flagged Products

```bash
curl -X GET http://localhost:8080/admin/products/flagged
```

**Expected Result:**
```json
[
  {
    "productId": 15,
    "productName": "PayPal transfer needed",
    "flagged": true,
    "flagReason": "Contains prohibited keyword: 'wire transfer'",
    "reportCount": 0,
    "sellerId": 1,
    "sellerName": "Jane Smith"
  }
]
```

---

## Test 10: Admin Unflags Product

```bash
curl -X POST http://localhost:8080/admin/products/15/unflag
```

**Expected Result:**
- Product.flagged = false
- Product.flagReason = null
- Product available normally

---

## Test 11: Admin Views All Prohibited Keywords

```bash
curl -X GET http://localhost:8080/admin/prohibited-keywords
```

**Expected Result:** List of 48 prohibited keywords across 6 categories

---

## Test 12: Admin Views Keywords by Category

```bash
curl -X GET http://localhost:8080/admin/prohibited-keywords/category/drugs
```

**Expected Result:** All drug-related keywords

---

## Test 13: Admin Adds New Prohibited Keyword

```bash
curl -X POST http://localhost:8080/admin/prohibited-keywords \
  -H "Content-Type: application/json" \
  -H "userId: 1" \
  -d '{
    "keyword": "stolen goods",
    "category": "scam_indicators",
    "severity": "high",
    "autoAction": "reject",
    "description": "Indicates stolen merchandise"
  }'
```

**Expected Result:**
- Keyword added successfully
- Future products with "stolen goods" will be auto-rejected

---

## Test 14: Admin Deletes Prohibited Keyword

```bash
# First get the keyword ID from the list, then:
curl -X DELETE http://localhost:8080/admin/prohibited-keywords/49
```

---

## Test 15: Test Product Edit with Moderation

### Create a clean product first
```bash
curl -X POST http://localhost:8080/users/product \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "productName": "Laptop for sale",
    "productDescription": "Good laptop",
    "category": "Electronics",
    "condition": "Used",
    "price": 400.00,
    "imageUrls": ["http://example.com/laptop.jpg"]
  }'
```

### Now try to edit it with prohibited content
```bash
curl -X PUT http://localhost:8080/users/product/17 \
  -H "Content-Type: application/json" \
  -H "userId: 1" \
  -d '{
    "productDescription": "Great laptop, also selling marijuana on the side"
  }'
```

**Expected Result:**
- Edit is rejected due to prohibited keyword "marijuana"

---

## Quick Test Scenarios

### Scenario 1: Complete Report Workflow
1. User A creates product
2. User B reports product as "scam"
3. Admin reviews pending reports
4. Admin takes action (approve/reject/remove)
5. User B checks "my reports" to see status

### Scenario 2: Keyword Moderation
1. Admin adds new prohibited keyword
2. User tries to create product with that keyword
3. Product is auto-rejected or auto-flagged
4. Admin can see flagged products

### Scenario 3: Multiple Reports on Same Product
1. Create a product
2. Multiple users report the same product
3. Check `reportCount` field in admin queue
4. Admin takes action on product with multiple reports

---

## Testing with Postman/Thunder Client

Import these endpoints into your API testing tool:

**Base URL:** `http://localhost:8080`

**Collections:**
1. Product Creation (with moderation)
2. User Reporting
3. Admin Moderation Queue
4. Admin Keyword Management
5. Admin Report Review

---

## Expected Database Changes

### After Testing, Check Database:

```sql
-- Check flagged products
SELECT product_id, product_name, flagged, flag_reason, report_count 
FROM marketplace_products 
WHERE flagged = 1;

-- Check prohibited keywords
SELECT * FROM prohibited_keywords WHERE is_active = 1;

-- Check product reports
SELECT * FROM product_reports ORDER BY report_date DESC;

-- Check product images
SELECT * FROM product_images ORDER BY product_id, display_order;
```

---

## What to Look For

✅ **Success Indicators:**
- Products with prohibited keywords (high severity) are rejected
- Products with scam indicators (medium severity) are flagged
- User reports are stored in database
- Admin can review and take actions
- Report count increments on products
- Flagged products appear in admin queue
- Keywords can be added/deleted by admin

❌ **Potential Issues:**
- Products not being flagged/rejected
- Reports not saving to database
- Admin actions not working
- Ownership verification preventing legitimate edits
- Database constraints errors

---

## Tips for Testing

1. **Use different user IDs** to simulate real users
2. **Check database** after each test to verify changes
3. **Test ownership** - try to edit another user's product (should fail)
4. **Test multiple reports** on the same product
5. **Test all severity levels** (high, medium, low)
6. **Test all report reasons** (prohibited_item, scam, counterfeit, etc.)

---

## Clean Up After Testing

```sql
-- Remove test products
DELETE FROM marketplace_products WHERE product_id > 13;

-- Remove test reports
DELETE FROM product_reports;

-- Reset auto-increment
-- (SQL Server specific syntax if needed)
```
