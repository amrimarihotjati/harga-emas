import os
import requests

queries = [
    ("ic_vendor_ubs.png", "https://logospng.org/download/ubs/ubs-256.png"),
    ("ic_vendor_galeri_24.png", "https://www.galeri24.co.id/assets/img/logo-galeri24.png"),
    ("ic_vendor_lotus_archi.png", "https://lotusarchi.com/wp-content/uploads/2021/08/logo-lotus-archi.png")
]

dir_path = "/Users/amrimarihotjati/.gemini/antigravity/scratch/harga-emas/android/app/src/main/res/drawable"

for name, url in queries:
    try:
        print(f"Downloading {url} for {name}...")
        r = requests.get(url, timeout=10, headers={"User-Agent": "Mozilla/5.0"})
        if r.status_code == 200:
            with open(os.path.join(dir_path, name), "wb") as f:
                f.write(r.content)
        else:
            print(f"Failed {name} with HTTP {r.status_code}")
    except Exception as e:
        print(f"Failed {name}: {e}")

# Copy the ubs logo to its variants
for variant in ["ic_vendor_ubs_anna.png", "ic_vendor_ubs_disney.png", "ic_vendor_ubs_elsa.png", "ic_vendor_ubs_hello_kitty.png", "ic_vendor_ubs_mickey_fullbody.png"]:
    os.system(f"cp {dir_path}/ic_vendor_ubs.png {dir_path}/{variant}")
# Copy the galeri24 logo to its variants
for variant in ["ic_vendor_dinar_g24.png", "ic_vendor_baby_galeri_24.png"]:
    os.system(f"cp {dir_path}/ic_vendor_galeri_24.png {dir_path}/{variant}")
# Copy lotus archi
os.system(f"cp {dir_path}/ic_vendor_lotus_archi.png {dir_path}/ic_vendor_lotus_archi_gift.png")
