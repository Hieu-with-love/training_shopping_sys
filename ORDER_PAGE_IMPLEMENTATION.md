# Order Page Implementation - Complete Guide

## Overview

This document describes the complete implementation of the Order Page functionality according to the specification requirements.

## Implementation Summary

### 1. Database Schema Changes

**File**: `src/main/resources/migration-add-delivery-fields.sql`

Added two new columns to `trproductorder` table:

- `order_delivery_address` VARCHAR(400) - Delivery address
- `order_delivery_date` VARCHAR(8) - Delivery date in YYYYMMDD format

**To apply migration**:

```sql
psql -U your_username -d your_database -f src/main/resources/migration-add-delivery-fields.sql
```

Or run directly in your database client.

### 2. Entity Updates

**File**: `src/main/java/com/training/shopping_sys/entity/TrProductOrder.java`

Added fields:

```java
@Column(name = "order_delivery_address", length = 400)
private String orderDeliveryAddress;

@Column(name = "order_delivery_date", length = 8)
private String orderDeliveryDate;
```

### 3. DTO Updates

**File**: `src/main/java/com/training/shopping_sys/dto/OrderItemDTO.java`

Added `productDescription` field to display product descriptions in order table.

### 4. Repository Updates

**File**: `src/main/java/com/training/shopping_sys/repository/TrProductOrderRepository.java`

Added method to find maximum order ID:

```java
@Query("SELECT MAX(t.id.orderId) FROM TrProductOrder t")
Long findMaxOrderId();
```

### 5. Controller Updates

**File**: `src/main/java/com/training/shopping_sys/controller/OrderController.java`

#### Updated `showOrderPage()` method:

- Added `productDescription` to OrderItemDTO creation
- Preserves search state (keyword, producttypeId, page) for cancel functionality

#### Updated `showOrderPageSingle()` method:

- Added `productDescription` to OrderItemDTO creation

#### Completely Rewritten `confirmOrder()` method:

**New Parameters**:

- `customerName` - Customer name (max 200 chars)
- `deliveryAddress` - Delivery address (max 400 chars)
- `deliveryDate` - Delivery date in YYYY/MM/DD format (max 10 chars)
- `productIds` - List of product IDs
- `quantities` - List of quantities
- `keyword`, `producttypeId`, `page` - Search state parameters

**Validation Logic**:

1. ✅ Customer name not empty
2. ✅ Delivery address not empty
3. ✅ Delivery date not empty
4. ✅ Delivery date format YYYY/MM/DD
5. ✅ Delivery date is valid date
6. ✅ Delivery date is not in the past (>= today)
7. ✅ Stock availability for each product

**Error Handling**:

- Single product stock error: Display error message on that product row
- Multiple product stock errors: Display alert with all invalid products + error on each row
- Other validation errors: Display inline error messages
- On error: Return to order page with all data preserved
- On success: Redirect to order success page

**Order ID Generation**:

- Uses `max(order_id) + 1` from database (not timestamp)
- All products in one order share the same order_id

**Database Operations**:

- Converts date from YYYY/MM/DD to YYYYMMDD for storage
- Creates TrProductOrder records with all fields including delivery info
- Wrapped in try-catch for transaction safety

### 6. View Updates

**File**: `src/main/resources/templates/order.html`

#### HTML Structure:

```html
<form id="orderForm" th:action="@{/orders/confirm}" method="post">
  <!-- Hidden fields for search state -->
  <input type="hidden" name="keyword" th:value="${keyword}" />
  <input type="hidden" name="producttypeId" th:value="${producttypeId}" />
  <input type="hidden" name="page" th:value="${page}" />

  <!-- Customer Information Section -->
  <div class="form-group">
    <label>Người đặt hàng: *</label>
    <input type="text" name="customerName" maxlength="200" />
  </div>

  <div class="form-group">
    <label>Địa chỉ giao hàng: *</label>
    <input type="text" name="deliveryAddress" maxlength="400" />
  </div>

  <div class="form-group">
    <label>Ngày giao hàng (YYYY/MM/DD): *</label>
    <input type="text" name="deliveryDate" maxlength="10" />
  </div>

  <!-- Product List Table -->
  <table>
    <tr th:each="item : ${orderItems}">
      <td>
        <input
          type="number"
          name="quantities"
          class="quantity-input"
          th:data-product-id="${item.productId}"
          th:data-product-name="${item.productName}"
          th:data-available-stock="${item.availableStock}"
          th:value="${item.quantity}"
        />
        <input type="hidden" name="productIds" th:value="${item.productId}" />
      </td>
    </tr>
  </table>

  <!-- Buttons -->
  <button type="button" onclick="submitOrder()">Đặt hàng</button>
  <button type="button" onclick="cancelOrder()">Cancel</button>
</form>
```

#### JavaScript Validation Functions:

**validateQuantity(input)** - Lost focus validation:

- Checks if quantity is empty (allows empty, no error)
- Checks if quantity is integer
- Checks if quantity is positive (> 0)
- Checks if quantity <= available stock
- Displays error message inline below input field

**validateDate(dateString)** - Date validation:

- Checks format matches YYYY/MM/DD regex
- Validates date is a real calendar date
- Checks date is not in the past (>= today)
- Returns {valid: true/false, message: "error text"}

**submitOrder()** - Form submission:

1. Clears all previous error messages
2. Validates customer name (not empty)
3. Validates delivery address (not empty)
4. Validates delivery date (not empty + format + not past)
5. Validates all quantities (integer, positive, <= stock)
6. Collects all stock validation errors
7. If single stock error: Show inline error on that product
8. If multiple stock errors: Show alert dialog + inline errors
9. If any errors: Focus first error field, prevent submission
10. If all valid: Submit form via `document.getElementById('orderForm').submit()`

**cancelOrder()** - Cancel with confirmation:

1. Shows confirmation dialog: "Bạn có muốn kết thúc đặt hàng quay trở về màn hình danh sách sản phẩm?"
2. If confirmed: Redirect to product list with search state preserved
3. Builds URL with keyword, producttypeId, page, and current quantities
4. Preserves quantities in URL as qty\_<productId>=<quantity>

#### CSS Styling:

- `.order-form-container` - White background, rounded corners, shadow
- `.form-group` - Spacing and label styling
- `.field-error-message` - Red text, hidden by default, shown with `.show` class
- `.alert-error` - Server-side error message box (red background)
- Input validation states (`.error` class for red border)
- Responsive design for mobile devices

### 7. Header Updates

**File**: `src/main/resources/templates/fragments/header.html`

Added user information display and logout button:

```html
<header th:fragment="header">
  <div style="display: flex; justify-content: space-between;">
    <h4>システム管理</h4>
    <div>
      <span sec:authentication="name">User</span>
      <form th:action="@{/logout}" method="post">
        <button type="submit">ログアウト</button>
      </form>
    </div>
  </div>
</header>
```

## Validation Rules Summary

### Client-Side (JavaScript)

| Field             | Validation          | Error Message                                                                              |
| ----------------- | ------------------- | ------------------------------------------------------------------------------------------ |
| Customer Name     | Not empty           | Xin hãy nhập thông tin người đặt hàng                                                      |
| Delivery Address  | Not empty           | Xin hãy nhập thông tin địa chỉ giao hàng                                                   |
| Delivery Date     | Not empty           | Ngày giao hàng không hợp lệ, xin hãy nhập lại                                              |
| Delivery Date     | Format YYYY/MM/DD   | Ngày giao hàng không hợp lệ, xin hãy nhập lại                                              |
| Delivery Date     | Valid calendar date | Ngày giao hàng không hợp lệ, xin hãy nhập lại                                              |
| Delivery Date     | >= Today            | Ngày giao hàng không hợp lệ, xin hãy nhập lại                                              |
| Quantity (blur)   | Integer             | Số lượng đặt hàng không hợp lệ, xin hãy nhập lại                                           |
| Quantity (blur)   | Positive (> 0)      | Số lượng đặt hàng không hợp lệ, xin hãy nhập lại                                           |
| Quantity (blur)   | <= Available Stock  | Số lượng đặt hàng của sản phẩm {name} không đủ trong kho. Xin hãy nhập số lượng <= {stock} |
| Quantity (submit) | All of above        | Same messages                                                                              |

### Server-Side (Java)

| Validation                   | Action                                     |
| ---------------------------- | ------------------------------------------ |
| Customer name empty          | Add error, return to page                  |
| Delivery address empty       | Add error, return to page                  |
| Delivery date empty          | Add error, return to page                  |
| Delivery date format invalid | Add error, return to page                  |
| Delivery date < today        | Add error, return to page                  |
| Stock insufficient           | Add error for each product, return to page |

## Data Flow

### Submit Order Flow:

```
1. User fills form → 2. Click "Đặt hàng"
   ↓
3. JavaScript validates all fields
   ↓
4. If validation fails → Show errors, focus first error field, STOP
   ↓
5. If validation passes → Submit form to POST /orders/confirm
   ↓
6. Controller validates server-side
   ↓
7. If server validation fails → Return to order page with errors preserved
   ↓
8. If server validation passes:
   a. Get max(order_id) from database
   b. Generate new order_id = max + 1
   c. Convert date YYYY/MM/DD → YYYYMMDD
   d. Create TrProductOrder for each product
   e. Save to database
   ↓
9. Redirect to /orders/success with success message
```

### Cancel Order Flow:

```
1. User clicks "Cancel" → 2. Show confirmation dialog
   ↓
3. If user confirms:
   a. Build URL with search parameters (keyword, producttypeId, page)
   b. Add current quantities to URL (qty_1=5&qty_2=3)
   c. Redirect to /products/list with all parameters
   ↓
4. Product list page:
   a. Restores search state
   b. Pre-fills quantity inputs with preserved values
```

## Testing Checklist

### Functional Tests:

- [ ] Customer name validation (empty, max 200 chars)
- [ ] Delivery address validation (empty, max 400 chars)
- [ ] Delivery date validation:
  - [ ] Empty
  - [ ] Invalid format (2024-01-01, 2024/1/1, etc.)
  - [ ] Invalid date (2024/02/30)
  - [ ] Past date
  - [ ] Today (should be valid)
  - [ ] Future date (should be valid)
- [ ] Quantity validation on blur:
  - [ ] Empty (should allow)
  - [ ] Non-integer (abc, 1.5)
  - [ ] Zero or negative
  - [ ] Exceeds stock
  - [ ] Valid quantity
- [ ] Quantity validation on submit:
  - [ ] All validations from blur
  - [ ] Multiple products with various errors
- [ ] Order submission:
  - [ ] Single product order
  - [ ] Multiple product order
  - [ ] Order ID generation (sequential)
  - [ ] Date conversion YYYY/MM/DD → YYYYMMDD
  - [ ] Database record creation with all fields
- [ ] Cancel functionality:
  - [ ] Confirmation dialog shows
  - [ ] Search state preserved (keyword, type, page)
  - [ ] Quantities preserved
  - [ ] Redirect to correct page
- [ ] Header display:
  - [ ] Username shown
  - [ ] Logout button works
- [ ] Error handling:
  - [ ] Single stock error display
  - [ ] Multiple stock errors display (alert + inline)
  - [ ] Server-side errors display
  - [ ] Form data preserved on error

### UI/UX Tests:

- [ ] Form fields show default values correctly
- [ ] Error messages appear in correct locations
- [ ] Error messages use correct styling
- [ ] Focus moves to first error field
- [ ] Responsive design works on mobile
- [ ] Table displays product info correctly
- [ ] Buttons styled consistently

### Integration Tests:

- [ ] Database migration applied successfully
- [ ] Entity saves with new fields
- [ ] Repository queries work correctly
- [ ] Success page displays after order
- [ ] Product list receives cancel redirect correctly

## Configuration Notes

### Database:

- Run migration script before first use
- Ensure PostgreSQL version supports `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`

### Spring Security:

- Header fragment uses `sec:authentication="name"` (requires Thymeleaf Spring Security extras)
- Logout form uses POST method as per Spring Security defaults

### Date Handling:

- Client-side: JavaScript Date object for validation
- Display format: YYYY/MM/DD (user-friendly)
- Storage format: YYYYMMDD (database varchar)

## Known Limitations

1. **Date Format**: Only YYYY/MM/DD accepted, no localization
2. **Order ID**: Sequential numbering may have race conditions in high-concurrency scenarios
3. **Transaction**: No @Transactional annotation - relies on try-catch for error handling
4. **Validation**: Some validations duplicated between client and server (intentional for security)

## Future Enhancements

1. Add @Transactional to confirmOrder() method for proper transaction management
2. Implement order history page
3. Add order tracking functionality
4. Support multiple date formats
5. Add email confirmation
6. Implement order modification/cancellation
7. Add proper transaction isolation for order ID generation

## Files Modified/Created

### Created:

- `src/main/resources/migration-add-delivery-fields.sql`
- `ORDER_PAGE_IMPLEMENTATION.md` (this file)

### Modified:

- `src/main/java/com/training/shopping_sys/entity/TrProductOrder.java`
- `src/main/java/com/training/shopping_sys/dto/OrderItemDTO.java`
- `src/main/java/com/training/shopping_sys/repository/TrProductOrderRepository.java`
- `src/main/java/com/training/shopping_sys/controller/OrderController.java`
- `src/main/resources/templates/order.html`
- `src/main/resources/templates/fragments/header.html`

## Deployment Steps

1. **Database Migration**:

   ```bash
   psql -U postgres -d shopping_sys -f src/main/resources/migration-add-delivery-fields.sql
   ```

2. **Build Application**:

   ```bash
   mvn clean package
   ```

3. **Run Application**:

   ```bash
   java -jar target/shopping-sys-0.0.1-SNAPSHOT.jar
   ```

4. **Verify**:
   - Access login page
   - Search for products
   - Add quantities and proceed to order page
   - Test all validation scenarios
   - Submit valid order
   - Verify order in database

## Support

For issues or questions about this implementation, refer to:

- Main README.md
- Swagger documentation at /swagger-ui.html
- QUICK_START_SWAGGER.md

---

**Implementation Date**: 2026-02-02
**Version**: 1.0
**Status**: Complete ✅
