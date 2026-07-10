import requests
import json
import datetime
import os
import re

def scrape_galeri24():
    url = "https://galeri24.co.id/harga-emas"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    
    print("Fetching URL:", url)
    resp = requests.get(url, headers=headers)
    if resp.status_code != 200:
        print("Failed to fetch page. Status code:", resp.status_code)
        return None
        
    html = resp.text
    match = re.search(r'<script type="application/json" id="__NUXT_DATA__"[^>]*>(.*?)</script>', html, re.DOTALL)
    if not match:
        print("No NUXT_DATA found in HTML")
        return None

    try:
        data_array = json.loads(match.group(1))
    except Exception as e:
        print("Failed to parse JSON:", e)
        return None

    results = []
    
    def resolve(val):
        if isinstance(val, int) and 0 <= val < len(data_array):
            return data_array[val]
        return val

    for item in data_array:
        if isinstance(item, dict) and "vendorName" in item and "sellingPrice" in item and "buybackPrice" in item and "denomination" in item:
            try:
                vendor = resolve(item["vendorName"])
                denom = resolve(item["denomination"])
                sell = resolve(item["sellingPrice"])
                buy = resolve(item["buybackPrice"])
                
                if isinstance(vendor, str) and isinstance(denom, str):
                    try:
                        sell_val = int(sell)
                    except:
                        sell_val = 0
                        
                    try:
                        buy_val = int(buy)
                    except:
                        buy_val = 0
                        
                    results.append({
                        "weight": denom,
                        "unit": f"gram - {vendor}",
                        "buy_price": buy_val,
                        "sell_price": sell_val
                    })
            except Exception as e:
                pass
                
    if not results:
        print("No prices extracted.")
        return None

    # Sort results by vendor, then weight (attempting float cast)
    def sort_key(x):
        try:
            w = float(x["weight"])
        except:
            w = 0.0
        return (x["unit"], w)
        
    results = sorted(results, key=sort_key)

    now = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=7)))
    date_str = now.strftime("%Y-%m-%d %H:%M:%S")

    data = {
        "last_updated": date_str,
        "vendor": "Galeri24",
        "prices": results
    }
    
    return data

def main():
    print("Starting scraper...")
    data = scrape_galeri24()
    
    if not data:
        print("Scraping failed.")
        return
        
    json_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'json')
    os.makedirs(json_dir, exist_ok=True)
    
    prices_path = os.path.join(json_dir, 'prices.json')
    with open(prices_path, 'w') as f:
        json.dump(data, f, indent=4)
        
    print(f"Data saved to {prices_path}")
    print(f"Extracted {len(data['prices'])} items.")
    
if __name__ == "__main__":
    main()
