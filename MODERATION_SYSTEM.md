# Content Moderation System - UoN Marketplace

## Overview
The content moderation system protects the UoN student marketplace from prohibited items, inappropriate content, and scams. It uses a multi-layer filtering approach to automatically flag or reject violating products.

## Features

### 1. **Prohibited Keywords Database**
- Dynamic keyword blacklist organized by category
- Categories: drugs, weapons, alcohol, tobacco, scam_indicators, profanity
- Severity levels: high, medium, low
- Auto-actions: reject, flag, warn

### 2. **Content Filtering Layers**
1. **Keyword Filter**: Checks against prohibited items database
2. **Profanity Filter**: Built-in common profanity wordlist
3. **Product Review Text**: Applies filtering to titles and descriptions

### 3. **User Reporting System**
- Users can report inappropriate products
- Auto-flag products after 3 reports
- Admin moderation queue for review
- Prevent duplicate reports from same user

### 4. **Admin Dashboard**
- Manage prohibited keywords (add/remove/view)
- Review flagged products
- Process user reports
- Override flags and unflag products

## Database Schema

### prohibited_keywords
```sql
keyword_id       BIGINT (PK, auto-increment)
keyword          NVARCHAR(100) UNIQUE NOT NULL
category         NVARCHAR(50) NOT NULL
severity         NVARCHAR(20) NOT NULL (high/medium/low)
auto_action      NVARCHAR(50) (reject/flag/warn)
added_by         BIGINT (FK to users)
added_date       DATETIME NOT NULL
is_active        BIT DEFAULT 1
description      NVARCHAR(255)
```

### product_reports
```sql
report_id        BIGINT (PK, auto-increment)
product_id       BIGINT (FK to marketplace_products)
reporter_id      BIGINT (FK to users)
report_reason    NVARCHAR(50) NOT NULL
report_details   NVARCHAR(500)
report_date      DATETIME NOT NULL
status           NVARCHAR(20) DEFAULT 'pending' (pending/approved/rejected/remove_product)
reviewed_by      BIGINT (FK to users)
reviewed_at      DATETIME
admin_notes      NVARCHAR(500)
```

### marketplace_products (additions)
```sql
flagged          BIT DEFAULT 0
flag_reason      NVARCHAR(200)
report_count     INT DEFAULT 0
```

## API Endpoints

### User Endpoints

#### Submit Product Report
```
POST /api/reports/product
Headers: userId: <user_id>
Body: {
    "productId": 123,
    "reportReason": "prohibited_item",
    "reportDetails": "Selling drugs"
}
Response: {
    "success": true,
    "message": "Product reported successfully",
    "reportId": 456
}
```

#### Get My Reports
```
GET /api/reports/my-reports
Headers: userId: <user_id>
Response: [array of ProductReport objects]
```

### Admin Endpoints (requires ADMIN or SUPER_ADMIN role)

#### Get All Prohibited Keywords
```
GET /admin/prohibited-keywords
Response: [array of ProhibitedKeyword objects]
```

#### Get Keywords by Category
```
GET /admin/prohibited-keywords/category/drugs
Response: [array of ProhibitedKeyword objects in 'drugs' category]
```

#### Add Prohibited Keyword
```
POST /admin/prohibited-keywords
Headers: userId: <admin_id>
Body: {
    "keyword": "marijuana",
    "category": "drugs",
    "severity": "high",
    "autoAction": "reject",
    "description": "Illegal substance"
}
Response: {saved ProhibitedKeyword object}
```

#### Delete Prohibited Keyword
```
DELETE /admin/prohibited-keywords/{keywordId}
Response: {
    "success": true,
    "message": "Keyword deleted successfully"
}
```

#### Get Pending Reports (Moderation Queue)
```
GET /admin/reports/pending
Response: [array of pending ProductReport objects]
```

#### Review Product Report
```
POST /admin/reports/{reportId}/review
Headers: userId: <admin_id>
Params: 
    action: approved | rejected | remove_product
    adminNotes: "Optional admin notes"
Response: {
    "success": true,
    "message": "Report reviewed successfully"
}
```

#### Get Flagged Products
```
GET /admin/products/flagged
Response: [array of flagged MarketPlaceProduct objects]
```

#### Unflag Product (Admin Override)
```
POST /admin/products/{productId}/unflag
Response: {
    "success": true,
    "message": "Product unflagged successfully"
}
```

## How It Works

### Product Creation Flow
1. User submits product (POST /api/products/create)
2. ContentModerationService checks:
   - Keyword filter scans title + description
   - Profanity filter scans title + description
3. Based on result:
   - **APPROVED**: Product saved with status "available"
   - **FLAGGED**: Product saved with flagged=true, status="pending_review"
   - **REJECTED**: HTTP 400 error, product not created
4. Response returned to user

### User Reporting Flow
1. User reports product (POST /api/reports/product)
2. System checks if user already reported (prevent spam)
3. ProductReport created with status "pending"
4. Product report_count incremented
5. If report_count >= 3: Auto-flag product
6. Admin notified (moderation queue)

### Admin Review Flow
1. Admin views pending reports (GET /admin/reports/pending)
2. Admin reviews report details
3. Admin takes action (POST /admin/reports/{id}/review):
   - **approved**: Report valid, no action on product
   - **rejected**: Report invalid/spam
   - **remove_product**: Flag product, set status="removed"
4. Report status updated with admin notes

## Moderation Rules

### Pre-Seeded Prohibited Keywords

**DRUGS** (high severity, auto-reject):
- marijuana, weed, cannabis, thc, cbd
- cocaine, heroin, meth, mdma, ecstasy
- pills, prescription drugs

**WEAPONS** (high/medium severity):
- gun, pistol, rifle, firearm (high - reject)
- knife, blade (medium - flag)
- ammunition, explosive (high - reject)

**ALCOHOL** (high severity, auto-reject):
- alcohol, beer, wine, vodka, whiskey, rum, liquor

**TOBACCO & VAPING** (high severity, auto-reject):
- cigarette, tobacco, vape, vaping
- e-cigarette, juul, nicotine

**SCAM INDICATORS** (medium severity, flag):
- "100% legit", "guaranteed", "no refunds"
- "cash only", "wire transfer", "send money first"

**PROFANITY** (low severity, flag):
- Common profanity words (flagged for review)

### Auto-Actions
- **reject**: Product creation fails immediately (400 error)
- **flag**: Product created but flagged for admin review
- **warn**: Product allowed, user warned (future implementation)

## Installation

### 1. Run Database Migration
```bash
# Connect to your SQL Server database
sqlcmd -S your_server -d marketplace_db -i moderation_system_migration.sql
```

### 2. Verify Tables Created
```sql
SELECT * FROM prohibited_keywords;
SELECT * FROM product_reports;
SELECT flagged, flag_reason, report_count FROM marketplace_products;
```

### 3. Test Moderation System
```bash
# Build and run the application
./mvnw spring-boot:run

# Test prohibited keyword detection
curl -X POST http://localhost:8080/api/products/create \
  -H "Content-Type: application/json" \
  -H "userId: 1" \
  -d '{
    "productName": "Selling weed",
    "productDescription": "High quality cannabis",
    "category": "Other",
    "condition": "New",
    "price": 50.00
  }'
# Expected: 400 Bad Request - "Product contains prohibited content"
```

## Future Enhancements

### Optional AI Integration
- **PerspectiveAPI** (Google): Detect toxic/profane text
- **OpenAI Moderation API**: Advanced content filtering
- **Image Recognition**: Scan product images for prohibited items

### Auto-Ban System
- Track user violations (flagged products count)
- Auto-ban users with 3+ violations
- Suspicious activity detection (mass reporting, spam)

### Message Moderation
- Extend filtering to user messages
- Prevent sharing contact info to avoid platform bypass

### Review Moderation
- Apply filters to buyer/seller reviews
- Flag fake/spam reviews

## Maintenance

### Regular Tasks
1. Review flagged products weekly
2. Update prohibited keywords as needed
3. Monitor false positive rate
4. Check user report queue daily
5. Analyze moderation metrics

### Metrics to Track
- Products flagged per day
- User reports per day
- Admin response time
- False positive rate
- Most common violation categories

## Security Considerations
- All admin endpoints require ADMIN or SUPER_ADMIN role
- Keyword matching is case-insensitive
- Duplicate reports prevented (same user + product)
- Soft delete for keywords (set is_active=false)
- Admin actions logged (reviewed_by, reviewed_at)

## Support
For questions or issues with the moderation system, contact the admin team or create a ticket in the admin dashboard.
