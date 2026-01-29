"""
Script to generate SQL INSERT statements with real image data from image files.
This script reads image files and converts them to hexadecimal format for MySQL BLOB insertion.

Usage:
1. Place your product images in an 'images' folder
2. Name them: product1.jpg, product2.jpg, etc.
3. Run this script to generate SQL file with image data

Requirements: pip install Pillow
"""

import os
import base64
from pathlib import Path

def read_image_as_hex(image_path):
    """Read image file and convert to hexadecimal string for SQL"""
    try:
        with open(image_path, 'rb') as f:
            image_data = f.read()
        # Convert to hex format for MySQL
        hex_data = image_data.hex()
        return f"X'{hex_data}'"
    except Exception as e:
        print(f"Error reading {image_path}: {e}")
        return None

def read_image_as_base64(image_path):
    """Read image file and convert to base64 for FROM_BASE64()"""
    try:
        with open(image_path, 'rb') as f:
            image_data = f.read()
        b64_data = base64.b64encode(image_data).decode('utf-8')
        return f"FROM_BASE64('{b64_data}')"
    except Exception as e:
        print(f"Error reading {image_path}: {e}")
        return None

def generate_sql_with_images(images_folder='images', output_file='db-init-with-real-images.sql', use_base64=True):
    """Generate SQL file with product data including real images"""
    
    # Product data (without images)
    products = [
        (1, 'Laptop Dell XPS 15', 'Laptop cao cấp với màn hình 15.6 inch 4K OLED, CPU Intel Core i7 thế hệ 13', 1),
        (2, 'iPhone 15 Pro Max', 'Smartphone flagship với chip A17 Pro, camera 48MP, màn hình ProMotion 120Hz', 1),
        (3, 'Samsung Galaxy S24 Ultra', 'Điện thoại Android cao cấp với bút S Pen, camera zoom 100x, màn hình Dynamic AMOLED', 1),
        (4, 'Áo sơ mi nam', 'Áo sơ mi công sở cao cấp, chất liệu cotton 100%, form slim fit', 2),
        (5, 'Quần jean nữ', 'Quần jean nữ skinny, co giãn 4 chiều, màu xanh nhạt', 2),
        (6, 'Nồi cơm điện Philips', 'Nồi cơm điện 1.8L, công nghệ nấu 3D, lòng nồi chống dính', 3),
        (7, 'Máy hút bụi Dyson V15', 'Máy hút bụi không dây, công suất hút mạnh, pin 60 phút', 3),
        (8, 'Sách Đắc Nhân Tâm', 'Sách kỹ năng sống bán chạy nhất mọi thời đại của Dale Carnegie', 4),
        (9, 'Sách Clean Code', 'Sách lập trình: A Handbook of Agile Software Craftsmanship by Robert C. Martin', 4),
        (10, 'MacBook Pro M3', 'Laptop Apple với chip M3 Pro, 16GB RAM, SSD 512GB, màn hình Liquid Retina XDR', 1),
    ]
    
    sql_lines = []
    sql_lines.append("-- Generated SQL with real image data")
    sql_lines.append("-- Images are stored as BLOB (binary data)")
    sql_lines.append("")
    sql_lines.append("-- Clear existing data")
    sql_lines.append("DELETE FROM trproductorder;")
    sql_lines.append("DELETE FROM mstproduct;")
    sql_lines.append("DELETE FROM mstproducttype;")
    sql_lines.append("")
    sql_lines.append("-- Insert Product Types")
    sql_lines.append("INSERT INTO mstproducttype (producttype_id, producttype_name, status) VALUES")
    sql_lines.append("(1, 'Điện tử', '0'),")
    sql_lines.append("(2, 'Thời trang', '0'),")
    sql_lines.append("(3, 'Gia dụng', '0'),")
    sql_lines.append("(4, 'Sách', '0');")
    sql_lines.append("")
    sql_lines.append("-- Insert Products with images")
    
    # Check if images folder exists
    images_path = Path(images_folder)
    if not images_path.exists():
        print(f"Warning: Images folder '{images_folder}' not found.")
        print("Creating sample products without images...")
    
    for product_id, name, description, type_id in products:
        # Try to find image file
        image_file = None
        for ext in ['.jpg', '.jpeg', '.png', '.gif']:
            potential_file = images_path / f"product{product_id}{ext}"
            if potential_file.exists():
                image_file = potential_file
                break
        
        if image_file:
            print(f"Processing product {product_id}: {name} with image {image_file}")
            if use_base64:
                image_data = read_image_as_base64(image_file)
            else:
                image_data = read_image_as_hex(image_file)
            
            if image_data:
                sql_lines.append(f"INSERT INTO mstproduct (product_id, product_name, product_description, product_img, producttype_id, status)")
                sql_lines.append(f"VALUES ({product_id}, '{name}', '{description}', {image_data}, {type_id}, '0');")
                sql_lines.append("")
        else:
            print(f"Warning: No image found for product {product_id}: {name}")
            sql_lines.append(f"INSERT INTO mstproduct (product_id, product_name, product_description, product_img, producttype_id, status)")
            sql_lines.append(f"VALUES ({product_id}, '{name}', '{description}', NULL, {type_id}, '0');")
            sql_lines.append("")
    
    # Add orders
    sql_lines.append("-- Insert some orders")
    sql_lines.append("INSERT INTO trproductorder (productorder_id, product_id, user_name, order_quantity, status) VALUES")
    sql_lines.append("(1, 1, 'admin', 5, '0'),")
    sql_lines.append("(2, 2, 'user', 3, '0'),")
    sql_lines.append("(3, 3, 'admin', 2, '0'),")
    sql_lines.append("(4, 4, 'user', 10, '0'),")
    sql_lines.append("(5, 5, 'admin', 7, '0'),")
    sql_lines.append("(6, 6, 'user', 4, '0'),")
    sql_lines.append("(7, 1, 'user', 2, '0'),")
    sql_lines.append("(8, 2, 'admin', 1, '0');")
    sql_lines.append("")
    sql_lines.append("-- Verify data")
    sql_lines.append("SELECT product_id, product_name, LENGTH(product_img) as image_size_bytes FROM mstproduct;")
    
    # Write to file
    output_path = Path(output_file)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))
    
    print(f"\nSQL file generated: {output_path}")
    print(f"Total products: {len(products)}")
    return output_path

def create_sample_images():
    """Create sample colored images for testing if PIL is available"""
    try:
        from PIL import Image
        
        images_folder = Path('images')
        images_folder.mkdir(exist_ok=True)
        
        colors = [
            (255, 0, 0),      # Red
            (0, 255, 0),      # Green  
            (0, 0, 255),      # Blue
            (255, 255, 0),    # Yellow
            (255, 0, 255),    # Magenta
            (0, 255, 255),    # Cyan
            (255, 128, 0),    # Orange
            (128, 0, 255),    # Purple
            (0, 128, 255),    # Sky Blue
            (255, 192, 203),  # Pink
        ]
        
        for i, color in enumerate(colors, start=1):
            img = Image.new('RGB', (100, 100), color)
            img.save(images_folder / f'product{i}.jpg', 'JPEG')
            print(f"Created sample image: product{i}.jpg with color {color}")
        
        print(f"\nCreated {len(colors)} sample images in '{images_folder}' folder")
        return True
    except ImportError:
        print("PIL/Pillow not installed. Run: pip install Pillow")
        return False

if __name__ == '__main__':
    print("=" * 60)
    print("SQL Generator with Real Image Data")
    print("=" * 60)
    print()
    
    # Create sample images if they don't exist
    images_folder = Path('images')
    if not images_folder.exists() or not list(images_folder.glob('product*.jpg')):
        print("No images found. Creating sample images...")
        if create_sample_images():
            print("\nGenerating SQL with sample images...")
            generate_sql_with_images(use_base64=True)
        else:
            print("\nGenerating SQL without images...")
            generate_sql_with_images(use_base64=True)
    else:
        print(f"Using existing images from '{images_folder}' folder...")
        generate_sql_with_images(use_base64=True)
    
    print("\n" + "=" * 60)
    print("USAGE INSTRUCTIONS:")
    print("=" * 60)
    print("1. Run this script to generate SQL file")
    print("2. Execute the generated SQL file in your MySQL database")
    print("3. Start your Spring Boot application")
    print("4. Navigate to /products/list and search for products")
    print("5. Images should display correctly in the table")
    print()
    print("To use your own images:")
    print("- Place JPEG/PNG images in 'images' folder")
    print("- Name them: product1.jpg, product2.jpg, etc.")
    print("- Re-run this script")
