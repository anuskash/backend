# Product Edit Functionality

## New Endpoint Added

### Update Product
**Endpoint:** `PUT /users/product/{productId}`

**Description:** Allows users to edit their posted products. Updates product details including name, description, category, condition, price, and images.

**Request Body:**
```json
{
  "productName": "Updated Product Name",
  "productDescription": "Updated description",
  "category": "Electronics",
  "condition": "Like New",
  "price": 99.99,
  "imageUrls": [
    "http://example.com/image1.jpg",
    "http://example.com/image2.jpg"
  ]
}
```

**Notes:**
- All fields are **optional** - only provided fields will be updated
- Product name and description are run through **content moderation**
- If prohibited content is detected, the update is **rejected**
- If suspicious content is found, product is **flagged for review**
- `lastUpdate` timestamp is automatically updated

**Response:**
```json
{
  "productId": 123,
  "sellerId": 1,
  "sellerName": "John Doe",
  "productName": "Updated Product Name",
  "productDescription": "Updated description",
  "category": "Electronics",
  "condition": "Like New",
  "price": 99.99,
  "productImageUrl": "http://example.com/image1.jpg",
  "postedDate": "2025-11-07T10:30:00",
  "lastUpdate": "2025-11-07T21:59:00",
  "status": "Available",
  "flagged": false,
  "flagReason": null,
  "reportCount": 0
}
```

**Error Responses:**
- `404 Not Found` - Product doesn't exist
- `400 Bad Request` - Update rejected due to prohibited content
  ```json
  {
    "error": "Product update rejected: Contains prohibited keyword 'drugs'"
  }
  ```

## Example Usage

### Update only the price:
```bash
curl -X PUT http://localhost:8080/users/product/123 \
  -H "Content-Type: application/json" \
  -d '{"price": 149.99}'
```

### Update title and description:
```bash
curl -X PUT http://localhost:8080/users/product/123 \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "iPhone 13 Pro Max",
    "productDescription": "Excellent condition, barely used"
  }'
```

### Update all details:
```bash
curl -X PUT http://localhost:8080/users/product/123 \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "iPhone 13 Pro Max 256GB",
    "productDescription": "Like new, includes original box and charger",
    "category": "Electronics",
    "condition": "Like New",
    "price": 899.00,
    "imageUrls": [
      "http://example.com/iphone-front.jpg",
      "http://example.com/iphone-back.jpg"
    ]
  }'
```

## Existing Endpoints (Still Available)

These partial update endpoints are still available for convenience:

- `PUT /users/product/{productId}/price?newPrice=99.99` - Update price only
- `PUT /users/product/{productId}/status?newStatus=Sold` - Update status only
- `PUT /users/product/{productId}/images` - Update images only
- `PUT /users/product/{productId}/unavailable` - Mark as unavailable
- `PUT /users/product/{productId}/sold?buyerUserId=5` - Mark as sold

## Security Features

✅ **Content Moderation:** All product updates go through the same moderation pipeline as new products  
✅ **Prohibited Keywords:** Updates with drugs, weapons, alcohol, etc. are rejected  
✅ **Profanity Filter:** Inappropriate language triggers a flag  
✅ **Auto-flagging:** Suspicious content marks product for admin review  
✅ **Seller Ownership:** (Recommend adding userId check to ensure only the seller can edit their products)

## Recommended Enhancement

Add seller verification to the controller:
```java
@PutMapping("/product/{productId}")
public ResponseEntity<MarketPlaceProduct> updateProduct(
        @PathVariable Long productId, 
        @RequestBody UpdateProductRequest request,
        @RequestHeader("userId") Long userId) {
    // Verify product belongs to this user
    MarketPlaceProduct product = productService.getProductById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    if (!product.getSellerId().equals(userId)) {
        throw new RuntimeException("Unauthorized: You can only edit your own products");
    }
    return ResponseEntity.ok(userService.updateProduct(productId, request));
}
```
