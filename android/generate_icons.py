from PIL import Image, ImageDraw
import os

source_image = "/Users/amrimarihotjati/.gemini/antigravity/brain/15f1ad68-5f15-4e97-b1f9-1b041985ed98/media__1783694925589.jpg"
res_dir = "app/src/main/res"

def resize_and_save(img, size, path):
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(path, format="PNG")

try:
    img = Image.open(source_image).convert("RGBA")
    
    # Standard icon sizes
    sizes = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192
    }
    
    # Foreground icon sizes for adaptive icons (108dp)
    fg_sizes = {
        "mdpi": 108,
        "hdpi": 162,
        "xhdpi": 216,
        "xxhdpi": 324,
        "xxxhdpi": 432
    }

    for density, size in sizes.items():
        dir_path = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(dir_path, exist_ok=True)
        # ic_launcher
        resize_and_save(img, size, os.path.join(dir_path, "ic_launcher.png"))
        
        # ic_launcher_round (we'll just use the same square and let OS mask it or we can mask it into a circle)
        mask = Image.new("L", (size, size), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        round_img = img.copy().resize((size, size), Image.Resampling.LANCZOS)
        round_img.putalpha(mask)
        round_img.save(os.path.join(dir_path, "ic_launcher_round.png"), format="PNG")

    for density, fg_size in fg_sizes.items():
        dir_path = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(dir_path, exist_ok=True)
        # To prevent text cut off, we should scale the image down inside the 108dp canvas
        # The safe zone is 72dp. So image should fit in 72dp = 2/3 of fg_size
        safe_size = int(fg_size * 2 / 3)
        fg_img = Image.new("RGBA", (fg_size, fg_size), (0, 0, 0, 0)) # transparent background
        resized = img.resize((safe_size, safe_size), Image.Resampling.LANCZOS)
        offset = (fg_size - safe_size) // 2
        fg_img.paste(resized, (offset, offset))
        fg_img.save(os.path.join(dir_path, "ic_launcher_foreground.png"), format="PNG")
        
    print("Icons generated successfully.")
except Exception as e:
    print(f"Error: {e}")
