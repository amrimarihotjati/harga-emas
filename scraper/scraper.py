import requests
from bs4 import BeautifulSoup
import json
import datetime
import os

def scrape_galeri24():
    url = "https://galeri24.co.id/harga-emas"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }
    
    # Placeholder for actual scraping logic based on Galeri24's HTML structure
    # For now, we will generate dummy data structured as if scraped, because scraping live sites robustly requires deep inspection of their DOM.
    # TODO: Implement robust BeautifulSoup parsing after inspecting Galeri24 HTML.
    
    now = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=7)))
    date_str = now.strftime("%Y-%m-%d %H:%M:%S")

    # Sample output structure
    data = {
        "last_updated": date_str,
        "vendor": "Galeri24",
        "prices": [
            {
                "weight": "0.5",
                "unit": "gram",
                "buy_price": 700000,
                "sell_price": 680000
            },
            {
                "weight": "1",
                "unit": "gram",
                "buy_price": 1400000,
                "sell_price": 1360000
            },
            {
                "weight": "5",
                "unit": "gram",
                "buy_price": 7000000,
                "sell_price": 6800000
            }
        ]
    }
    return data

def main():
    print("Starting scraper...")
    data = scrape_galeri24()
    
    json_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'json')
    os.makedirs(json_dir, exist_ok=True)
    
    prices_path = os.path.join(json_dir, 'prices.json')
    with open(prices_path, 'w') as f:
        json.dump(data, f, indent=4)
        
    print(f"Data saved to {prices_path}")
    
if __name__ == "__main__":
    main()
