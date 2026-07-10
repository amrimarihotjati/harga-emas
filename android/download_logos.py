import urllib.request
from duckduckgo_search import DDGS
import os
import requests

queries = {
    "ic_vendor_antam.png": "logo antam lm png",
    "ic_vendor_ubs.png": "logo ubs lifestyle emas png",
    "ic_vendor_galeri_24.png": "logo galeri 24 pegadaian png",
    "ic_vendor_lotus_archi.png": "logo lotus archi emas png"
}

dir_path = "/Users/amrimarihotjati/.gemini/antigravity/scratch/harga-emas/android/app/src/main/res/drawable"

ddgs = DDGS()
for name, q in queries.items():
    try:
        results = list(ddgs.images(q, max_results=3))
        if results:
            url = results[0]['image']
            print(f"Downloading {url} for {name}...")
            r = requests.get(url, timeout=10, headers={"User-Agent": "Mozilla/5.0"})
            if r.status_code == 200:
                with open(os.path.join(dir_path, name), "wb") as f:
                    f.write(r.content)
            else:
                print(f"Failed {name} with HTTP {r.status_code}")
        else:
            print(f"No results for {q}")
    except Exception as e:
        print(f"Failed {name}: {e}")

# Copy the antam logo to its variants
for variant in ["ic_vendor_antam_mulia_retro.png", "ic_vendor_antam_non_pegadaian.png"]:
    os.system(f"cp {dir_path}/ic_vendor_antam.png {dir_path}/{variant}")
# Copy the ubs logo to its variants
for variant in ["ic_vendor_ubs_anna.png", "ic_vendor_ubs_disney.png", "ic_vendor_ubs_elsa.png", "ic_vendor_ubs_hello_kitty.png", "ic_vendor_ubs_mickey_fullbody.png"]:
    os.system(f"cp {dir_path}/ic_vendor_ubs.png {dir_path}/{variant}")
# Copy the galeri24 logo to its variants
for variant in ["ic_vendor_dinar_g24.png", "ic_vendor_baby_galeri_24.png"]:
    os.system(f"cp {dir_path}/ic_vendor_galeri_24.png {dir_path}/{variant}")
# Copy lotus archi
os.system(f"cp {dir_path}/ic_vendor_lotus_archi.png {dir_path}/ic_vendor_lotus_archi_gift.png")

