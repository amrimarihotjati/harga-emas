import os
import json
import requests

json_path = "json/prices.json"
drawable_dir = "android/app/src/main/res/drawable"

os.makedirs(drawable_dir, exist_ok=True)

with open(json_path, "r") as f:
    data = json.load(f)

# Extract vendor names from units (e.g. "gram - ANTAM")
vendors = set()
for p in data.get("prices", []):
    unit = p.get("unit", "")
    if " - " in unit:
        vendor = unit.split(" - ")[1].strip()
        vendors.add(vendor)

print("Found vendors:", vendors)

for vendor in vendors:
    safe_name = vendor.lower().replace(" ", "_").replace("-", "_")
    filename = f"ic_vendor_{safe_name}.png"
    filepath = os.path.join(drawable_dir, filename)
    
    # Download from ui-avatars as a clean placeholder
    url = f"https://ui-avatars.com/api/?name={vendor.replace(' ', '+')}&background=F59E0B&color=1E293B&bold=true&size=128&rounded=true"
    
    try:
        r = requests.get(url)
        if r.status_code == 200:
            with open(filepath, "wb") as img_f:
                img_f.write(r.content)
            print(f"Downloaded {filename}")
        else:
            print(f"Failed to download {filename}, status {r.status_code}")
    except Exception as e:
        print(f"Error downloading {filename}: {e}")

print("Done downloading icons.")
