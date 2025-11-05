# Testing Multiple Image Upload & Display

## Step-by-Step Testing Guide

### Step 1: Upload Multiple Images

**Endpoint**: `POST /users/product/upload-multiple-images`

**In Swagger UI**:
1. Open http://localhost:8080/swagger-ui/index.html
2. Find `POST /users/product/upload-multiple-images`
3. Click "Try it out"
4. Click "Choose Files" for the `files` field
5. **Important**: Select MULTIPLE files (Cmd/Ctrl + click to select 2-3 images)
6. Click "Execute"

**Expected Response**:
```json
{
  "imageUrls": [
    "http://localhost:8080/uploads/products/abc123_image1.jpg",
    "http://localhost:8080/uploads/products/def456_image2.jpg",
    "http://localhost:8080/uploads/products/ghi789_image3.jpg"
  ],
  "message": "Images uploaded successfully",
  "count": 3
}
```

**Save these URLs** - you'll need them in the next step!

---

### Step 2: Create Product with Multiple Images

**Endpoint**: `POST /users/product`

**Request Body** (use the URLs from Step 1):
```json
{
  "sellerId": 4,
  "productName": "Test Product with Multiple Images",
  "productDescription": "Testing multiple image display",
  "price": 99.99,
  "condition": "New",
  "category": "Electronics",
  "imageUrls": [
    "http://localhost:8080/uploads/products/abc123_image1.jpg",
    "http://localhost:8080/uploads/products/def456_image2.jpg",
    "http://localhost:8080/uploads/products/ghi789_image3.jpg"
  ]
}
```

**Expected Response**:
```json
{
  "productId": 123,
  "sellerId": 4,
  "sellerName": "John Doe",
  "productName": "Test Product with Multiple Images",
  "productImageUrl": "http://localhost:8080/uploads/products/abc123_image1.jpg",
  "price": 99.99,
  ...
}
```

**Note the productId** - you'll need it for Step 3!

---

### Step 3: Retrieve Product Images

**Endpoint**: `GET /users/product/{productId}/images`

**Example**: `GET /users/product/123/images`

**Expected Response**:
```json
{
  "productId": 123,
  "imageUrls": [
    "http://localhost:8080/uploads/products/abc123_image1.jpg",
    "http://localhost:8080/uploads/products/def456_image2.jpg",
    "http://localhost:8080/uploads/products/ghi789_image3.jpg"
  ],
  "count": 3
}
```

✅ If you see all 3 URLs here, **your backend is working correctly!**

---

### Step 4: Verify in Database

Run this query to check the product_images table:

```sql
SELECT * FROM product_images WHERE product_id = 123 ORDER BY display_order;
```

**Expected Result**:
| image_id | product_id | image_url | display_order | is_primary |
|----------|------------|-----------|---------------|------------|
| 1 | 123 | .../abc123_image1.jpg | 0 | 1 |
| 2 | 123 | .../def456_image2.jpg | 1 | 0 |
| 3 | 123 | .../ghi789_image3.jpg | 2 | 0 |

---

## Frontend Integration

### Simple Test in Browser Console

Open your Angular app in Chrome, open DevTools Console, and run:

```javascript
// Replace with your actual product ID
const productId = 123;

fetch(`http://localhost:8080/users/product/${productId}/images`)
  .then(res => res.json())
  .then(data => {
    console.log('Images:', data);
    console.log('Count:', data.count);
    console.log('URLs:', data.imageUrls);
  });
```

---

### Angular Component Template (Quick Fix)

If your product details component currently shows only one image, update it to:

```html
<!-- OLD (single image) -->
<img [src]="product.productImageUrl" alt="Product">

<!-- NEW (multiple images with gallery) -->
<div class="image-gallery" *ngIf="productImages.length > 0">
  <!-- Main image -->
  <img [src]="productImages[currentImageIndex]" alt="Product" class="main-image">
  
  <!-- Image counter -->
  <div *ngIf="productImages.length > 1">
    Image {{ currentImageIndex + 1 }} of {{ productImages.length }}
  </div>
  
  <!-- Navigation -->
  <button (click)="previousImage()" *ngIf="productImages.length > 1">Previous</button>
  <button (click)="nextImage()" *ngIf="productImages.length > 1">Next</button>
  
  <!-- Thumbnails -->
  <div class="thumbnails">
    <img 
      *ngFor="let img of productImages; let i = index"
      [src]="img"
      (click)="currentImageIndex = i"
      [class.active]="i === currentImageIndex">
  </div>
</div>
```

### Component TypeScript (Quick Fix)

```typescript
export class ProductDetailsComponent implements OnInit {
  product: any;
  productImages: string[] = [];
  currentImageIndex = 0;

  ngOnInit() {
    const productId = this.route.snapshot.params['id'];
    
    // Load product
    this.productService.getProduct(productId).subscribe(product => {
      this.product = product;
      
      // Load images
      this.productService.getProductImages(productId).subscribe({
        next: (response) => {
          this.productImages = response.imageUrls;
        },
        error: () => {
          // Fallback to single image if GET fails
          this.productImages = [product.productImageUrl];
        }
      });
    });
  }

  nextImage() {
    this.currentImageIndex = (this.currentImageIndex + 1) % this.productImages.length;
  }

  previousImage() {
    this.currentImageIndex = this.currentImageIndex === 0 
      ? this.productImages.length - 1 
      : this.currentImageIndex - 1;
  }
}
```

### Add to Product Service

```typescript
// product.service.ts
getProductImages(productId: number): Observable<any> {
  return this.http.get(`${this.baseUrl}/users/product/${productId}/images`);
}
```

---

## Common Issues & Solutions

### Issue 1: "count": 1 when uploading 2+ files

**Problem**: Only one file is being sent to the backend

**Solution**: In Swagger UI, make sure you're selecting MULTIPLE files:
- Click the file input
- Hold Cmd (Mac) or Ctrl (Windows)
- Click multiple files
- OR use the file picker's multi-select feature

**cURL Alternative**:
```bash
curl -X POST "http://localhost:8080/users/product/upload-multiple-images" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "files=@/path/to/image3.jpg"
```

### Issue 2: GET /product/{id}/images returns empty array

**Problem**: Images weren't saved to product_images table

**Solution**: 
1. Check that you sent `imageUrls` array (not `productImageUrl` string) when creating the product
2. Verify the UserService.addMarketPlaceProduct is calling `marketPlaceProductService.saveProductImages()`
3. For existing products created before the fix, use PUT endpoint to attach images:

```bash
curl -X PUT "http://localhost:8080/users/product/123/images" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrls": [
      "http://localhost:8080/uploads/products/image1.jpg",
      "http://localhost:8080/uploads/products/image2.jpg"
    ]
  }'
```

### Issue 3: Frontend shows broken images

**Problem**: Image URLs are relative instead of absolute, or backend isn't serving static files

**Solution**: 
1. Check that URLs in database start with `http://localhost:8080/uploads/...`
2. Verify uploads directory exists: `ls -la /Users/anuskash/Downloads/marketplace-main/uploads/products/`
3. Check application.properties has the correct static resource mapping
4. Test direct URL access: open `http://localhost:8080/uploads/products/yourimage.jpg` in browser

### Issue 4: Angular can't fetch images (CORS error)

**Problem**: CORS blocking cross-origin requests

**Solution**: Backend CORS is already configured, but verify:
```typescript
// In your Angular environment.ts
export const environment = {
  apiUrl: 'http://localhost:8080'  // Must match backend exactly
};
```

---

## Quick Debugging Commands

```bash
# Check uploaded files exist
ls -la /Users/anuskash/Downloads/marketplace-main/uploads/products/

# Check database entries
sqlcmd -S localhost -U market_user -P MarketPass1234 -d marketplace -Q "SELECT * FROM product_images ORDER BY product_id, display_order"

# Check product with images
sqlcmd -S localhost -U market_user -P MarketPass1234 -d marketplace -Q "SELECT p.product_id, p.product_name, COUNT(pi.image_id) as image_count FROM marketplace_products p LEFT JOIN product_images pi ON p.product_id = pi.product_id GROUP BY p.product_id, p.product_name"

# Test GET endpoint directly
curl "http://localhost:8080/users/product/123/images"
```

---

## Success Checklist

- [ ] Upload 3 images via POST /upload-multiple-images → get count: 3
- [ ] Create product with imageUrls array → product created
- [ ] GET /product/{id}/images → returns all 3 URLs
- [ ] Database shows 3 rows in product_images table
- [ ] Direct URL access works (open image URL in browser)
- [ ] Frontend component fetches imageUrls successfully
- [ ] Frontend displays image gallery with all images
- [ ] Navigation between images works

If all checks pass ✅, your multi-image system is fully operational!
